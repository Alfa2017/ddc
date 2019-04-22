package ddc.exception;

public enum ExceptionMessages {

    //счета
    NO_ACCOUNT_WITH_SUCH_ADDRESS("Не удалось найти аккаунт с адресом %s"),
    ACCOUNT_ALREADY_EXIST("Счет с номером %s уже существует. Введите уникальный номер счета."),
    NO_SECTION_FOR_SUCH_ACCOUNT("Не найден раздел %s для счета %s"),
    SECTION_FOR_SUCH_ACCOUNT_ALREADY_EXIST("Раздел с номером %s у счета %s уже существует"),
    WRONG_ACCOUNT_FORMAT("Неверный формат номера счета. Секций меньше 4"),
    NO_ACCOUNT_METADATA("Не удалось получить метаданные аккаунта %s"),
    NO_ACCOUNT_WITH_SUCH_NUMBER("Не удалось найти счет с номером %s"),
    ERROR_FIND_DY("Ошибка при нахождении счета ДУ"),
    ERROR_FIND_DX("Ошибка при нахождении счета ДХ"),
    NO_ACCOUNT_FOR_DEPONENT_WITH_NUMBER("Счет для депонента %s с номером %s не найден в системе"),
    NO_ACCOUNT_FOR_DEPONENT("Не найден счет владельца"),
    NO_ACCOUNT_FOR_ORGANIZATION_WITH_OGRN("Не удалось найти счет с номером %s для депозитария с огрн %s"),
    ACCOUNT_FOR_DATE_REPORT_NOT_FOUND("Не найден счет по номеру %s для выписки по счету депо на дату "),
    DX_ACCOUNT_NOT_FOUND("Не удалось найти аккаунт ДХ с адресом %s"),
    DY_ACCOUNT_NOT_FOUND("Не удалось найти аккаунт ДУ с адресом %s"),
    CANT_CREATE_ACCOUNT("Не удалось создать счет"),
    UNCORRECT_ACCOUNT_ADDRESS("Некорректный адрес аккаунта: %s"),
    NO_ACCOUNT_FOR_GROUP_MOVE("Не найден счет владельца %s для группового перевода "),
    CANT_UPDATE_MIRROR_ACCOUNT("Не удалось обновить зеркальный счет в аккаунте для депозитария %s с номером %s"),
    ACCOUNT_NOT_CREATED_ON_REPORTING_DATE("Счет с номером %s еще не создан на отчетную дату"),
    CANT_GET_ACCOUNT_STATUS_BY_CODE("Не удалось получить статус счета по его коду %s"),

    //закладные
    NOT_VALID_MORTGAGES("Следующие закладные не прошли предварительную валидацию: %s"),
    REFUSE_ALL_NOT_VALID("Все закладные, указанные в поручении на групповой перевод, не прошли валидацию"),
    NO_MORTGAGE_WITH_SUCH_ADDRESS("Не удалось найти закладную с адресом %s"),
    NO_MORTGAGES_WITH_SUCH_REG_NUMS("Не удалось найти закладные по указанным номерам %s"),
    CANT_FIND_MORTGAGE_IN_TEMPORARY_FOLDER("Не удалось найти закладную во временной папке"),
    MORTGAGES_FOR_DEDUCT_NOT_FOUND("Не найдены закладные по поручению на списание"),
    SECURITY_CARD_NOT_FOUND("Не удалось найти карточку ценной бумаги для %s"),
    NO_MORTGAGE_BY_PATH("Не возможно найти закладную по указанному пути %s"),
    REFUSE_NON_PARTIAL_MESSAGE("Не удалось произвести групповой перевод, т.к. частичное исполнение запрещено"),
    REFUSE_REASON("Ошибка при исполнении группового перевода"),
    CANT_GET_MORTGAGE_REG_NUM("Не удалось получить регистрационный номер закладной"),
    NO_MORTGAGES_FOR_MOVE("Для перевода закладных необходимо указать хотя бы одну закладную"),
    NO_MORTGAGE_INFO_BY_NUMBER("Не удалось получить информацию для закладной по номеру %s"),
    NO_MORTGAGE_INFO_BY_ADDRESS("Не удалось получить информацию по закладной %s"),
    MORTGAGE_LIST_CANT_BE_NULL("Список закладных в поручении не может быть пустым"),
    WRONG_MORTGAGES_NUMBER("Количество закладных в xml не совпадает с количеством закладных, находящимся в депозитарии"),
    DEPLOY_MORTGAGE_ERROR("Ошибка при размещении закладной. Список событий пуст"),
    UNCORRECT_MORTGAGE_ADDRESS("Не корректный адрес закладной: %s"),
    CANT_PATCH_MORTGAGE("Не удалось пропатчить закладную %s на дату %s %s"),
    LINK_NOT_FOUND("Не удалось найти ссылку в событии MortgageEvent"),
    MORTGAGE_NUMBER_NOT_FOUND_IN_AGREEMENT("Номер закладной не найден в файле соглашения"),
    CANT_DEDUCT_MORTGAGE("Не удалось списать закладную со всех счетов"),
    MORTGAGE_WITH_SUCH_NUMBER_ALREADY_EXIST("Закладная с номером %s уже существует в системе"),

