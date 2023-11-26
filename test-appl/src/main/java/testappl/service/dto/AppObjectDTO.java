package testappl.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import testappl.domain.enumeration.AppObjectType;
import testappl.domain.enumeration.StandardRecordStatus;

/**
 * A DTO for the {@link testappl.domain.AppObject} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppObjectDTO implements Serializable {

    private Long id;

    @NotNull
    private String code;

    @NotNull
    private String name;

    @Lob
    private String description;

    private AppObjectType objectType;

    private Instant lastChange;

    private Long seq;

    private StandardRecordStatus status;

    private Double quantity;

    private Instant validFrom;

    private Instant validUntil;

    private Boolean isValid;

    private LocalDate creationDate;

    private AppObjectDTO parent;

    private ProductDTO product;

    private ProductDTO product2;

    private ProductDTO product3;

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

    public AppObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(AppObjectType objectType) {
        this.objectType = objectType;
    }

    public Instant getLastChange() {
        return lastChange;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public StandardRecordStatus getStatus() {
        return status;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public AppObjectDTO getParent() {
        return parent;
    }

    public void setParent(AppObjectDTO parent) {
        this.parent = parent;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public ProductDTO getProduct2() {
        return product2;
    }

    public void setProduct2(ProductDTO product2) {
        this.product2 = product2;
    }

    public ProductDTO getProduct3() {
        return product3;
    }

    public void setProduct3(ProductDTO product3) {
        this.product3 = product3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppObjectDTO)) {
            return false;
        }

        AppObjectDTO appObjectDTO = (AppObjectDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, appObjectDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppObjectDTO{" +
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
            ", parent=" + getParent() +
            ", product=" + getProduct() +
            ", product2=" + getProduct2() +
            ", product3=" + getProduct3() +
            "}";
    }
}
