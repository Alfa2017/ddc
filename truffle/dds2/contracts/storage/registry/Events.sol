pragma solidity ^0.4.21;

import "./Users.sol";
import "../AbstractContainer.sol";

contract Events is Users {

    /// @dev Модификатор вызова: ID автора транзакции активен в реестре,
    /// источник вызова метода - смарт-контракт (контейнера)
    modifier senderAndOriginAreValid() {
        require(msg.sender != tx.origin && userIsEnabled(tx.origin));
        _;
    }

    /// @notice Событие создания контейнера
    /// @param  origin автор транзакции по созданию контейнера
    /// @param  container адрес смарт-контракта контейнера
    /// @param  modifierId ID модификатора контейнера
    event NewContainer(
        address indexed origin,
        address container,
        bytes32 indexed modifierId);

    /// @notice Проверить возможность и зафиксировать событие создания
    /// контейнера, а также назначения в нём роли организации отправителя
    /// @param  _roleModel ролевая модель для контейнера
    /// @param  _orgRoleId роль организации создателя контейнера
    /// @param  _modifierId ID модификатора контейнера
    /// @return ID организации создателя контейнера
    function tryCreateContainer(RoleModel _roleModel, bytes32 _modifierId,
                                bytes32 _orgRoleId)
    public enabled senderAndOriginAreValid returns(bytes32) {
        require(userCanWriteByRoleModel(tx.origin, _roleModel, _modifierId, _orgRoleId));
        emit NewContainer(tx.origin, msg.sender, _modifierId);
        bytes32 orgId = users[tx.origin].orgId;
        emit OrgRoleGranted(tx.origin, msg.sender, orgId,
                            _orgRoleId, _modifierId);
        return orgId;
    }

    /// @notice Событие добавления архива к контейнеру
    /// @param  origin автор транзакции по добавлению архива к контейнеру
    /// @param  container адрес смарт-контракта контейнера
    /// @param  archiveId индекс нового архива в списке контейнеров
    /// @param  modifierId ID модификатора нового архива
    event NewArchive(
        address indexed origin,
        address indexed container,
        uint256 archiveId,
        bytes32 indexed modifierId);

    /// @notice Проверить возможность и зафиксировать событие добавления
    /// архива с указанным модификатором к контейнеру
    /// @param  _modifierId ID модификатора
    function tryAppendArchive(bytes32 _modifierId)
    public enabled senderAndOriginAreValid {
        AbstractContainer container = AbstractContainer(msg.sender);
        require(userCanWrite(tx.origin, container, _modifierId));
        emit NewArchive(tx.origin, msg.sender,
            container.getArchivesCount(), _modifierId);
    }

    /// @notice Проверить возможность и зафиксировать событие добавления
    /// архива с указанным модификатором из конструктора контейнера
    /// @param  _roleModel ролевая модель
    /// @param  _modifierId ID модификатора
    /// @param  _orgRoleId ID роли организации
    /// @param  _archiveId ID (номер) нового архива
    function tryAppendArchiveFromConstructor(RoleModel _roleModel,
                                             bytes32 _modifierId,
                                             bytes32 _orgRoleId,
                                             uint256 _archiveId)
    public enabled senderAndOriginAreValid {
        require(userCanWriteByRoleModel(tx.origin, _roleModel,
                                        _modifierId, _orgRoleId));
        emit NewArchive(tx.origin, msg.sender, _archiveId, _modifierId);
    }

    /// @notice Событие назначения организации роли в контейнере
    /// @param  origin автор транзакции по назначению роли
    /// @param  container адрес смарт-контракта контейнера
    /// @param  orgId ID организации, которой назначена роль
    /// @param  orgRoleId ID назначенной роли
    /// @param  modifierId ID модификатора контейнера
    event OrgRoleGranted(
        address indexed origin,
        address container,
        bytes32 indexed orgId,
        bytes32 orgRoleId,
        bytes32 indexed modifierId);

    /// @notice Проверить возможность и зафиксировать событие назначения
    /// организации роли в контейнере
    /// @param  _orgId ID организации, которой назначается роль
    /// @param  _orgRoleId ID роли организации
    function tryGrantOrgRole(bytes32 _orgId, bytes32 _orgRoleId)
    public enabled senderAndOriginAreValid {
        AbstractContainer container = AbstractContainer(msg.sender);
        bytes32 modifierId = container.modifierId();
        require(orgIsEnabled(_orgId) &&
                userCanWrite(tx.origin, container, modifierId));
        emit OrgRoleGranted(tx.origin, msg.sender, _orgId,
                            _orgRoleId, modifierId);
    }

    /// @notice Проверить возможность и зафиксировать событие назначения
    /// организации роли в контейнере
    /// @param  _roleModel ролевая модель
    /// @param  _containerModifierId ID модификатора контейнера
    /// @param  _containerOrgRoleId ID роли организации автора контейнера
    /// @param  _orgId ID организации, которой назначается роль
    /// @param  _orgRoleId ID роли организации
    function tryGrantOrgRoleFromConstructor(RoleModel _roleModel,
                                            bytes32 _containerModifierId,
                                            bytes32 _containerOrgRoleId,
                                            bytes32 _orgId, bytes32 _orgRoleId)
    public enabled senderAndOriginAreValid {
        require(orgIsEnabled(_orgId) &&
            userCanWriteByRoleModel(tx.origin, _roleModel,
                                    _containerModifierId, _containerOrgRoleId));
        emit OrgRoleGranted(tx.origin, msg.sender, _orgId,
                            _orgRoleId, _containerModifierId);
    }

    /// @notice Событие снятия с организации роли в контейнере
    /// @param  origin автор транзакции по снятию роли
    /// @param  container адрес смарт-контракта контейнера
    /// @param  orgId ID организации, у которой снята роль
    /// @param  orgRoleId ID снятой роли
    /// @param  modifierId ID модификатора контейнера
    event OrgRoleRevoked(
        address indexed origin,
        address container,
        bytes32 indexed orgId,
        bytes32 orgRoleId,
        bytes32 indexed modifierId);

    /// @notice Проверить возможность и зафиксировать событие снятия
    /// с организации роли в контейнере
    /// @param  _orgId ID организации с которой снимается роль
    /// @param  _orgRoleId ID роли организации
    function tryRevokeOrgRole(bytes32 _orgId, bytes32 _orgRoleId)
    public enabled senderAndOriginAreValid {
        AbstractContainer container = AbstractContainer(msg.sender);
        bytes32 modifierId = container.modifierId();
        require(userCanWrite(tx.origin, container, modifierId));
        emit OrgRoleRevoked(tx.origin, msg.sender, _orgId,
                            _orgRoleId, modifierId);
    }

    /// @notice Проверить право блокировки контейнера
    function canDisableContainer()
    public view enabled senderAndOriginAreValid returns(bool) {
        AbstractContainer container = AbstractContainer(msg.sender);
        bytes32 modifierId = container.modifierId();
        return userCanWrite(tx.origin, container, modifierId);
    }
}
