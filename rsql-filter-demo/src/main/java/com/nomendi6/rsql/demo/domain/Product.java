// Bp:Spiral5|Entity.java|4.1
package com.nomendi6.rsql.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.nomendi6.rsql.demo.domain.enumeration.StandardRecordStatus;

/**
 * A Product.
 */
@Entity
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "seq")
    private Long seq;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StandardRecordStatus status;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @ManyToOne
    private ProductType tproduct;

    @ManyToOne
    @JsonIgnoreProperties(value = { "tproduct", "parent" }, allowSetters = true)
    private Product parent;

    public Product() {
    }

    public Product(Long id, String code, String name, String tproductName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.tproduct = new ProductType().withName(tproductName);
        this.tproduct.setId(1L);
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product withId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public Product withCode(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public Product withName(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public Product withDescription(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSeq() {
        return this.seq;
    }

    public Product withSeq(Long seq) {
        this.seq = seq;
        return this;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public StandardRecordStatus getStatus() {
        return this.status;
    }

    public Product withStatus(StandardRecordStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Instant getValidFrom() {
        return this.validFrom;
    }

    public Product withValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }

    public Product withValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public ProductType getTproduct() {
        return this.tproduct;
    }

    public Product withTproduct(ProductType productType) {
        this.setTproduct(productType);
        return this;
    }

    public void setTproduct(ProductType productType) {
        this.tproduct = productType;
    }

    public Product getParent() {
        return this.parent;
    }

    public Product withParent(Product product) {
        this.setParent(product);
        return this;
    }

    public void setParent(Product product) {
        this.parent = product;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        return id != null && id.equals(((Product) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Product{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", seq=" + getSeq() +
            ", status='" + getStatus() + "'" +
            ", validFrom='" + getValidFrom() + "'" +
            ", validUntil='" + getValidUntil() + "'" +
            "}";
    }
}
