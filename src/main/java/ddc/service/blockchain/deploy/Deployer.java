package ddc.service.blockchain.deploy;

import ddc.sc2.*;
import ddc.service.blockchain.BlockchainService;
import ddc.service.blockchain.Web3jUtils;
import ddc.service.blockchain.contract.*;
import ddc.service.blockchain.contract.gasProvider.DdsGasProvider;
import ddc.service.blockchain.contract.meta.*;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import ddc.model.enums.Dictionaries;
import ddc.model.enums.UserRole;
import ru.iteco.aft.dds.sc2.*;
import ru.iteco.aft.dds.service.blockchain.contract.*;
import ru.iteco.aft.dds.service.blockchain.contract.meta.*;
import ddc.util.struct.DocumentLink;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ddc.service.blockchain.deploy.ContainerModifier.*;


/**
 * Пошагово деплоит контракты необходимые для работы ДДС с тестовыми данными
 */
@Slf4j
@Data
public class Deployer {

    @Setter
    protected String registryAddress;

    @Setter
    protected String roleModelAddress;

    @Setter
    protected String accountFactoryAddress;

    @Setter
    protected String mortgageFactoryAddress;

    @Setter
    public String ddsAddress;

    private Map<String, String> accountNumbers;

    private ContractGasProvider gasProvider = new DdsGasProvider();

    private static final BigInteger EXPIRY = BigInteger.valueOf(LocalDate.now(ZoneId.of("Europe/Moscow")).plusYears(10).atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond());


    private BlockchainService blockchainService;
    private final Map<Org, OrgInfo> orgs;
    private final List<OrgInfo> depositories;

