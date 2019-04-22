pragma solidity ^0.4.21;

import "./Documents.sol";

/// @title Списки участников ДДС
contract Organizations is Documents {

    uint64 orgCounter = 1;
    uint64 orgDeponentCounter = 1;

    struct OrgInfo {
        bool isExist;
        address container;
        bytes meta;
    }

    /// @notice Зарегистрированные организации
    mapping(bytes32 => OrgInfo) public organizations;

    /// @notice Метаданные депонента организации
    /// организация => депонент => Информация о депоненте
    mapping(bytes32 => mapping(bytes32 => OrgInfo)) public deponents;

    event OrganizationEvent (
        uint64 counter,
        bytes32 indexed action,
        bytes32 indexed orgId
    );

    event DeponentEvent (
        uint64 counter,
        bytes32 action,
        bytes32 orgId,
        bytes32 deponentOrgId
    );

    /// @notice Проверка регистрации организации
    /// @param orgId OrgId организации
    /// @return bool Зарегистрирована (true), не зарегистрирована (false)
    function orgIsExist(bytes32 orgId) public view returns (bool) {
        return organizations[orgId].isExist;
    }

    /// @notice Проверка регистрации депонента
    /// @param orgId OrgId орагнизации
    /// @param deponentOrgId OrgId депонента
    /// @return bool Зарегистрирована (true), не зарегистрирована (false)
    function deponentIsExist(bytes32 orgId, bytes32 deponentOrgId) public view returns (bool) {
        return deponents[orgId][deponentOrgId].isExist;
    }

    /// @notice Возвращает информацию об организации
    /// @param orgId OrgId организации
    /// @return bool
    /// @return address
    /// @return bytes
    function getOrgInfo(bytes32 orgId) public view returns (bool, address, bytes) {
        OrgInfo memory org = organizations[orgId];
        return (org.isExist, org.container, org.meta);
    }


    /// @notice Возвращает информацию о депоненте депозитария
    /// @param orgId OrgId организации
    /// @param deponentOrgId OrgId депонента
    /// @return bool
    /// @return address
    /// @return bytes
    function getDeponentOrgInfo(bytes32 orgId, bytes32 deponentOrgId) public view returns (bool, address, bytes) {
        OrgInfo memory org = deponents[orgId][deponentOrgId];
        return (org.isExist, org.container, org.meta);
    }

    /// @notice Редактирование ранее зарегистрированной организации
    /// @param orgId OrgId орагнизации
    /// @param container Контейнер орагнизации
    /// @param meta Метаинформация об организации
    function editOrganization(bytes32 action, bytes32 orgId, address container, bytes meta)
    external {
        if (!checkAftPermissions(msg.sender, action)) {
            emit ErrorEvent("NOT_DDS_ADMIN");
            return;
        }

        if (action == "AFTDDSAddOrganization") {
            if (orgIsExist(orgId)) {
                emit ErrorEvent("ORGANIZATION_ALREADY_EXIST");
                return;
            }
        } else if (action == "AFTDDSEditOrganization") {
            // Организация должна быть зарегистрирована
            if (!orgIsExist(orgId)) {
                emit ErrorEvent("ORGANIZATION_NOT_EXIST");
                return;
            }
        } else if (action == "AFTDDSRemoveOrganization") {
            require(orgIsExist(orgId));
            if (!orgIsExist(orgId)) {
                emit ErrorEvent("ORGANIZATION_NOT_EXIST");
                return;
            }
            delete organizations[orgId];
            return;
        } else {
            emit ErrorEvent("UNKNOWN_ACTION");
            return;
        }
        emit OrganizationEvent(orgCounter++, action, orgId);

        organizations[orgId] = OrgInfo(true, container, meta);
        deponents[orgId][orgId] = OrgInfo(true, container, meta);
    }

    /// @notice Редактирование информации о депоненте
    /// @param action действие (add, edit, remove)
    /// @param orgId OrgId орагнизации, которая добавляет депонента
    /// @param deponentOrgId OrgId депонента
    /// @param container Контейнер депонента
    /// @param meta Метаинформация о депоненте
    function editDeponent(bytes32 action, bytes32 orgId, bytes32 deponentOrgId, address container, bytes meta)
    external
    tryDo(action, orgId) {
        // организация должна существовать
        if(!orgIsExist(orgId)) {
            emit ErrorEvent("ORGANIZATION_NOT_EXIST");
            return;
        }

        if (action == "AFTDDSAddDeponent") {
            // Депонент не должен быть ранее зарегистрированным для данной организации
            if(deponentIsExist(orgId, deponentOrgId)) {
                emit ErrorEvent("ORGANIZATION_ALREADY_EXIST");
                return;
            }
        } else if (action == "AFTDDSEditDeponent") {
            // При редактировании депонент должен существовать
            if(!deponentIsExist(orgId, deponentOrgId)) {
                emit ErrorEvent("DEPONENT_NOT_EXIST");
                return;
            }
        } else if (action == "AFTDDSRemoveDeponent") {
            if(!deponentIsExist(orgId, deponentOrgId)) {
                emit ErrorEvent("DEPONENT_NOT_EXIST");
                return;
            }
            delete deponents[orgId][deponentOrgId];
            return;
        } else {
            // Ели действие неопределено
            emit ErrorEvent("UNKNOWN_ACTION");
            return;
        }
        emit DeponentEvent(orgDeponentCounter++, action, orgId, deponentOrgId);

        deponents[orgId][deponentOrgId] = OrgInfo(true, container, meta);
    }

}
