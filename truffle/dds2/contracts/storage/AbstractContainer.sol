pragma solidity ^0.4.21;

import "./Registry.sol";
import "./RoleModel.sol";
import "./Suspendable.sol";

/// @title  Базовый контейнер СПКС
/// @notice Содержит список архивных файлов и ролевую модель доступа
contract AbstractContainer is Suspendable {

    /// @notice Реестр СПКС, к которому относится контейнер
    function registry() public view returns (Registry);

    /// @notice Ролевая модель доступа к архивам контейнера
    function roleModel() public view returns (RoleModel);

    /// @notice ID модификатора контейнера, определяет права его создания,
    /// назначения ролей организаций, выключения
    function modifierId() public view returns (bytes32);

    /// @notice Список ID организаций, для которых назначены роли
    bytes32[] public orgIds;

    /// @notice Реестр ID ролей организаций по ID организаций
    mapping(bytes32 => bytes32[]) public orgRoleIds;

    /// @dev Перегруженный модификатор блокировки и инициализации
    modifier enabled() {
        require (disabled == false && orgIds.length > 0);
        _;
    }

    /// @notice Проверить право и зафиксировать событие блокировки
    function canDisable() public view returns(bool) {
        return registry().canDisableContainer();
    }

    /// @notice Запись об архиве
    struct Archive {
        address authorId;   /// ID сотрудника-автора
        bytes32 modifierId; /// ID модификатора для ролевой модели
        bytes32 hmac;       /// HMAC архива
        uint64 expiry;      /// срок годности архива
        uint256 prevVersion; /// ID предыдущей версии 
        uint256 nextVersion; /// ID следующей версии
    }

    /// @notice Список архивов в контейнере
    Archive[] public archives;

    /// @notice Инициализировать контейнер
    /// @param  _orgRoleId ID роли организации, создающей контейнер
    function initialize(bytes32 _orgRoleId) public {
        // check to not initialize second time
        require (orgIds.length == 0);
        // tryCreateContainer() проверяет возможность и фиксирует событие
        // создания контейнера, возвращает ID организации создателя
        bytes32 orgId = registry().tryCreateContainer(
            roleModel(), modifierId(), _orgRoleId);
        orgRoleIds[orgId] = new bytes32[](0);
        orgRoleIds[orgId].push(_orgRoleId);
        orgIds.push(orgId);
    }

    /// @notice Назначить роль организации (без проверок)
    function grantOrgRolePrivate(bytes32 _orgId, bytes32 _orgRoleId)
    private enabled {
        if (orgRoleIds[_orgId].length == 0) {
            orgIds.push(_orgId);
            orgRoleIds[_orgId] = new bytes32[](0);
        } else for (uint256 i = orgRoleIds[_orgId].length; i > 0; i--)
            if (orgRoleIds[_orgId][i-1] == _orgRoleId)
                return;
        orgRoleIds[_orgId].push(_orgRoleId);
    }

    /// @notice Назначить роль организации
    function grantOrgRole(bytes32 _orgId, bytes32 _orgRoleId)
    public enabled {
        // tryGrantOrgRole() проверяет возможность и фиксирует событие
        // назначения роли организации
        registry().tryGrantOrgRole(_orgId, _orgRoleId);
        grantOrgRolePrivate(_orgId, _orgRoleId);
    }

    /// @notice Назначить роль организации из конструктора контейнера
    function grantOrgRoleFromConstructor(bytes32 _orgId, bytes32 _orgRoleId)
    public enabled {
        bytes32 containerOrgId = registry().getUserOrgId(msg.sender);
        registry().tryGrantOrgRoleFromConstructor(roleModel(), modifierId(),
                                                  containerOrgId, _orgId,
                                                  _orgRoleId);
        grantOrgRolePrivate(_orgId, _orgRoleId);
    }

    /// @notice Снять роль с организации
    function revokeOrgRole(bytes32 _orgId, bytes32 _orgRoleId)
    public enabled {
        require(orgRoleIds[_orgId].length > 0);
        // tryRevokeOrgRole() проверяет возможность и фиксирует событие
        // снятия роли с организации
        registry().tryRevokeOrgRole(_orgId, _orgRoleId);
        uint256 i;
        if (orgRoleIds[_orgId].length == 1)
            // удалить организацию из контейнера
            for (i = orgIds.length; i > 0; i--)
                if (orgIds[i-1] == _orgId) {
                    orgIds[i-1] == orgIds[orgIds.length - 1];
                    orgIds.length--;
                    return;
                }
        else // удалить роль организации из списка ролей
            for (i = orgRoleIds[_orgId].length; i > 0; i--)
                if (orgRoleIds[_orgId][i-1] == _orgRoleId) {
                    uint256 last = orgRoleIds[_orgId].length - 1;
                    orgRoleIds[_orgId][i-1] == orgRoleIds[_orgId][last];
                    orgRoleIds[_orgId].length--;
                    return;
                }
    }

    /// @notice Пополнить контейнер архивом
    /// @param  _modifierId ID модификатора
    /// @param  _hmac HMAC архива
    /// @param  _expiry срок годности архива
    /// @return ID добавленного архива
    function appendArchive(bytes32 _modifierId, bytes32 _hmac, uint64 _expiry)
    public enabled returns(uint256) {
        // tryCreateArchive() проверяет возможность записи
        // в контейнер архива с данным модификатором от tx.origin
        registry().tryAppendArchive(_modifierId);
        uint newId = archives.length;
        archives.push(
            Archive(tx.origin, _modifierId, _hmac, _expiry, newId, newId));
        return newId;
    }

    /// @notice Пополнить контейнер архивом - обновленной версией
    /// уже существующего, прописывая в архивы перекрёстные ссылки
    function updateArchive(uint256 _prevId, bytes32 _hmac, uint64 _expiry)
        public enabled returns (uint256 _newArchiveId)
    {
        require(_prevId < getArchivesCount());
        require(isArchiveLatestVersion(_prevId));
        bytes32 modId = getArchiveTypeId(_prevId);
        uint256 newId = appendArchive(modId, _hmac, _expiry);
        archives[newId].prevVersion = _prevId;
        archives[_prevId].nextVersion = newId;
        return newId;
    }

    /// @notice Пополнить контейнер архивом из конструктора контейнера
    /// @param  _modifierId ID модификатора
    /// @param  _hmac HMAC архива
    /// @param  _expiry срок годности архива
    /// @return ID добавленного архива
    function appendArchiveFromConstructor(bytes32 _orgRoleId, bytes32 _modifierId,
                                          bytes32 _hmac, uint64 _expiry)
    public enabled {
        registry().tryAppendArchiveFromConstructor(
            roleModel(), _modifierId, _orgRoleId, archives.length
        );
        uint newId = archives.length;
        archives.push(Archive(tx.origin, _modifierId, _hmac, _expiry, newId, newId));
    }

    /// @notice Изменить срок годности архива от имени автора архива
    /// @param  _archiveId ID архива
    /// @param  _expiry срок годности архива
    function updateExpiry(uint256 _archiveId, uint64 _expiry)
    public enabled {
        require(userIsAuthor(msg.sender, _archiveId));
        archives[_archiveId].expiry = _expiry;
    }

    /// @notice Проверить, что сотрудник имеет право читать архив
    function userCanRead(address _userId, uint256 _archiveId)
    public view returns (bool) {
        require(_archiveId < archives.length);
        return registry().userCanRead(_userId, this,
                                      archives[_archiveId].modifierId);
    }

    /// @notice Проверить, что сотрудник автор архива
    function userIsAuthor(address _userId, uint256 _archiveId)
    public view returns (bool) {
        return archives[_archiveId].authorId == _userId;
    }

    /// @notice Получить список ID организаций,
    /// для которых назначен доступ в контейнер
    function getOrgIds()
    public view returns (bytes32[]) {
        return orgIds;
    }

    /// @notice Получить список ID ролей организации
    function getOrgRoleIds(bytes32 _orgId)
    public view returns (bytes32[]) {
        return orgRoleIds[_orgId];
    }

    /// @notice Получить количество ID ролей организации
    function getOrgRoleIdsCount(bytes32 _orgId)
    public view returns (uint256) {
        return orgRoleIds[_orgId].length;
    }

    /// @notice Получить список ID ролей организации
    function getOrgRoleId(bytes32 _orgId, uint256 _index)
    public view returns (bytes32) {
        return orgRoleIds[_orgId][_index];
    }

    /// @notice Получить количество архивов в контейнере
    function getArchivesCount()
    public view returns (uint256) {
        return archives.length;
    }

    /// @notice Получить ID автора архива под номером
    function getArchiveAuthorId(uint256 _archiveId)
    public view returns (address) {
        return archives[_archiveId].authorId;
    }

    /// @notice Получить ID модификатора архива под номером
    function getArchiveTypeId(uint256 _archiveId)
    public view returns (bytes32) {
        return archives[_archiveId].modifierId;
    }

    /// @notice Получить HMAC архива под номером
    function getArchiveHMAC(uint256 _archiveId)
    public view returns (bytes32) {
        return archives[_archiveId].hmac;
    }

    /// @notice Получить срок годности архива под номером
    function getArchiveExpiry(uint256 _archiveId)
    public view returns (uint64) {
        return archives[_archiveId].expiry;
    }

    /// @notice Проверить, что версия архива самая свежая
    function isArchiveLatestVersion(uint256 _archiveId)
    public view returns(bool) {
        return archives[_archiveId].nextVersion == _archiveId;
    }

    /// @notice Получить архив с предыдущей версией для данного
    function getArchivePrevVersion(uint256 _archiveId)
    public view returns(uint256) {
        return archives[_archiveId].prevVersion;
    }

    /// @notice Получить архив со следующей версией для данного
    function getArchiveNextVersion(uint256 _archiveId)
    public view returns(uint256) {
        return archives[_archiveId].nextVersion;
    }

    /// @notice Получить архив с самой свежей версией для данного
    function getArchiveLatestVersion(uint256 _archiveId)
    public view returns(uint256) {
        while (archives[_archiveId].nextVersion != _archiveId)
            _archiveId = archives[_archiveId].nextVersion;
        return _archiveId;
    }

}
