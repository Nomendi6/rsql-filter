package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.uuid.UuidRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UuidRecordRepository extends JpaRepository<UuidRecord, Long>, JpaSpecificationExecutor<UuidRecord> {}
