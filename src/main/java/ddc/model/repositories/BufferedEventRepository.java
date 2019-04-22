package ddc.model.repositories;

import ddc.model.entity.constraints.BufferedEventConstraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ddc.model.entity.BufferedEventEntity;


import java.util.List;

@Repository
public interface BufferedEventRepository extends JpaRepository<BufferedEventEntity, BufferedEventConstraint> {

    @Query("select b from BufferedEvent b where b.blockNumber < ?1 and b.processed = false order by b.blockNumber, b.transactionIndex, b.logIndex")
    List<BufferedEventEntity> findAllByBlockNumberIsLessThan(Long blockNumber);

}
