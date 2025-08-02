// Bp:spiral5|EntityDTO.java|3.0
package com.nomendi6.rsql.demo.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import com.nomendi6.rsql.demo.domain.enumeration.AppObjectType;
import com.nomendi6.rsql.demo.domain.enumeration.StandardRecordStatus;

/**
 * A DTO for the {@link com.nomendi6.rsql.demo.domain.AppObject} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppObjectDTO extends AbstractAuditingDTO implements Serializable {

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

    private AppObjectDTO parent;

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

    public AppObjectDTO getParent() {
        return parent;
    }

    public void setParent(AppObjectDTO parent) {
        this.parent = parent;
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
            ", parent=" + getParent() +
            "}";
    }
}