    public Deployer(BlockchainService blockchainService, Map<Org, OrgInfo> data, Set<Org> depositories) {
        this.blockchainService = blockchainService;
        this.orgs = data;
        this.depositories = data
                .entrySet().stream()
                .filter(orgOrgInfoEntry -> depositories.contains(orgOrgInfoEntry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        this.accountNumbers = new ConcurrentHashMap<>();
    }

    /**
     * Деплой СК registry (список организация и пользоватлей)
     *
     * @return адрес СК
     */
    public String deployRegistry() {
        OrgInfo aft = orgs.get(Org.AFT);

        registryAddress = "";

        CompletableFuture<Registry> completableFuture = new RegistryContract(blockchainService, null).deploy(
                aft.getOgrn(),
                ("node." + aft.getCode() + ".domain"),
                aft.getEthAccounts().getRobot(),
                aft.getEthAccounts().getAdmin()
        );

        Registry registry = completableFuture.join();
        registryAddress = registry.getContractAddress();

        log.info("Registry address: {} {}", registryAddress, registry.getTransactionReceipt().get().getGasUsed());

        return registryAddress;
    }

    /**
     * Деплой СК RoleModel и заполнение в соответствии с правилами ДДС
     *
     * @return Адрес СК
     */
    public String deployRoleModel() {
        OrgInfo aft = orgs.get(Org.AFT);
        String ddsAdmin = aft.getEthAccounts().getAdmin();
        return deployRoleModel(ddsAdmin);
    }

    /**
     * Деплой СК RoleModel и заполнение в соответствии с правилами ДДС
     *
     * @return Адрес СК
     */
    public String deployRoleModel(String ddsAdmin) {
        roleModelAddress = "";
        try {
            RoleModelContract roleModelContract = new RoleModelContract(blockchainService, "");

            RoleModel roleModel = roleModelContract.deploy(ddsAdmin).join();

            //права робота
            List<Modifier> robotActionModifiers = Arrays.asList(
                    ActionModifier.AddAccount,
                    ActionModifier.AddSection,
                    ActionModifier.EditSection,
                    ActionModifier.DisableSection,
                    ActionModifier.UpdateAccount,
                    AddDocument,
                    ChangeDocStatus,
                    ActionModifier.AddMortgage,
                    ActionModifier.ChangeAccount,
                    ActionModifier.ChangeAccountSection,
                    ActionModifier.RollbackToVersion,
                    ActionModifier.ChangeMortgageStatus,
                    ActionModifier.AddAgreement,
                    ActionModifier.SaveExtendedInfo,
                    ActionModifier.Redemption,
                    CreateContainer
            );
            //права менеджера
            List<Modifier> managerActionModifiers = new ArrayList<>(robotActionModifiers);
            managerActionModifiers.add(ActionModifier.AddDeponent);
            managerActionModifiers.add(ActionModifier.EditDeponent);
            managerActionModifiers.add(ActionModifier.RemoveDeponent);

            //права администратора организации
            List<Modifier> orgAdminActionModifiers = Arrays.asList(ActionModifier.DisableContract);


            final List<CompletableFuture<TransactionReceipt>> addRightsRobot =
                    this.assignWriters(roleModelContract, UserRole.DepositoryRobot, robotActionModifiers, ddsAdmin);
            final List<CompletableFuture<TransactionReceipt>> addRightsManager =
                    this.assignWriters(roleModelContract, UserRole.DepositoryManager, managerActionModifiers, ddsAdmin);
            final List<CompletableFuture<TransactionReceipt>> addRightsAdmin =
                    this.assignWriters(roleModelContract, UserRole.DepositoryAdmin, orgAdminActionModifiers, ddsAdmin);

            addRightsRobot.forEach(CompletableFuture::join);
            addRightsManager.forEach(CompletableFuture::join);
            addRightsAdmin.forEach(CompletableFuture::join);
            roleModelAddress = roleModel.getContractAddress();
            log.info("RoleModel address: {} {}", roleModelAddress, roleModel.getTransactionReceipt().get().getGasUsed());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return roleModelAddress;
    }

    private List<CompletableFuture<TransactionReceipt>> assignWriters(RoleModelContract roleModelContract, UserRole userRole, Iterable<Modifier> modifiers, String sender) {
        List<CompletableFuture<TransactionReceipt>> promises = new ArrayList<>();
        for (Modifier modifier : modifiers) {
            promises.add(
                    roleModelContract.assignWriter(modifier, OrgRole.Depository, userRole, sender)
            );
        }
        return promises;
    }

    /**
     * Деплой СК ДДС
     *
     * @return Адресс СК
     */
    public String deployDds() {
        if (registryAddress == null || roleModelAddress == null || registryAddress.length() != 42 || roleModelAddress.length() != 42) {
            throw new IllegalStateException("Адреса контрактов хранилища не установлены");
        }

        ddsAddress = "";

        try {
            OrgInfo aft = orgs.get(Org.AFT);
            String aftAdmin = aft.getEthAccounts().getAdmin();

            DdsContract ddsContract = new DdsContract(blockchainService, "");

            AccountFactory accountFactory = ddsContract.execute(
                    AccountFactory.deploy(
                            blockchainService.getWeb3j(),
                            blockchainService.getTM(aftAdmin),
                            gasProvider
                    )::send,
                    "Деплой фабрики счетов"
            ).join();

            MortgageFactory mortgageFactory = ddsContract.execute(
                    MortgageFactory.deploy(
                            blockchainService.getWeb3j(),
                            blockchainService.getTM(aftAdmin),
                            gasProvider
                    )::send,
                    "Деплой фабрики закладных"
            ).join();

            log.debug("AccountFactory: {} {}", accountFactory.getContractAddress(), accountFactory.getTransactionReceipt().get().getGasUsed());
            log.debug("MortgageFactory: {} {}", mortgageFactory.getContractAddress(), mortgageFactory.getTransactionReceipt().get().getGasUsed());


            DDSystem ddSystem = ddsContract.deploy(
                    registryAddress,
                    roleModelAddress,
                    accountFactory.getContractAddress(),
                    mortgageFactory.getContractAddress(),
                    aftAdmin
            ).join();
            setDdsAddress(ddSystem.getContractAddress());
            log.info("DDS address: {} {}", ddsAddress, ddSystem.getTransactionReceipt().get().getGasUsed());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return ddsAddress;
    }


    /**
     * Добавляет все огранизации в Registry и подключает их к ДДС
     */
    public Integer addOrganizations() {
        DdsContract ddsContract = new DdsContract(blockchainService, ddsAddress);

        setAftDdsAdmin();

        List<CompletableFuture<Boolean>> orgAdding = new LinkedList<>();
        orgAdding.add(addOrganization(ddsContract, orgs.get(Org.SBERSD)));
        orgAdding.add(addOrganization(ddsContract, orgs.get(Org.RAIF)));
        orgAdding.add(addOrganization(ddsContract, orgs.get(Org.REGION)));

        orgAdding.forEach(CompletableFuture::join);

        log.debug("Добавление организаций окончено");

        return orgs.values().size();
    }

    private void setAftDdsAdmin() {
        OrgInfo aft = orgs.get(Org.AFT);
        String aftAdmin = aft.getEthAccounts().getAdmin();

        final CompletableFuture<Container> orgAdminProfile = createContainer(aftAdmin).thenApply(addMockArchive());

        RegistryContract registryContract = new RegistryContract(blockchainService, registryAddress);

        registryContract.updateUserProfile(aftAdmin, orgAdminProfile.join().getContractAddress(), aftAdmin).join();

        registryContract.grantUserRole(aftAdmin, UserRole.DdsAdmin, BigInteger.ZERO, aftAdmin);
    }

    private CompletableFuture<Boolean> addOrganization(DdsContract ddsContract, OrgInfo orgInfo) {
        OrgInfo aft = orgs.get(Org.AFT);
        String orgContainer = this.registerOrganization(orgInfo);

        return ddsContract.editOrganization(
                ActionModifier.AddOrganization,
                orgInfo.getOgrn(),
                orgContainer,
                new DepositoryMeta()
                        // TODO надо еще заполнять номер лицензии
                        .setName(orgInfo.getFullName())
                        .setShortName(orgInfo.getShortName())
                        .setInn(orgInfo.getInn())
                        .setKpp(orgInfo.getKpp())
                        .setDateStart(dateStart())
                        .setDateEnd(dateEnd()),
                aft.getEthAccounts().getAdmin()
        );
    }

    private String dateStart() {
        Random random = new Random();
        LocalDate now = LocalDate.now();
        now = now.minusMonths(random.nextInt(12)).minusDays(random.nextInt(30));
        return now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String dateEnd() {
        Random random = new Random();
        LocalDate now = LocalDate.now();
        now = now.plusYears(random.nextInt(4)).plusMonths(random.nextInt(12)).plusDays(random.nextInt(30));
        return now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }


    /**
     * Регистрирует организацию и ее пользователей в Ролевой Модели
     * Важно!!! Адрес signer должен быть уникальным для каждой организации
     *
     * @param orgInfo
     * @return Адрес контейнера организации
     */
    public String registerOrganization(OrgInfo orgInfo) {
        log.debug("Регистрация организации {} в Registry", orgInfo.getShortName());
        final String aftAdmin = orgs.get(Org.AFT).getEthAccounts().getAdmin();

        //создаем контейнеры
        final CompletableFuture<Container> orgProfile = createContainer(aftAdmin);
        final CompletableFuture<Container> nodeProfile = createContainer(aftAdmin);
        final CompletableFuture<Container> signerProfile = createContainer(aftAdmin);

        //контейнеры пользователей должны иметь минимум один архив при добавлении пользователя
        final CompletableFuture<Container> orgAdminProfile = createContainer(aftAdmin).thenApply(addMockArchive());
        final CompletableFuture<Container> orgRobotProfile = createContainer(aftAdmin).thenApply(addMockArchive());
        final CompletableFuture<Container> orgManagerProfile = createContainer(aftAdmin).thenApply(addMockArchive());

        RegistryContract registryContract = new RegistryContract(blockchainService, registryAddress);

        //создаем узел организации, саму организацию и ее админа
        registryContract.createNode(
                orgInfo.getOgrn(),
                orgProfile.join().getContractAddress(),
                ("node." + orgInfo.getCode() + ".domain"),
                nodeProfile.join().getContractAddress(),
                orgInfo.getEthAccounts().getAdmin(),
                orgAdminProfile.join().getContractAddress(),
                Web3jUtils.randomAddress(),
                signerProfile.join().getContractAddress(),
                aftAdmin
        ).join();

        //создаем робота и менеджера
        CompletableFuture.allOf(
                registryContract.createUser(orgInfo.getOgrn(), orgInfo.getEthAccounts().getRobot(), orgRobotProfile.join().getContractAddress(), aftAdmin),
                registryContract.createUser(orgInfo.getOgrn(), orgInfo.getEthAccounts().getManager(), orgManagerProfile.join().getContractAddress(), aftAdmin),
                orgProfile.thenApply(container -> this.grantOrgRole(container, orgInfo)),
                orgAdminProfile.thenApply(container -> this.grantOrgRole(container, orgInfo)),
                orgRobotProfile.thenApply(container -> this.grantOrgRole(container, orgInfo)),
                orgManagerProfile.thenApply(container -> this.grantOrgRole(container, orgInfo))
        ).join();

        //связываем адрес пользователя и его роль
        CompletableFuture.allOf(
                registryContract.grantUserRole(orgInfo.getEthAccounts().getAdmin(), UserRole.DepositoryAdmin, BigInteger.ZERO, aftAdmin),
                registryContract.grantUserRole(orgInfo.getEthAccounts().getRobot(), UserRole.DepositoryRobot, BigInteger.ZERO, aftAdmin),
                registryContract.grantUserRole(orgInfo.getEthAccounts().getManager(), UserRole.DepositoryManager, BigInteger.ZERO, aftAdmin)
        ).join();
        return orgProfile.join().getContractAddress();
    }


    /**
     * Деплой счетов организациям
     */
    public void addAccounts() {
        DdsContract ddsContract = new DdsContract(blockchainService, ddsAddress);

        //деплоим всех депонентов
        CompletableFuture.allOf(
                deployDeponent(ddsContract, orgs.get(Org.SBERSD), orgs.get(Org.RAIF)),//сделать Райф депонентом Сбера
                deployDeponent(ddsContract, orgs.get(Org.SBERSD), orgs.get(Org.REGION)),//сделать Регион депонентом Сбера
                deployDeponent(ddsContract, orgs.get(Org.SBERSD), orgs.get(Org.VTB)), // сделать ВТБ ДЕПОНЕНТОМ, НЕ ОБМАНЫВАЙТЕ В КОММЕНТАРИЯХ, НЕВОЗМОЖНО ТАК РАБОТАТЬ!
                deployDeponent(ddsContract, orgs.get(Org.SBERSD), orgs.get(Org.AKBARS)),//сделать Ак Барс депонентом Сбера
//                deployDeponent(ddsContract, orgs.get(Org.SBERSD), orgs.get(Org.SBERSD)),

                deployDeponent(ddsContract, orgs.get(Org.RAIF), orgs.get(Org.SBERSD)),//сделать Сбер депонентом Райффа
                deployDeponent(ddsContract, orgs.get(Org.RAIF), orgs.get(Org.VTB)),//сделать ВТБ депонентом Райффа
                deployDeponent(ddsContract, orgs.get(Org.RAIF), orgs.get(Org.AKBARS)),//сделать Ак Барс депонентом Райффа
//                deployDeponent(ddsContract, orgs.get(Org.RAIF), orgs.get(Org.VTBSD)),//сделать ВТБ-спецдеп депонентом Райффа

                deployDeponent(ddsContract, orgs.get(Org.REGION), orgs.get(Org.SBERSD)),//сделать Сбер депонентом Региона
                deployDeponent(ddsContract, orgs.get(Org.REGION), orgs.get(Org.VTB)),//сделать ВТБ депонентом Региона
                deployDeponent(ddsContract, orgs.get(Org.REGION), orgs.get(Org.AKBARS))//сделать Ак Барс депонентом Региона
        ).join();

        //дожидаемся деплоя всех депонентов
        log.info("Деплой депонентов завершен");

        //деплоим все счета
        final CompletableFuture<Void> deployAllAccountsPromise = CompletableFuture.allOf(
                deployAccount(ddsContract, orgs.get(Org.SBERSD), "A50", "00000001", orgs.get(Org.SBERSD).getOgrn()),
                deployAccount(ddsContract, orgs.get(Org.SBERSD), "L34", "1.L34.2.11110001", orgs.get(Org.RAIF).getOgrn()),
                deployAccount(ddsContract, orgs.get(Org.SBERSD), "L34", "1.L34.3.11110002", orgs.get(Org.REGION).getOgrn()),
                deployAccount(ddsContract, orgs.get(Org.SBERSD), "L10", "11110004", orgs.get(Org.REGION).getOgrn()),

//                deployAccount(ddsContract, orgs.get(Org.SBERSD), "L10", "11110003", orgs.get(Org.AKBARS).getOgrn()),
//                deployAccount(ddsContract, orgs.get(Org.SBERSD), "L34", custodyAcc3Number, orgs.get(Org.VTB).getOgrn()),

//                deployAccount(ddsContract, orgs.get(Org.RAIF), "A24", "2.A24.1.11110001", orgs.get(Org.SBERSD).getOgrn()),
                deployAccount(ddsContract, orgs.get(Org.RAIF), "L10", "22220001", orgs.get(Org.VTB).getOgrn()),
                deployAccount(ddsContract, orgs.get(Org.RAIF), "A50", "00000001", orgs.get(Org.RAIF).getOgrn()),

//                deployAccount(ddsContract, orgs.get(Org.RAIF), "L10", "22220001", orgs.get(Org.RAIF).getOgrn()),

//                deployAccount(ddsContract, orgs.get(Org.RAIF), "L10", "3214125", orgs.get(Org.VTB).getOgrn()),
//                deployAccount(ddsContract, orgs.get(Org.RAIF), "L10", "22220002", orgs.get(Org.AKBARS).getOgrn()),

                deployAccount(ddsContract, orgs.get(Org.REGION), "L10", "33330002", orgs.get(Org.AKBARS).getOgrn())

//                deployAccount(ddsContract, orgs.get(Org.REGION), "L10", "33330001", orgs.get(Org.VTB).getOgrn()),
        );

        //дожидаемся деплоя всех счетов
        deployAllAccountsPromise.join();
        log.info("Деплой счетов завершен");
    }


    /**
     * Деплой закладной
     */
    public CompletableFuture<TransactionReceipt> addMortgages() {
        DdsContract ddsContract = new DdsContract(blockchainService, ddsAddress);
        final String custodyAssetAddress = ddsContract.getAccount(
                orgs.get(Org.SBERSD).getOgrn(),
                "00000001",
                orgs.get(Org.SBERSD).getEthAccounts().getManager()
        );
        final String custodyLiabilityAddress = ddsContract.getAccount(
                orgs.get(Org.SBERSD).getOgrn(),
                "1.L34.2.11110001",
                orgs.get(Org.SBERSD).getEthAccounts().getManager()
        );
        final String accountingAssetAddress = ddsContract.getAccount(
                orgs.get(Org.RAIF).getOgrn(),
                "2.A24.1.11110001",
                orgs.get(Org.RAIF).getEthAccounts().getManager()
        );
        final String accountingLiabilityAddress = ddsContract.getAccount(
                orgs.get(Org.RAIF).getOgrn(),
                "22220001",
                orgs.get(Org.RAIF).getEthAccounts().getManager()
        );
        final CompletableFuture<Container> mortgageContainer =
                createContainer(orgs.get(Org.SBERSD).getEthAccounts().getManager());

        mortgageContainer.thenApply(addMockArchive());

        DocumentLink documentLink = new DocumentLink(null, 1L);
        return mortgageContainer.thenCompose(container -> {
            documentLink.setContainerAddress(container.getContractAddress());
            return ddsContract.newDocument(
                    orgs.get(Org.SBERSD).getOgrn(),
                    orgs.get(Org.RAIF).getOgrn(),
                    container.getContractAddress(),
                    AddDocument,
                    EXPIRY.longValue(),
                    null,
                    new DocumentMeta().setDocType(Dictionaries.DocType.NOTICE_RELEASE_MORTGAGE.name()),
                    orgs.get(Org.SBERSD).getEthAccounts().getManager());
        }).thenCompose(transactionReceipt ->
                ddsContract.addMortgage(
                        documentLink.getContainerAddress(),
                        "test-0001",
                        custodyAssetAddress,
                        custodyLiabilityAddress,
                        accountingAssetAddress,
                        accountingLiabilityAddress,
                        new MortgageMeta().setCadNums(Collections.singletonList("123-321-415-612")),
                        documentLink,
                        orgs.get(Org.SBERSD).getEthAccounts().getManager())
        ).thenApply(transactionReceipt -> {
            List<DDSystem.MortgageEventEventResponse> events = ddsContract.getMortgageEventEvents(transactionReceipt, Web3jUtils.ZERO_ADDRESS);
            String mgAddress = events.get(0).mortgage;
            MortgageContract mortgageContract = new MortgageContract(blockchainService, mgAddress);
            final String managerAddress = orgs.get(Org.SBERSD).getEthAccounts().getManager();
            mortgageContract.changeStatus(Dictionaries.MgStatusRR.ACCOUNTED.name(), documentLink, managerAddress).join();
            mortgageContract.changeAccountSection("100000", "100000", documentLink, managerAddress).join();
            return transactionReceipt;
        });

    }


    private CompletableFuture<TransactionReceipt> deployDeponent(DdsContract ddsContract, OrgInfo depository, OrgInfo deponent) {
        final CompletableFuture<Container> deponentContainer = createContainer(depository.getEthAccounts().getManager());
        DeponentMeta deponentMeta = new DeponentMeta()
                .setComment("Комментарий")
                .setEgrulDate(System.currentTimeMillis() / 1000)
                .setEmail(deponent.getCode().toLowerCase() + "@mail.com")
                .setFactAdress("Фактический адрес")
                .setPostAddress("Почтовый адрес")
                .setFias("Почтовый адрес")
                .setFullName(deponent.getFullName())
                .setInn(deponent.getInn())
                .setKpp(deponent.getKpp())
                .setPhoneNumber("Номер телефона")
                .setName(deponent.getShortName())
                .setOgrn(deponent.getOgrn())
                .setDateStart(System.currentTimeMillis() / 1000)
                .setRegAndOgrnOrganName("Наименования органа выдавшего ОГРН")
                .setSubjectFederal("77")
                .setFias("Код фиас адреса местанахождения");

        return deponentContainer.thenCompose(container ->
                ddsContract.editDeponent(
                        ActionModifier.AddDeponent,
                        depository.getOgrn(),
                        deponent.getOgrn(),
                        deponentContainer.join().getContractAddress(),
                        deponentMeta,
                        depository.getEthAccounts().getManager())
        );
    }


    private CompletableFuture<TransactionReceipt> deployAccount(DdsContract ddsContract, OrgInfo depository, String accType, String accNumber, String deponentOgrn) {
        final CompletableFuture<Container> accountContainer = createContainer(depository.getEthAccounts().getManager());
        accountContainer.thenApply(addMockArchive());
        accountNumbers.put(depository.getOgrn() + deponentOgrn, accNumber);
        return accountContainer.thenCompose(container ->
                ddsContract.addAccount(
                        container.getContractAddress(),
                        depository.getOgrn(),
                        accType,
                        accNumber,
                        deponentOgrn,
                        new AccountMeta().setMirrorAccount("0x0"),
                        new DocumentLink(container.getContractAddress(), 0L),
                        depository.getEthAccounts().getManager()
                )
        );
    }

    /**
     * Создание контейнера
     *
     * @param fromAddress
     * @return
     */
    private CompletableFuture<Container> createContainer(String fromAddress) {
        return new ContainerContract(blockchainService, "").deploy(
                registryAddress,
                roleModelAddress,
                CreateContainer,
                OrgRole.Depository,
                fromAddress
        );
    }

    private Function<Container, Container> addMockArchive() {
        return container -> {
            blockchainService.execute(
                    container.appendArchive(AddDocument.getBytes(), Web3jUtils.strToBytes32("hmac"), EXPIRY)::send,
                    "Добавление пустого архива в контейнер"
            );
            return container;
        };
    }

    private CompletableFuture<TransactionReceipt> grantOrgRole(Container container, OrgInfo orgInfo) {
        return blockchainService.execute(
                container.grantOrgRole(Web3jUtils.createOrgIdFromOgrn(orgInfo.getOgrn()), OrgRole.Depository.getBytes())::send, "Назначение роли организации в контейнере"
        );
    }

}
