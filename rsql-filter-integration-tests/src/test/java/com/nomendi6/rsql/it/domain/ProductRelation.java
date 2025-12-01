package com.nomendi6.rsql.it.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity with multiple relations to the same table (Product).
 * Used for testing thread safety and JOIN handling with multiple relations.
 */
@Entity
@Table(name = "product_relation")
public class ProductRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "quantity", precision = 21, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StandardRecordStatus status;

    // Multiple relations to the SAME table (Product)
    @ManyToOne
    @JoinColumn(name = "product1_id")
    private Product product1;

    @ManyToOne
    @JoinColumn(name = "product2_id")
    private Product product2;

    @ManyToOne
    @JoinColumn(name = "product3_id")
    private Product product3;

    // Constructors
    public ProductRelation() {
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public StandardRecordStatus getStatus() {
        return status;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Product getProduct1() {
        return product1;
    }

    public void setProduct1(Product product1) {
        this.product1 = product1;
    }

    public Product getProduct2() {
        return product2;
    }

    public void setProduct2(Product product2) {
        this.product2 = product2;
    }

    public Product getProduct3() {
        return product3;
    }

    public void setProduct3(Product product3) {
        this.product3 = product3;
    }

    // Builder methods
    public ProductRelation withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductRelation withCode(String code) {
        this.code = code;
        return this;
    }

    public ProductRelation withName(String name) {
        this.name = name;
        return this;
    }

    public ProductRelation withDescription(String description) {
        this.description = description;
        return this;
    }

    public ProductRelation withQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public ProductRelation withStatus(StandardRecordStatus status) {
        this.status = status;
        return this;
    }

    public ProductRelation withProduct1(Product product1) {
        this.product1 = product1;
        return this;
    }

    public ProductRelation withProduct2(Product product2) {
        this.product2 = product2;
        return this;
    }

    public ProductRelation withProduct3(Product product3) {
        this.product3 = product3;
        return this;
    }

    @Override
    public String toString() {
        return "ProductRelation{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", product1=" + (product1 != null ? product1.getCode() : null) +
            ", product2=" + (product2 != null ? product2.getCode() : null) +
            ", product3=" + (product3 != null ? product3.getCode() : null) +
            '}';
    }
}
