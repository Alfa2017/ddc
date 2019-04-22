package ddc.service.blockchain;

import ddc.service.blockchain.custom.web3j.CustomJsonRpc2_0Web3j;
import ddc.service.blockchain.custom.web3j.CustomWeb3jService;
import ddc.service.blockchain.custom.web3j.model.CustomEthTransaction;
import ddc.service.blockchain.custom.web3j.model.CustomTransaction;
import ddc.service.blockchain.transaction.DdsTransactionManager;
import ddc.service.blockchain.transaction.DdsTransactionService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Async;
import ddc.AppState;
import ddc.exception.ExceptionMessages;
import ddc.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BlockchainService {

    private final AppState appState;
    private final CustomWeb3jService customWeb3jService;

    @Getter
    private DdsTransactionService transactionService;

    @Getter
    private Web3j web3j;

    @Getter
    private Admin web3jAdmin;


    @Autowired
    public BlockchainService(AppState appState, CustomWeb3jService customWeb3jService, Web3jService web3jService, DdsTransactionService transactionService) {
        this.appState = appState;
        this.customWeb3jService = customWeb3jService;
        web3j = new CustomJsonRpc2_0Web3j(web3jService, 1000L, Async.defaultExecutorService(), () -> {
            this.appState.getSessions().clear();
        });
        web3jAdmin = Admin.build(web3jService);
        this.transactionService = transactionService;
    }

    public interface ConnectExceptionCallback {
        void call();
    }


    public BigInteger getBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return BigInteger.ZERO;
    }

    @Cacheable("transactionManager")
    public TransactionManager getTM(String address) {
        return new DdsTransactionManager(web3j, address, transactionService);
    }

    @Cacheable("blockTimestamp")
    public Long getTimestampByBlockHash(String blockHash) {
        try {
            return Utils.nullSafe(
                    blockHash,
                    bh -> web3j.ethGetBlockByHash(bh, false).sendAsync().join().getBlock().getTimestamp().longValue(),
                    () -> {
                        log.error("Не удалось получить timestamp по хэшу блока {}", blockHash);
                        return 0L;
                    }
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return 0L;
    }

    @Cacheable("transactionInfo")
    public Optional<CustomTransaction> getTransactionInfoByTransactionHash(String transactionHash) {
        try {
            CustomEthTransaction tr = customWeb3jService.ethGetTransactionByHash(transactionHash);
            return tr.getTransaction();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    public <T> CompletableFuture<T> execute(Callable<T> callable, String message) {
        return transactionService.run(callable, message);
    }


    /**
     * Возвращает список аккаунтов доступных на ноде
     * @return
     */
    public List<String> getAccounts() {
        try {
            return web3j.ethAccounts().send().getAccounts();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BlockchainDdsException(e.getMessage());
        }
    }

    public List<Log> getEventsList(String address, Event event) {
        DefaultBlockParameter startBlock = DefaultBlockParameterName.EARLIEST;
        DefaultBlockParameter endBlock = DefaultBlockParameterName.LATEST;
        EthFilter filter = new EthFilter(startBlock, endBlock, address);
        filter.addSingleTopic(EventEncoder.encode(event));

        return getEventsList(filter);
    }

    public List<Log> getEventsList(EthFilter filter) {
        try {
            final EthLog ethLog = web3j.ethGetLogs(filter).send();
            if (ethLog.hasError()) {
                throw new BlockchainDdsException(ethLog.getError().getMessage());
            }
            return Optional.ofNullable(ethLog.getLogs())
                    .map(logResults ->
                            logResults.stream()
                                    .map(logResult -> (Log) logResult)
                                    .collect(Collectors.toList())
                    ).orElseGet(ArrayList::new);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BlockchainDdsException(e.getMessage());
        }
    }

    public BigInteger getTimestampByBlockNumber(Long blockNumber) throws Exception {
        return web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), false).send().getBlock().getTimestamp();
    }

    public Long getTimestampByTransactionHash(String transactionHash) {
        EthTransaction tr = web3j.ethGetTransactionByHash(transactionHash).sendAsync().join();
        return this.getTimestampByBlockHash(tr.getTransaction().get().getBlockHash());
    }

    public Long getEstimateBlockFormationTime(long substruction) {
        try {
            if (substruction <= 0)
                throw new IllegalArgumentException(ExceptionMessages.BLOCKS_NUMBER_NOT_POSITIVE.getMessage());
            long currentBlockNumber = this.getBlockNumber().longValue();
            long currentMinusTenBlockNumber = currentBlockNumber - 10;
            long currentBlockTimestamp = this.getTimestampByBlockNumber(currentBlockNumber).longValue();
            long currentMinusTenBlockTimestamp = this.getTimestampByBlockNumber(currentMinusTenBlockNumber).longValue();
            return (currentBlockTimestamp - currentMinusTenBlockTimestamp) / 10;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
