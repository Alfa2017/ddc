pragma solidity ^0.4.21;

import "./Getters.sol";
import "../AbstractContainer.sol";
import "../RoleModel.sol";

contract Users is Getters {

    /// @dev Проверка прав администратора какого-либо узла
    function userIsAnyNodeAdmin(address _userId) public view returns(bool) {
        if (_userId == admin) return true;
        bytes32 orgId = users[_userId].orgId;
        for (uint256 i = 0; i < orgs[orgId].nodeIds.length; i++)
            if (nodes[orgs[orgId].nodeIds[i]].admin == _userId)
                return true;
        return false;
    }

    /// @notice Добавление пользователя к организации от имени администратора организации
    /// @param  _orgId ID организации назначения
    /// @param  _userId ID пользователя
    /// @param  _profile адрес контейнера с профилем сотрудника
    /// @param  _sigCert сертификат подписи токенов сотрудником
    /// @param  _encCert сертификат шифрования данных для сотрудника
    function createUser(bytes32 _orgId, address _userId, address _profile,
                        bytes _sigCert, bytes _encCert)
    public senderIsOrgAdmin(_orgId) {
        require(_userId != 0 && users[_userId].orgId == "" && orgIsEnabled(_orgId));
        users[_userId] = User(false, _orgId, _profile,
                              _sigCert, _encCert, new bytes32[](0));
        orgs[_orgId].userIds.push(_userId);
    }

    /// @notice Блокировка сотрудника от имени администратора его организации
    /// @param  _userId ID сотрудника для блокировки
    function disableUser(address _userId)
    public enabled senderIsUserAdmin(_userId) {
        require(userIsEnabled(_userId));
        users[_userId].disabled = true;
    }

    /// @notice Разблокировка сотрудника от имени администратора его организации
    /// @param  _userId ID сотрудника для разблокировки
    function enableUser(address _userId)
    public enabled senderIsAdmin {
        require(userIsKnown(_userId) && users[_userId].disabled == true);
        users[_userId].disabled = false;
    }

    /// @notice Изменение адреса контейнера с профилем сотрудника,
    /// выполняется от имени администратора сотрудника
    /// @param  _userId ID сотрудника назначения
    /// @param  _newUserProfile адрес контейнера
    function updateUserProfile(address _userId, address _newUserProfile)
    public enabled senderIsUserAdmin(_userId) {
        require(_newUserProfile != 0);
        users[_userId].profile = _newUserProfile;
    }

    /// @notice Изменение сертификата подписи токенов сотрудником
    /// @param  _userId ID сотрудника назначения
    /// @param  _newUserSigCert новый сертификат
    function updateUserSigCert(address _userId, bytes _newUserSigCert)
    public enabled senderIsUserAdmin(_userId) {
        require(_newUserSigCert.length != 0);
        users[_userId].sigCert = _newUserSigCert;
    }

    /// @notice Изменение сертификата шифрования данных для сотрудника
    /// @param  _userId ID сотрудника назначения
    /// @param  _newUserEncCert новый сертификат
    function updateUserEncCert(address _userId, bytes _newUserEncCert)
    public enabled senderIsUserAdmin(_userId) {
        require(_newUserEncCert.length != 0);
        users[_userId].encCert = _newUserEncCert;
    }

    /// @notice Назначение сотруднику роли от имени администратора
    /// его организации (или администратора реестра)
    /// @param _userId ID сотрудника
    /// @param _roleId ID роли
    /// @param _baseArchiveId ID архива с основанием для назначения роли
    function grantUserRole(address _userId, bytes32 _roleId, uint256 _baseArchiveId)
    public enabled {
        require (userIsAnyNodeAdmin(msg.sender) &&
                 users[_userId].roleNumbers[_roleId] == 0 &&
                 AbstractContainer(users[_userId].profile)
                    .getArchiveAuthorId(_baseArchiveId) != 0);
        users[_userId].roleIds.push(_roleId);
        users[_userId].roleNumbers[_roleId] = users[_userId].roleIds.length;
        users[_userId].baseArchives[_roleId] = _baseArchiveId;
    }

    /// @notice Снятие роли с сотрудника от имени администратора
    /// его организации (или администратора реестра)
    /// @param _userId ID сотрудника
    /// @param _roleId ID роли
    function revokeUserRole(address _userId, bytes32 _roleId)
    public enabled senderIsUserAdmin(_userId) {
        require (users[_userId].roleNumbers[_roleId] != 0);
        uint256 roleIndex = users[_userId].roleNumbers[_roleId] - 1;
        uint256 lastRoleIndex = users[_userId].roleIds.length - 1;
        bytes32 lastRoleId = users[_userId].roleIds[lastRoleIndex];
        users[_userId].roleIds[roleIndex] = lastRoleId;
        users[_userId].roleIds.length--;
        delete users[_userId].roleNumbers[_roleId];
    }

    /// @notice Проверка наличия у пользователя роли
    function userHasRole(address _userId, bytes32 _roleId)
    public view returns(bool) {
        return users[_userId].roleIds.length > 0 &&
            users[_userId].roleNumbers[_roleId] != 0;
    }

    /// @notice Проверка права доступа сотрудника организации с указанной
    /// ролью к указанному модификатору с перебором ролей сотрудника
    /// @param  _filter фильтр доступа по ролевой модели
    /// @param  _userId ID сотрудника
    /// @param  _modifierId ID модификатора
    /// @param  _orgRoleId роль организации сотрудника
    function canAccessByOrgRole(function (bytes32, bytes32, bytes32)
                                external view returns (bool) _filter,
                                address _userId, bytes32 _modifierId,
                                bytes32 _orgRoleId)
    private view returns(bool) {
        if (_filter(_modifierId, _orgRoleId, "AFTMstorAnyUserRole"))
            return true;
        for (uint256 i = users[_userId].roleIds.length; i > 0; i--)
            if (_filter(_modifierId, _orgRoleId, users[_userId].roleIds[i-1]))
                return true;
        return false;
    }

    /// @notice Проверка права доступа сотрудника к указанному модификатору
    /// в указанном контейнере с перебором ролей организации сотрудника
    /// @param  _filter фильтр доступа по ролевой модели
    /// @param  _userId ID сотрудника
    /// @param  _container контейнер
    /// @param  _modifierId ID модификатора
    function canAccess(function (bytes32, bytes32, bytes32)
                       external view returns (bool) _filter,
                       address _userId, AbstractContainer _container,
                       bytes32 _modifierId)
    private view returns(bool) {
        if (canAccessByOrgRole(_filter, _userId, _modifierId,
                               "AFTMstorAnyOrgRole"))
            return true;
        bytes32 orgId = getUserOrgId(_userId);
        for (uint256 i = _container.getOrgRoleIdsCount(orgId); i > 0; i--)
            if (canAccessByOrgRole(_filter, _userId, _modifierId,
                                   _container.getOrgRoleId(orgId, i-1)))
                return true;
        return false;
    }

    /// @notice Проверка права сотрудника на запись по ролевой модели,
    /// роли организации и модификатору
    /// @param  _userId ID сотрудника
    /// @param  _roleModel ролевая модель
    /// @param  _modifierId ID модификатора
    /// @param  _orgRoleId ID роли организации
    function userCanWriteByRoleModel(address _userId, RoleModel _roleModel,
                                     bytes32 _modifierId, bytes32 _orgRoleId)
    public view returns(bool) {
        return userIsAnyNodeAdmin(_userId) ||
            canAccessByOrgRole(_roleModel.canWrite,
                               _userId, _modifierId, _orgRoleId);
    }

    /// @notice Проверка права сотрудника на запись в контейнер архива
    /// с указанным модификатором
    /// @param  _userId ID сотрудника
    /// @param  _container контейнер
    /// @param  _modifierId ID модификатора
    function userCanWrite(address _userId, AbstractContainer _container,
                          bytes32 _modifierId)
    public view returns(bool) {
        return userIsAnyNodeAdmin(_userId) ||
            canAccess(_container.roleModel().canWrite,
                      _userId, _container, _modifierId);
    }

    /// @notice Проверка права сотрудника на чтение из контейнера архива
    /// с указанным модификатором
    /// @param  _userId ID сотрудника
    /// @param  _container контейнер
    /// @param  _modifierId ID модификатора
    function userCanRead(address _userId, AbstractContainer _container,
                         bytes32 _modifierId)
    public view returns(bool) {
        return canAccess(_container.roleModel().canRead,
                         _userId, _container, _modifierId);
    }
}
