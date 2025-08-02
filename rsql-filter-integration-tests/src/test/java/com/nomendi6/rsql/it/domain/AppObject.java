package com.nomendi6.rsql.it.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "app_object")
public class AppObject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "seq")
    private Long seq;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "uuid_field")
    private UUID uuidField;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "app_object_type")
    @Enumerated(EnumType.STRING)
    private AppObjectType appObjectType;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StandardRecordStatus status;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private AppObject parent;

    // Constructors
    public AppObject() {
    }

    public AppObject(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
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

    public AppObjectType getAppObjectType() {
        return appObjectType;
    }

    public void setAppObjectType(AppObjectType appObjectType) {
        this.appObjectType = appObjectType;
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

    public StandardRecordStatus getStatus() {
        return status;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public AppObject getParent() {
        return parent;
    }

    public void setParent(AppObject parent) {
        this.parent = parent;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public UUID getUuidField() {
        return uuidField;
    }

    public void setUuidField(UUID uuidField) {
        this.uuidField = uuidField;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // Builder methods
    public AppObject withId(Long id) {
        this.id = id;
        return this;
    }

    public AppObject withCode(String code) {
        this.code = code;
        return this;
    }

    public AppObject withName(String name) {
        this.name = name;
        return this;
    }

    public AppObject withAppObjectType(AppObjectType appObjectType) {
        this.appObjectType = appObjectType;
        return this;
    }

    public AppObject withCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public AppObject withLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public AppObject withStatus(StandardRecordStatus status) {
        this.status = status;
        return this;
    }

    public AppObject withParent(AppObject parent) {
        this.parent = parent;
        return this;
    }

    public AppObject withValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public AppObject withSeq(Long seq) {
        this.seq = seq;
        return this;
    }

    public AppObject withQuantity(Double quantity) {
        this.quantity = quantity;
        return this;
    }

    public AppObject withUuidField(UUID uuidField) {
        this.uuidField = uuidField;
        return this;
    }

    public AppObject withCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public AppObject withIsValid(Boolean isValid) {
        this.isValid = isValid;
        return this;
    }

    public AppObject withDescription(String description) {
        this.description = description;
        return this;
    }

    public AppObject withProduct(Product product) {
        this.product = product;
        return this;
    }
}