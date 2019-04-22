package ddc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ddc.AppState;
import ddc.model.request.DeployDdsRequest;
import ddc.model.request.DeployRoleModelRequest;
import ddc.model.response.SimpleResponse;
import ddc.service.DdsConfigService;
import ddc.service.blockchain.BlockchainDdsException;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.deploy.Data;
import ddc.service.blockchain.deploy.Deployer;
import ddc.service.blockchain.deploy.Org;
import ddc.service.blockchain.deploy.OrgAccounts;
import ddc.service.spks.SpksContractService;
import ddc.util.Utils;

import java.util.*;

/**
 * Контроллер для админа АФТ
 */
@RestController
@Slf4j
@RequestMapping("/deploy/")
public class DeployerController {

    private BlockchainService blockchainService;
    private AppState appState;
    private final SpksContractService spksContractService;
    private final DdsConfigService ddsConfigService;

    private String registry = null;
    private String roleModel = null;
    private String dds = null;


    /**
     * Минимальное количество аккаунтов на ноде, необходимое для деплоя тестового окружения
     */
    private int threshold = 11;

    @Autowired
    public DeployerController(BlockchainService blockchainService, AppState appState,
                              SpksContractService spksContractService,
                              DdsConfigService ddsConfigService) {
        this.blockchainService = blockchainService;
        this.appState = appState;
        this.spksContractService = spksContractService;
        this.ddsConfigService = ddsConfigService;
    }

    private Deployer createDeployer() {
        List<String> accounts = blockchainService.getAccounts();
        if (accounts.size() < threshold) {
            throw new BlockchainDdsException("Для запуска теста количество аккаунтов на ноде должно быть не менее " + threshold);
        }

        TreeMap<Org, OrgAccounts> ethAccounts = new TreeMap<>();
        ethAccounts.put(Org.AFT, new OrgAccounts(accounts.get(0), Web3jUtils.randomAddress(), null));
        ethAccounts.put(Org.SBERSD, new OrgAccounts(accounts.get(1), accounts.get(2), accounts.get(3)));
        ethAccounts.put(Org.RAIF, new OrgAccounts(accounts.get(4), accounts.get(5), accounts.get(6)));
        ethAccounts.put(Org.REGION, new OrgAccounts(accounts.get(7), accounts.get(8), accounts.get(9)));

        Set<Org> depositories = new TreeSet<>();
        depositories.add(Org.SBERSD);
        depositories.add(Org.RAIF);
        depositories.add(Org.REGION);

        return new Deployer(blockchainService, Data.getOrganizations(ethAccounts), depositories);
    }

    @PostMapping("/registry")
    public synchronized SimpleResponse<String> deployRegistry() {
        this.checkDdsNotSet();
        Deployer deployer = createDeployer();
        registry = deployer.deployRegistry();
        appState.setRegistryAddress(registry);
        return new SimpleResponse<>(registry);
    }

    @PostMapping("/role-model")
    public synchronized SimpleResponse<String> deployRoleModel(@RequestBody(required = false) DeployRoleModelRequest request) {
        this.checkDdsNotSet();
        Deployer deployer = createDeployer();
        if (request == null || Utils.isBlank(request.getDdsAdmin())) {
            roleModel = deployer.deployRoleModel();
        } else {
            roleModel = deployer.deployRoleModel(request.getDdsAdmin());
        }
        appState.setRoleModelAddress(roleModel);
        return new SimpleResponse<>(roleModel);
    }


    @PostMapping("/dds")
    public synchronized SimpleResponse<String> deployDds(@RequestBody(required = false) DeployDdsRequest request) {
        this.checkDdsNotSet();
        Deployer deployer = createDeployer();

        deployer.setRegistryAddress((request != null && !Utils.isBlank(request.getRegistry())) ? request.getRegistry() : registry);
        deployer.setRoleModelAddress((request != null && !Utils.isBlank(request.getRoleModel())) ? request.getRoleModel() : roleModel);

        String ddsAddress = deployer.deployDds();

        appState.setDdsAddress(ddsAddress);

        return new SimpleResponse<>(ddsAddress);
    }

