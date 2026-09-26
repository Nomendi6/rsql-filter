package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/** Refers to a {@link KeyedDocument} through a three-column foreign key. */
@Entity
@Table(name = "keyed_payment")
public class KeyedPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "reference")
    private String reference;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "doc_company_code", referencedColumnName = "company_code"),
        @JoinColumn(name = "doc_year", referencedColumnName = "doc_year"),
        @JoinColumn(name = "doc_no", referencedColumnName = "doc_no"),
    })
    private KeyedDocument document;

    public KeyedPayment() {}

    public KeyedPayment(Long id, String reference, KeyedDocument document) {
        this.id = id;
        this.reference = reference;
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public KeyedDocument getDocument() {
        return document;
    }
}
