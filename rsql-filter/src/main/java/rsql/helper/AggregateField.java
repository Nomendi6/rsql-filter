package rsql.helper;

public class AggregateField {
    public enum AggregateFunction {
        SUM, AVG, COUNT, MIN, MAX, COUNT_DISTINCT, NONE
    }

    private final String fieldPath;
    private final AggregateFunction function;
    private final String alias;

    public AggregateField(String fieldPath, AggregateFunction function, String alias) {
        this.fieldPath = fieldPath;
        this.function = function;
        this.alias = alias;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public AggregateFunction getFunction() {
        return function;
    }

    public String getAlias() {
        return alias;
    }

    public static AggregateField of(String fieldPath, AggregateFunction function, String alias) {
        return new AggregateField(fieldPath, function, alias);
    }

    public static AggregateField groupBy(String fieldPath, String alias) {
        return new AggregateField(fieldPath, AggregateFunction.NONE, alias);
    }
}
