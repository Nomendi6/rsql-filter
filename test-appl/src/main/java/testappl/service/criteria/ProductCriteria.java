package testappl.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;
import testappl.domain.enumeration.StandardRecordStatus;

/**
 * Criteria class for the {@link testappl.domain.Product} entity. This class is used
 * in {@link testappl.web.rest.ProductResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /products?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProductCriteria implements Serializable, Criteria {

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

    private LongFilter seq;

    private StandardRecordStatusFilter status;

    private InstantFilter validFrom;

    private InstantFilter validUntil;

    private LongFilter tproductId;

    private LongFilter parentId;

    private Boolean distinct;

    public ProductCriteria() {}

    public ProductCriteria(ProductCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.seq = other.seq == null ? null : other.seq.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.validFrom = other.validFrom == null ? null : other.validFrom.copy();
        this.validUntil = other.validUntil == null ? null : other.validUntil.copy();
        this.tproductId = other.tproductId == null ? null : other.tproductId.copy();
        this.parentId = other.parentId == null ? null : other.parentId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ProductCriteria copy() {
        return new ProductCriteria(this);
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

    public LongFilter getTproductId() {
        return tproductId;
    }

    public LongFilter tproductId() {
        if (tproductId == null) {
            tproductId = new LongFilter();
        }
        return tproductId;
    }

    public void setTproductId(LongFilter tproductId) {
        this.tproductId = tproductId;
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
        final ProductCriteria that = (ProductCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(name, that.name) &&
            Objects.equals(seq, that.seq) &&
            Objects.equals(status, that.status) &&
            Objects.equals(validFrom, that.validFrom) &&
            Objects.equals(validUntil, that.validUntil) &&
            Objects.equals(tproductId, that.tproductId) &&
            Objects.equals(parentId, that.parentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, seq, status, validFrom, validUntil, tproductId, parentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProductCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (code != null ? "code=" + code + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (seq != null ? "seq=" + seq + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (validFrom != null ? "validFrom=" + validFrom + ", " : "") +
            (validUntil != null ? "validUntil=" + validUntil + ", " : "") +
            (tproductId != null ? "tproductId=" + tproductId + ", " : "") +
            (parentId != null ? "parentId=" + parentId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
