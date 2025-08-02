package com.nomendi6.rsql.demo.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import com.nomendi6.rsql.demo.domain.ProductType;

/**
 * Spring Data SQL repository for the ProductType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long>, JpaSpecificationExecutor<ProductType> {}
