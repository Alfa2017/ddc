pragma solidity ^0.4.21;

import "./Orgs.sol";

contract Nodes is Orgs {

    /// @notice Добавление организации и узла в реестр
    /// @param  _orgId ID организации
    /// @param  _orgProfile адрес контейнера с профилем организации
    /// @param  _nodeId ID узла
    /// @param  _nodeProfile адрес контейнера с профилем узла
    /// @param  _nodeSigCert сертификат подписи токенов узлом
    /// @param  _nodeEncCert сертификат шифрования данных для для узла
    /// @param  _admin ID администратора организации
    /// @param  _adminProfile адрес контейнера с профилем сотрудника
    /// @param  _adminSigCert сертификат подписи токенов администратором узла
    /// @param  _adminEncCert сертификат шифрования данных для администратора узла
    function createNode(bytes32 _orgId, address _orgProfile,
                        bytes32 _nodeId, address _nodeProfile,
                        bytes _nodeSigCert, bytes _nodeEncCert,
                        address _admin, address _adminProfile,
                        bytes _adminSigCert, bytes _adminEncCert)
    public enabled senderIsAdmin {
        require(_orgId != "" && orgs[_orgId].admin == 0 &&
                _nodeId != "" && nodes[_nodeId].admin == 0 &&
                _admin != 0 && users[_admin].orgId == "" &&
                _nodeSigCert.length != 0 && _nodeEncCert.length != 0 &&
                _adminSigCert.length != 0 && _adminEncCert.length != 0);
        nodes[_nodeId] = Node(false, _admin,
                              _nodeProfile, _nodeSigCert, _nodeEncCert);
        nodeIds.push(_nodeId);
        createOrg(_orgId, _nodeId, _orgProfile, _admin,
                  _adminProfile, _adminSigCert, _adminEncCert);
    }

    /// @notice Блокировка организации от имени администратора реестра
    /// @param  _nodeId ID организации для блокировки
    function disableNode(bytes32 _nodeId)
    public enabled senderIsAdmin {
        require(nodeIsEnabled(_nodeId));
        nodes[_nodeId].disabled = true;
    }

    /// @notice Разблокировка организации от имени администратора реестра
    /// @param  _nodeId ID организации для разблокировки
    function enableNode(bytes32 _nodeId)
    public enabled senderIsAdmin {
        require(nodeIsKnown(_nodeId) && nodes[_nodeId].disabled == true);
        nodes[_nodeId].disabled = false;
    }

    /// @notice Изменение адреса администратора узла,
    /// выполняется от имени администратора узла
    /// @param  _nodeId ID узла назначения
    /// @param  _newNodeAdmin ID сотрудника (адрес Мастерчейн) для назначения
    function updateNodeAdmin(bytes32 _nodeId, address _newNodeAdmin)
    public enabled senderIsNodeAdmin(_nodeId) {
        require(userIsEnabled(_newNodeAdmin));
        nodes[_nodeId].admin = _newNodeAdmin;
    }

    /// @notice Изменение адреса контейнера с профилем узла,
    /// выполняется от имени администратора узла
    /// @param  _nodeId ID узла назначения
    /// @param  _newNodeProfile адрес контейнера
    function updateNodeProfile(bytes32 _nodeId, address _newNodeProfile)
    public enabled senderIsNodeAdmin(_nodeId) {
        require(_newNodeProfile != 0);
        nodes[_nodeId].profile = _newNodeProfile;
    }

    /// @notice Изменение сертификата подписи токенов узлом
    /// @param  _nodeId ID узла назначения
    /// @param  _newNodeSigCert новый сертификат
    function updateNodeSigCert(bytes32 _nodeId, bytes _newNodeSigCert)
    public enabled senderIsNodeAdmin(_nodeId) {
        require(_newNodeSigCert.length != 0);
        nodes[_nodeId].sigCert = _newNodeSigCert;
    }

    /// @notice Изменение сертификата шифрования данных для узла
    /// @param  _nodeId ID узла назначения
    /// @param  _newNodeEncCert новый сертификат
    function updateNodeEncCert(bytes32 _nodeId, bytes _newNodeEncCert)
    public enabled senderIsNodeAdmin(_nodeId) {
        require(_newNodeEncCert.length != 0);
        nodes[_nodeId].encCert = _newNodeEncCert;
    }
}
