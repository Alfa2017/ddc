pragma solidity ^0.4.21;

import "./storage/Registry.sol";
import "./controllers/Mortgages.sol";
import "./controllers/Accounts.sol";
import "./controllers/Documents.sol";

/// @title Децентрализованная Депозитарная Система
contract DDSystem is Mortgages {

    /// @notice Номер блока деплоя ДДС
    uint public startAtBlock;


    /// @notice Конструктор ДДС
    /// @param registry Адрес реестра
    /// @param roleModel Адрес ролевой модели
    /// @param mgFactory Адрес фабрики счетов
    /// @param mgFactory Адрес фабрики закладных
    function DDSystem(
        DDSystem _precursor,
        Registry registry,
        RoleModel roleModel,
        address accFactory,
        address mgFactory
    )
    AftAdmined(_precursor, registry, roleModel)
    Accounts(accFactory)
    Mortgages(mgFactory)
    public
    {
        startAtBlock = block.number;
    }

    /// @notice Название смарт-контракта
    function contractName() public pure returns (string) {
        return "SCAFTDDSDDSystem";
    }
}
