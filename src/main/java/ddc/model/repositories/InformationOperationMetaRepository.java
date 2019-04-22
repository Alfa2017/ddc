package ddc.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ddc.model.entity.InformationOperationMetaEntity;

@Repository
public interface InformationOperationMetaRepository extends CrudRepository<InformationOperationMetaEntity, Long> {
}