    //депозитарии и депоненты
    DEPOSITORY_IS_NOT_DEPONENT_IN_DX("Текущий депозитарий не является депонентом в Депозитарии учета"),
    DEPONENT_WITH_SUCH_OGRN_ALREADY_EXIST("Депонент с ОГРН %s уже существует"),
    ORGANIZATION_WITH_SUCH_OGRN_ALREADY_EXIST("Депозитарий с ОГРН %s уже существует"),
    ORGANIZATION_WITH_SUCH_OGRN_DOES_NOT_EXIST("Депозитарий с ОГРН %s не существует в системе"),
    CANT_ADD_DEPOSITORY_WITH_ADDRESS("Не удалось добавить депозитарий с адресом %s"),
    NO_ORGANIZATION_WITH_SUCH_ADDRESS("Не удалось найти организацию с адресом %s"),
    NO_ORGANIZATION_WITH_SUCH_OGRN("Не удалось найти организацию с огрн %s"),
    NO_CUSTODY_DEPOSITORY_WITH_SUCH_ADDRESS("Не удалось найти депозитарий хранения с адресом %s"),
    NO_ACCOUNTING_DEPOSITORY_WITH_SUCH_ADDRESS("Не удалось найти депозитарий учета с адресом %s"),
    NO_DEPONENT_WITH_SUCH_ADDRESS("Не удалось найти депонента по адресу %s"),
    NO_DEPONENT_WITH_SUCH_OGRN("Не удалось найти депонента с огрн %s"),
    WRONG_DEPONENT_TYPE("Неверный тип счета владельца/НД: %s"),
    DY_ORGANIZATION_NOT_FOUND("Не удалось найти организацию ДУ с адресом %s"),
    DX_ORGANIZATION_NOT_FOUND("Не удалось найти организацию ДХ с адресом %s"),
    CANT_CREATE_DEPONENT("Не удалось создать депонента. Список событий пуст"),
    CANT_CREATE_ORGANIZATION("Не удалось создать депозитарий. Список событий пуст"),
    UPDATE_DEPONENT_METADATA_ERROR("Ошибка при обновлении метаданных депонента. Список событий пуст"),
    UNCORRECT_ORGANIZATION_ADDRESS("Некорректный адрес организации: %s"),
    CANT_GET_ORGANIZATION_CONTAINER("Не удалось получить контейнер текущего депозитария"),
    NO_DEPONENT_FOR_ACCOUNT_WITH_OGRN("Для счета с огрн %s не был найден депонент"),
    CANT_FIND_ORGANIZATION_WITH_OPENED_ACCOUNT("Не удалось найти организацию, на которую открыт счет"),

    //документы
    NO_DOCUMENT_WITH_SUCH_NUMBER("Не удалось найти документ с номером в депозитарии %s"),
    NO_DOCUMENT_WITH_SUCH_ID("Не удалось найти документ с id %s в контейнере %s"),
    NO_DOCUMENT_WITH_SUCH_ADDRESS("Не удалось найти документ по адрессу %s"),
    ERROR_CHANGE_DOCUMENT_STATUS("Не удалось сменить статус документа для встречного поручения %s"),
    NO_LOAN_TERMINATION_DATE_IN_DOCUMENT("В документе закладной отсутствует дата окончания кредитного договора"),
    CANT_EXPORT_DOCUMENT("Не удалось экспортировать документ по причине: %s"),

