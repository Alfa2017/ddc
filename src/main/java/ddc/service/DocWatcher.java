package ddc.service;

import ddc.service.bt.BusinessTransactionWorker;
import ddc.service.bt.MockBtWorker;
import ddc.service.bt.MoveBtWorker;
import ddc.service.bt.sale.OurDeductBtWorker;
import ddc.service.bt.sale.OurEnrollBtWorker;
import ddc.service.bt.sale.TheirDeductBtWorker;
import ddc.service.bt.sale.TheirEnrollBtWorker;
import ddc.service.domain.document.DocumentService;
import ddc.service.domain.mortgage.sale.MortgageSaleReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ddc.AppState;
import ddc.exception.ServerErrorDdsException;
import ddc.model.entity.DocumentEntity;
import ddc.model.enums.Dictionaries;
import ddc.model.enums.DocStatus;
import ru.iteco.aft.dds.service.bt.sale.*;
import ddc.util.DateUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DocWatcher {

    @Autowired
    private TheirDeductBtWorker theirDeductBtWorker;

    @Autowired
    private OurDeductBtWorker ourDeductBtWorker;

    @Autowired
    private TheirEnrollBtWorker theirEnrollBtWorker;

    @Autowired
    private OurEnrollBtWorker ourEnrollBtWorker;

    @Autowired
    private AppState appState;

    @Autowired
    private MockBtWorker mockBtWorker;

    @Autowired
    private MoveBtWorker moveBtWorker;

    @Autowired
    private DocumentService documentService;

    @Value("${app.instruction.timeout.hours}")
    private Integer timeoutHours;

    @Autowired
    private MortgageSaleReportService reportService;

    private List<String> docTypes = Arrays.asList(
            Dictionaries.DocType.OW_INSTRUCTION_ENROLL.toString(),
            Dictionaries.DocType.OW_INSTRUCTION_DEDUCT.toString(),
            Dictionaries.DocType.INNER_INSTRUCTION_TRANSFER.toString(),
            Dictionaries.DocType.OW_INSTRUCTION_TRANSFER.toString()
    );

    @Scheduled(fixedDelay = 3000)
    public void watch() {
        Thread.currentThread().setName("DocWatch-" + LocalDateTime.now().toLocalTime().format(DateTimeFormatter.ofPattern("HHmmss")));

        try {
            this.resolveNewDocuments();
            this.resolveAcceptedDocuments();
            this.resolveDelayedDocuments();
            this.resolveValidatingDocuments();
            this.resolveProcessingDocuments();
        } catch (Exception e) {
            log.error("Ошибка при обработке статуса документа: {} {}", e.getClass().getSimpleName(), e.getMessage());
        }

    }

    /**
     * Обработка документов в статусе ACCEPTED и перевод из в статус DELAYED или PROCESSING
     * @throws ServerErrorDdsException
     */
    private void resolveNewDocuments() throws ServerErrorDdsException {
        List<DocumentEntity> newDocuments = getDocumentsByActualStatus(DocStatus.NEW);
        checkBusinessTransactions(newDocuments);
    }

    /**
     * Обработка документов в статусе ACCEPTED и перевод из в статус DELAYED или PROCESSING
     * @throws ServerErrorDdsException
     */
    private void resolveAcceptedDocuments() {
        List<DocumentEntity> checkedDocuments = getDocumentsByActualStatus(DocStatus.ACCEPTED);


        List<DocumentEntity> shouldBeValidating = checkedDocuments.stream()
                .filter(document -> {
                    LocalDateTime startProcessingDate = DateUtils.toLocalDateTime(document.getOpMinDate());
                    return startProcessingDate.isBefore(DateUtils.currentLocalDateTime());
                }).collect(Collectors.toList());

        List<DocumentEntity> shouldBeDelayed = checkedDocuments.stream()
                .filter(document -> {
                    LocalDateTime startProcessingDate = DateUtils.toLocalDateTime(document.getOpMinDate());
                    return startProcessingDate.isAfter(DateUtils.currentLocalDateTime());
                })
                .collect(Collectors.toList());

        this.changeDocStatuses(shouldBeValidating, DocStatus.VALIDATING);
        this.changeDocStatuses(shouldBeDelayed, DocStatus.DELAYED);
    }

    /**
     * Проверяет наступила ли дата обработки документа
     * @throws ServerErrorDdsException
     */
    private void resolveDelayedDocuments() {
        List<DocumentEntity> checkedDocuments = getDocumentsByActualStatus(DocStatus.DELAYED);

        List<DocumentEntity> shouldBeValidating = checkedDocuments.stream()
                .filter(document -> DateUtils.toLocalDateTime(document.getOpMinDate()).isBefore(DateUtils.currentLocalDateTime()))
                .collect(Collectors.toList());

        this.changeDocStatuses(shouldBeValidating, DocStatus.VALIDATING);
    }

    /**
     * Проверяет документы в статусе
     * @throws ServerErrorDdsException
     */
    private void resolveValidatingDocuments() throws Exception {
        List<DocumentEntity> shouldBeValidating = getDocumentsByActualStatus(DocStatus.VALIDATING);
        this.validateBusinessTransactions(shouldBeValidating);
    }

    /**
     * Проверяет документы в статусе
     * @throws Exception
     */
    private void resolveProcessingDocuments() throws Exception {
        List<DocumentEntity> processingDocuments = getDocumentsByActualStatus(DocStatus.PROCESSING);

        List<DocumentEntity> shouldBeExpired = processingDocuments.stream()
                .filter(
                        document -> {
                            LocalDateTime startProcessingDate = DateUtils.toLocalDateTime(document.getOpMinDate());
                            LocalDateTime expireDate = startProcessingDate.plusHours(timeoutHours);
                            return expireDate.isBefore(DateUtils.currentLocalDateTime());
                        }
                )
                .collect(Collectors.toList());

        List<DocumentEntity> shouldBeSendToWorker = processingDocuments.stream()
                .filter(
                        document -> {
                            LocalDateTime startProcessingDate = DateUtils.toLocalDateTime(document.getOpMinDate());
                            LocalDateTime expireDate = startProcessingDate.plusHours(timeoutHours);
                            return expireDate.isAfter(DateUtils.currentLocalDateTime());
                        }
                )
                .collect(Collectors.toList());

        this.expireDocuments(shouldBeExpired);
        this.executeBusinessTransactions(shouldBeSendToWorker);
    }

    private List<DocumentEntity> getDocumentsByActualStatus(DocStatus status) {
        return documentService.getDocumentsByActualStatus(status).stream()
            .filter(x -> docTypes.contains(x.getDocType())).collect(Collectors.toList());
    }

    private void checkBusinessTransactions(List<DocumentEntity> entities) throws ServerErrorDdsException {
        for (DocumentEntity documentEntity : entities) {
            this.getWorkerByDocumentEntity(documentEntity).accept(documentEntity);
        }
    }


    private void executeBusinessTransactions(List<DocumentEntity> entities) throws Exception {
        for (DocumentEntity documentEntity : entities) {
            this.getWorkerByDocumentEntity(documentEntity).execute(documentEntity);
        }
    }

    private void validateBusinessTransactions(List<DocumentEntity> entities) throws Exception {
        for (DocumentEntity documentEntity : entities) {
            this.getWorkerByDocumentEntity(documentEntity).validate(documentEntity);
        }
    }

    private void expireDocuments(List<DocumentEntity> entities) throws ServerErrorDdsException {
        for (DocumentEntity documentEntity : entities) {
            this.getWorkerByDocumentEntity(documentEntity).expire(documentEntity);
        }
        this.changeDocStatuses(entities, DocStatus.EXPIRED);
        reportService.expiringRefuseReport(entities);
    }

    private void changeDocStatuses(List<DocumentEntity> documentEntities, DocStatus docStatus) {
        if (documentEntities == null || documentEntities.size() == 0) return;

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (DocumentEntity documentEntity : documentEntities) {
            futures.add(documentService.setDocStatus(documentEntity.getGlobalCounter(), docStatus));
        }
        futures.forEach(CompletableFuture::join);
    }

    private BusinessTransactionWorker getWorkerByDocumentEntity(DocumentEntity documentEntity) {
        switch (Dictionaries.DocType.valueOf(documentEntity.getDocType())) {
            case OW_INSTRUCTION_ENROLL: {
                return appState.getDepositoryOgrn().equals(documentEntity.getOrganizationOgrn()) ? ourEnrollBtWorker : theirEnrollBtWorker;
            }
            case OW_INSTRUCTION_DEDUCT: {
                return appState.getDepositoryOgrn().equals(documentEntity.getOrganizationOgrn()) ? ourDeductBtWorker : theirDeductBtWorker;
            }
            case INNER_INSTRUCTION_TRANSFER:
            case OW_INSTRUCTION_TRANSFER: {
                return moveBtWorker;
            }
            default: {
                return mockBtWorker;
            }
        }
    }
}
