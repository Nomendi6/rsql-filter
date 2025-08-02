package com.nomendi6.rsql.it.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "seq")
    private Long seq;

    @Column(name = "description")
    private String description;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StandardRecordStatus status;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Product parent;

    // Constructors
    public Product() {
    }

    public Product(Long id, String code, String name, String productTypeName) {
        this.id = id;
        this.code = code;
        this.name = name;
        // productTypeName is not stored, it's just for the constructor
    }

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public StandardRecordStatus getStatus() {
        return status;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    // Builder methods
    public Product withId(Long id) {
        this.id = id;
        return this;
    }

    public Product withCode(String code) {
        this.code = code;
        return this;
    }

    public Product withName(String name) {
        this.name = name;
        return this;
    }

    public Product withDescription(String description) {
        this.description = description;
        return this;
    }

    public Product withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public Product withStatus(StandardRecordStatus status) {
        this.status = status;
        return this;
    }

    public Product withCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Product withLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public Product withProductType(ProductType productType) {
        this.productType = productType;
        return this;
    }

    public Product getParent() {
        return parent;
    }

    public void setParent(Product parent) {
        this.parent = parent;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Product withParent(Product parent) {
        this.parent = parent;
        return this;
    }

    public Product withSeq(Long seq) {
        this.seq = seq;
        return this;
    }
}