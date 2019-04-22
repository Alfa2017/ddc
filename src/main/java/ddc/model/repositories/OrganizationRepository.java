package ddc.model.repositories;

import ddc.model.entity.OrganizationEntity;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends PageableRepository<OrganizationEntity, String> {

    Optional<OrganizationEntity> findByOgrn(String s);

    List<OrganizationEntity> findAllByNameIgnoreCaseContaining(String name);

    @Override
    List<OrganizationEntity> findAll();

    List<OrganizationEntity> findAllByOgrnIsNotLike(String depositoryOgrn);
}
