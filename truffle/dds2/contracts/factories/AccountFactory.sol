pragma solidity ^0.4.21;

import "../Account.sol";
import "../controllers/Accounts.sol";
import "../storage/Upgradable.sol";
import "../storage/Container.sol";

/// @title фабрика смарт-контрактов счетов
contract AccountFactory {
    function createNew(
        Upgradable _precursor,
        Container _container,
        Accounts _accounts,
        bytes32 _orgId,
        bytes8 _accType,
        bytes32 _number,
        bytes32 _deponent,
        bytes _meta,
        bytes32 _documentLink
    ) public returns (Account) {
        return new Account(_precursor, _container, _accounts, _orgId, _accType, _number, _deponent, _meta, _documentLink);
    }
}
