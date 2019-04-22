pragma solidity ^0.4.21;

import "../Admined.sol";
import "./Data.sol";

contract Modifiers is Admined, Data {

    /// @notice Проверить ID узла на нахождение в реестре
    function nodeIsKnown(bytes32 _nodeId) public view returns (bool) {
        return _nodeId != "" && nodes[_nodeId].admin != 0;
    }

    /// @notice Проверить ID узла на отсутсвие блокировки
    function nodeIsEnabled(bytes32 _nodeId) public view returns (bool) {
        return nodeIsKnown(_nodeId) && nodes[_nodeId].disabled == false;
    }

    /// @notice Проверить ID организации на нахождение в реестре
    function orgIsKnown(bytes32 _orgId) public view returns (bool) {
        return _orgId != "" && orgs[_orgId].admin != 0;
    }

    /// @notice Проверить ID организации на отсутсвие блокировки
    function orgIsEnabled(bytes32 _orgId) public view returns (bool) {
        return orgIsKnown(_orgId) && orgs[_orgId].disabled == false;
    }

    /// @notice Проверить ID сотрудника на нахождение в реестре
    function userIsKnown(address _userId) public view returns (bool) {
        return _userId != 0 && orgIsKnown(users[_userId].orgId);
    }

    /// @notice Проверить ID сотрудника на отсутсвие блокировки
    function userIsEnabled(address _userId) public view returns (bool) {
        return userIsKnown(_userId) && users[_userId].disabled == false &&
            orgIsEnabled(users[_userId].orgId);
    }

    /// @notice Проверить, что сотрудник может быть администратором реестра
    function canBeAdmin(address _newAdmin) public view returns (bool) {
        return userIsEnabled(_newAdmin);
    }

    /// @dev Модификатор нахождения узла и наличия прав
    /// его администратора (или прав администратора реестра)
    modifier senderIsNodeAdmin(bytes32 _nodeId) {
        require(msg.sender == admin || (nodeIsKnown(_nodeId) &&
            nodeIsEnabled(_nodeId) && msg.sender == nodes[_nodeId].admin));
        _;
    }

    /// @dev Модификатор нахождения организации и наличия прав
    /// её администратора (или прав администратора реестра)
    modifier senderIsOrgAdmin(bytes32 _orgId) {
        require(msg.sender == admin || (orgIsKnown(_orgId) &&
            orgIsEnabled(_orgId) && msg.sender == orgs[_orgId].admin));
        _;
    }

    /// @dev Модификатор нахождения сотрудника и наличия прав
    /// администратора его организации (или прав администратора реестра)
    modifier senderIsUserAdmin(address _userId) {
        require(msg.sender == admin || (userIsKnown(_userId) &&
            orgIsEnabled(users[_userId].orgId) &&
                msg.sender == orgs[users[_userId].orgId].admin));
        _;
    }
}
