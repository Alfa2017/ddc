package ddc.service.blockchain.contract;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.EventValues;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import ddc.sc2.Container;
import ddc.sc2.Registry;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.gasProvider.AbstractDdsGasProvider;
import ddc.service.blockchain.contract.gasProvider.ContainerGasProvider;
import ddc.service.blockchain.deploy.ContainerModifier;
import ddc.service.blockchain.deploy.OrgRole;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
public class ContainerContract {

    private final BlockchainService blockchainService;
    @Getter
    private String containerAddress;
    private AbstractDdsGasProvider gasProvider = new ContainerGasProvider();

    private BigInteger expiry = BigInteger.valueOf(LocalDate.now(ZoneId.of("Europe/Moscow")).plusYears(10).atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond());

    /**
     * @param blockchainService
     * @param containerAddress  Адрес СК ДДС
     */
    public ContainerContract(BlockchainService blockchainService, String containerAddress) {
        this.blockchainService = blockchainService;
        this.containerAddress = containerAddress;
    }

    public CompletableFuture<Container> deploy(String registry, String roleModel, ContainerModifier modifier, OrgRole orgRole, String sender) {
        // TODO: modifier - ENUM Модификаторов контейнеров

        return blockchainService.execute(
                Container.deploy(
                        blockchainService.getWeb3j(),
                        blockchainService.getTM(sender),
                        gasProvider,
                        "0x0",
                        registry,
                        roleModel,
                        modifier.getBytes(),
                        orgRole.getBytes()
                )::send,
                String.format("Деплой контейнера с реестром хранилища \"%s\", ролевой моделью \"%s\" и модификатором \"%s\"", registry, roleModel, modifier.name())
        ).thenApply(tr -> {
            this.containerAddress = tr.getContractAddress();
            return tr;
        });
    }

    public CompletableFuture<Long> append(ContainerModifier modifier, byte[] hmac, String sender) {
        Container container = Container.load(containerAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);

        return blockchainService.execute(
                container.appendArchive(
                        modifier.getBytes(),
                        hmac,
                        expiry
                )::send,
                String.format("Добавление архива с модификатором \"%s\" в контейнер", modifier.name())
        ).thenApply(tr -> {
            final List<EventValues> eventValues = tr.getLogs().stream()
                    .map(eventLog -> Contract.staticExtractEventParameters(Registry.NEWARCHIVE_EVENT, eventLog))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (eventValues.size() != 1)
                throw new IllegalStateException("Ожидалось 1 событие добавления архива, актуальное значение " + eventValues.size());
            return ((BigInteger) eventValues.get(0).getNonIndexedValues().get(0).getValue()).longValue();
        });
    }

    public CompletableFuture<TransactionReceipt> grantOrgRole(String orgId, String sender) {
        Container container = Container.load(containerAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
        return blockchainService.execute(
                container.grantOrgRole(Web3jUtils.createOrgIdFromOgrn(orgId), OrgRole.Depository.getBytes())::send,
                String.format("Назначения роли %s организации %s в контейнере %s", OrgRole.Depository.name(), orgId, containerAddress)
        ).thenApply(transactionReceipt -> {
            log.debug("Назначил роль {} организации {} в контейнере {}", OrgRole.Depository.name(), orgId, containerAddress);
            return transactionReceipt;
        }).exceptionally(ex -> {
            log.error("Не удалось назначить роль {} организации {} в контейнере {}. Ошибка {}", OrgRole.Depository.name(), orgId, containerAddress, ex.getMessage());
            throw new CompletionException(ex);
        });
    }
}
