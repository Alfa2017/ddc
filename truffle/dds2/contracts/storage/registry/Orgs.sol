pragma solidity ^0.4.21;

import "./Events.sol";

contract Orgs is Events {

    /// @notice Добавить организацию в реестр к известному узлу
    /// @param  _orgId ID организации 
    /// @param  _nodeId ID узла
    /// @param  _orgProfile адрес контейнера с профилем организации
    /// @param  _admin ID администратора организации 
    /// @param  _adminProfile адрес контейнера с профилем сотрудника
    /// @param  _adminSigCert сертификат для подписи токенов
    /// @param  _adminEncCert сертификат для шифрования данных
    function createOrg(bytes32 _orgId, bytes32 _nodeId, address _orgProfile,
                       address _admin, address _adminProfile,
                       bytes _adminSigCert, bytes _adminEncCert)
    public enabled senderIsNodeAdmin(_nodeId) {
        require(_orgId != "" && orgs[_orgId].admin == 0 &&
                nodeIsEnabled(_nodeId) && _admin != 0);
        orgs[_orgId] = Org(false, _admin, _orgProfile,
                           new bytes32[](0), new address[](0));
        orgs[_orgId].nodeIds.push(_nodeId);
        orgIds.push(_orgId);
        users[_admin] = User(false, _orgId, _adminProfile,
                             _adminSigCert, _adminEncCert, new bytes32[](0));
        orgs[_orgId].userIds.push(_admin);
    }

    /// @notice Заблокировать организацию от имени администратора реестра
    function disableOrg(bytes32 _orgId)
    public enabled senderIsAdmin {
        require(orgIsEnabled(_orgId));
        orgs[_orgId].disabled = true;
    }

    /// @notice Разблокировать организацию от имени администратора реестра
    function enableOrg(bytes32 _orgId)
    public enabled senderIsAdmin {
        require(orgIsKnown(_orgId) && orgs[_orgId].disabled == true);
        orgs[_orgId].disabled = false;
    }

    /// @notice Изменить адрес администратора организации,
    /// выполняется от имени администратора организации или реестра
    /// @param  _orgId ID организации назначения
    /// @param  _newOrgAdmin ID сотрудника (адрес Мастерчейн) для назначения
    function updateOrgAdmin(bytes32 _orgId, address _newOrgAdmin)
    public enabled senderIsOrgAdmin(_orgId) {
        require(userIsEnabled(_newOrgAdmin) && _orgId == users[_newOrgAdmin].orgId);
        orgs[_orgId].admin = _newOrgAdmin;
    }

    /// @notice Изменение адреса контейнера с профилем организации,
    /// выполняется от имени администратора организации
    /// @param  _orgId ID организации назначения
    /// @param  _newOrgProfile адрес контейнера
    function updateOrgProfile(bytes32 _orgId, address _newOrgProfile)
    public enabled senderIsOrgAdmin(_orgId) {
        require(_newOrgProfile != 0);
        orgs[_orgId].profile = _newOrgProfile;
    }

    /// @notice Добавить ID узла к организации, выполняется от имени
    /// администратора организации
    function appendOrgNodeId(bytes32 _orgId, bytes32 _orgNodeId)
    public enabled senderIsOrgAdmin(_orgId) {
        require(_orgNodeId != "");
        orgs[_orgId].nodeIds.push(_orgNodeId);
    }

    /// @notice Удалить ID узла из организации, выполняется от имени
    /// администратора организации или ресстра, если узлов организации более 1
    function removeOrgNodeId(bytes32 _orgId, bytes32 _orgNodeId)
    public enabled senderIsOrgAdmin(_orgId) {
        require(_orgNodeId != "" && orgs[_orgId].nodeIds.length > 1);
        uint256 i = orgs[_orgId].nodeIds.length - 1;
        if (orgs[_orgId].nodeIds[i] == _orgNodeId)
            orgs[_orgId].nodeIds.length--;
        else for (; i > 0; i--) // переместить последний на место удаляемого
            if (orgs[_orgId].nodeIds[i-1] == _orgNodeId) {
                uint256 last = orgs[_orgId].nodeIds.length - 1;
                orgs[_orgId].nodeIds[i-1] = orgs[_orgId].nodeIds[last];
                orgs[_orgId].nodeIds.length--;
                return;
            }
        require(false); // указанный ID не найден
    }
}
