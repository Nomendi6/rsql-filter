package testappl.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import testappl.domain.enumeration.AppObjectType;
import testappl.domain.enumeration.StandardRecordStatus;

/**
 * A AppObject.
 */
@Entity
@Table(name = "app_object")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppObject implements Serializable {

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

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description")
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

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @ManyToOne
    @JsonIgnoreProperties(value = { "parent" }, allowSetters = true)
    private AppObject parent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AppObject id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public AppObject code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public AppObject name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public AppObject description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AppObjectType getObjectType() {
        return this.objectType;
    }

    public AppObject objectType(AppObjectType objectType) {
        this.setObjectType(objectType);
        return this;
    }

    public void setObjectType(AppObjectType objectType) {
        this.objectType = objectType;
    }

    public Instant getLastChange() {
        return this.lastChange;
    }

    public AppObject lastChange(Instant lastChange) {
        this.setLastChange(lastChange);
        return this;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public Long getSeq() {
        return this.seq;
    }

    public AppObject seq(Long seq) {
        this.setSeq(seq);
        return this;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public StandardRecordStatus getStatus() {
        return this.status;
    }

    public AppObject status(StandardRecordStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Double getQuantity() {
        return this.quantity;
    }

    public AppObject quantity(Double quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Instant getValidFrom() {
        return this.validFrom;
    }

    public AppObject validFrom(Instant validFrom) {
        this.setValidFrom(validFrom);
        return this;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }

    public AppObject validUntil(Instant validUntil) {
        this.setValidUntil(validUntil);
        return this;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getIsValid() {
        return this.isValid;
    }

    public AppObject isValid(Boolean isValid) {
        this.setIsValid(isValid);
        return this;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public LocalDate getCreationDate() {
        return this.creationDate;
    }

    public AppObject creationDate(LocalDate creationDate) {
        this.setCreationDate(creationDate);
        return this;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public AppObject getParent() {
        return this.parent;
    }

    public void setParent(AppObject appObject) {
        this.parent = appObject;
    }

    public AppObject parent(AppObject appObject) {
        this.setParent(appObject);
        return this;
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
            ", isValid='" + getIsValid() + "'" +
            ", creationDate='" + getCreationDate() + "'" +
            "}";
    }
}
