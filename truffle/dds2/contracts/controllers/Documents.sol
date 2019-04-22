pragma solidity ^0.4.21;

import "./AftAdmined.sol";
import "../storage/Container.sol";

/// @title СК для ведения списка документов
contract Documents is AftAdmined {

    uint64 public globalDocCounter;
    mapping(bytes32 => uint64) public organizationDocCounter;

    mapping(address => mapping(uint => uint64)) docCounters;

    struct DocStatus {
        bytes32 owner;
        bytes32 status;
    }

    struct Document {
        DocStatus from;
        DocStatus to;
        uint64 depositoryCounter;
        address container;
        uint archiveId;
        bytes meta;
    }

    mapping(uint => Document) docs;

    event DocumentEvent(
        uint64 globalCounter,
        uint64 organizationCounter,
        bytes32 indexed organization,
        bytes32 indexed counterparty,
        address container,
        uint archiveId
    );

    struct Reserve {
        uint64 globalCounter;
        uint64 organizationCounter;
    }

    mapping (bytes32 => Reserve) reserved;

    /// @notice Резервирование id документа
    /// @param orgId Id организации выполняющей резерв
    /// @param reserveId Id резерва
    function reserveCounters(bytes32 orgId, bytes32 reserveId) tryDo("AFTDDSAddDocument", orgId) public {
        bytes32 key = keccak256(orgId, reserveId);
        reserved[key] = Reserve(++globalDocCounter, ++organizationDocCounter[orgId]);
        // TODO: Может быть бросить событие ReserveIdEvent
    }

    function getReservedCounters(bytes32 orgId, bytes32 reserveId) public view returns (uint64, uint64) {
        bytes32 key = keccak256(orgId, reserveId);
        return (reserved[key].globalCounter, reserved[key].organizationCounter);
    }

    function getCounters(bytes32 orgId, bytes32 reserveId) internal returns (uint64, uint64) {
        bytes32 key = keccak256(orgId, reserveId);
        if (reserved[key].globalCounter > 0) {
            uint64 globalCounter = reserved[key].globalCounter;
            uint64 organizationCounter = reserved[key].organizationCounter;
            delete reserved[key];
            return (globalCounter, organizationCounter);
        }
        return (++globalDocCounter, ++organizationDocCounter[orgId]);
    }

    /// @notice Добавление документа/архива в контейнер
    /// @param orgId Id Орагнизации загружающей документ
    /// @param counterOrgId ID орагнизации контрагента для документа
    /// @param container Адрес контейнера
    /// @param modifierId ID модификатора документа (Document)
    /// @param hmac HMAC содержимого документа
    /// @param expiry Срок годности архива
    /// @param reserveId Id резерва номера документа
    /// @param meta Метаинформация документа
    function newDocument(
        bytes32 orgId,
        bytes32 counterOrgId,
        address container,
        bytes32 modifierId,
        bytes32 hmac,
        uint64 expiry,
        bytes32 reserveId,
        bytes meta // TODO: Возможно для событий это поле лишнее, можно сэкономить газ
    )
    public
    {
        if(!checkUserOrg(msg.sender, orgId)) {
            emit ErrorEvent("NOT_MANAGER"); return;
        }
        if(!checkPermissions(msg.sender, "AFTDDSAddDocument")) {
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW"); return;
        }

        // добавляем архив к контейнеру
        uint archiveId = Container(container).appendArchive(modifierId, hmac, expiry);

        uint64 globalCounter;
        uint64 organizationCounter;

        (globalCounter, organizationCounter) = getCounters(orgId, reserveId);

        docCounters[container][archiveId] = globalCounter;

        docs[globalCounter] = Document(
            DocStatus(orgId, "NEW"),
            DocStatus(counterOrgId, "NEW"),
            organizationCounter,
            container,
            archiveId,
            meta
        );

        emit DocumentEvent(
            globalCounter,
            organizationCounter,
            orgId,
            counterOrgId,
            container,
            archiveId
        );
    }

    /// @notice Возвращает информацию о документе
    function getDocInfo(address container, uint archiveId) public view returns (bytes32, bytes32, bytes) {
        return getDocInfo(docCounters[container][archiveId]);
    }

    /// @notice Возвращает информацию о документе
    function getDocInfo(uint docId) public view returns (bytes32, bytes32, bytes) {
        return (docs[docId].from.status, docs[docId].to.status, docs[docId].meta);
    }

    /// @notice Устанавливает статус документа
    function setDocStatus(address container, uint archiveId, bytes32 status) public returns (bytes32, bytes32) {
        return setDocStatus(docCounters[container][archiveId], status);
    }

    /// @notice Устанавливает статус документа
    function setDocStatus(uint64 docId, bytes32 status) public returns (bytes32, bytes32) {

        if(!checkPermissions(msg.sender, "AFTDDSChangeDocStatus")) {
            emit ErrorEvent("ROLE_MODEL_NOT_ALLOW"); return;
        }

        // смена статуса отправителя
        if (checkUserOrg(msg.sender, docs[docId].from.owner)) {
            docs[docId].from.status = status;
        }

        // смена статуса получателя
        if (checkUserOrg(msg.sender, docs[docId].to.owner)) {
            docs[docId].to.status = status;
        }

        // TODO: не бросать событие, если статус не изменился

        emit DocumentEvent(docId,
            docs[docId].depositoryCounter,
            docs[docId].from.owner,
            docs[docId].to.owner,
            docs[docId].container,
            docs[docId].archiveId
        );
        return (docs[docId].from.status, docs[docId].to.status);
    }


}
