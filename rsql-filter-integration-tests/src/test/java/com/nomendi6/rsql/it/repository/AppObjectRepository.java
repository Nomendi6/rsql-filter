package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.AppObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AppObjectRepository extends JpaRepository<AppObject, Long>, JpaSpecificationExecutor<AppObject> {
}