package ddc.service.blockchain.contract;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ddc.sc2.DDSystem;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.meta.AccountMeta;
import ddc.util.struct.DocumentLink;

import java.util.List;
import java.util.concurrent.CompletableFuture;

abstract class AccountsContract extends OrganizationsContract {

    /**
     * Добавляет счета в СК для организации по типу и номеру
     *
     * @param container
     * @param ogrn
     * @param accType
     * @param number
     * @param deponentOgrn
     * @param meta
     * @param dl
     * @param sender
     * @return
     * @throws BlockchainDdsException
     */
    public CompletableFuture<TransactionReceipt> addAccount(
            String container, String ogrn, String accType, String number, String deponentOgrn, AccountMeta meta, DocumentLink dl, String sender
    ) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        return this.execute(
                ddSystem.addAccount(
                        Web3jUtils.ZERO_ADDRESS,
                        container,
                        Web3jUtils.strToBytes32(addOgrnPrefix(ogrn)),
                        Web3jUtils.strToBytes8(accType),
                        Web3jUtils.strToBytes32(number),
                        Web3jUtils.strToBytes32(addOgrnPrefix(deponentOgrn)),
                        meta.compress(),
                        dl.toByteArray()

                )::send,
                String.format("Добавление счета в СК для организации \"%s\" по типу \"%s\" и номеру \"%s\" для депонента \"%s\"", ogrn, accType, number, deponentOgrn)
        );
    }

    /**
     * Возвращает адрес СК счета для организации по типу и номеру
     *
     * @param ogrn
     * @param number
     * @param sender
     * @return адрес СК счета
     */
    public String getAccount(String ogrn, String number, String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        return ddSystem.getAccount(
                Web3jUtils.createOrgIdFromOgrn(ogrn),
                Web3jUtils.strToBytes32(number)
        ).sendAsync().join();
    }

    public List<DDSystem.AccountEventEventResponse> getAccountEventsBy(TransactionReceipt transactionReceipt){
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return ddSystem.getAccountEventEvents(transactionReceipt);
    }
}