// Bp:Spiral5|Entity.java|4.1
package testappl.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import testappl.domain.enumeration.AppObjectType;
import testappl.domain.enumeration.StandardRecordStatus;

/**
 * A AppObject.
 */
@Entity
@Table(name = "app_object")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppObject extends AbstractAuditingEntity<Long> implements Serializable {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type")
    private AppObjectType objectType;

    @Column(name = "last_change")
    private Instant lastChange;

    @Column(name = "seq")
    private Long seq;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StandardRecordStatus status;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;
    
    @Column(name = "uuid_field")
    private UUID uuidField;
    
    @Column(name = "is_valid")
    private Boolean isValid;
    
    @Column(name = "creation_date")
    private Instant creationDate;

    @ManyToOne
    @JsonIgnoreProperties(value = { "parent" }, allowSetters = true)
    private AppObject parent;
    
    @ManyToOne
    @JsonIgnoreProperties(value = { "productType" }, allowSetters = true)
    private Product product;

    // Constructor required for JPQL queries in tests
    public AppObject() {
        // Default constructor
    }
    
    // Constructor used in JPQL query for tests
    public AppObject(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppObject withId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public AppObject withCode(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public AppObject withName(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public AppObject withDescription(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AppObjectType getObjectType() {
        return this.objectType;
    }

    public AppObject withObjectType(AppObjectType objectType) {
        this.objectType = objectType;
        return this;
    }

    public void setObjectType(AppObjectType objectType) {
        this.objectType = objectType;
    }

    public Instant getLastChange() {
        return this.lastChange;
    }

    public AppObject withLastChange(Instant lastChange) {
        this.lastChange = lastChange;
        return this;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public Long getSeq() {
        return this.seq;
    }

    public AppObject withSeq(Long seq) {
        this.seq = seq;
        return this;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public StandardRecordStatus getStatus() {
        return this.status;
    }

    public AppObject withStatus(StandardRecordStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Double getQuantity() {
        return this.quantity;
    }

    public AppObject withQuantity(Double quantity) {
        this.quantity = quantity;
        return this;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Instant getValidFrom() {
        return this.validFrom;
    }

    public AppObject withValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }

    public AppObject withValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public UUID getUuidField() {
        return this.uuidField;
    }

    public AppObject withUuidField(UUID uuidField) {
        this.setUuidField(uuidField);
        return this;
    }

    public void setUuidField(UUID uuidField) {
        this.uuidField = uuidField;
    }

    public Boolean getIsValid() {
        return this.isValid;
    }

    public AppObject withIsValid(Boolean isValid) {
        this.setIsValid(isValid);
        return this;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Instant getCreationDate() {
        return this.creationDate;
    }

    public AppObject withCreationDate(Instant creationDate) {
        this.setCreationDate(creationDate);
        return this;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public AppObject getParent() {
        return this.parent;
    }

    public AppObject withParent(AppObject appObject) {
        this.setParent(appObject);
        return this;
    }

    public void setParent(AppObject appObject) {
        this.parent = appObject;
    }

    public Product getProduct() {
        return this.product;
    }

    public AppObject withProduct(Product product) {
        this.setProduct(product);
        return this;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppObject)) {
            return false;
        }
        return id != null && id.equals(((AppObject) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppObject{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", objectType='" + getObjectType() + "'" +
            ", lastChange='" + getLastChange() + "'" +
            ", seq=" + getSeq() +
            ", status='" + getStatus() + "'" +
            ", quantity=" + getQuantity() +
            ", validFrom='" + getValidFrom() + "'" +
            ", validUntil='" + getValidUntil() + "'" +
            ", uuidField='" + getUuidField() + "'" +
            ", isValid='" + getIsValid() + "'" +
            ", creationDate='" + getCreationDate() + "'" +
            "}";
    }
}
