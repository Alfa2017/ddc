package ddc.service.blockchain.contract;

import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.EventValues;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import ddc.sc2.Account;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.gasProvider.AbstractDdsGasProvider;
import ddc.service.blockchain.contract.gasProvider.AccountGasProvider;
import ddc.service.blockchain.contract.meta.AccountMeta;
import ddc.service.blockchain.contract.meta.Meta;
import ddc.service.blockchain.contract.meta.SectionMeta;
import ddc.service.blockchain.contract.pojo.AccountInfo;
import ddc.service.blockchain.contract.pojo.SectionInfo;
import ddc.service.blockchain.deploy.ActionModifier;
import ddc.util.CompressUtils;
import ddc.util.struct.DocumentLink;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class AccountContract {

    private BlockchainService blockchainService;
    private String accountAddress;
    private AbstractDdsGasProvider gasProvider = new AccountGasProvider();

    public AccountContract(BlockchainService blockchainService, String accountAddress) {
        this.blockchainService = blockchainService;
        this.accountAddress = accountAddress;
    }

    /**
     * Редактирование разделов счета
     *
     * @param action
     * @param sectionNumber
     * @param documentLink
     * @param meta
     * @param sender
     * @return
     */
    public CompletableFuture<TransactionReceipt> editSection(ActionModifier action, String sectionNumber, DocumentLink documentLink, SectionMeta meta, String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return blockchainService.execute(
                account.editSection(
                        action.getBytes(),
                        Web3jUtils.strToBytes16(sectionNumber),
                        documentLink.toByteArray(),
                        meta.compress()
                )::send,
                String.format("Редактирование раздела \"%s\" счета для действия \"%s\"", sectionNumber, action.name())
        );
    }


    /**
     * Редактирует мета данные счета
     *
     * @param meta
     * @param sender
     * @return
     */
    public CompletableFuture<TransactionReceipt> updateMeta(AccountMeta meta, String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return blockchainService.execute(
                account.updateMeta(meta.compress())::send,
                String.format("Редактирование метаданнных счета у отправителя \"%s\"", sender)
        );
    }

    /**
     * Получить состояние счета
     *
     * @param sender
     * @return
     */
    public CompletableFuture<AccountInfo> getAccountState(String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return account.getAccountState().sendAsync()
                .thenApply(tuple7 ->
                        new AccountInfo()
                                .setDepositoryOgrn(Web3jUtils.removeOgrnPrefix(tuple7.getValue1()))
                                .setAccType(Web3jUtils.trimToString(tuple7.getValue2()))
                                .setNumber(Web3jUtils.trimToString(tuple7.getValue3()))
                                .setDeponentOgrn(Web3jUtils.removeOgrnPrefix(tuple7.getValue4()))
                                .setDocumentLink(DocumentLink.fromByteArray(tuple7.getValue5()))
                                .setAccStatus(tuple7.getValue6().longValue())
                                .setMeta(Meta.decompress(tuple7.getValue7(), AccountMeta.class))
                );
    }

    public CompletableFuture<AccountMeta> meta(String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return account.meta().sendAsync()
                .thenApply(bytes -> CompressUtils.decompressToObject(bytes, AccountMeta.class));
    }

    public CompletableFuture<String> orgId() {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return account.orgId().sendAsync()
                .thenApply(Web3jUtils::trimToString);
    }

    public CompletableFuture<String> deponent(String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return account.deponent().sendAsync()
                .thenApply(Web3jUtils::trimToString);
    }

    public CompletableFuture<Long> status(String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return account.status().sendAsync()
                .thenApply(BigInteger::longValue);
    }

    public CompletableFuture<String> accType(String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return account.accType().sendAsync()
                .thenApply(Web3jUtils::trimToString);
    }

    public CompletableFuture<String> number(String sender) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return account.number().sendAsync()
                .thenApply(Web3jUtils::trimToString);
    }


    /**
     * Возвращает список из разделов счета, полученных их событий AccountSectionEvent
     * Для каждой секции получаем последнее событие, меняющее состояние
     *
     * @return
     */
    public List<SectionInfo> getAccountSectionEvents() {
        List<SectionInfo> sectionInfos = new ArrayList<>();
        try {
            final List<Log> eventsList = blockchainService.getEventsList(this.accountAddress, Account.SECTIONEVENT_EVENT);
            for (Log log : eventsList) {
                final Long createdAt = blockchainService.getTimestampByBlockHash(log.getBlockHash());
                final EventValues eventValues = Contract.staticExtractEventParameters(Account.SECTIONEVENT_EVENT, log);
                List<Type> vals = eventValues.getNonIndexedValues();
                final String sectionNumber = Web3jUtils.trimToString((byte[]) vals.get(1).getValue());
                final SectionInfo sectionInfo = new SectionInfo()
                        .setTransactionHash(log.getTransactionHash())
                        .setStatus(Web3jUtils.trimToString((byte[]) vals.get(0).getValue()))
                        .setNumber(sectionNumber)
                        .setDocumentLink(DocumentLink.fromByteArray((byte[]) vals.get(2).getValue()))
                        .setSectionMeta(Meta.decompress((byte[]) vals.get(3).getValue(), SectionMeta.class))
                        .setCreatedAt(createdAt)
                        .setEventNumber(log.getLogIndex().longValue());
                sectionInfos.add(sectionInfo);
            }
            return sectionInfos.stream().collect(
                    Collectors.groupingBy(SectionInfo::getNumber, Collectors.maxBy(Comparator.comparingLong(SectionInfo::getCreatedAt)))
            ).values().stream()
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Не удалось получить список разделов для счета с адресом {}. Причина {}", accountAddress, e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SectionInfo> getSectionEventEvents(TransactionReceipt tr) {
        Account account = Account.load(accountAddress, blockchainService.getWeb3j(), blockchainService.getTM(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return account.getSectionEventEvents(tr).stream().map(event -> {
            Long createdAt = blockchainService.getTimestampByBlockHash(event.log.getBlockHash());
            return new SectionInfo()
                    .setTransactionHash(tr.getTransactionHash())
                    .setStatus(Web3jUtils.trimToString(event.status))
                    .setDocumentLink(DocumentLink.fromByteArray(event.documentLink))
                    .setSectionMeta(SectionMeta.decompress(event.meta, SectionMeta.class))
                    .setNumber(Web3jUtils.trimToString(event.number))
                    .setCreatedAt(createdAt)
                    .setEventNumber(event.log.getLogIndex().longValue());
        }).collect(Collectors.toList());

    }
}
