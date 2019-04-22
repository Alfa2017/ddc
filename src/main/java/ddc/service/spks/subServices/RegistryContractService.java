package ddc.service.spks.subServices;

import ddc.service.blockchain.contract.RegistryContract;
import ddc.service.blockchain.contract.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ddc.AppState;
import ddc.model.enums.UserRole;
import ddc.service.blockchain.BlockchainService;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistryContractService {

    private BlockchainService blockchainService;
    private final AppState appState;


    public RegistryContractService(BlockchainService blockchainService, AppState appState) {
        this.blockchainService = blockchainService;
        this.appState = appState;
    }

    /**
     * Проверяет на соответствие обертки СК реестра.
     * Может давать ложно отрицательный вариант, когда СК реестра обновился в блокчейне, но не обновилась обертка
     * @param registryAddress
     * @return
     */
    public Boolean isValidContract(String registryAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, registryAddress);
        return registryContract.isValidContract();
    }

    /**
     * Проверяет существование организации в реестре
     * @param registryAddress
     * @param ogrn
     * @return
     */
    public Boolean orgIsKnown(String registryAddress, String ogrn) {
        RegistryContract registryContract = new RegistryContract(blockchainService, registryAddress);
        return registryContract.orgIsKnown(ogrn).join();
    }

    /**
     * @return список адрессов пользавтелей для нашей организации
     */
    public List<String> getOrgUsers() {
        return getOrgUserIds(appState.getDepositoryOgrn());
    }



    public UserInfo getUserOrgIdAndRoleIds(String userAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        return registryContract.getUserOrgIdAndRoleIds(userAddress);
    }

    public List<String> getOrgUserIds(String ogrn) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        return registryContract.getOrgUserIds(ogrn).join();
    }

    /**
     * @param userAddress - адресс пользователя, по которому нужны списки ролей
     * @return
     */
    public List<String> getUserRoles(String userAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        UserInfo userOrgIdAndRoleIds = registryContract.getUserOrgIdAndRoleIds(userAddress);
        return userOrgIdAndRoleIds.getRoles();
    }

    public String getUserStatus(String userAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        Boolean userIsEnabled = registryContract.userIsEnabled(userAddress).join();
        return userIsEnabled ? "ACTIVE" : "DISABLED";
    }

    /**
     * Создает юзера для депозитария, если ранее не был создан
     *
     * @param depositoryOgrn       - огрн депозитария, которому создаем пользователя
     * @param userAddress          - аресс аккаунта на ноде, который станет участником системы
     * @param userContainerAddress - адресс заранее задеплоинного контейнера пользователя (по идее там еще и профиль должен лежать)
     * @param adminAddress         - адресс админа, который выполняет операцию
     * @return
     */
    public CompletableFuture<Optional<TransactionReceipt>> createUser(String depositoryOgrn, String userAddress, String userContainerAddress, String adminAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        return registryContract.createUser(depositoryOgrn, userAddress, userContainerAddress, adminAddress);
    }

    public CompletableFuture<Optional<TransactionReceipt>> grantUserRole(String userAddress, UserRole userRole, BigInteger expiry, String adminAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        return registryContract.grantUserRole(userAddress, userRole, expiry, adminAddress);
    }

    public CompletableFuture<List<String>> getAllNodesIds() {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        return registryContract.getAllNodesIds().thenApply(
                list -> ((ArrayList<Object>) list).stream().map(n -> new String((byte[]) n).trim()).collect(Collectors.toList())
        );
    }

    public String getUserSigcert(String userAddress) {
        RegistryContract registryContract = new RegistryContract(blockchainService, appState.getRegistryAddress());
        byte[] cert = registryContract.getUserSigcert(userAddress).join();
        return DatatypeConverter.printHexBinary(cert);
    }
}
