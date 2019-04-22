package ddc.service.blockchain.deploy;

import ddc.service.blockchain.Web3jUtils;
import lombok.Getter;

/**
 * Модификаторы действия в ДДС
 * Важно!!! Модификаторы ниже и в контрактах ДДС должны быть согласованы м/у собой
 */
public enum ActionModifier implements Modifier {

    //*************************** Действия над контрактом ***************************//
    DisableContract("Блокировка контракта"),


    //*************************** Действия над организацией ***************************//
    AddOrganization("Добавление организации"),
    EditOrganization("Редактирование информации об организации"),
    RemoveOrganization("Удаление информации об организации"),

    AddDeponent("Создание депонента"),
    EditDeponent("Редактирование депонента"),
    RemoveDeponent("Удаление депонента"),


    //*************************** Действия над документом ***************************//
//    CREATE_CONTAINER("Создание контейнера"),
//    ADD_DOCUMENT("Добавление документа в контейнер"),
//    CHANGE_DOC_STATUS("Сменить статус документа"),


    //*************************** Действия над счетом ***************************//
    AddAccount("Создание счета"),
    UpdateAccount("Обновление метаданных счета"),
    AddSection("Добавление секции"),
    EditSection("Редактирование секции"),
    DisableSection("Отключение секции"),
    //todo REMOVE_ACCOUNT ?


    //*************************** Действия над закладной ***************************//
    AddMortgage("Создание закладной"),
    ChangeAccount("Смена счета закладной"),
    ChangeAccountSection("Смена раздела счета закладной"),
    ChangeMortgageStatus("Смена статуса закладной"),
    RollbackToVersion("Возврат состояния закладной на версию"),
    AddAgreement("Добавление соглашения об изменении"),
    SaveExtendedInfo("Сохранение расширенной карточки закладной в контейнере закладной"),
    Redemption("Аннулирование закладной"),
    GetSnapshot("Получение состояния закладной на версию");


    @Getter
    private final String desc;

    ActionModifier(String desc) {
        this.desc = desc;
    }

    public byte[] getBytes(String prefix) {
        // TODO: Скорее всего понадобится конвертировать в camelCase
        return Web3jUtils.strToBytes32(prefix + this.name());
    }
}
