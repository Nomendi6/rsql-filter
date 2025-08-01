package com.nomendi6.rsql.it.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "product_type")
public class ProductType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Builder methods
    public ProductType withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductType withCode(String code) {
        this.code = code;
        return this;
    }

    public ProductType withName(String name) {
        this.name = name;
        return this;
    }

    public ProductType withDescription(String description) {
        this.description = description;
        return this;
    }
}