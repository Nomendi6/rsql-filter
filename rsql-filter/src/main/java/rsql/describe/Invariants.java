package rsql.describe;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Checks shared by the model records. Package-private on purpose - not part of the published API.
 */
final class Invariants {

    /**
     * The Java types a described value may have, mirroring what the execution path produces for each literal
     * kind. All of them are immutable, so a value cannot be changed after the tree is built.
     */
    private static final Set<Class<?>> ALLOWED_VALUE_TYPES = Set.of(
        String.class, Long.class, BigDecimal.class, LocalDate.class, Instant.class, LocalDateTime.class, Boolean.class
    );

    static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    static Object requireAllowedType(Object value) {
        Objects.requireNonNull(value, "value");
        if (!ALLOWED_VALUE_TYPES.contains(value.getClass())) {
            throw new IllegalArgumentException(
                "value type not allowed: " + value.getClass().getName()
                    + " - expected one of String, Long, BigDecimal, LocalDate, Instant, LocalDateTime, Boolean"
            );
        }
        return value;
    }

    static boolean isLikeFamily(FilterOperator operator) {
        return operator == FilterOperator.LIKE
            || operator == FilterOperator.NLIKE
            || operator == FilterOperator.CLIKE
            || operator == FilterOperator.CNLIKE;
    }

    private Invariants() {
    }
}
