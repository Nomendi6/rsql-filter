package rsql.describe;

import java.util.List;
import java.util.Objects;

/**
 * The right-hand side of a condition. Six shapes, one per way the grammar can complete a comparison.
 * <p>
 * Not sealed, for the same reason as {@link Operand}: describing a HAVING clause will need an expression here
 * ({@code SUM(debit)=gt=SUM(credit)} compares two expressions and is implemented today), and adding it must
 * not break consumers.
 */
public interface RightSide {

    /**
     * No value at all - the whole condition is {@code field==null} or {@code field!=null}, carried by the
     * {@code IS_NULL} / {@code IS_NOT_NULL} operator.
     * <p>
     * {@code field==true} and {@code field==false} are deliberately <em>not</em> here: the library executes
     * them as an ordinary comparison ({@code criteriaBuilder.equal(path, true)}), and the HAVING grammar lists
     * {@code TRUE} and {@code FALSE} among its literals, so they are {@link SingleValue}s holding a
     * {@code Boolean}.
     */
    record NoValue() implements RightSide {
    }

    /**
     * A single value. The type is one of String, Long, BigDecimal, LocalDate, Instant or Boolean, matching
     * what the execution path produces for the corresponding literal.
     */
    record SingleValue(Object value) implements RightSide {
        public SingleValue {
            Invariants.requireAllowedType(value);
        }
    }

    /**
     * Another field, as in {@code status==ACTIVE}.
     * <p>
     * The language distinguishes three things that look alike: {@code status==ACTIVE} compares two columns,
     * {@code status=='ACTIVE'} compares with a string, and {@code status==#ACTIVE#} with an enum constant.
     * A report that printed the first as "Status is ACTIVE" would be wrong - there the cell should hold the
     * label of the other field.
     */
    record FieldRef(String fieldPath) implements RightSide {
        public FieldRef {
            Invariants.requireNonBlank(fieldPath, "fieldPath");
        }
    }

    /** A named parameter, whose value is not known at parse time. */
    record Parameter(String name) implements RightSide {
        public Parameter {
            Invariants.requireNonBlank(name, "name");
        }
    }

    /** The elements of {@code IN} / {@code NIN}; at least one, copied defensively. */
    record ValueList(List<ListItem> values) implements RightSide {
        public ValueList {
            Objects.requireNonNull(values, "values");
            values = List.copyOf(values);
            if (values.isEmpty()) {
                throw new IllegalArgumentException("an IN list has at least one element");
            }
        }
    }

    /** The two bounds of {@code BETWEEN} / {@code NOT BETWEEN}. */
    record Range(ListItem from, ListItem to) implements RightSide {
        public Range {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }
    }
}
