pragma solidity ^0.4.21;

import "./storage/Upgradable.sol";
import "./libs/HasContainer.sol";
import "./controllers/Accounts.sol";

/// @title СК Счета
contract Account is Upgradable, HasContainer, Throwable {


    /**** Properties ************/


    bytes32 public orgId; // ОГРН владельца счета
    bytes8 public accType; // тип аккаунта (e.g. L34, A50)
    bytes32 public number; // номер счета
    bytes32 public deponent; // ОГРН депонента
    bytes32 public documentLink; // ссылка на документ основание
    uint8 public status; // статус счета CLOSED=0 | OPEN=1 | BLOCKED=2 | DEDUCT_BLOCKED=3
    bytes public meta;
    mapping(bytes16 => Section) sections; // номер раздела => Секция


    /*** Contracts **************/


    Accounts accounts; // Контракт для обращения к ролевой модели


    /*** Structures **************/


    struct Section {
        bytes16 status; // CLOSED | OPEN
        bytes16 number;
        bytes32 documentLink;
        bytes meta;
        //        uint createdAt;
        //        uint updatedAt;
        //        bytes4 secType;
    }


    /*** Events **************/


    event SectionEvent(
        bytes16 status,
        bytes16 number,
        bytes32 documentLink,
        bytes meta
    );


    /*** Modifiers *************/


    //todo сменить на msg.sender
    /// @dev действие action может выполнить только менеджер депозитария - владельца счета
    modifier tryDo(bytes32 action) {
        if (!accounts.checkUserOrg(tx.origin, orgId)) {//отправитель менеджер организации
            emit ErrorEvent("NOT_MANAGER");
        } else if (!accounts.checkPermissions(tx.origin, action)) {//отправитель может совершить действие
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW");
        } else {
            _;
        }
    }


    /*** Constructor **********/


    function Account(
        Upgradable _precursor,
        Container _container,
        Accounts _accounts,
        bytes32 _orgId,
        bytes8 _accType,
        bytes32 _number,
        bytes32 _deponent,
        bytes _meta,
        bytes32 _documentLink
    ) public
    Upgradable(_precursor)
    HasContainer(_container)
    {
        accounts = _accounts;
        orgId = _orgId;
        accType = _accType;
        number = _number;
        deponent = _deponent;
        meta = _meta;
        documentLink = _documentLink;
        status = 1;

        if (bytes4("L34") == accType || bytes4("L10") == accType) {
            editSection("AFTDDSAddSection", bytes16("100000"), 0, "");
            if (bytes4("L34") == accType) {
                editSection("AFTDDSAddSection", bytes16("120000"), 0, "");
            }
            editSection("AFTDDSAddSection", bytes16("140000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("210000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("230000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("240000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("250000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("260000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("270000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("280000"), 0, "");
            editSection("AFTDDSAddSection", bytes16("290000"), 0, "");
        } else if (bytes4("A50") == accType || bytes4("A10") == accType || bytes4("A24") == accType) {
            editSection("AFTDDSAddSection", bytes16("100000"), 0, "");
        }
    }


    /*** Methods **********************************/


    /// @notice Обновление метаданных счета
    function updateMeta(bytes _meta) external
    tryDo("AFTDDSUpdateAccount")
    {
        meta = _meta;
        accounts.emitAccountEvent("updateAccount");
    }


    /// @notice Общий метод для изменения состояния раздела счета
    /// @param _action Действие над секцией
    /// @param _number Номер секции
    /// @param _documentLink Идентификатор документа-основания в хранилище. Может быть 0
    /// @param _meta Метаинформация раздела счета. Может быть пустой
    function editSection(bytes32 _action, bytes16 _number, bytes32 _documentLink, bytes _meta) public
    tryDo(_action)
    {
        require(_action != 0 && _number != 0);
        if (_action == "AFTDDSAddSection") {
            require(sections[_number].status == bytes16(""));
            sections[_number] = Section(bytes16("OPEN"), _number, _documentLink, _meta);
        } else if (_action == "AFTDDSEditSection") {
            require(sections[_number].status == bytes16("OPEN"));
            sections[_number].meta = _meta;
        } else if (_action == "AFTDDSDisableSection") {
            require(sections[_number].status != bytes16("CLOSED"));
            sections[_number].status = bytes16("CLOSED");
        } else {
            emit ErrorEvent("UNKNOWN_ACTION");
            return;
        }
        emit SectionEvent(
            sections[_number].status,
            _number,
            sections[_number].documentLink,
            sections[_number].meta
        );
    }


    /// @notice Проверка права блокировки
    function canDisable() public view returns (bool) {
        return accounts.checkUserOrg(msg.sender, orgId) &&
        accounts.checkPermissions(msg.sender, "AFTDDSDisableContract");
    }


    /// @notice Название смарт-контракта
    function contractName() public pure returns (string) {
        return "SCAFTDDSAccount";
    }

    /// @notice Возвращает состояние этого смартконтракта счета
    function getAccountState() public view
    returns (
        bytes32 _orgId,
        bytes8 _accType,
        bytes32 _number,
        bytes32 _deponent,
        bytes32 _documentLink,
        uint8 _status,
        bytes _meta
    ) {
        return (orgId, accType, number, deponent, documentLink, status, meta);
    }


}
