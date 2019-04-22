package ddc.service.blockchain.contract;

import ddc.sc2.Registry;
import ddc.service.blockchain.contract.pojo.UserInfo;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import ddc.model.enums.UserRole;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.gasProvider.AbstractDdsGasProvider;
import ddc.service.blockchain.contract.gasProvider.RegistryGasProvider;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RegistryContract {

    private static final String OGRN_PREFIX = "OGRN:";

    private BlockchainService blockchainService;
    private final String registryAddress;
    private AbstractDdsGasProvider gasProvider = new RegistryGasProvider();

    private BigInteger expiry = BigInteger.valueOf(LocalDate.now(ZoneId.of("Europe/Moscow")).plusYears(10).atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond());

    public RegistryContract(BlockchainService blockchainService, String registryAddress) {
        this.blockchainService = blockchainService;
        this.registryAddress = registryAddress;
    }

    public CompletableFuture<Registry> deploy(String ogrn, String nodeId, String signer, String sender ) {

        //sgn:debug
        String nodesigcert = "0x00";
        String nodecrycert = "0x00";
        String sigcert = "0x00";
        String crycert = "0x00";

        return blockchainService.execute(
                Registry.deploy(
                        blockchainService.getWeb3j(),
                        blockchainService.getTM(sender),
                        gasProvider,
                        "0x0",
                        Web3jUtils.strToBytes32(getOrgId(ogrn)),
                        Web3jUtils.strToBytes32(nodeId),
                        Web3jUtils.strToBytes32(nodesigcert),
                        Web3jUtils.strToBytes32(nodecrycert),
                        Web3jUtils.strToBytes32(sigcert),
                        Web3jUtils.strToBytes32(crycert)
                )::send,
                String.format("Деплой реестра c ogrn \"%s\" и id узла \"%s\"", ogrn, nodeId)
        );
    }

    /**
     * Проверка соответствия обертки и контракта в БЧ
     * @return
     */
    public Boolean isValidContract() {
        if (registryAddress.length() != 42) {
            return false;
        }

        Registry registry = loadForRead();
        try {
            return registry.isValid();
        } catch (Exception e) {
            throw new BlockchainDdsException(e);
        }
    }

    /**
     * @param userAddress
     * @return
     */
    public UserInfo getUserOrgIdAndRoleIds(String userAddress) {
        Registry registry = loadForRead();

        Tuple2<byte[], List<byte[]>> userInfo = registry.getUserOrgIdAndRoleIds(userAddress).sendAsync().join();

        List<String> userRoles = userInfo.getValue2().stream()
                .map(Web3jUtils::trimToString)
                .collect(Collectors.toList());

        return new UserInfo()
                .setOgrn(Web3jUtils.removeOgrnPrefix(userInfo.getValue1()))
                .setRoles(userRoles);
    }


    /**
     * Загрузка СК для чтения данных
     *
     * @return
     */
    public Registry loadForRead() {
        return loadForWrite(Web3jUtils.ZERO_ADDRESS);
    }

    /**
     * Загрузка СК для записи в блокчейн от имени sender
     *
     * @param sender
     * @return
     */
    private Registry loadForWrite(String sender) {
        return Registry.load(registryAddress, blockchainService.getWeb3j(), blockchainService.getTM(sender), gasProvider);
    }

    /**
     * Обновление профилья пользователя
     *
     * @param userId аккаунт пользователя
     * @param newProfileContainer контейнер профиля
     * @param sender отправитель транзакции
     * @return
     */
    public CompletableFuture<TransactionReceipt> updateUserProfile(String userId, String newProfileContainer, String sender) {
        Registry registry = loadForWrite(sender);

        return blockchainService.execute(
                registry.updateUserProfile(userId, newProfileContainer)::send,
                String.format("Обновление профиля пользователя %s", userId)
        );
    }

    /**
     * Регистрирует организацию в реестре, если ранее не была зарегистрирована
     */
    public CompletableFuture<Optional<TransactionReceipt>> createNode(String ogrn, String orgProfile, String nodeId, String nodeProfile, String orgAdminAddress, String orgAdminProfile, String signer, String signerProfile, String sender) {
        Registry registry = loadForWrite(sender);

        //sgn:debug
        String nodesigcert = "0x00";
        String nodecrycert = "0x00";
        String adminsigcert = "0x00";
        String admincrycert = "0x00";

        return registry.orgs(Web3jUtils.strToBytes32(getOrgId(ogrn)))
                .sendAsync()
                .thenApply(orgTuple -> {
                    if (Web3jUtils.isNotZeroAddress(orgTuple.getValue2()) && Web3jUtils.isNotZeroAddress(orgTuple.getValue3())) {
                        return Optional.empty();
                    } else {
                        return Optional.of(blockchainService.execute(
                            registry.createNode(
                                    Web3jUtils.strToBytes32(getOrgId(ogrn)),
                                    orgProfile,
                                    Web3jUtils.strToBytes32(nodeId),
                                    nodeProfile,
                                    Web3jUtils.strToBytes32(nodesigcert),
                                    Web3jUtils.strToBytes32(nodecrycert),
                                    orgAdminAddress,
                                    orgAdminProfile,
                                    Web3jUtils.strToBytes32(adminsigcert),
                                    Web3jUtils.strToBytes32(admincrycert)
                            )::send,
                            String.format("Создание организации и узла в реестр по ogrn \"%s\" и nodeId \"%s\" администратором \"%s\"", ogrn, nodeId, orgAdminAddress)
                        ).join());
                    }
                });
    }

    //*
    public CompletableFuture<Optional<TransactionReceipt>> createUser(String ogrn, String address, String profile, String sender) {
        Registry registry = loadForWrite(sender);

        //sgn:debug
        String sigcert = "0x00";
        String crycert = "0x00";

        return registry.users(address)
                .sendAsync()
                .thenApply(userTuple -> {
                    if (Web3jUtils.isNotZeroAddress(userTuple.getValue3()) && getOrgId(ogrn).equals(Web3jUtils.trimToString(userTuple.getValue2()))) {
                        return Optional.empty();
                    } else {
                        return Optional.of(
                            blockchainService.execute(
                                registry.createUser(
                                    Web3jUtils.createOrgIdFromOgrn(ogrn),
                                    address,
                                    profile,
                                    Web3jUtils.strToBytes32(sigcert),
                                    Web3jUtils.strToBytes32(crycert)
                                )::send,
                                String.format("Создание пользователя с адресом \"%s\" в реестре организации с ogrn \"%s\" от имени \"%s\"", address, ogrn, sender)
                            ).join()
                        );
                    }
                });
    }
    //*/

    /**
     * Устанавливает роль пользователя, если роль еще не установлена
     */
    public CompletableFuture<Optional<TransactionReceipt>> grantUserRole(String orgAdminAddress, UserRole userRole, BigInteger archiveId, String sender) {

        Registry registry = loadForWrite(sender);

        return registry.userHasRole(orgAdminAddress, userRole.getBytes())
                .sendAsync()
                .thenApply(userHasRole -> {
                    if (userHasRole) {
                        return Optional.empty();
                    } else {
                        return Optional.of(
                            blockchainService.execute(
                                registry.grantUserRole(orgAdminAddress, userRole.getBytes(), archiveId)::send,
                                String.format("Назначение пользователю \"%s\" роль \"%s\" в реестре от имени \"%s\"", orgAdminAddress, userRole.name(), sender)
                            ).join()
                        );
                    }
                });
    }

    /**
     * @param ogrn - ОГРН организации
     * @return промис на список адрессов пользавателей организации
     */
    public CompletableFuture<Boolean> orgIsKnown(String ogrn) {
        Registry registry = loadForRead();
        return registry.orgIsKnown(Web3jUtils.createOrgIdFromOgrn(ogrn)).sendAsync();
    }

    /**
     * @param orgId - id организации (ее огрн)
     * @return промис на список адрессов пользавателей организации
     */
    public CompletableFuture<List> getOrgUserIds(String orgId) {
        Registry registry = loadForRead();
        return registry.getOrgUserIds(Web3jUtils.strToBytes32(getOrgId(orgId))).sendAsync();
    }

    public CompletableFuture<Boolean> userIsEnabled(String userId) {
        Registry registry = loadForRead();
        return registry.userIsEnabled(userId).sendAsync();
    }

    private String getOrgId(String ogrn) {
        return OGRN_PREFIX.concat(ogrn);
    }

    public CompletableFuture<List> getAllNodesIds() {
        Registry registry = loadForWrite(Web3jUtils.ZERO_ADDRESS);
        return registry.getNodeIds().sendAsync();
    }

    public CompletableFuture<byte[]> getUserSigcert(String userId) {
        Registry registry = loadForRead();
        return registry.getUserSigCert(userId).sendAsync();
    }
}
