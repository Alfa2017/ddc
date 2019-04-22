pragma solidity ^0.4.21;

/// @title Абстрактный контракт с возможностью блокировки
contract Suspendable {

    /// @notice Флаг блокировки контракта
    bool public disabled;

    /// @dev Модификатор блокировки контракта
    modifier enabled() {
        require(disabled == false);
        _;
    }

    /// @dev Проверить право блокировки контракта
    function canDisable() public view returns(bool);

    /// @notice Запретить изменения в контракте
    function disable() public enabled {
        require(canDisable());
        disabled = true;
    }

    /// @notice Разрешить изменения в контракте
    function enable() public {
        require(disabled == true && canDisable());
        disabled = false;
    }
}
