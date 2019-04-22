package ddc.service.blockchain;

import ddc.sc2.DDSystem;
import ddc.service.DdsConfigService;
import ddc.service.blockchain.contract.DdsContract;
import ddc.service.blockchain.subscriber.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import ddc.AppState;
import ddc.exception.ObserverDdsRuntimeException;
import ddc.exception.ServerErrorDdsException;
import ru.iteco.aft.dds.service.blockchain.subscriber.*;

import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

@Slf4j
@Service
public class BlockchainEventService implements Observer {


    @Getter
    private DdsConfigService ddsConfigService;

    @Autowired
    BlockchainService blockchainService;

    @Autowired
    BlockSubscriber<EthBlock> blockSubscriber;

    @Autowired
    OrganizationsSubscriber organizationSubscriber;

    @Autowired
    DeponentsSubscriber deponentsSubscriber;

    @Autowired
    AccountsSubscriber accountsSubscriber;

    @Autowired
    MortgageSubscriber mortgageSubscriber;

    @Autowired
    DocumentSubscriber documentSubscriber;

    @Autowired
    @Getter
    private AppState appState;

    private Boolean isSubscribed = Boolean.FALSE;

    @Getter
    @Setter
    private DefaultBlockParameter organizationsEventLastBlock;
    @Getter
    @Setter
    private DefaultBlockParameter accountsEventLastBlock;
    @Getter
    @Setter
    private DefaultBlockParameter mortgagesEventLastBlock;
    @Getter
    @Setter
    private DefaultBlockParameter documentEventLastBlock;

    @Autowired
    private void BlockchainEventService(DdsConfigService ddsConfigService) {
        this.ddsConfigService = ddsConfigService;
        ddsConfigService.addObserver(this);
    }

    class Subscr {
        AbstractSubscriber subscriber;
        Subscription subscription;
        DefaultBlockParameter lastBlock;
    }

    public void resetLastBlock(Long currentBlockNumber) {
        if (isSubscribed) {
            log.error("Попытка сброса номера последнего блока при неотмененной подписке");
            return;
        }
        log.debug("Сбрасываю номер последнего блока");
        DefaultBlockParameter startBlock = new DefaultBlockParameterNumber(currentBlockNumber);
        organizationsEventLastBlock = startBlock;
        accountsEventLastBlock = startBlock;
        mortgagesEventLastBlock = startBlock;
        documentEventLastBlock = startBlock;
    }

    public synchronized void subscribe() throws ServerErrorDdsException {
        if (isSubscribed) {
            log.error("Уже подписан на события");
            return;
        }

        DdsContract ddsContract = ddsConfigService.getDdsContract();
        DDSystem ddSystem = ddsContract.getContract(appState.getRobotAddress());

        BigInteger startAtBlock = ddsContract.startAtBlock(appState.getRobotAddress());
        DefaultBlockParameter startBlock = DefaultBlockParameter.valueOf(startAtBlock);

        log.debug("Подписываюсь на события ДДС с блока: {}", startAtBlock);

        DefaultBlockParameter endBlock = DefaultBlockParameterName.LATEST;


        ddSystem.documentEventEventFlowable(startBlock, endBlock).subscribe(documentSubscriber);
        ddSystem.organizationEventEventFlowable(startBlock, endBlock).subscribe(organizationSubscriber);
        ddSystem.deponentEventEventFlowable(startBlock, endBlock).subscribe(deponentsSubscriber);
        ddSystem.accountEventEventFlowable(startBlock, endBlock).subscribe(accountsSubscriber);
        ddSystem.mortgageEventEventFlowable(startBlock, endBlock).subscribe(mortgageSubscriber);


        blockchainService.getWeb3j().blockFlowable(false).subscribe(blockSubscriber::onNext);


//        web3jService.getWeb3j().pendingTransactionFlowable().subscribe(tx -> {
//            log.debug("Транзакция c хэшом [{}] добавлена в мемпул ноды", tx.getHash());
//        });

        isSubscribed = Boolean.TRUE;
    }

    /* public synchronized void unsubscribe() {
        if (!isSubscribed) {
            return;
        }

        log.debug("отменяю подписку на события ДДС");
        organizationsSubscriber.unsubscribe();
        accountsSubscribe.unsubscribe();
        mortgagesSubscribe.unsubscribe();
        dictionarySubscribe.unsubscribe();
        managersSubscribe.unsubscribe();
        documentSubscribe.unsubscribe();
        blockSubscribe.unsubscribe();
        isSubscribed = false;
    } */

    @Override
    public void update(Observable observable, Object o) {
        log.debug("Перезапуск подписчиков событий");

        if (ddsConfigService.getDdsContract() == null) {
            log.debug("DdsContract ещё не готов");
            // TODO: Отписаться, почистить базу
            return;
        }

        try{
//            Long currentBlockNumber = onStartupEventResolver.getPreviousEvents();
//            resetLastBlock(currentBlockNumber);
            subscribe();
        } catch (ServerErrorDdsException e){
            throw new ObserverDdsRuntimeException(e);
        }
    }
}
