pragma solidity ^0.4.21;

import "./Upgradable.sol";

/// @title Абстрактный контракт с администратором
contract Admined is Upgradable {

    /// @notice ID сотрудника-администратора контракта
    address public admin;

    /// @notice Конструктор с указанием предыдущей версии контракта
    /// @param  _precursor предыдущая версия контракта
    function Admined(Admined _precursor)
    public Upgradable(_precursor) {
        require(tx.origin == msg.sender);
        admin = tx.origin;
    }

    /// @notice Проверить право блокировки контракта
    function canDisable() public view returns(bool) {
        return tx.origin == admin;
    }

    /// @notice Модификатор прав администратора контракта
    modifier senderIsAdmin() {
        require(canDisable());
        _;
    }

    /// @notice Проверить, что сотрудник может быть администратором контракта
    function canBeAdmin(address _newAdmin)
    public view returns (bool);

    /// @notice Назначить нового администратора контракта
    function updateAdmin(address _newAdmin)
    public enabled senderIsAdmin {
        require(canBeAdmin(_newAdmin));
        admin = _newAdmin;
    }
}

