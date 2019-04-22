pragma solidity ^0.4.21;

import "./Suspendable.sol";

/// @title Абстрактный контракт с возможностью обновления
contract Upgradable is Suspendable {

    /// @notice Адрес предыдущей версии
    Upgradable public precursor;

    /// @notice Адрес следующей версии
    Upgradable public successor;

    /// @notice Конструктор с указанием предыдущей версии,
    /// вызывает её метод upgradeWith(this)
    /// @param  _precursor предыдущая версия контракта
    function Upgradable(Upgradable _precursor) public {
        if (_precursor != address(0))
            _precursor.upgradeWith(this);
        precursor = _precursor;
    }

    /// @notice Указать адрес новой версии контракта и заблокировать
    function upgradeWith(Upgradable _successor) public {
        disable();
        successor = _successor;
    }

    /// @notice Разрешить изменения, с проверкой на пустой successor
    function enable() public {
        require(successor == address(0));
        super.enable();
    }

    /// @notice Получить название смарт-контракта
    function contractName() public pure returns(string);

    /// @notice Получить версию смарт-контракта
    function contractVersion() public pure returns(string) {
        return "0.2.7";
    }

}
