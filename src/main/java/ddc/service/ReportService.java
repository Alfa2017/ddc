package ddc.service;

import ddc.model.entity.*;
import ddc.service.blockchain.contract.meta.DocumentMeta;
import ddc.service.blockchain.contract.pojo.DocCounters;
import ddc.service.domain.account.AccountsDbService;
import ddc.service.domain.deponent.DeponentDbService;
import ddc.service.domain.document.DocumentContractService;
import ddc.service.domain.document.DocumentService;
import ddc.service.domain.mortgage_old.MortgageTransactionDbService;
import ddc.service.domain.operation.OperationsService;
import ddc.service.domain.organization.OrganizationsDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ddc.AppState;
import ddc.exception.ExceptionMessages;
import ddc.exception.NotFoundDdsException;
import ddc.exception.ReportDdsException;
import ddc.model.enums.Dictionaries;
import ddc.model.reports.account.AccountReport;
import ddc.model.reports.account.AccountReportItem;
import ddc.model.reports.operation.DepositoryOperationReport;
import ddc.model.repositories.InformationOperationMetaRepository;
import ddc.model.request.AccountReportRequest;
import ddc.model.request.OperationReportRequest;
import ddc.util.CompressUtils;
import ddc.util.Utils;
import ddc.util.struct.DocumentLink;

import java.util.*;
import java.util.stream.Collectors;

import static ddc.model.enums.Dictionaries.AccKind.L10;
import static ddc.model.enums.Dictionaries.AccKind.L34;


@Component
@Slf4j
public class ReportService {


    @Autowired
    private OperationsService operationsService;

    @Autowired
    private AccountsDbService accountsDbService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentContractService documentContractService;


    @Autowired
    private OrganizationsDbService organizationsDbService;

    @Autowired
    private DeponentDbService deponentDbService;

    @Autowired
    private InformationOperationMetaRepository metaRepository;

    @Autowired
    private DepositoryOperationReport depositoryOperationReport;

    @Autowired
    private MortgageTransactionDbService mortgageTransactionDbService;

    @Autowired
    private AppState appState;

    public DepositoryOperationReport buildOperationsReport(OperationReportRequest request) throws ReportDdsException {
        DeponentEntity deponentEntity = deponentDbService.getEntityByOgrn(request.getDeponentOgrn()).orElseThrow(() ->
                new ReportDdsException(ExceptionMessages.CANT_GENERATE_REPORT_DEPONENT_NOT_FOUND.getMessage())
        );
        OrganizationEntity organizationEntity = organizationsDbService.getEntityByOgrn(appState.getDepositoryOgrn()).orElseThrow(() ->
                new ReportDdsException(ExceptionMessages.CANT_GENERATE_REPORT_DEPOSITORY_NOT_FOUND.getMessage())
        );
        depositoryOperationReport.setDepository(organizationEntity.getName());
        depositoryOperationReport.setDeponent(deponentEntity.getName());
        depositoryOperationReport.setCreatedDate(new Date().getTime());
        depositoryOperationReport.setReportDateFrom(request.getReportFromTimestamp());
        depositoryOperationReport.setReportDateTo(request.getReportTillTimestamp());
        depositoryOperationReport.setDeponentAccountNumber(request.getAccountNumber());
        depositoryOperationReport.setDeponentAccountType(request.getAccTypeName());

        List<OperationEntity> operationsForDepositaryReport = operationsService.getOperationsForDepositaryReport(request);

        Map<String, List<OperationEntity>> map = operationsForDepositaryReport.stream().collect(Collectors.groupingBy(OperationEntity::getType));
        depositoryOperationReport.setInventoryOperations(map.getOrDefault(Dictionaries.OpType.INV.name(), new ArrayList<>()));
        depositoryOperationReport.setAdministratorOperations(map.getOrDefault(Dictionaries.OpType.ADM.name(), new ArrayList<>()));
        return depositoryOperationReport;
    }