    // пользователи
    NO_USER_WITH_SUCH_ADDRESS("Не найден пользователь по адресу: %s"),
    ERROR_ADD_MANAGER("Ошибка при добавлении пользователя %s в Managers"),
    CANT_UPDATE_MANAGER_DATA("Не удалось обновить данные пользователя %s"),
    UNABLE_GET_USER_DATA("Не удалось получить данные для пользователя %s"),
    UNABLE_PUT_USER_DATA("Не удалось поместить данные пользователя %s в хранилище"),
    REMOVE_MANAGER_ERROR("Ошибка при удалении пользователя %s в Managers"),

    // операции
    NO_OPERATION_WITH_SUCH_ID("Не удалось найти операцию c id %s"),

    // транзакции
    NO_TRANSACTION_WITH_SUCH_HASH("Не удалось получить транзакцию по хэшу"),
    ERROR_TRANSACTION_REQUEST("Error processing transaction request: %s"),
    TRANSACTION_NULL_STATUS("Статус транзакции 0x0"),
    NO_TRANSACTON_FOR_BUFFER("Не удалось получить транзакцию по хэшу для буферизированного события %s"),
    TRANSFER_BY_DEFERRED_ORDER("Попытка перехода к бизнес транзакции по отложенному поручению [%s]"),
    CANT_GET_EVENT_ABOUT_MORTGAGE("Не удалось получить событие из транзакции по обновлению (аннулированию) закладной"),

    // валидация
    VALIDATION_DATA_ERROR("Данные поля не прошли валидацию: %s"),
    VALIDATION_SALE_ERROR("Ошибка валидации при продаже"),
    VALIDATION_ERROR_BY_SALE_WITH_DOCUMENT("Ошибка валидации при продаже по поручению %s. "),
    VALIDATION_NOT_OUR_MORTGAGE("Попытка валидации закладной, которая находится не в нашем депозитарии %s"),
    VALIDATION_NOT_OUR_DEPOSITORY("Файл не предназначен для использования в данном депозитарии"),

    // файлы
    NO_FILE_WITH_SUCH_ID("Не найден файл по id %s"),
    MUST_BE_XML_FORMAT("Необходим файл формата xml"),
    WRONG_REQUEST_TYPE("Некорректный тип файла request.xml"),
    CANT_FIND_REQUEST_FILE("Не удалось найти файл request.xml. Файл не существует по пути %s"),
    MOVE_FILE_NOT_EXIST("Файл для перемещения не существует по пути %s"),
    BASE_FILE_NOT_FOUND("Не найден файл-основание для группового перевода %s"),
    ERROR_INSTRUCTION_TYPE("Поручение должно иметь тип OwInstructionDeduct или OwInstructionEnroll"),
    NOT_GROUP_MOVE_FILE("Загруженный файл не является файлом группового перевод"),
    FAIL_REQUEST_NOT_FOUND("Не удалось найти файл \"request.xml\" с описанием имени файла соглашения"),
    CONTENT_FAILED_VALIDATION("Содержимое файла %s не прошло проверку"),
    REQUEST_IN_TEMPORARY_FOLDER_NOT_FOUND("Не удалось найти файл запроса во временной папке"),
    CANT_ADD_CANCELLATION_FILE("Не удалось добавить файлы аннулирования закладной"),
    UNABLE_PROCESS_GROUP_MOVE_FILE("Не удалось обработать файл группового перевода"),
    ONLY_ZIP_FILE("Во входящий каталог может быть загружен только файл в формате zip. %s не соответствует этому требованию"),
    CANT_CREATE_TEMPORARY_FILE("Не удалось создать временный файл: %s"),
    NO_MIGRATION_FILE_BY_PATTERN("Не удалось найти xml файл миграции по паттерну %s"),
    HANDLER_FOR_FILE_NOT_FOUND("Не найден обработчик для входящего файла "),
    NO_ZIP_FILE_IN_MIGRATION_ARCHIVE("В архиве по миграциям закладных не найдено зип файла закладной"),
    NO_REPORT_IN_MIGRATION_ARCHIVE("В архиве по миграциям закладных не найдено отчета о закладной"),
    FILE_CANT_BE_NULL("Файл не может быть пустым"),
    CANT_MOVE_FILE("Не удалось переместить файл: %s"),
    CANT_REMOVE_DIRECTORY("Не удалось удалить директорию %s"),
    CANT_FIND_FILE_BY_PATTERN("Не удалось найти файл по паттерну %s в папке %s"),
    CANT_IMPORT_FILE("Не удалось импортировать файл по причине: %s"),
    ERROR_XSD_VALIDATION_FILE("Файл не прошел валидацию по XSD схеме"),

