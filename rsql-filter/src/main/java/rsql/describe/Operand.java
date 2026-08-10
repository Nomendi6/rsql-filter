package rsql.describe;

/**
 * The left-hand side of a condition.
 * <p>
 * Only {@link FieldOperand} is published: every one of the fifteen {@code singleCondition} alternatives in the
 * WHERE grammar starts with {@code field}, so nothing else can be produced from a WHERE filter.
 * <p>
 * The interface is <em>not</em> sealed, and that is the point - it is a documented extension point. Describing
 * a HAVING clause will need an aggregate function and a SELECT alias on this side, and those can be added
 * later as a plain addition. Sealing it would make every such addition a breaking change: a consumer's
 * exhaustive {@code switch} stops compiling, and one compiled earlier fails with {@code MatchException}.
 * <p>
 * The concrete HAVING shapes are deliberately not published yet, because their right shape is not known: the
 * obvious {@code (AggregateFunction, String, boolean distinct)} would encode {@code COUNT DISTINCT} twice, and
 * a record's components freeze on publication.
 */
public interface Operand {

    /**
     * A field, possibly a path across relations: {@code name}, {@code productType.name}.
     *
     * @param fieldPath The path as written in the filter, dots included
     */
    record FieldOperand(String fieldPath) implements Operand {
        public FieldOperand {
            Invariants.requireNonBlank(fieldPath, "fieldPath");
        }
    }
}
