pragma solidity ^0.4.21;

import "../storage/Registry.sol";
import "../storage/Upgradable.sol";
import "../storage/RoleModel.sol";
import "../libs/Throwable.sol";

contract AftAdmined is Upgradable, Throwable {
    Registry public registry;
    RoleModel public roleModel;

    /// @notice СК администрируеммый АФТ
    /// @param _registry Адрес реестра
    /// @param _roleModel Адрес ролевой модели
    function AftAdmined(AftAdmined _precursor, Registry _registry, RoleModel _roleModel) public
    Upgradable(_precursor)
    {
        registry = _registry;
        roleModel = _roleModel;
    }

    /// @notice Проверка права выполнить операцию modifierId организацией с ролью DdsAdmin
    /// @param userId адрес пользователя
    /// @param modifierId ID модификатора/действия
    /// @return bool Разрешено true или запрещено false
    function checkAftPermissions(address userId, bytes32 modifierId) public view returns (bool)  {
        return registry.userCanWriteByRoleModel(userId, roleModel, modifierId, "Aft");
    }


    /// @notice Проверка права выполнить операцию modifierId организацией с ролью Depository
    /// @param userId адрес пользователя
    /// @param modifierId ID модификатора/действия
    /// @return bool Разрешено true или запрещено false
    function checkPermissions(address userId, bytes32 modifierId) public view returns (bool)  {
        return registry.userCanWriteByRoleModel(userId, roleModel, modifierId, "Depository");
    }

    function getOrgId(address userId) public view returns(bytes32) {
        return registry.getUserOrgId(userId);
    }

    /// @notice Проверка принадлежности пользователя к организации
    /// @param userId адрес пользователя
    /// @param orgId ID орагнизации
    /// @return bool Принадлежит true или не принадлежит false
    function checkUserOrg(address userId, bytes32 orgId) public view returns (bool) {
        return registry.getUserOrgId(userId) == orgId;
    }

    /// @dev действие action может выполнить только менеджер депозитария
    modifier tryDo(bytes32 action,  bytes32 orgId) {
        if (!checkUserOrg(msg.sender, orgId)) {//отправитель менеджер организации
            emit ErrorEvent("NOT_MANAGER");
        } else if (!checkPermissions(msg.sender, action)) {//отправитель может совершить действие
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW");
        } else {
            _;
        }
    }

    /// @notice Проверка права блокировки
    function canDisable() public view returns(bool) {
        return tx.origin == registry.admin();
    }
}
