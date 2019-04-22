pragma solidity ^0.4.21;

import "./Admined.sol";

/// @title  Ролевая модель СПКС
/// @notice Назначает права доступа по модификаторам,
/// ролям организаций и ролям их сотрудников
contract RoleModel is Admined {

    /// @notice Получить название смарт-контракта
    function contractName() public pure returns(string) {
        return "AFTMstorRoleModel";
    }

    /// @notice Битовые флаги прав доступа
    uint8 constant ABSENT = 0; /// отсутствует (по умолчанию)
    uint8 constant DENIED = 1; /// отказано
    uint8 constant READER = 2; /// чтение
    uint8 constant WRITER = 4; /// запись
    uint8 constant BOTHRW = 6; /// чтение и запись
    uint8 constant ACCESS = 7; /// любой из флагов

    /// @notice Права доступа по ролям сотрудников для роли организации
    struct UserRoles {
        // Список ID ролей сотрудников, пр: "AFTMstorAdmin"
        bytes32[] userRoleIds;
        // Права доступа по ролям сотрудников
        mapping(bytes32 => uint8) userAccess;
    }

    /// @notice Права доступа по ролям организаций для модификатора
    struct OrgRoles {
        // Список ID ролей организаций, пр: "AFTMstorAuthority"
        bytes32[] orgRoleIds;
        // Права досупа сотрудников по ролям организаций
        mapping(bytes32 => UserRoles) userRoles;
    }

    /// @notice Права доступа организаций по модификаторам
    mapping(bytes32 => OrgRoles) orgRoles;

    /// @notice Список ID модификаторов, пр: "AFTMstorCreateContainer"
    bytes32[] public modifierIds;

    /// @notice Конструктор ролевой модели
    /// @param  _precursor предыдущая версия контракта
    function RoleModel(RoleModel _precursor)
    public Admined(_precursor) {}

    /// @notice Проверить, что адрес может быть администратором ролевой модели
    function canBeAdmin(address _newAdmin) public view returns (bool) {
        return _newAdmin != 0;
    }

    /// @notice Назначить право доступа по модификатору, роли
    /// организации и роли сотрудника в ней
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    /// @param _access право доступа
    function setAccess(bytes32 _modifierId, bytes32 _orgRoleId,
                       bytes32 _userRoleId, uint8 _access)
    private enabled senderIsAdmin {
        require(_access & ACCESS != 0);
        if (orgRoles[_modifierId].orgRoleIds.length == 0) {
            modifierIds.push(_modifierId);
            orgRoles[_modifierId] = OrgRoles(new bytes32[](0));
        }
        if (orgRoles[_modifierId].userRoles[_orgRoleId]
                .userRoleIds.length == 0) {
            orgRoles[_modifierId].orgRoleIds.push(_orgRoleId);
            orgRoles[_modifierId].userRoles[_orgRoleId] =
                UserRoles(new bytes32[](0));
        }
        if (orgRoles[_modifierId].userRoles[_orgRoleId]
                .userAccess[_userRoleId] == ABSENT)
            orgRoles[_modifierId].userRoles[_orgRoleId]
                .userRoleIds.push(_userRoleId);
        orgRoles[_modifierId].userRoles[_orgRoleId]
            .userAccess[_userRoleId] = _access;
    }

    /// @notice Назначить право записи по модификатору, роли
    /// организации и роли сотрудника в ней
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    function assignWriter(bytes32 _modifierId, bytes32 _orgRoleId,
                          bytes32 _userRoleId) public {
        setAccess(_modifierId, _orgRoleId, _userRoleId, WRITER);
    }

    /// @notice Назначить право чтения по модификатору, роли
    /// организации и роли сотрудника в ней
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    function assignReader(bytes32 _modifierId, bytes32 _orgRoleId,
                          bytes32 _userRoleId) public {
        setAccess(_modifierId, _orgRoleId, _userRoleId, READER);
    }

    /// @notice Запретить доступ по модификатору, роли
    /// организации и роли сотрудника в ней
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    function assignDenied(bytes32 _modifierId, bytes32 _orgRoleId,
                          bytes32 _userRoleId) public {
        setAccess(_modifierId, _orgRoleId, _userRoleId, DENIED);
    }

    /// @notice Получить прав доступа к модификатору для роли организации и роли сотрудника
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    function getAccess(bytes32 _modifierId, bytes32 _orgRoleId,
                       bytes32 _userRoleId)
    private view returns (uint8) {
        return orgRoles[_modifierId].userRoles[_orgRoleId].userAccess[_userRoleId];
    }

    /// @notice Проверить право чтения для указанного модификатора,
    /// организации и роли сотрудника
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    function canRead(bytes32 _modifierId, bytes32 _orgRoleId,
                     bytes32 _userRoleId)
    public view returns (bool) {
        return getAccess(_modifierId, _orgRoleId, _userRoleId) & BOTHRW != 0;
    }

    /// @notice Проверить право записи для указанного модификатора,
    /// организации и роли сотрудника
    /// @param _modifierId ID модификатора
    /// @param _orgRoleId ID роли организации
    /// @param _userRoleId ID роли сотрудника
    function canWrite(bytes32 _modifierId, bytes32 _orgRoleId,
                      bytes32 _userRoleId)
    public view returns (bool) {
        return getAccess(_modifierId, _orgRoleId, _userRoleId) & WRITER != 0;
    }

    /// @notice Получить список ID модификаторов
    function getModifierIds()
    public view returns (bytes32[]) {
        return modifierIds;
    }

    /// @notice Получить ID организаций по модификатору
    function getOrgRoleIds(bytes32 _modifierId)
    public view returns (bytes32[]) {
        return orgRoles[_modifierId].orgRoleIds;
    }

    /// @notice Получить ID ролей сотрудников по модификатору и ID организации
    function getUserRoleIds(bytes32 _modifierId, bytes32 _orgRoleId)
    public view returns (bytes32[]) {
        return orgRoles[_modifierId].userRoles[_orgRoleId].userRoleIds;
    }
}
