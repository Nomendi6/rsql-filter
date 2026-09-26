package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.compositekey.LineKey;
import com.nomendi6.rsql.it.domain.compositekey.KeyedLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyedLineRepository extends JpaRepository<KeyedLine, LineKey>, JpaSpecificationExecutor<KeyedLine> {}
