package ddc.service.blockchain.contract;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ddc.sc2.DDSystem;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.meta.MortgageMeta;
import ddc.util.struct.DocumentLink;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

abstract class MortgagesContract extends AccountsContract {

    /**
     * Добавление закладной от указанного менеджера
     *
     * @param container           Контейнер закладной
     * @param number              Регистрационный номер закладной
     * @param custodyAsset        Активный счет в ДХ
     * @param custodyLiability    Пассивный счет в ДХ
     * @param accountingAsset     Активный счет в ДУ
     * @param accountingLiability Пассивный счет в ДУ
     * @param meta                Метаинформация
     * @param dl                  Документ-основание
     * @param sender              Отправитель транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public CompletableFuture<TransactionReceipt> addMortgage(
            String container,
            String number,
            String custodyAsset,
            String custodyLiability,
            String accountingAsset,
            String accountingLiability,
            MortgageMeta meta,
            DocumentLink dl,
            String sender
    ) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        try {
            return this.execute(
                    ddSystem.addMortgage(
                            "0x0",
                            container,
                            dl.toByteArray(),
                            number.getBytes(),
                            custodyAsset, // Активный счет  в ДХ
                            custodyLiability,
                            accountingAsset,
                            accountingLiability,
                            meta.compress()
                    )::send,
                    String.format("Добавление закладной \"%s\" для контейнера \"%s\" Активный счет в ДХ \"%s\" Активный счет в ДУ \"%s\" Пассивный счет в ДХ \"%s\" Пассивный счет в ДУ \"%s\"",
                            number, container, custodyAsset, accountingLiability, custodyLiability, accountingLiability)
            );
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    public List<DDSystem.MortgageEventEventResponse> getMortgageEventEvents(TransactionReceipt receipt, String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        return ddSystem.getMortgageEventEvents(receipt);
    }

    public String getMortgageByNumber(String number) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return ddSystem.getMortgageByNumber(number.getBytes()).sendAsync().join();
    }
}
