package ddc.service.blockchain.contract;

import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import ddc.sc2.DDSystem;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.meta.DeponentMeta;
import ddc.service.blockchain.contract.meta.DepositoryMeta;
import ddc.service.blockchain.contract.pojo.OrgInfo;
import ddc.service.blockchain.deploy.ActionModifier;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
abstract class OrganizationsContract extends DocumentsContract {

    public static final String PREFIX = "OGRN:";

    /**
     * @param ogrn   ОГРН проверяемой организации
     * @param sender Аккаунт отправителя транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public Boolean orgIsExist(String ogrn, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        try {
            return ddSystem.orgIsExist(Web3jUtils.createOrgIdFromOgrn(ogrn)).send();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    /**
     * Возаращает информацию об организации
     *
     * @param ogrn   ОГРН организации
     * @param sender Аккаунт отправителя транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public OrgInfo<DepositoryMeta> getOrgInfo(String ogrn, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        try {
            Tuple3<Boolean, String, byte[]> tuple3 = ddSystem.getOrgInfo(Web3jUtils.strToBytes32(addOgrnPrefix(ogrn))).send();
            return new OrgInfo<DepositoryMeta>()
                    .setIsExist(tuple3.getValue1())
                    .setContainer(tuple3.getValue2())
                    .setMeta(DepositoryMeta.decompress(tuple3.getValue3(), DepositoryMeta.class));
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }


    /**
     * Возаращает информацию об организации
     *
     * @param ogrn         ОГРН организации
     * @param deponentOgrn ОГРН депонента
     * @param sender       Аккаунт отправителя транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public OrgInfo<DeponentMeta> getDeponentOrgInfo(String ogrn, String deponentOgrn, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        try {
            Tuple3<Boolean, String, byte[]> tuple3 = ddSystem.getDeponentOrgInfo(
                    Web3jUtils.strToBytes32("OGRN:" + ogrn),
                    Web3jUtils.strToBytes32("OGRN:" + deponentOgrn)
            ).send();
            return new OrgInfo<DeponentMeta>()
                    .setIsExist(tuple3.getValue1())
                    .setContainer(tuple3.getValue2())
                    .setMeta(DeponentMeta.decompress(tuple3.getValue3(), DeponentMeta.class));
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    /**
     * @param ogrn         ОГРН организации
     * @param deponentOgrn ОГРН депонента в организации
     * @param sender       Аккаунт отправителя транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public Boolean deponentIsExist(String ogrn, String deponentOgrn, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        try {
            return ddSystem.deponentIsExist(
                    Web3jUtils.createOrgIdFromOgrn(ogrn),
                    Web3jUtils.createOrgIdFromOgrn(deponentOgrn)
            ).send();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    /**
     * Добавление/редактирование/удаление организации
     *
     * @param action    Действие (Добавление/редактирование/удаление)
     * @param ogrn      ОГРН
     * @param container контейнер
     * @param meta      метаинформация
     * @param sender    Отправитель транзакции
     * @return
     * @throws BlockchainDdsException
     */
    public CompletableFuture<Boolean> editOrganization(ActionModifier action, String ogrn, String container, DepositoryMeta meta, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);

        return this.execute(
                ddSystem.editOrganization(
                        action.getBytes(),
                        Web3jUtils.createOrgIdFromOgrn(ogrn),
                        container,
                        meta.compress()
                )::send,
                action.getDesc()
        ).thenApply(TransactionReceipt::isStatusOK);
    }

    /**
     * Добавление/редактирование/удаление депонента организации
     *
     * @param action
     * @param ogrn
     * @param deponentOgrn
     * @param container
     * @param meta
     * @param sender
     * @return
     */
    public CompletableFuture<TransactionReceipt> editDeponent(ActionModifier action, String ogrn, String deponentOgrn, String container, DeponentMeta meta, String sender) throws BlockchainDdsException {
        DDSystem ddSystem = DDSystem.load(ddsAddress, blockchainService.getWeb3j(), getTm(sender), gasProvider);
        return this.execute(
                ddSystem.editDeponent(
                        action.getBytes(),
                        Web3jUtils.createOrgIdFromOgrn(ogrn),
                        Web3jUtils.createOrgIdFromOgrn(deponentOgrn),
                        container,
                        meta.compress()
                )::send,
                action.getDesc()
        );
    }


    protected String addOgrnPrefix(String ogrn) {
        if (!ogrn.startsWith(PREFIX)) return PREFIX + ogrn;
        return ogrn;
    }


    /**
     * Получить список событий OrganizationEvent где есть искомый ОГРН и модификатор действия
     * @param ddsAddress
     * @param organizationOgrn
     * @param modifier
     * @return
     */
    public List<DDSystem.OrganizationEventEventResponse> getOrganizationEventByOgrnAndAction(String ddsAddress, String organizationOgrn, ActionModifier modifier) {

        EthFilter filter = new EthFilter(
                DefaultBlockParameter.valueOf(BigInteger.ZERO),
                DefaultBlockParameter.valueOf(DefaultBlockParameterName.LATEST.name()),
                ddsAddress
        ).addSingleTopic(EventEncoder.encode(DDSystem.ORGANIZATIONEVENT_EVENT))
        .addSingleTopic(Web3jUtils.createTopicFromBytes32(modifier.getBytes()))
        .addSingleTopic(Web3jUtils.createTopicFromBytes32(Web3jUtils.createOrgIdFromOgrn(organizationOgrn)));

        return blockchainService.getEventsList(filter)
                .stream().map(log -> Contract.staticExtractEventParameters(DDSystem.ORGANIZATIONEVENT_EVENT, log))
                .filter(Objects::nonNull)
                .map(this::getOrganizationEvent)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private DDSystem.OrganizationEventEventResponse getOrganizationEvent(EventValues eventValues) {
        List<Type> indexedVals = eventValues.getIndexedValues();
        List<Type> nonindexedVals = eventValues.getNonIndexedValues();
        DDSystem.OrganizationEventEventResponse event = new DDSystem.OrganizationEventEventResponse();
        event.counter = (BigInteger) nonindexedVals.get(0).getValue();
        event.action = (byte[]) indexedVals.get(0).getValue();
        event.orgId = (byte[]) indexedVals.get(1).getValue();
        return event;
    }

}