    public AccountReport buildAccountReport(AccountReportRequest request) {
        AccountEntity accountEntity = accountsDbService.getEntityByNumber(request.getAccountNumber())
                .orElseThrow(() -> new ReportDdsException(ExceptionMessages.CANT_GENERATE_REPORT_ACCOUNT_NOT_FOUND.getMessage()));
        /*
         К отчетной дате прибавляем 1 день, чтобы получить выписку на конец этого дня, так как приходит таймстемп на время 00:00 отчетного дня,
         пример: таймстемп равен 154630080(01.01.2019 00:00), прибавляем 1 день и сможем получить выписку по счету, открытому ранее дня отчета и в сам день отчета
         */
        //проверяем, что дата формирования выписки после создания счета
        final Long reportDateAsLongPlusDays = request.getReportDateAsLongPlusDays(1);
        if (reportDateAsLongPlusDays < accountEntity.getCreatedAt()) {
            throw new IllegalArgumentException(ExceptionMessages.ACCOUNT_NOT_CREATED_ON_REPORTING_DATE.getMessage(request.getAccountNumber()));
        }

        DeponentEntity deponentEntity = deponentDbService.getEntityByOgrn(accountEntity.getDeponentOgrn())
                .orElseThrow(() -> new ReportDdsException(ExceptionMessages.CANT_GENERATE_REPORT_ACCOUNT_HOLDER_NOT_FOUND.getMessage(accountEntity.getNumber())));
        //если номер секции не задан, то подставляем вместо него '%'(0 или более символов)
        String sectionNumber = Utils.isBlank(request.getSectionNumber()) ? "%" : request.getSectionNumber();

        AccountReport accountReport = new AccountReport();
        accountReport.setOrganizationOgrn(deponentEntity.getOgrn());
        accountReport.setDeponentName(deponentEntity.getName());
        accountReport.setReportDate(request.getReportDateAsLongPlusDays(0));
        accountReport.setDeponentDepoAccNum(request.getAccountNumber());
        accountReport.setSectionNumber(request.getSectionNumber().replace("%", ""));
        organizationsDbService.getEntityByOgrn(appState.getDepositoryOgrn())
                .ifPresent(depositary -> accountReport.setDepositoryName(depositary.getName()));

        List<AccountReportItem> operationsForDeponentReport;
        if (L10.equals(accountEntity.getAccType())) {
            operationsForDeponentReport = mortgageTransactionDbService.getDeponentAccountingAccountReportOnDate(
                    accountEntity.getAddress(),
                    sectionNumber,
                    request.getReportDateAsLongPlusDays(1)
            );
        } else if (L34.equals(accountEntity.getAccType())) {
            operationsForDeponentReport = mortgageTransactionDbService.getDeponentCustodyAccountReportOnDate(
                    accountEntity.getAddress(),
                    sectionNumber,
                    request.getReportDateAsLongPlusDays(1)
            );
        } else {
            throw new IllegalArgumentException("Тип счета " + accountEntity.getAccType() + " не поддерживается для получения отчета об операциях");
        }
        accountReport.setAccountReportItems(operationsForDeponentReport);
        accountReport.setReportCreationTime(System.currentTimeMillis() / 1000);
        return accountReport;
    }


