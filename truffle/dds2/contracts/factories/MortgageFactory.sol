pragma solidity ^0.4.21;

import "../DDSystem.sol";
import "../Mortgage.sol";

/// @title фабрика смарт-контрактов закладных
contract MortgageFactory {
    function createNew(
        Upgradable _precursor,
        Container _container,
        Mortgages _mortgages,
        bytes _number,
        Account _custodyAsset,
        Account _custodyLiability,
        Account _accountingAsset,
        Account _accountingLiability,
        bytes _meta
    ) public returns (Mortgage) {
        return new Mortgage(_precursor, _container, _mortgages, _number, _custodyAsset, _custodyLiability, _accountingAsset, _accountingLiability, _meta);
    }
}
