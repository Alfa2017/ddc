package ddc.service;

import ddc.service.blockchain.contract.DdsContract;
import ddc.service.domain.manager.ManagerService;
import ddc.service.spks.subServices.RegistryContractService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthSign;
import ddc.AppState;
import ddc.service.blockchain.BlockchainService;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Service
@Slf4j
public class DdsConfigService extends Observable implements Observer {

    @Getter
    private BlockchainService blockchainService;

    private ManagerService managerService;

    @Autowired
    private RegistryContractService registryContractService;

    @Getter
    private AppState appState;

    @Getter
    private DdsContract ddsContract;

    @Autowired
    public void DdsConfigService(AppState appState, BlockchainService blockchainService, ManagerService managerService)
    {
        this.appState = appState;
        this.blockchainService = blockchainService;
        this.managerService = managerService;
        this.addObserver(managerService);
        appState.addObserver(this);
    }

    /**
     * Проверка контракта ДДС
     * @param appState
     * @return
     */
    public DdsContract loadDdsContract(AppState appState) {
        if (appState.getDdsAddress().length() != 42) {
            log.warn("Некорректный адрес контракта DDSystem");
            return null;
        }

        ddsContract = new DdsContract(blockchainService, appState.getDdsAddress());

        if (!ddsContract.isValidContract()) {
            log.warn("Недействительный контракт DDSystem");
            return null;
        }

        log.debug("Смарт-контракт DDSystem подключен");

        return ddsContract;
    }

    /**
     * Проверка адреса робота
     * @param appState
     * @return
     */
    public Boolean checkRobotAccount(AppState appState) {
        if (appState.getRobotAddress().length() != 42) {
            log.warn("Неверный адрес аккаунта робота");
            return false;
        }

        try {
            EthSign ethSign = blockchainService.getWeb3j().ethSign(appState.getRobotAddress(), "data").send();
            if (ethSign.hasError()) {
                log.debug("Аккаунт робота готов к работе");
                return true;
            } else {
                log.error("Аккаунт робота не готов к работе: {}", ethSign.getError());
            }
        } catch (IOException e) {
            log.error("Аккаунт робота {} отсутствует на ноде или не разлочен: {}", appState.getRobotAddress(),  e.getMessage());
        }
        return false;
    }

    private Boolean isCorrectRegistry() {

        if (!registryContractService.isValidContract(appState.getRegistryAddress())) {
            log.error("СК Registry неактуален коду ДДС");
            return false;
        }

        // Проверка что организация существует
        if (!registryContractService.orgIsKnown(appState.getRegistryAddress(), appState.getDepositoryOgrn())) {
            log.error("Организации с ОГРН {} не существует в реестре {}", appState.getDepositoryOgrn(), appState.getRegistryAddress());
            return false;
        }

        List<String> orgUsers = registryContractService.getOrgUsers();

        if (orgUsers.size() == 0) {
            log.error("Для организации {} не зарегистрирован ни один пользователь", appState.getDepositoryOgrn());
            return false;
        }


        return true;
    }

    @Override
    public void update(Observable observable, Object o) {
        ddsContract = loadDdsContract(appState);
        if (ddsContract == null) {
            return;
        }

        appState.setRegistryAddress(ddsContract.getRegistryAddress());
        appState.setRoleModelAddress(ddsContract.getRoleModelAddress());

        if(!isCorrectRegistry()) {
            return;
        }


        if (!managerService.isCorrectRobotAddress(appState.getRobotAddress())) {
            log.warn("Указанный адресс робота {} не принадлежит организации {}. Будет установлен первый робот из организации.", appState.getRobotAddress(), appState.getDepositoryOgrn());
            String firstRobot = managerService.getFirstRobot(appState.getDepositoryOgrn());
            if (firstRobot == null) {
                log.error("Для организации {} нет ни одного пользователя с ролью робота", appState.getDepositoryOgrn());
                return;
            }
            appState.setRobotAddress(firstRobot);
        }

        log.debug("Адрес робота: {}", appState.getRobotAddress());

        this.setChanged();
        this.notifyObservers(o);
    }

}
