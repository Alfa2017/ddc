pragma solidity ^0.4.21;

import "./Admined.sol";
import "./registry/Nodes.sol";

/// @title  Реестр СПКС
/// @notice Содержит реестры: узлов СПКС, организаций и сотрудников.
/// ID сотрудников получаются из сертификатов ЭП как адреса в Мастерчейн:
/// последние 20 байт хэш-значения (по ГОСТ 2012) от открытого ключа.
/// В сертификате указан ID организации, к которой принадлежит сотрудник.
/// Каждому сотруднику назначен список ролей в его организации.
/// Множество организаций может использовать один узел СПКС.
/// Одна организация может использовать несколько узлов СПКС.
/// Для авторизации запроса сотрудника узел сообщает ему уникальную ЭП
/// в качестве токена авторизации, сотрудник прикрепляет в следующем запросе
/// свою ЭП этого токена, по которой возможно восстановить ID сотрудника.
contract Registry is Nodes {

    /// @notice Получить название смарт-контракта
    function contractName() public pure returns(string) {
        return "AFTMstorRegistry";
    }

    /// @notice Конструктор реестра
    /// @dev    Назначает создателя администратором реестра и его организации
    /// @param  _precursor предыдущая версия контракта
    /// @param  _orgId ID организации администратора, Пр: "OGRN:1177700002150"
    /// @param  _nodeId ID узла, Пр: "mstor.fintechru.org"
    /// @param  _nodeSigCert сертификат подписи токенов узлом
    /// @param  _nodeEncCert сертификат шифрования данных для для узла
    /// @param  _adminSigCert сертификат подписи токенов администратором реестра
    /// @param  _adminEncCert сертификат шифрования данных для администратора реестра
    function Registry(Registry _precursor, bytes32 _orgId, bytes32 _nodeId,
                      bytes _nodeSigCert, bytes _nodeEncCert,
                      bytes _adminSigCert, bytes _adminEncCert)
    public Admined(_precursor) {
        address _admin = msg.sender;
        require(_orgId != "" && _nodeId != "");
        nodes[_nodeId] = Node(false, _admin, 0, _nodeSigCert, _nodeEncCert);
        nodeIds.push(_nodeId);
        orgs[_orgId] = Org(false, _admin, 0, new bytes32[](0), new address[](0));
        orgs[_orgId].nodeIds.push(_nodeId);
        orgIds.push(_orgId);
        users[_admin] = User(false, _orgId, 0, _adminSigCert, _adminEncCert, new bytes32[](0));
        orgs[_orgId].userIds.push(_admin);
    }
}
