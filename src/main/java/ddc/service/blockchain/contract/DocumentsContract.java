package ddc.service.blockchain.contract;

import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import ddc.sc2.DDSystem;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.meta.DocumentMeta;
import ddc.service.blockchain.contract.pojo.DocCounters;
import ddc.service.blockchain.contract.pojo.DocInfo;
import ddc.service.blockchain.deploy.ContainerModifier;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
abstract class DocumentsContract extends AftAdminedContract {

    /**
     * Резервирование номеров документа
     *
     * @param ogrn      ОГРН кем резервируется
     * @param reserveId Id резерва
     * @param sender    Отправитель транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public CompletableFuture<DocCounters> reserveCounters(String ogrn, String reserveId, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        return this.execute(
                ddSystem.reserveCounters(
                        Web3jUtils.createOrgIdFromOgrn(ogrn),
                        Web3jUtils.strToBytes32(reserveId)
                )::send,
                String.format("Бронирование номера документа организацией с ogrn \"%s\", id резерва \"%s\"", ogrn, reserveId)
        ).thenApply(tr ->
                ddSystem.getReservedCounters(
                        Web3jUtils.createOrgIdFromOgrn(ogrn),
                        Web3jUtils.strToBytes32(reserveId)
                ).sendAsync().thenApply(this::mapDocCounters).join()
        );
    }

    /**
     * Информация о документе по globalId
     *
     * @param globalDocId
     * @param blockNumber
     * @return
     * @throws BlockchainDdsException
     */
    public DocInfo getDocInfo(Long globalDocId, Long blockNumber) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);

        if (blockNumber > 0) {
            ddSystem.setDefaultBlockParameter(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)));
        }

        try {
            Tuple3<byte[], byte[], byte[]> docInfo = ddSystem.getDocInfo(
                    BigInteger.valueOf(globalDocId)
            ).send();
            ddSystem.setDefaultBlockParameter(DefaultBlockParameterName.LATEST);
            return mapDocInfo(docInfo);
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    private DocCounters mapDocCounters(Tuple2<BigInteger, BigInteger> counters) {
        return new DocCounters()
                .setGlobalCounter(counters.getValue1().longValue())
                .setOrgCounter(counters.getValue2().longValue());
    }

    private DocInfo mapDocInfo(Tuple3<byte[], byte[], byte[]> docInfo) {
        return new DocInfo()
                .setStatusFrom(Web3jUtils.trimToString(docInfo.getValue1()))
                .setStatusTo(Web3jUtils.trimToString(docInfo.getValue2()))
                .setMeta(DocumentMeta.decompress(docInfo.getValue3(), DocumentMeta.class));
    }

    /**
     * Информация о документе по Id архива контейнера
     *
     * @param container
     * @param archiveId
     * @param blockNumber номер блока на который нужно получить информацию
     * @return
     * @throws BlockchainDdsException
     */
    public DocInfo getDocInfo(String container, Long archiveId, Long blockNumber) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);

        if (blockNumber > 0) {
            ddSystem.setDefaultBlockParameter(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)));
        }

        try {
            Tuple3<byte[], byte[], byte[]> docInfo = ddSystem.getDocInfo(
                    container,
                    BigInteger.valueOf(archiveId)
            ).send();
            ddSystem.setDefaultBlockParameter(DefaultBlockParameterName.LATEST);
            return mapDocInfo(docInfo);
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    public CompletableFuture<Boolean> setDocStatus(Long globalDocId, String status, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        return this.execute(
                ddSystem.setDocStatus(
                        BigInteger.valueOf(globalDocId),
                        Web3jUtils.strToBytes32(status)
                )::send,
                String.format("Смена статуса документа по docId \"%s\" на статус \"%s\"", globalDocId.toString(), status)
        ).thenApply(TransactionReceipt::isStatusOK);
    }

    public CompletableFuture<Boolean> setDocStatus(String containerAddress, Long archiveId, String status, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        return this.execute(
                ddSystem.setDocStatus(
                        containerAddress,
                        BigInteger.valueOf(archiveId),
                        Web3jUtils.strToBytes32(status)
                )::send,
                String.format("Смена статуса документа по id архива контейнера \"%s\" с адресом \"%s\" на статус \"%s\"", archiveId.toString(), containerAddress, status)
        ).thenApply(TransactionReceipt::isStatusOK);
    }

    /**
     * Добавление документа в ДДС
     *
     * @param ogrn              ОГРН от кого добавляется документ
     * @param counterOgrn       ОГРН контрагента
     * @param containerAddress  Адрес контейнера куда кладется документ
     * @param containerModifier Модификатор документа // TODO: мб это использовать в качестве типа токумента?
     * @param expiry            Время годности документа
     * @param reserveId         Строка с номером бронирования счетчиков. null - случайная строка
     * @param documentMeta      Метаданные документа
     * @param sender            Отправитель транзакции, дб менеджером органицазии ogrn
     * @return
     */
    public CompletableFuture<TransactionReceipt> newDocument(String ogrn, String counterOgrn, String containerAddress, ContainerModifier containerModifier, Long expiry, String reserveId, DocumentMeta documentMeta, String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        reserveId = reserveId != null ? reserveId : Arrays.toString(Web3jUtils.getRandomBytes(16));

        return this.execute(
                ddSystem.newDocument(
                        Web3jUtils.createOrgIdFromOgrn(ogrn),
                        Web3jUtils.createOrgIdFromOgrn(counterOgrn),
                        containerAddress,
                        containerModifier.getBytes(),
                        Web3jUtils.strToBytes32("hmac"),
                        BigInteger.valueOf(expiry),
                        Web3jUtils.strToBytes32(reserveId),
                        documentMeta.compress()
                )::send,
                String.format("Создание документа в контейнере с адресом \"%s\" и модификатором \"%s\" организацией с ogrn \"%s\"", containerAddress, containerModifier.name(), ogrn)
        ).exceptionally(ex -> {
            log.error(ex.getMessage());
            throw new CompletionException(ex);
        });
    }

    public CompletableFuture<BigInteger> getOrganizationDocCounter(String orgId, String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        return this.execute(
                ddSystem.organizationDocCounter(Web3jUtils.strToBytes32(orgId))::send,
                String.format("Получение счетчика документов организации с ogrn \"%s\"", orgId)
        );
    }

    public List<DDSystem.DocumentEventEventResponse> getDocumentEventEvents(TransactionReceipt receipt, String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        return ddSystem.getDocumentEventEvents(receipt);
    }
}
