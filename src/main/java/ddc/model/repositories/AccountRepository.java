package ddc.model.repositories;

import ddc.model.entity.AccountEntity;

import java.util.Collection;
import java.util.Optional;

public interface AccountRepository extends PageableRepository<AccountEntity, String> {

    Optional<AccountEntity> findByDeponentOgrn(String s);

    Optional<AccountEntity> findByAddress(String s);
    /**
     * Поиск по точному соответствию депозитария и номера счета
     */
    Optional<AccountEntity> findByDepositoryOgrnAndNumber(String ogrn, String number);

    /**
     * Поиск по точному соответствию депозитария и адресса счета
     */
    Optional<AccountEntity> findByDepositoryOgrnAndAddress(String d, String n);

    /**
     * Поиск по депозитарию и вхождению номера счета
     */
    Optional<AccountEntity> findByDepositoryOgrnAndNumberContaining(String d, String n);

    /**
     * Поиск по адресу депозитария и типу аккаунта.  Тип аккаунта любой из accTypes. Возвращается первая подходящая запись.
     */
    Optional<AccountEntity> findFirstByDepositoryOgrnAndAccTypeIn(String depository, Collection<String> accTypes);

    Optional<AccountEntity> findByNumberAndDeponentOgrn(String number, String holder);

    Optional<AccountEntity> findByNumberAndDeponentOgrnAndDepositoryOgrn(String number, String holder, String depository);

    Optional<AccountEntity> findTop1ByDepositoryOgrnAndAccTypeOrderByCreatedAtAsc(String depository, String accType);

    Optional<AccountEntity> findByMirrorAccountNumber(String number);

}
