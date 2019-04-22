package ddc.service.blockchain;

import ddc.service.blockchain.subscriber.AbstractSubscriber;
import ddc.service.blockchain.subscriber.SubscriberFactory;
import ddc.service.domain.BufferedEventService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ddc.AppState;
import ddc.exception.ExceptionMessages;
import ddc.exception.ServerErrorDdsException;
import ddc.model.entity.BufferedEventEntity;
import ddc.util.Utils;

import java.util.List;

@Slf4j
@Service
public class EventResolver {

    @Value("${app.events.buffer.delay}")
    private Integer delay;

    @Autowired
    private BufferedEventService bufferedEventService;

    @Getter
    @Autowired
    private SubscriberFactory subscriberFactory;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private AppState appState;

    public synchronized void resolveEvents(Long currentBlockNumber) {
        List<BufferedEventEntity> entities = bufferedEventService.findAllWithLifetimeMoreThanSpecified(currentBlockNumber, delay.longValue());
        entities.forEach(this::processEntity);
    }

    private void processEntity(BufferedEventEntity e) {
        try {
            TransactionReceipt tr = blockchainService.getWeb3j().ethGetTransactionReceipt(e.getTransactionHash()).send().getTransactionReceipt()
                    .orElseThrow(() -> new ServerErrorDdsException(ExceptionMessages.NO_TRANSACTON_FOR_BUFFER.getMessage(e.getTransactionHash())));
            e.setBlockHash(tr.getBlockHash());
            e.setBlockNumber(tr.getBlockNumber().longValue());
            e.setTransactionIndex(tr.getTransactionIndex().longValue());
            Object event = Utils.jsonToObject(e.getEventJson(), Class.forName(e.getEventClass()));
            AbstractSubscriber subscriber = subscriberFactory.getSubscriberByEvent(event);
            subscriber.process(event);
            e.setProcessed(true);
        } catch (Exception ex) {
            e.setProcessed(true);
            log.error("Не удалось вызвать execute для транзакции {}, буферизированного события {}: {}", e.getTransactionHash(), e.getEventClass(), ex.getMessage());
            log.error("DdsAddress: {}, OrgId: {}", appState.getDdsAddress(), appState.getDepositoryOgrn());
        }
        bufferedEventService.createOrUpdate(e);
    }
}
