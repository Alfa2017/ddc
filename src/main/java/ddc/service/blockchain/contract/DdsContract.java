package ddc.service.blockchain.contract;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ddc.sc2.DDSystem;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DdsContract extends MortgagesContract {

    /**
     * @param blockchainService
     * @param ddsAddress Адрес СК ДДС
     */
    public DdsContract(BlockchainService blockchainService, String ddsAddress) {
        this.ddsAddress = ddsAddress;
        this.blockchainService = blockchainService;
    }

    /**
     * Проверка соответствия обертки и контракта в БЧ
     * @return
     */
    public Boolean isValidContract() {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);
        try {
            return ddSystem.isValid();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    public BigInteger startAtBlock(String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        try {
            return ddSystem.startAtBlock().send();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    public DDSystem getContract(String sender) {
        return DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
    }


    public String getRegistryAddress() {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);
        try {
            return ddSystem.registry().send();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    public String getRoleModelAddress() {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);
        try {
            return ddSystem.roleModel().send();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    public CompletableFuture<DDSystem> deploy(String registryAddress, String roleModelAddress, String accountFactory, String mortgageFactory, String sender) {
        return this.execute(
                DDSystem.deploy(
                        blockchainService.getWeb3j(),
                        blockchainService.getTM(sender),
                        gasProvider,
                        "0x0",
                        registryAddress,
                        roleModelAddress,
                        accountFactory,
                        mortgageFactory
                )::send,
                String.format("Деплой СК ДДС c реестром \"%s\" и ролевой моделью \"%s\"", registryAddress, roleModelAddress)
        ).thenApply(tr -> {
            this.ddsAddress = tr.getContractAddress();
            return tr;
        });
    }

    public List<DDSystem.DeponentEventEventResponse> getDeponentEventsBy(TransactionReceipt transactionReceipt){
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(Web3jUtils.ZERO_ADDRESS), gasProvider);
        return ddSystem.getDeponentEventEvents(transactionReceipt);
    }

    public List<DDSystem.ErrorEventEventResponse> getErrorEventsBy(TransactionReceipt receipt, String sender) {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        return ddSystem.getErrorEventEvents(receipt);
    }
}
