pragma solidity ^0.4.21;

import "../factories/MortgageFactory.sol";
import "./Accounts.sol";

contract Mortgages is Accounts {

    /// Адрес контракта закладной => keccak(номер закладной)
    mapping(address => bytes32) mortgages;

    /// keccak(номер закладной) => адрес контракта закладной
    mapping(bytes32 => address) numbers;

    /// Адрес фабрики закладных
    address mgFactoryAddress;

    uint64 mgCounter = 0;

    event MortgageEvent(
        uint64 counter,
        bytes32 action,
        address mortgage,
        bytes32 documentLink, //ссылка на документ вида containerAddress_archiveId
        uint64 version // номер версии закладной, где зафиксированы изменения
    );

    function Mortgages(address _mgFactoryAddress) public {
        mgFactoryAddress = _mgFactoryAddress;
    }

    function getMortgageByNumber(bytes number) public view returns (address) {
        return numbers[keccak256(number)];
    }


    function addMortgage(
        Upgradable precursor,
        Container container,
        bytes32 documentLink,
        bytes number,
        Account _custodyAsset,
        Account _custodyLiability,
        Account _accountingAsset,
        Account _accountingLiability,
        bytes meta
    ) public
    returns (Mortgage) {
        if (!checkUserOrg(msg.sender, _custodyAsset.orgId()) && !checkUserOrg(msg.sender, _accountingAsset.orgId())) {//отправитель менеджер организации
            emit ErrorEvent("NOT_MANAGER"); return;
        }
        if (!checkPermissions(msg.sender, "AFTDDSAddMortgage")) {//отправитель может совершить действие
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW"); return;
        }

        bytes32 key = keccak256(number);

        if (numbers[key] != address(0)) {
            emit ErrorEvent("MORTGAGE_ALREADY_EXIST"); return;
        }

        Mortgage mortgage = MortgageFactory(mgFactoryAddress).createNew(precursor, container, this, number, _custodyAsset, _custodyLiability, _accountingAsset, _accountingLiability, meta);

        mortgages[mortgage] = key;
        numbers[key] = mortgage;

        emit MortgageEvent(mgCounter++, "addMortgage", mortgage, documentLink, 1);

        return mortgage;
    }

    /// @notice Бросить событие изменения закладной. Может быть вызван только зарегистрированным контрактом закладной
    function emitMortgageEvent(bytes32 action, bytes32 documentLink, uint64 version) external {
        if (mortgages[msg.sender] == 0) {
            emit ErrorEvent("NOT_REGISTERED_MORTGAGE");
        } else {
            emit MortgageEvent(mgCounter++, action, msg.sender, documentLink, version);
        }
    }
}
