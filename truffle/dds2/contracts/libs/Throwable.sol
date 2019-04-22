pragma solidity ^0.4.21;

/*
 * See ru.iteco.aft.dds.model.enums.BlockchainErrorEnum

 * NOT_CUSTODY_MANAGER : Действие позволено только менеджеру депозитария хранения
 * NOT_ACCOUNTING_MANAGER : Действие позволено только менеджеру депозитария учета
 * NOT_CUSTODY_ACCOUNTING_MANAGER : Действие позволено только менеджеру депозитария хранения или депозитария учета
 * NOT_MANAGER("Действие позволено только менеджеру депозитария"),
 * NOT_DDS_ADMIN : Действие позволено только администратору системы
 * ROLE_MODEL_NOT_ALLOW : Действие не позволено ролевой моделью
 *
 * CUSTODY_DEPOSITORY_NOT_EXIST : Депозиторий хранения не зарегистрирован в ДДС
 * ACCOUNTING_DEPOSITORY_NOT_EXIST : Депозиторий учета не зарегистрирован в ДДС
 * DEPONENT_NOT_EXIST : Депонент не зарегистрирован в ДДС
 * ORGANIZATION_NOT_EXIST : Организация не зарегистрирована в ДДС
 * NOT_REGISTERED_MORTGAGE : Действие позволено только зарегистрированному контракту закладной
 * NOT_REGISTERED_ACCOUNT : Действие позволено только зарегистрированному контракту счета
 *
 * AGREEMENT_NUMBER_ALREADY_EXIST : Такой номер соглашения об изменения уже существует в закладной
 * MORTGAGE_ALREADY_EXIST : Закладная с таким номером уже загружена
 * ACCOUNT_ALREADY_EXIST : Счет с таким номером уже создан
 * DEPONENT_ALREADY_EXIST : Депонент с таким ОГРН уже зарегистрирован в системе
 * ORGANIZATION_ALREADY_EXIST : Организация с таким ОГРН уже зарегистрирована в системе
 *
 * NOT_EXPECTED_STATE : Сменить статус закладной на "BLOCKED_FOR_TRANSFER" можно только при текущем статусе "AVAILABLE"
 * INACTIVE_MORTGAGE : Попытка действия над неактивной закладной
 *
 * UNKNOWN_ACTION : Неизвестный модификатор действия
*/
contract Throwable {

    event ErrorEvent(bytes32 errorDetails);

}
