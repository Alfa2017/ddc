package ddc.model.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ddc.model.entity.ManagerEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends PageableRepository<ManagerEntity, String> {

    Optional<ManagerEntity> findByAddress(String address);

    @Query("select m from ManagerEntity m JOIN m.roles mr"
            + " where m.status = ?1"
            + " and mr <> ?2"
            + " and (m.depositoryOgrn = ?3 or m.depositoryOgrn is null)")
    List<ManagerEntity> findAuthManagers(String status, String unwantedUserRoleName, String depositoryOgrn);

}
