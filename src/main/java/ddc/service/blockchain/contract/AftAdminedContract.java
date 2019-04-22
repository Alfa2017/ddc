package ddc.service.blockchain.contract;

import org.web3j.tx.TransactionManager;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.contract.gasProvider.AbstractDdsGasProvider;
import ddc.service.blockchain.contract.gasProvider.DdsGasProvider;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

abstract class AftAdminedContract {

    protected String ddsAddress;

    protected BlockchainService blockchainService;

    protected AbstractDdsGasProvider gasProvider = new DdsGasProvider();

    TransactionManager getTm(String address) {
        return blockchainService.getTM(address);
    }

    /**
     * Выполнить запрос к смарт-контракту
     * @param callable
     * @param description
     * @param <T>
     * @return
     */
    public <T> CompletableFuture<T> execute(Callable<T> callable, String description) {
        return blockchainService.execute(callable, description);
    }
}