    // Registry и RoleModel
    USER_CANT_WRITE_ROLE_MODEL("Вызов Registry.userCanWriteRoleModel вернул null"),
    NOT_REGISTRY_OR_ROLE_MODEL("Ожидалось RoleModel или Registry, переданное значение %s"),

    // архив
    NO_ARCHIVE_WITH_SUCH_ID("Не удалось найти архив с id %s"),
    CANT_GET_ARCHIVE_FROM_REPOSITORY("Не удалось получить архив из хранилища"),
    BLOCK_XML_FILE_NOT_FOUND("В архиве не найден xml файл по разблокированию"),
    INSTRUCTION_XML_FILE_NOT_FOUND("В архиве не найден xml файл Поручения"),
    CANT_PUT_ARCHIVE_WITH_DDSID("Не удалось положить архив с ddsId = %s"),
    CANT_GET_ARCHIVE_FROM_CONTAINER("Не удалось получить архив с ID %s, по контейнеру %s из удаленного хранилища мастерчейн"),

    // отчет
    CANT_GENERATE_REPORT_DEPONENT_NOT_FOUND("Не удалось сформировать отчет, данный депонент не найден в системе"),
    CANT_GENERATE_REPORT_DEPOSITORY_NOT_FOUND("Не удалось сформировать отчет, данная организация не найден в системе"),
    CANT_GENERATE_REPORT_ACCOUNT_NOT_FOUND("Не удалось сформировать отчет, счет не найден в системе"),
    CANT_GENERATE_REPORT_ACCOUNT_HOLDER_NOT_FOUND("Не удалось сформировать отчет, не найден владелец счета %s"),

    // другие ошибки
    //нужно нормальное название
    STATEMENT_PAST_DATE("Выписку можно создать только на прошедшую дату (формируется на конец дня)"),
    NO_METADATA_WRONG_ID("Не удалось получить метаданные: параметр id документа не целое число, переданное значение %s"),

    SERVER_INNER_ERROR("Внутреняя ошибка сервера: %s"),
    UNEXPECTED_RESULT_TYPE_MUST_BE_HASH("Неожиданный тип результата: %s, ожидался тип %s"),
    FAILED_TO_CANCEL_FILTER("не удалось отменить фильтр с id '%s'"),
    REQUEST_ERROR("Ошибка запроса: %s"),
    BLOCKS_NUMBER_NOT_POSITIVE("Количество блоков, на которые вычислеяется среднее время меньше или равно нулю"),
    CANT_GET_ARCHIVE("Не удалось получить архив из хранилища"),
    CANT_FIND_ATTACHMENT("Не удалось найти вложение"),
    GROUP_MOVE_RECORD_NOT_FOUND("Не удалось найти запись, связанную с файлом, который является основанием для группового перевода"),
    EVENT_FOR_SECTION_NOT_FOUND("Не удалось получить событие для открытия раздела счета"),
    EVENT_FOR_UPDATE_DEPONENT_NOT_FOUND("Не удалось получить событие из транзакции по обновлению депонента"),
    NO_CONTAINER_WITH_ADDRESS("Не удалось получить контейнер с адресом %s"),
    ARGUMENT_NUMBER_FOR_LOG_MARKER("Количество аргументов для лога маркера FILE должно быть не меньше 1"),
    LAST_ARGUMENT_TYPE("Последний аргумент должен быть типа FileLogEntryStruct"),
    NOTIFICATION_NOT_FOUND("Оповещение с заданным идентификатором не найдено"),
    FAILED_CONVERT_DATE_TO_XML("Не удалось преобразовать дату в xml дату %s"),
    TARGET_FOLDER_NOT_EXIST("Целевая папка не существует по пути %s"),
    CANT_FIND_SCHEMA_BY_WAY("Не удалось найти схему по пути %s"),
    CANT_GET_EVENT_ABOUT_NEW_ACCOUNT("Не удалось получить событие из транзакции по добавлению нового счета"),
    NO_METAINFO_BY_ID("Не удалось найти метаинформацию по ddsId = %s"),

    UNEXPECTED_ERROR("Неизвестный тип ошибки %s"),

    ACCOUNT_NOT_FOUND("Счет не найден"),


    DDS_SYSTEM_ERROR("Контракт DDSystem не инициализирован"),
    ERROR("Ошибка %s");

    private final String text;

    ExceptionMessages(String text) {
        this.text = text;
    }

    public String getMessage(Object... args) {
        return String.format(text, args);
    }
}
