package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.ProductRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRelationRepository extends JpaRepository<ProductRelation, Long>, JpaSpecificationExecutor<ProductRelation> {
}
