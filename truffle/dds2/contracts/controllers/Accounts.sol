pragma solidity ^0.4.21;

import "./Organizations.sol";
import "../factories/AccountFactory.sol";
import "../Account.sol";

contract Accounts is Organizations {

    /// Адрес фабрики счетов
    AccountFactory accFactoryAddress;

    /// orgId => number => Account
    mapping(bytes32 => mapping(bytes32 => Account)) accounts;

    function Accounts(address _accFactoryAddress) public {
        accFactoryAddress = AccountFactory(_accFactoryAddress);
    }

    event AccountEvent(
        bytes32 action,
        bytes32 indexed orgId,
        address account
    );

    function addAccount(
        Upgradable precursor,
        Container container,
        bytes32 orgId,
        bytes8 accType,
        bytes32 number,
        bytes32 deponent,
        bytes meta,
        bytes32 documentLink
    )
    public
    tryDo("AFTDDSAddAccount", orgId)
    returns (Account) {

        //проверяем, что такого счета еще не создано
        if(accounts[orgId][number] != address(0)){
            emit ErrorEvent("ACCOUNT_ALREADY_EXIST");
            return;
        }

        //счет создается организацией самой себе(e.g.A10,A50) или депонент должен быть с списке депонентов или депонент должен быть в списке организаций(e.g.A24)
        if(orgId != deponent && !deponents[orgId][deponent].isExist && !organizations[deponent].isExist){
            emit ErrorEvent("DEPONENT_NOT_EXIST");
            return;
        }

        Account account = accFactoryAddress.createNew(
            precursor,
            container,
            this,
            orgId,
            accType,
            number,
            deponent,
            meta,
            documentLink
        );

        accounts[orgId][number] = account;

        emit AccountEvent("addAccount", orgId, account);

        return account;
    }


    function emitAccountEvent(bytes32 action) external {
        Account caller = Account(msg.sender);
        bytes32 orgId = caller.orgId();
        if(accounts[orgId][caller.number()] != caller){
            emit ErrorEvent("NOT_REGISTERED_ACCOUNT");
        } else {
            emit AccountEvent(action, orgId, msg.sender);
        }
    }

    function getAccount(bytes32 orgId, bytes32 number) external view returns(Account) {
        return accounts[orgId][number];
    }

}
