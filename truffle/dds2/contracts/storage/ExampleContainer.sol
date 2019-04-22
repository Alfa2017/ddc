pragma solidity ^0.4.21;

import './Upgradable.sol';
import './AbstractContainer.sol';

/// @title Пример внешнего объекта, предоставляющего контенеру часть полей
contract ExampleLogic {

    Registry public registry;
    RoleModel public roleModel;

    function ExampleLogic(Registry _registry, RoleModel _roleModel) public {
        registry = _registry;
        roleModel = _roleModel;
    }
}

/// @title Пример контейнера со сложным конструктором
contract ExampleContainer is Upgradable, AbstractContainer {

    /// @notice Название смарт-контракта
    function contractName() public pure returns(string) {
        return "AFTMstorExampleContainer";
    }

    ExampleLogic public logic;

    function registry() public view returns (Registry) {
        return logic.registry();
    }

    function roleModel() public view returns (RoleModel) {
        return logic.roleModel();
    }

    function modifierId() public view returns (bytes32) {
        return "AFTMstorExample";
    }

    /// @notice Контейнер принимает в конструкторе две организации,
    /// одну из них неявно: организацию сотрудника, который создаёт контейнер,
    /// @param  _precursor предыдущая версия контракта
    /// @param  _creatorOrgRoleId роль организации-создателя
    /// @param  _anotherOrgId организация - второй участник
    /// @param  _anotherOrgRoleId роль второй организации
    /// @param  _archiveModifierId модификатор первого архива
    /// @param  _archiveHmac HMAC первого архива
    /// @param  _archiveExpiry срок годности первого архива
    function ExampleContainer(ExampleContainer _precursor, ExampleLogic _logic,
                              bytes32 _creatorOrgRoleId, bytes32 _anotherOrgId,
                              bytes32 _anotherOrgRoleId, bytes32 _archiveModifierId,
                              bytes32 _archiveHmac, uint64 _archiveExpiry)
    public Upgradable(_precursor) {
        logic = _logic;
        // Инициализируем запись о первой организации (создателе) её ролью
        initialize(_creatorOrgRoleId);
        // Добавляем вторую организацию
        grantOrgRoleFromConstructor(_anotherOrgId, _anotherOrgRoleId);
        // Добавляем первый архив
        appendArchiveFromConstructor(_creatorOrgRoleId, _archiveModifierId,
                                     _archiveHmac, _archiveExpiry);
    }
}
