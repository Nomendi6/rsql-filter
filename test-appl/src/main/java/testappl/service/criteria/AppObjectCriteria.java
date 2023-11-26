package testappl.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;
import testappl.domain.enumeration.AppObjectType;
import testappl.domain.enumeration.StandardRecordStatus;

/**
 * Criteria class for the {@link testappl.domain.AppObject} entity. This class is used
 * in {@link testappl.web.rest.AppObjectResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /app-objects?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AppObjectCriteria implements Serializable, Criteria {

    /**
     * Class for filtering AppObjectType
     */
    public static class AppObjectTypeFilter extends Filter<AppObjectType> {

        public AppObjectTypeFilter() {}

        public AppObjectTypeFilter(AppObjectTypeFilter filter) {
            super(filter);
        }

        @Override
        public AppObjectTypeFilter copy() {
            return new AppObjectTypeFilter(this);
        }
    }

    /**
     * Class for filtering StandardRecordStatus
     */
    public static class StandardRecordStatusFilter extends Filter<StandardRecordStatus> {

        public StandardRecordStatusFilter() {}

        public StandardRecordStatusFilter(StandardRecordStatusFilter filter) {
            super(filter);
        }

        @Override
        public StandardRecordStatusFilter copy() {
            return new StandardRecordStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter code;

    private StringFilter name;

    private AppObjectTypeFilter objectType;

    private InstantFilter lastChange;

    private LongFilter seq;

    private StandardRecordStatusFilter status;

    private DoubleFilter quantity;

    private InstantFilter validFrom;

    private InstantFilter validUntil;

    private BooleanFilter isValid;

    private LocalDateFilter creationDate;

    private LongFilter parentId;

    private LongFilter productId;

    private LongFilter product2Id;

    private LongFilter product3Id;

    private Boolean distinct;

    public AppObjectCriteria() {}

    public AppObjectCriteria(AppObjectCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.objectType = other.objectType == null ? null : other.objectType.copy();
        this.lastChange = other.lastChange == null ? null : other.lastChange.copy();
        this.seq = other.seq == null ? null : other.seq.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.quantity = other.quantity == null ? null : other.quantity.copy();
        this.validFrom = other.validFrom == null ? null : other.validFrom.copy();
        this.validUntil = other.validUntil == null ? null : other.validUntil.copy();
        this.isValid = other.isValid == null ? null : other.isValid.copy();
        this.creationDate = other.creationDate == null ? null : other.creationDate.copy();
        this.parentId = other.parentId == null ? null : other.parentId.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.product2Id = other.product2Id == null ? null : other.product2Id.copy();
        this.product3Id = other.product3Id == null ? null : other.product3Id.copy();
        this.distinct = other.distinct;
    }

    @Override
    public AppObjectCriteria copy() {
        return new AppObjectCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getCode() {
        return code;
    }

    public StringFilter code() {
        if (code == null) {
            code = new StringFilter();
        }
        return code;
    }

    public void setCode(StringFilter code) {
        this.code = code;
    }

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public AppObjectTypeFilter getObjectType() {
        return objectType;
    }

    public AppObjectTypeFilter objectType() {
        if (objectType == null) {
            objectType = new AppObjectTypeFilter();
        }
        return objectType;
    }

    public void setObjectType(AppObjectTypeFilter objectType) {
        this.objectType = objectType;
    }

    public InstantFilter getLastChange() {
        return lastChange;
    }

    public InstantFilter lastChange() {
        if (lastChange == null) {
            lastChange = new InstantFilter();
        }
        return lastChange;
    }

    public void setLastChange(InstantFilter lastChange) {
        this.lastChange = lastChange;
    }

    public LongFilter getSeq() {
        return seq;
    }

    public LongFilter seq() {
        if (seq == null) {
            seq = new LongFilter();
        }
        return seq;
    }

    public void setSeq(LongFilter seq) {
        this.seq = seq;
    }

    public StandardRecordStatusFilter getStatus() {
        return status;
    }

    public StandardRecordStatusFilter status() {
        if (status == null) {
            status = new StandardRecordStatusFilter();
        }
        return status;
    }

    public void setStatus(StandardRecordStatusFilter status) {
        this.status = status;
    }

    public DoubleFilter getQuantity() {
        return quantity;
    }

    public DoubleFilter quantity() {
        if (quantity == null) {
            quantity = new DoubleFilter();
        }
        return quantity;
    }

    public void setQuantity(DoubleFilter quantity) {
        this.quantity = quantity;
    }

    public InstantFilter getValidFrom() {
        return validFrom;
    }

    public InstantFilter validFrom() {
        if (validFrom == null) {
            validFrom = new InstantFilter();
        }
        return validFrom;
    }

    public void setValidFrom(InstantFilter validFrom) {
        this.validFrom = validFrom;
    }

    public InstantFilter getValidUntil() {
        return validUntil;
    }

    public InstantFilter validUntil() {
        if (validUntil == null) {
            validUntil = new InstantFilter();
        }
        return validUntil;
    }

    public void setValidUntil(InstantFilter validUntil) {
        this.validUntil = validUntil;
    }

    public BooleanFilter getIsValid() {
        return isValid;
    }

    public BooleanFilter isValid() {
        if (isValid == null) {
            isValid = new BooleanFilter();
        }
        return isValid;
    }

    public void setIsValid(BooleanFilter isValid) {
        this.isValid = isValid;
    }

    public LocalDateFilter getCreationDate() {
        return creationDate;
    }

    public LocalDateFilter creationDate() {
        if (creationDate == null) {
            creationDate = new LocalDateFilter();
        }
        return creationDate;
    }

    public void setCreationDate(LocalDateFilter creationDate) {
        this.creationDate = creationDate;
    }

    public LongFilter getParentId() {
        return parentId;
    }

    public LongFilter parentId() {
        if (parentId == null) {
            parentId = new LongFilter();
        }
        return parentId;
    }

    public void setParentId(LongFilter parentId) {
        this.parentId = parentId;
    }

    public LongFilter getProductId() {
        return productId;
    }

    public LongFilter productId() {
        if (productId == null) {
            productId = new LongFilter();
        }
        return productId;
    }

    public void setProductId(LongFilter productId) {
        this.productId = productId;
    }

    public LongFilter getProduct2Id() {
        return product2Id;
    }

    public LongFilter product2Id() {
        if (product2Id == null) {
            product2Id = new LongFilter();
        }
        return product2Id;
    }

    public void setProduct2Id(LongFilter product2Id) {
        this.product2Id = product2Id;
    }

    public LongFilter getProduct3Id() {
        return product3Id;
    }

    public LongFilter product3Id() {
        if (product3Id == null) {
            product3Id = new LongFilter();
        }
        return product3Id;
    }

    public void setProduct3Id(LongFilter product3Id) {
        this.product3Id = product3Id;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AppObjectCriteria that = (AppObjectCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(name, that.name) &&
            Objects.equals(objectType, that.objectType) &&
            Objects.equals(lastChange, that.lastChange) &&
            Objects.equals(seq, that.seq) &&
            Objects.equals(status, that.status) &&
            Objects.equals(quantity, that.quantity) &&
            Objects.equals(validFrom, that.validFrom) &&
            Objects.equals(validUntil, that.validUntil) &&
            Objects.equals(isValid, that.isValid) &&
            Objects.equals(creationDate, that.creationDate) &&
            Objects.equals(parentId, that.parentId) &&
            Objects.equals(productId, that.productId) &&
            Objects.equals(product2Id, that.product2Id) &&
            Objects.equals(product3Id, that.product3Id) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            code,
            name,
            objectType,
            lastChange,
            seq,
            status,
            quantity,
            validFrom,
            validUntil,
            isValid,
            creationDate,
            parentId,
            productId,
            product2Id,
            product3Id,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppObjectCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (code != null ? "code=" + code + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (objectType != null ? "objectType=" + objectType + ", " : "") +
            (lastChange != null ? "lastChange=" + lastChange + ", " : "") +
            (seq != null ? "seq=" + seq + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (quantity != null ? "quantity=" + quantity + ", " : "") +
            (validFrom != null ? "validFrom=" + validFrom + ", " : "") +
            (validUntil != null ? "validUntil=" + validUntil + ", " : "") +
            (isValid != null ? "isValid=" + isValid + ", " : "") +
            (creationDate != null ? "creationDate=" + creationDate + ", " : "") +
            (parentId != null ? "parentId=" + parentId + ", " : "") +
            (productId != null ? "productId=" + productId + ", " : "") +
            (product2Id != null ? "product2Id=" + product2Id + ", " : "") +
            (product3Id != null ? "product3Id=" + product3Id + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
