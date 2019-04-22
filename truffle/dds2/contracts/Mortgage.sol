pragma solidity ^0.4.21;

import "./Account.sol";
import "./storage/Upgradable.sol";
import "./libs/HasContainer.sol";
import "./controllers/Mortgages.sol";
import "./libs/Throwable.sol";

contract Mortgage is Upgradable, HasContainer, Throwable {


    /**** Properties ************/


    uint64 version; // версия закладной (инкрементируется при изменении состояния)
    bytes number; // рег. номер закладной
    mapping(uint64 => MortgageData) snapshots; // номер версии => состояние закладной на данную версию
    mapping(bytes32 => uint64) public agreements; // номер соглашения => дата соглашения
    bytes32 public redemptionNumber; // номер документа на аннулирование. При создании закладной пуст
    uint public extendedInfo;  // номер архива c расширенной информацией в контейнере закладной


    /*** Contracts **************/


    Mortgages mortgages;


    /*** Structures **************/


    struct Acct {
        Account asset; // активный счет (e.g. A50, A24)
        Account liability; // пассивный счет (e.g. L10, L34)
        bytes16 liabilitySection; // номер раздела пассивного счета, где находится закладная
    }


    struct MortgageData {
        Acct custody; // данные по счету ДХ
        Acct accounting; // данные по счету ДУ
        bytes32 status; // Статус закладной AVAILABLE | BLOCKED | BLOCKED_FOR_TRANSFER | ANNULLED (See Dictionaries.MgStatusRR)
        bytes meta; // метаданные закладной
        bytes32[] agreementKeys; // номера соглашений
    }


    /*** Modifiers *************/


    /// @dev Действие можно выполнить только над активной закладной
    modifier onlyActive() {
        if (snapshots[version].custody.liability != address(0) && snapshots[version].accounting.liability != address(0)) {
            _;
        } else {
            emit ErrorEvent("INACTIVE_MORTGAGE");
        }
    }


    /// @dev Действие action может выполнить только менеджер ДХ
    modifier onlyCustodyDepositoryManager(bytes32 action) {
        bytes32 custodyDepositoryOrgId = snapshots[version].custody.liability.orgId();
        if (!mortgages.checkUserOrg(msg.sender, custodyDepositoryOrgId)) {//отправитель менеджер ДХ
            emit ErrorEvent("NOT_CUSTODY_MANAGER");
        } else if (!mortgages.checkPermissions(msg.sender, action)) {//отправитель может совершить действие
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW");
        } else {
            _;
        }
    }


    /// @dev Действие action может выполнить как менеджер ДУ, так и менеджер ДХ
    modifier bothManagers(bytes32 action) {
        if (!mortgages.checkUserOrg(msg.sender, snapshots[version].custody.liability.orgId()) && //отправитель менеджер ДУ или ДХ
                !mortgages.checkUserOrg(msg.sender, snapshots[version].accounting.liability.orgId())) {
            emit ErrorEvent("NOT_CUSTODY_ACCOUNTING_MANAGER");
        } else if (!mortgages.checkPermissions(msg.sender, action)) {//отправитель может совершить действие
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW");
        } else {
            _;
        }
    }


    /*** Constructor **********/


    /// @dev Конструктор закладной
    /// @param _precursor предыдущая версии закладной
    /// @param _container контейнер закладной
    /// @param _mortgages точка взаимодействия закладной с ДДС
    /// @param _number рег. номер закладной
    /// @param _custodyAsset активный счет ДХ
    /// @param _custodyLiability пассивный счет ДХ
    /// @param _accountingAsset активный счет ДУ
    /// @param _accountingLiability пассивный счет ДУ
    function Mortgage(
        Upgradable _precursor,
        Container _container,
        Mortgages _mortgages,
        bytes _number,
        Account _custodyAsset,
        Account _custodyLiability,
        Account _accountingAsset,
        Account _accountingLiability,
        bytes _meta
    ) public
    Upgradable(_precursor)
    HasContainer(_container)
    {
        version = 1;
        number = _number;
        mortgages = _mortgages;

        MortgageData memory newData = MortgageData({
            custody : Acct(_custodyAsset, _custodyLiability, bytes16("230000")),
            accounting : Acct(_accountingAsset, _accountingLiability, bytes16("230000")),
            status : "BLOCKED",
            meta : _meta,
            agreementKeys : new bytes32[](0)
        });

        snapshots[version] = newData;
    }


    /*** Methods **********************************/


    /// @notice Сменить счет закладной
    function changeAccount(
        Account _custodyAssetTo,
        Account _custodyLiabilityTo,
        bytes16 _custodySectionTo,
        Account _accountingAssetTo,
        Account _accountingLiabilityTo,
        bytes16 _accountingSectionTo,
        bytes32 _documentLink
    ) public
    onlyActive
    bothManagers("AFTDDSChangeAccount")
    {
        // проверяем, что новые ДУ и ДХ либо равны нулю, либо зарегистрированы в ДДС
        if (_custodyLiabilityTo != address(0) && !mortgages.orgIsExist(_custodyLiabilityTo.orgId())) {
            emit ErrorEvent("CUSTODY_DEPOSITORY_NOT_EXIST");
            return;
        }
        if (_accountingLiabilityTo != address(0)  && !mortgages.orgIsExist(_accountingLiabilityTo.orgId())) {
            emit ErrorEvent("ACCOUNTING_DEPOSITORY_NOT_EXIST");
            return;
        }
        // если меняем ДУ, проверяем, что отправитель менеджер ДУ
        if (_accountingLiabilityTo != address(0) && _accountingLiabilityTo.orgId() != snapshots[version].accounting.liability.orgId()) {
            if (!mortgages.checkUserOrg(msg.sender, snapshots[version].accounting.liability.orgId())) {
                emit ErrorEvent("NOT_ACCOUNTING_MANAGER");
                return;
            }
        }

        mortgages.emitMortgageEvent("mgAcctDeduct", _documentLink, version);

        MortgageData memory data = snapshots[version];

        data.custody = Acct(_custodyAssetTo, _custodyLiabilityTo, _custodySectionTo);
        data.accounting = Acct(_accountingAssetTo, _accountingLiabilityTo, _accountingSectionTo);

        snapshots[++version] = data;
        mortgages.emitMortgageEvent("mgAcctEnroll", _documentLink, version);
    }


    /// @notice Сменить раздел счета закладной
    function changeAccountSection(bytes16 _custodySectionTo, bytes16 _accountingSectionTo, bytes32 _documentLink) external
    onlyActive
    bothManagers("AFTDDSChangeAccountSection")
    {
        require(_accountingSectionTo != 0 && _custodySectionTo != 0 && _documentLink != 0);

        mortgages.emitMortgageEvent("mgSectDeduct", _documentLink, version);

        MortgageData memory data = snapshots[version];

        data.custody = Acct(data.custody.asset, data.custody.liability, _custodySectionTo);
        data.accounting = Acct(data.accounting.asset, data.accounting.liability, _accountingSectionTo);

        snapshots[++version] = data;
        mortgages.emitMortgageEvent("mgSectEnroll", _documentLink, version);
    }


    /// @notice вернуть состояние закладной на версию _version
    function rollbackToVersion(uint64 _version, bytes32 _documentLink) external
    onlyActive
    bothManagers("AFTDDSRollbackToVersion")
    {
        require(_version > 0 && _version <= version);
        snapshots[++version] = snapshots[_version];
        mortgages.emitMortgageEvent("mgRollback", _documentLink, version);
    }


    /// @notice Сменить статус закладной
    function changeStatus(bytes32 _status, bytes32 documentLink) public
    onlyActive
    bothManagers("AFTDDSChangeMortgageStatus")
    {
        if (_status == "BLOCKED_FOR_TRANSFER" && snapshots[version].status != "AVAILABLE") {
            emit ErrorEvent("NOT_EXPECTED_STATE");
            return;
        }
        snapshots[version].status = _status;
        mortgages.emitMortgageEvent("mgChStatus", documentLink, version);
    }


    /// @notice Добавить соглашение
    function addAgreement(bytes32 agreementNumber, uint64 date, bytes32 _documentLink, bytes _meta) external
    onlyActive
    onlyCustodyDepositoryManager("AFTDDSAddAgreement")
    returns (uint)
    {
        if (agreements[agreementNumber] != 0) {
            emit ErrorEvent("AGREEMENT_NUMBER_ALREADY_EXIST");
            return;
        }


        agreements[agreementNumber] = date;

        mortgages.emitMortgageEvent("mgSectDeduct", _documentLink, version);

        MortgageData storage data = snapshots[version - 1];
        data.agreementKeys.push(agreementNumber);
        data.meta = _meta;

        snapshots[++version] = data;
        mortgages.emitMortgageEvent("mgSectEnroll", _documentLink, version);
        mortgages.emitMortgageEvent("mgAddAgreement", _documentLink, version);
    }

    /// @notice Функция списания закладной со всех счетов
    function redemption(bytes32 _redemptionNumber, bytes32 _documentLink) external
    onlyActive
    onlyCustodyDepositoryManager("AFTDDSRedemption")
    {
        redemptionNumber = _redemptionNumber;
        changeStatus("ANNULLED", _documentLink);
        changeAccount(Account(0), Account(0), bytes16(0), Account(0), Account(0), bytes16(0), _documentLink);
    }


    /// @notice Сохраняет расширенную карточку закладной в контейнере закладной
    function saveExtendedInfo(bytes32 _modifierId, bytes32 _hmac, uint64 _expiry) external
    bothManagers("AFTDDSSaveExtendedInfo")
    {
        extendedInfo = Container(container).appendArchive(_modifierId, _hmac, _expiry);
    }


    /// @notice Получить состояние закладной на версию
    function getSnapshot(uint64 _version) public view
    returns (
        uint64 currentVersion,
        bytes regNumber,
        address storageContainer,
        Account custodyAsset,
        Account custodyLiability,
        bytes16 custodyLiabilitySection,
        Account accountingAsset,
        Account accountingLiability,
        bytes16 accountingLiabilitySection,
        bytes32 status,
        bytes meta,
        bytes32[] agreementKeys
    ) {
        if (_version == 0) {
            _version = version;
        } else {
            require(_version > 0 && _version <= version);
        }
        MortgageData memory snapshot = snapshots[_version];
        return (
            version,
            number,
            container,
            snapshot.custody.asset,
            snapshot.custody.liability,
            snapshot.custody.liabilitySection,
            snapshot.accounting.asset,
            snapshot.accounting.liability,
            snapshot.accounting.liabilitySection,
            snapshot.status,
            snapshot.meta,
            snapshot.agreementKeys
        );
    }


    /// @notice Проверка права блокировки
    function canDisable() public view returns (bool) {
        //отключить может менеджер ДХ если имеет право на это по ролевой модели
        return mortgages.checkUserOrg(msg.sender, snapshots[version].custody.liability.orgId()) &&
        mortgages.checkPermissions(msg.sender, "AFTDDSDisableContact");
    }


    /// @notice Название смарт-контракта
    function contractName() public pure returns (string) {
        return "SCAFTDDSMortgage";
    }

}
