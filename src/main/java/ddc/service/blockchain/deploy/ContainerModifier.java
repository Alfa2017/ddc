package ddc.service.blockchain.deploy;

import ddc.service.blockchain.Web3jUtils;
import lombok.Getter;

/**
 * Модификаторы контейнеров и архивов
 */
public enum ContainerModifier implements Modifier {

    //*************************** Действия над документом ***************************//
    CreateContainer("Создание контейнера"),
    AddDocument("Добавление документа в контейнер"),
    ChangeDocStatus("Смена статуса документа");

    @Getter
    private final String desc;

    ContainerModifier(String desc) {
        this.desc = desc;
    }

    public byte[] getBytes(String prefix) {
        // TODO: Скорее всего понадобится конвертировать в camelCase
        return Web3jUtils.strToBytes32(prefix + this.name());
    }
}
