pragma solidity ^0.4.21;

import "./Modifiers.sol";

contract Getters is Modifiers {

    /// @notice Рассчитать значение по ГОСТ Р 34.10-2012
    /// @param  _data данные для хэширования
    function gostHash256(bytes _data)
    pure public returns(bytes32) {
        return sha256(_data);
    }

    /// @notice Рассчитать хэш-значение для первого параметра ecrecover()
    /// @param  _data 32 байта для хэшироваиня
    function gostHashForEcrecover(bytes32 _data)
    pure public returns(bytes32) {
        return sha256('\x19Signed Message:\n32', _data);
    }

    /// @notice Нахождение ID сотрудника в реестре по его подписи
    /// @dev    Восстанавливает адрес из подписи и возвращает его,
    /// если находит в реестре сотрудников
    /// @param  _token подписанный токен
    /// @param  _r компонента подписи
    /// @param  _s компонента подписи
    /// @param  _v компонента подписи
    /// @return Найденный в реестре адрес автора подписи
    function getUserBySignature(bytes32 _token,
        bytes32 _r, bytes32 _s, uint8 _v)
    public view returns (address) {
        address signer = ecrecover(_token, _v, _r, _s);
        return userIsEnabled(signer) ? signer : 0;
    }

    /// @notice Получение списка ID узлов
    function getNodeIds()
    public view returns(bytes32[]) {
        return nodeIds;
    }

    /// @notice Получение адреса администратора узла по ID
    /// @param  _nodeId исходный ID организации
    function getNodeAdmin(bytes32 _nodeId)
    public view returns(address) {
        return nodes[_nodeId].admin;
    }

    /// @notice Получение адреса контейнера с профилем узла по ID
    /// @param  _nodeId исходный ID организации
    function getNodeProfile(bytes32 _nodeId)
    public view returns(address) {
        return nodes[_nodeId].profile;
    }

    /// @notice Получение списка ID организаций
    function getOrgIds()
    public view returns(bytes32[]) {
        return orgIds;
    }

    /// @notice Получение адреса администратора организации по ID организации
    function getOrgAdmin(bytes32 _orgId)
    public view returns(address) {
        return orgs[_orgId].admin;
    }

    /// @notice Получение адреса контейнера с профилем организации по ID
    function getOrgProfile(bytes32 _orgId)
    public view returns(address) {
        return orgs[_orgId].profile;
    }

    /// @notice Получение ID узла по ID организации
    function getOrgNodeIds(bytes32 _orgId)
    public view returns(bytes32[]) {
        return orgs[_orgId].nodeIds;
    }

    /// @notice Получение списка ID сотрудников по ID организации
    function getOrgUserIds(bytes32 _orgId)
    public view returns(address[]) {
        return orgs[_orgId].userIds;
    }

    /// @notice Получение ID организации по ID сотрудника
    function getUserOrgId(address _userId)
    public view returns(bytes32) {
        return users[_userId].orgId;
    }

    /// @notice Получение адреса контейнера с профилем по ID сотрудника
    function getUserProfile(address _userId)
    public view returns(address) {
        return users[_userId].profile;
    }

    /// @notice Получение ID организации и списка ID ролей по ID сотрудника
    function getUserOrgIdAndRoleIds(address _userId)
    public view returns(bytes32, bytes32[]) {
        return (users[_userId].orgId, users[_userId].roleIds);
    }

    /// @notice Получение ID узла по ID сотрудника
    function getUserNodeIds(address _userId)
    public view returns(bytes32[]) {
        return orgs[users[_userId].orgId].nodeIds;
    }

    /// @notice Получение сертификата подписи токенов по ID узла
    function getNodeSigCert(bytes32 _nodeId)
    public view returns(bytes) {
        return nodes[_nodeId].sigCert;
    }

    /// @notice Получение сертификата шифрования по ID узла
    function getNodeEncCert(bytes32 _nodeId)
    public view returns(bytes) {
        return nodes[_nodeId].encCert;
    }

    /// @notice Получение сертификата подписи токенов по ID сотрудника
    function getUserSigCert(address _userId)
    public view returns(bytes) {
        return users[_userId].sigCert;
    }

    /// @notice Получение сертификата шифрования по ID сотрудника
    function getUserEncCert(address _userId)
    public view returns(bytes) {
        return users[_userId].encCert;
    }
}
