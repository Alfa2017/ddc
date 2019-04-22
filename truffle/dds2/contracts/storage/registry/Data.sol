pragma solidity ^0.4.21;

contract Data {

    /// @notice Список ID узлов (Пр: "mstor.fintechru.org")
    bytes32[] public nodeIds;

    /// @notice Запись об узле СПКС
    struct Node {
        bool disabled;   /// флаг блокировки
        address admin;   /// адрес в Мастерчейн администратора
        address profile; /// адрес контейнера с профилем узла
        bytes sigCert;   /// сертификат для подписи токенов
        bytes encCert;   /// сертификат для шифрования данных
    }

    /// @notice Реестр записей об узлах СПКС по ID
    mapping(bytes32 => Node) public nodes;

    /// @notice Список ID организаций (Пр: "OGRN:1177700002150")
    bytes32[] public orgIds;

    /// @notice Запись об организации
    struct Org {
        bool disabled;     /// флаг блокировки
        address admin;     /// адрес в Мастерчейн администратора
        address profile;   /// адрес контейнера с профилем организации
        bytes32[] nodeIds; /// список ID узлов СПКС
        address[] userIds; /// список ID сотрудников
    }

    /// @notice Реестр записей об организациих по ID
    mapping(bytes32 => Org) public orgs;

    /// @notice Запись о сотруднике
    struct User {
        bool disabled;     /// флаг блокировки
        bytes32 orgId;     /// ID организации из сертификата
        address profile;   /// адрес контейнера с профилем сотрудника
        bytes sigCert;     /// сертификат для подписи токенов
        bytes encCert;     /// сертификат для шифрования данных
        bytes32[] roleIds; /// список ID ролей сотрудника
        mapping(bytes32 => uint256) roleNumbers; /// реестр номеров ролей
        /// реестр номеров архивов с основаниями для назначения ролей
        mapping(bytes32 => uint256) baseArchives;
    }

    /// @notice Реестр записей о сотрудниках по ID
    mapping(address => User) public users;
}
