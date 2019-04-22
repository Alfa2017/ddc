package ddc.service.blockchain.contract;

import lombok.Getter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;
import ddc.exception.ExceptionMessages;
import ddc.sc2.Mortgage;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.gasProvider.AbstractDdsGasProvider;
import ddc.service.blockchain.contract.gasProvider.MortgageGasProvider;
import ddc.service.blockchain.contract.meta.Meta;
import ddc.service.blockchain.contract.meta.MortgageMeta;
import ddc.service.blockchain.contract.pojo.MgSnapshot;
import ddc.service.blockchain.deploy.ActionModifier;
import ddc.util.struct.DocumentLink;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MortgageContract {

    private BlockchainService blockchainService;
    @Getter
    private String mortgageAddress;
    private AbstractDdsGasProvider gasProvider = new MortgageGasProvider();

    public MortgageContract(BlockchainService blockchainService, String mortgageAddress) {
        if (!Web3jUtils.isAddress(mortgageAddress)) {
            throw new IllegalArgumentException(ExceptionMessages.UNCORRECT_MORTGAGE_ADDRESS.getMessage(mortgageAddress));
        }
        this.blockchainService = blockchainService;
        this.mortgageAddress = mortgageAddress;
    }

    /**
     * Смена счетов для указанной закладной
     *
     * @param custodyAssetTo        активный счет
     * @param custodyLiabilityTo
     * @param custodySectionTo
     * @param accountingAssetTo
     * @param accountingLiabilityTo
     * @param accountingSectionTo
     * @param documentLink
     * @param sender
     * @return
     */
    public CompletableFuture<TransactionReceipt> changeAccount(
            String custodyAssetTo,
            String custodyLiabilityTo,
            String custodySectionTo,
            String accountingAssetTo,
            String accountingLiabilityTo,
            String accountingSectionTo,
            DocumentLink documentLink,
            String sender
    ) {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                mortgage.changeAccount(
                        custodyAssetTo,
                        custodyLiabilityTo,
                        Web3jUtils.strToBytes16(custodySectionTo),
                        accountingAssetTo,
                        accountingLiabilityTo,
                        Web3jUtils.strToBytes16(accountingSectionTo),
                        documentLink.toByteArray()
                )::send,
                String.format("Смена счетов: " +
                        "Раздел учета \"%s\" Пассив учета \"%s\" Актив учета \"%s\", Раздел хранения \"%s\" Пассив хранения \"%s\" Актив хранения \"%s\"", accountingSectionTo, accountingLiabilityTo, accountingAssetTo, custodySectionTo, custodyLiabilityTo, custodyAssetTo)
        );
    }

    /**
     * Измененяет разделы счета закладной
     *
     * @param custodySectionTo
     * @param accountingSectionTo
     * @param documentLink
     * @param sender
     * @return
     */
    public CompletableFuture<TransactionReceipt> changeAccountSection(String custodySectionTo, String accountingSectionTo, DocumentLink documentLink, String sender) {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                mortgage.changeAccountSection(
                        Web3jUtils.strToBytes16(custodySectionTo),
                        Web3jUtils.strToBytes16(accountingSectionTo),
                        documentLink.toByteArray())::send,
                String.format("Изменение разделов счета закладной Раздел хранения \"%s\" Раздел учета \"%s\"", custodySectionTo, accountingSectionTo)
        );
    }

    /**
     * Срез данных закладной для версии
     * @param version
     * @return
     */
    public MgSnapshot getSnapshot(Long version) {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(Web3jUtils.ZERO_ADDRESS), gasProvider);

        Tuple12<BigInteger, byte[], String, String, String, byte[], String, String, byte[], byte[], byte[], List<byte[]>> tuple12 = mortgage.getSnapshot(BigInteger.valueOf(version)).sendAsync().join();

        List<String> agreements = tuple12.getValue12().stream().map(Web3jUtils::trimToString).collect(Collectors.toList());

        return new MgSnapshot()
                .setVersion(tuple12.getValue1().longValue())
                .setRegNumber(Web3jUtils.trimToString(tuple12.getValue2()))
                .setContainerAddress(tuple12.getValue3())
                .setCustodyAsset(tuple12.getValue4())
                .setCustodyLiability(tuple12.getValue5())
                .setCustodyLiabilitySection(Web3jUtils.trimToString(tuple12.getValue6()))
                .setAccountingAsset(tuple12.getValue7())
                .setAccountingLiability(tuple12.getValue8())
                .setAccountingLiabilitySection(Web3jUtils.trimToString(tuple12.getValue9()))
                .setStatus(Web3jUtils.trimToString(tuple12.getValue10()))
                .setMeta(MortgageMeta.decompress(tuple12.getValue11(), MortgageMeta.class))
                .setAgreements(agreements);
    }

    /**
     * Изменение статуса закладной
     *
     * @param status
     * @param documentLink
     * @return
     */
    public CompletableFuture<TransactionReceipt> changeStatus(String status, DocumentLink documentLink, String sender) {

        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                mortgage.changeStatus(
                        Web3jUtils.strToBytes32(status),
                        documentLink.toByteArray()
                )::send,
                String.format("Изменение статуса закладной на \"%s\"", status)
        );
    }


    public CompletableFuture<TransactionReceipt> saveExtendedInfo(ActionModifier actionModifier, String hmac, Long expiry, String sender) {

        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                mortgage.saveExtendedInfo(
                        actionModifier.getBytes(), Web3jUtils.strToBytes32(hmac), BigInteger.valueOf(expiry)
                )::send,
                String.format("Сохранение расширенной информации в закладной \"%s\"", mortgageAddress)
        );
    }

    /**
     * Добавление соглашения для указаной закладной
     *
     * @param agreementNumber
     * @param createdAt
     * @param documentLink
     * @param meta
     * @return
     */
    public CompletableFuture<TransactionReceipt> addAgreement(
            String agreementNumber, Long createdAt,
            DocumentLink documentLink, Meta meta, String sender
    ) {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                mortgage.addAgreement(
                        Web3jUtils.strToBytes32(agreementNumber),
                        BigInteger.valueOf(createdAt),
                        documentLink.toByteArray(),
                        meta.compress()
                )::send,
                String.format("Добавление соглашения \"%s\" для указаной закладной", agreementNumber)
        );
    }

    /**
     * аннулирование закладной
     *
     * @param redemptionNumber
     * @param documentLink
     * @return
     */
    public CompletableFuture<TransactionReceipt> redemption(
            String redemptionNumber, DocumentLink documentLink, String sender
    ) {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                mortgage.redemption(
                        Web3jUtils.strToBytes32(redemptionNumber),
                        documentLink.toByteArray()
                )::send,
                String.format("Аннулирование закладной %s. Номер аннулирования: \"%s\"", mortgageAddress, redemptionNumber)
        );
    }

    public Long extendedInfo() {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return mortgage.extendedInfo().sendAsync().join().longValue();
    }

    public String container() {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return mortgage.container().sendAsync().join();
    }

    public String number() {
        return this.getSnapshot(0L).getRegNumber();
    }

    public Long version() {
        return this.getSnapshot(0L).getVersion();
    }

    public CompletableFuture<TransactionReceipt> rollbackToVersion(Long versionToRollback, DocumentLink documentLink, String sender) {
        Mortgage mortgage = Mortgage.load(mortgageAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return mortgage.rollbackToVersion(BigInteger.valueOf(versionToRollback), documentLink.toByteArray()).sendAsync();
    }


}
