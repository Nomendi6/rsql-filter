package testappl.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import testappl.domain.ProductType;

/**
 * Spring Data JPA repository for the ProductType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long>, JpaSpecificationExecutor<ProductType> {}
