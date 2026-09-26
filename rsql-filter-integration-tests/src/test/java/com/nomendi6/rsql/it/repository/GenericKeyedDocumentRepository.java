package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.compositekey.DocumentKey;
import com.nomendi6.rsql.it.domain.compositekey.GenericKeyedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GenericKeyedDocumentRepository extends JpaRepository<GenericKeyedDocument, DocumentKey>, JpaSpecificationExecutor<GenericKeyedDocument> {}