    @PostMapping("/test-orgs")
    public SimpleResponse<String> deployTestOrgs() {
        Deployer deployer = createDeployer();

        deployer.setDdsAddress(appState.getDdsAddress());
        deployer.setRegistryAddress(appState.getRegistryAddress());
        deployer.setRoleModelAddress(appState.getRoleModelAddress());

        deployer.addOrganizations();

        return new SimpleResponse<>(deployer.getDdsAddress());
    }

    @PostMapping("/test-accounts")
    public SimpleResponse<Boolean> deployTestAccounts() {
        Deployer deployer = createDeployer();

        deployer.setDdsAddress(appState.getDdsAddress());
        deployer.setRegistryAddress(appState.getRegistryAddress());
        deployer.setRoleModelAddress(appState.getRoleModelAddress());

        deployer.addAccounts();

        return new SimpleResponse<>(true);
    }

    @PostMapping("/test-mortgage")
    public SimpleResponse<Boolean> deployTestMortgage() {
        Deployer deployer = createDeployer();

        deployer.setDdsAddress(appState.getDdsAddress());
        deployer.setRegistryAddress(appState.getRegistryAddress());
        deployer.setRoleModelAddress(appState.getRoleModelAddress());

        deployer.addMortgages();

        return new SimpleResponse<>(true);
    }

    @PostMapping("/all-except-registry-and-role-model")
    public SimpleResponse deployAllExceptRegistryAndRoleModel(@RequestBody(required = false) DeployDdsRequest request) {
        this.checkDdsNotSet();
        String registry = Utils.notNull(request, DeployDdsRequest::getRegistry)
                .filter(r -> !StringUtils.isEmpty(r))
                .orElseGet(() -> appState.getRegistryAddress());

        String roleModel = Utils.notNull(request, DeployDdsRequest::getRoleModel)
                .filter(r -> !StringUtils.isEmpty(r))
                .orElseGet(() -> appState.getRoleModelAddress());

        this.registry = registry;
        this.roleModel = roleModel;

        return deployAll(new DeployDdsRequest().setRegistry(registry).setRoleModel(roleModel));
    }

    @PostMapping("/all")
    public synchronized SimpleResponse deployAll(@RequestBody(required = false) DeployDdsRequest request) {
        this.checkDdsNotSet();
        Deployer deployer = createDeployer();

        String registry = Utils.notNull(request, DeployDdsRequest::getRegistry)
                .orElseGet(() -> this.deployRegistry().getResult());

        String roleModel = Utils.notNull(request, DeployDdsRequest::getRoleModel)
                .orElseGet(() -> this.deployRoleModel(new DeployRoleModelRequest()).getResult());

        deployer.setRegistryAddress(registry);
        deployer.setRoleModelAddress(roleModel);

        log.info("Начало deploy dds");
        SimpleResponse<String> ddsDeploy = this.deployDds(new DeployDdsRequest());
        log.info("Окончание deploy dds");


        deployer.setDdsAddress(ddsDeploy.getResult());

        log.info("Начало add organizations");
        deployer.addOrganizations();
        log.info("Окончание add organizations");

        appState.notifyObservers();

        log.info("Начало add accounts");
        deployer.addAccounts();
        log.info("Окончание add accounts");

        log.info("Начало add mortgages");
        deployer.addMortgages().join();
        log.info("Окончание add mortgages");


        HashMap<String, String> result = new HashMap<>();

        result.put("DDSystem", appState.getDdsAddress());
        result.put("RoleModel", appState.getRoleModelAddress());
        result.put("Registry", appState.getRegistryAddress());

        log.debug(" = = = = > Deployed: {}", result);

        return new SimpleResponse<>(result);
    }

    /**
     * Проверяет, что адрес ДДС не был установлен, т.е. не был размещен в блокчейн ранее
     */
    private void checkDdsNotSet() {
        if(!Utils.isBlank(appState.getDdsAddress())) {
            throw new IllegalStateException("Адрес ДДС установлен. Оставьте его пустым в файле конфигурации и перезапустите приложение");
        }
    }

}
