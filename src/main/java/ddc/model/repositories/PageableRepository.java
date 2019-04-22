package ddc.model.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface PageableRepository<E, ID> extends PagingAndSortingRepository<E, ID>, JpaSpecificationExecutor<E> {

}
