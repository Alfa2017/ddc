pragma solidity ^0.4.21;

import "./Upgradable.sol";
import "./AbstractContainer.sol";

/// @title  Обновляемый контейнер СПКС
contract Container is Upgradable, AbstractContainer {

    /// @notice Получить название смарт-контракта
    function contractName() public pure returns(string) {
        return "AFTMstorContainer";
    }

    /// @notice Реестр СПКС, к которому относится контейнер
    Registry public registry;

    /// @notice Реестр СПКС, к которому относится контейнер
    function registry() public view returns (Registry) { return registry; }

    /// @notice Ролевая модель доступа к архивам контейнера
    RoleModel public roleModel;

    /// @notice Ролевая модель доступа к архивам контейнера
    function roleModel() public view returns (RoleModel) { return roleModel; }

    /// @notice ID модификатора контейнера, определяет права его создания,
    /// назначения ролей организаций, выключения
    bytes32 public modifierId;

    /// @notice ID модификатора контейнера, определяет права его создания,
    /// назначения ролей организаций, выключения
    function modifierId() public view returns (bytes32) { return modifierId; }

    /// @notice Конструктор контейнера
    /// @param  _precursor предыдущая версия контракта
    /// @param  _registry реестр СПКС, к которому относится контейнер
    /// @param  _roleModel ролевая модель доступа к контейнеру
    /// @param  _modifierId ID модификатора контейнера
    /// @param  _orgRoleId ID роли организации, создающей контейнер
    function Container(Container _precursor,
                       Registry _registry, RoleModel _roleModel,
                       bytes32 _modifierId, bytes32 _orgRoleId)
    public Upgradable(_precursor) {
        registry = _registry;
        roleModel = _roleModel;
        modifierId = _modifierId;
        initialize(_orgRoleId);
    }
}
