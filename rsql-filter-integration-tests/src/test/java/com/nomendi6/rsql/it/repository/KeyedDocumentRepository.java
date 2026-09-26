package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.compositekey.DocumentKey;
import com.nomendi6.rsql.it.domain.compositekey.KeyedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyedDocumentRepository extends JpaRepository<KeyedDocument, DocumentKey>, JpaSpecificationExecutor<KeyedDocument> {}
