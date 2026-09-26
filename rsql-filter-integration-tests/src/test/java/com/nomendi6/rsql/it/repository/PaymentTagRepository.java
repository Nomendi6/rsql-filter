package com.nomendi6.rsql.it.repository;

import com.nomendi6.rsql.it.domain.compositekey.PaymentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTagRepository extends JpaRepository<PaymentTag, PaymentTag.Key>, JpaSpecificationExecutor<PaymentTag> {}