    /**
     * Регистрация выписки по счету депо на дату
     *
     * @param request
     * @param managerAddress
     * @return
     */
    public DocumentLink registerDocument(AccountReport accountReport, AccountReportRequest request, String managerAddress, String ogrn) throws Exception {
        DocumentMeta documentMeta = new DocumentMeta();
        documentMeta.setInOut("out");
        documentMeta.setSenderDate(System.currentTimeMillis() / 1000);
        documentMeta.setDocType(Dictionaries.DocType.REPORT_TO_OW_ACC_STATEMENT.name());

        String name;
        Optional<OrganizationEntity> optionalOrganizationEntity = organizationsDbService.getEntityByOgrn(ogrn);
        if (optionalOrganizationEntity.isPresent()) {
            name = optionalOrganizationEntity.get().getName();
        } else {
            Optional<DeponentEntity> optionalDeponentEntity = deponentDbService.getEntityByOgrn(ogrn);
            if (optionalDeponentEntity.isPresent()) {
                name = optionalDeponentEntity.get().getName();
            } else {
                throw new NotFoundDdsException(ExceptionMessages.NO_ORGANIZATION_WITH_SUCH_OGRN.getMessage(ogrn));
            }
        }
        documentMeta.setRecipient(name);
        documentMeta.setRecipientNumber(request.getAccountNumber());
        documentMeta.setStartReportPeriod(request.getReportDateAsLongPlusDays(0));
        documentMeta.setEndReportPeriod(request.getReportDateAsLongPlusDays(1));
        documentMeta.setMetaContent(CompressUtils.compressObject(request.getAccountNumber()));

        if ("Депонент".equals(request.getDestination())) {
            documentMeta.setRecipient(name);
        } else if ("ЦБ".equals(request.getDestination())) {
            documentMeta.setRecipient("Центробанк");
        } else {
            documentMeta.setRecipient(request.getDestination());
        }

        String fileName = UUID.randomUUID().toString() + ".xls";
        documentMeta.setMimeType("application/vnd.ms-excel");
        documentMeta.setFileName(fileName);

        String reserveId = accountReport.hashCode() + "" + request.hashCode() + ogrn;
        DocCounters docCounters = documentContractService.reserveCounters(reserveId).join();
        accountReport.setRegNumber(docCounters.getGlobalCounter());

        byte[] reportPayload = accountReport.buildExcelDocument();

        return documentService.addDocument(reportPayload, documentMeta, managerAddress, reserveId).join();
    }

    /**
     * Регистрация отчета о депозитарных операциях
     *
     * @param request
     * @param managerAddress
     * @return
     */
    public DocumentLink registerDocument(DepositoryOperationReport depositoryOperationReport, OperationReportRequest request, String managerAddress) throws Exception {
        DocumentMeta documentMeta = new DocumentMeta();
        documentMeta.setInOut("out");
        documentMeta.setSenderDate(System.currentTimeMillis() / 1000);
        documentMeta.setDocType(Dictionaries.DocType.REPORT_TO_OW_OPERATIONS.name());

        DeponentEntity deponentEntity = deponentDbService.getEntityByOgrn(request.getDeponentOgrn())
                .orElseThrow(() -> new NotFoundDdsException(ExceptionMessages.NO_DEPONENT_WITH_SUCH_ADDRESS.getMessage(request.getDeponentOgrn())));

        if ("Депонент".equals(request.getDestination())) {
            documentMeta.setRecipient(deponentEntity.getName());
        } else if ("ЦБ".equals(request.getDestination())) {
            documentMeta.setRecipient("Центробанк");
        } else {
            documentMeta.setRecipient(request.getDestination());
        }

        documentMeta.setRecipientNumber(request.getAccountNumber());
        documentMeta.setStartReportPeriod(request.getReportFromTimestamp());
        documentMeta.setEndReportPeriod(request.getReportTillTimestamp());
        documentMeta.setMetaContent(CompressUtils.compressObject(request.getAccountNumber()));

        String fileName = UUID.randomUUID().toString() + ".xls";
        documentMeta.setMimeType("application/vnd.ms-excel");
        documentMeta.setFileName(fileName);

        String reserveId = depositoryOperationReport.hashCode() + "" + request.hashCode();
        DocCounters docCounters = documentContractService.reserveCounters(reserveId).join();
        depositoryOperationReport.setRegNumber(docCounters.getGlobalCounter());

        byte[] reportPayload = depositoryOperationReport.buildExcelDocument();

        return documentService.addDocument(reportPayload, documentMeta, managerAddress, reserveId).join();
    }

    public InformationOperationMetaEntity getMetaById(long id) {
        Optional<InformationOperationMetaEntity> maybe = metaRepository.findById(id);
        return maybe.orElseGet(() -> {
            log.warn(ExceptionMessages.NO_METAINFO_BY_ID.getMessage(id));
            return new InformationOperationMetaEntity();
        });
    }
}
