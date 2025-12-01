package rsql.where;

import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.antlr.where.RsqlWhereParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import rsql.exceptions.SyntaxErrorException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.criteria.Path;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import static rsql.where.RsqlWhereHelper.*;

@SuppressWarnings("unchecked")
@Service
public class WhereSpecificationVisitor<T> extends RsqlWhereBaseVisitor<Specification<T>> {

    private final Logger log = LoggerFactory.getLogger(WhereSpecificationVisitor.class);

    protected RsqlContext<T> rsqlContext;

    public WhereSpecificationVisitor() {}

    private Object getInListElement(RsqlWhereParser.InListElementContext ctx) {
        if (ctx.field() != null) {
            return getPropertyPath(getFieldName(ctx.field()), rsqlContext.root);
        }
        return getInListLiteral(ctx);
    }

    /**
     * Find the path to the field
     *
     * @param fieldName Field name or path
     * @param startRoot Root to start with
     * @return Returns the path to the field
     */
    private Path<?> getPropertyPath(String fieldName, Path<?> startRoot) {
        String[] graph = fieldName.split("\\.");

        if (graph.length == 1) {
            return startRoot.get(fieldName);
        } else {
            return getPropertyPathRecursive(fieldName, startRoot);
        }
    }

    /**
     * If the field is not directly on the original class, then find the path to it.
     * Uses shared JOIN cache from rsqlContext to ensure consistency with SELECT/HAVING clauses.
     *
     * @param fieldName Field name
     * @param startRoot Current root
     * @return Property path
     */
    private Path<?> getPropertyPathRecursive(String fieldName, Path<?> startRoot) {
        String[] graph = fieldName.split("\\.");

        // try to build a property path
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> classMetadata = metamodel.managedType(startRoot.getJavaType());
        Path<?> root = startRoot;

        log.trace("getPropertyPathRecursive: field={}, joinsMapSize={}", fieldName, rsqlContext.joinsMap.size());

        // Check if we already have a cached join for this path (except last element)
        String pathKey = "";
        if (graph.length > 1) {
            pathKey = joinArrayItems(graph, graph.length - 1, ".");
            if (rsqlContext.joinsMap.containsKey(pathKey)) {
                Path<?> pathRoot = rsqlContext.joinsMap.get(pathKey);

                log.trace("  Found cached join for pathKey={}", pathKey);

                ManagedType<?> pathClassMetadata = rsqlContext.classMetadataMap.get(pathKey);
                String property = graph[graph.length - 1];
                root = pathRoot.get(property);
                if (isEmbeddedType(property, pathClassMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, pathClassMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
                return root;
            }
        }

        // Build property path, creating and caching joins as needed
        pathKey = "";
        for (String property : graph) {
            if (!hasPropertyName(property, classMetadata)) {
                throw new SyntaxErrorException(
                        "Unknown property: " + property + " from entity " + classMetadata.getJavaType().getName()
                );
            }

            if (isAssociationType(property, classMetadata)) {
                // Build path key for caching
                if (pathKey.length() > 0) {
                    pathKey = pathKey.concat(".").concat(property);
                } else {
                    pathKey = property;
                }

                // Check if join already exists in cache
                if (rsqlContext.joinsMap.containsKey(pathKey)) {
                    Path<?> cachedJoin = rsqlContext.joinsMap.get(pathKey);

                    log.trace("  Reusing cached join: pathKey={}", pathKey);

                    classMetadata = rsqlContext.classMetadataMap.get(pathKey);
                    root = cachedJoin;
                } else {
                    // Create new LEFT JOIN and cache it
                    Class<?> associationType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(associationType);

                    log.trace("  Creating LEFT JOIN for pathKey={}", pathKey);

                    root = ((From) root).join(property, JoinType.LEFT);
                    rsqlContext.joinsMap.put(pathKey, root);
                    rsqlContext.classMetadataMap.put(pathKey, classMetadata);
                }
            } else {
                log.trace("Create property path for type {} property {}.", classMetadata.getJavaType().getName(), property);
                root = root.get(property);
                if (isEmbeddedType(property, classMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
            }
        }
        return root;
    }

    /**
     * Helper method to join array elements into a string.
     */
    private String joinArrayItems(String[] graph, int len, String delimiter) {
        String key = graph[0];
        for (int i = 1; i < len; i++) {
            key = key.concat(delimiter).concat(graph[i]);
        }
        return key;
    }

    private Class<?> getFieldType(Path<?> root, String fieldName) {
        try {
            Field field = root.getJavaType().getDeclaredField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Field not found: " + fieldName, e);
        }
    }

    private Class<?> getFieldType(Path<?> pathField) {
        return pathField.getJavaType();
    }

    private Specification<T> getSpecificationForInCondition(String fieldName, RsqlWhereParser.InListContext inListContext) {
        return (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<?> pathField = getPropertyPath(fieldName, root);
            boolean isEnum = RsqlWhereHelper.isFieldEnumType(pathField);
            boolean isUuid = RsqlWhereHelper.isFieldUuidType(pathField);
            CriteriaBuilder.In<Object> in = criteriaBuilder.in(getPropertyPath(fieldName, root));
            for (int i = 0; i < inListContext.inListElement().size(); i++) {
                Object element = getInListElement(inListContext.inListElement(i));
                if (element != null) {
                    if (isEnum && element.getClass().equals(String.class)) {
                        Path<Enum> enumField = (Path<Enum>) pathField;
                        element = RsqlWhereHelper.getEnum((String) element, enumField.getJavaType());
                    } else if (isUuid && element.getClass().equals(String.class)) {
                        element = UUID.fromString((String) element);
                    }
                    in.value(element);
                }
            }

            return in;
        };
    }

    public void setSpecificationContext(RsqlContext<T> rsqlContext) {
        this.rsqlContext = rsqlContext;
    }

    @Override
    public Specification<T> visitConditionAnd(RsqlWhereParser.ConditionAndContext ctx) {
        Specification<T> left = super.visit(ctx.condition(0));
        Specification<T> right = super.visit(ctx.condition(1));
        return left.and(right);
    }

    @Override
    public Specification<T> visitConditionOr(RsqlWhereParser.ConditionOrContext ctx) {
        Specification<T> left = super.visit(ctx.condition(0));
        Specification<T> right = super.visit(ctx.condition(1));
        return left.or(right);
    }

    @Override
    public Specification<T> visitConditionParens(RsqlWhereParser.ConditionParensContext ctx) {
        return Specification.where(super.visit(ctx.condition()));
    }

    @Override
    public Specification<T> visitSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx) {
        String fieldName = getFieldName(ctx.field());

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            if (ctx.inListElement(0).STRING_LITERAL() != null && ctx.inListElement(1).STRING_LITERAL() != null) {
                String from = getStringFromStringLiteral(ctx.inListElement(0).STRING_LITERAL());
                String to = getStringFromStringLiteral(ctx.inListElement(1).STRING_LITERAL());
                Path<String> path = (Path<String>) getPropertyPath(fieldName, root);

                return criteriaBuilder.between(path, from, to);
            } else if (ctx.inListElement(0).DECIMAL_LITERAL() != null && ctx.inListElement(1).DECIMAL_LITERAL() != null) {
                Long from = Long.valueOf(ctx.inListElement(0).DECIMAL_LITERAL().getText());
                Long to = Long.valueOf(ctx.inListElement(1).DECIMAL_LITERAL().getText());
                Path<Long> path = (Path<Long>) getPropertyPath(fieldName, root);
                return criteriaBuilder.between(path, from, to);
            } else if (ctx.inListElement(0).REAL_LITERAL() != null && ctx.inListElement(1).REAL_LITERAL() != null) {
                BigDecimal from = new BigDecimal(ctx.inListElement(0).REAL_LITERAL().getText());
                BigDecimal to = new BigDecimal(ctx.inListElement(1).REAL_LITERAL().getText());
                Path<BigDecimal> path = (Path<BigDecimal>) getPropertyPath(fieldName, root);
                return criteriaBuilder.between(path, from, to);
            } else if (ctx.inListElement(0).DATE_LITERAL() != null && ctx.inListElement(1).DATE_LITERAL() != null) {
                LocalDate from = RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.inListElement(0).DATE_LITERAL());
                LocalDate to = RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.inListElement(1).DATE_LITERAL());
                Path<LocalDate> path = (Path<LocalDate>) getPropertyPath(fieldName, root);
                return criteriaBuilder.between(path, from, to);
            } else if (ctx.inListElement(0).DATETIME_LITERAL() != null && ctx.inListElement(1).DATETIME_LITERAL() != null) {
                Instant from = RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.inListElement(0).DATETIME_LITERAL());
                Instant to = RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.inListElement(1).DATETIME_LITERAL());
                Path<Instant> path = (Path<Instant>) getPropertyPath(fieldName, root);
                return criteriaBuilder.between(path, from, to);
            } else if (ctx.inListElement(0).PARAM_LITERAL() != null && ctx.inListElement(1).PARAM_LITERAL() != null) {
                String fromParam = getParamFromLiteral(ctx.inListElement(0).PARAM_LITERAL());
                String toParam = getParamFromLiteral(ctx.inListElement(1).PARAM_LITERAL());
                Path<String> path = (Path<String>) getPropertyPath(fieldName, root);
                final ParameterExpression<? extends String> parameterFrom = criteriaBuilder.parameter(path.getJavaType(), fromParam);
                final ParameterExpression<? extends String> parameterTo = criteriaBuilder.parameter(path.getJavaType(), toParam);
                return criteriaBuilder.between(path, parameterFrom, parameterTo);
            } else if (ctx.inListElement(0).field() != null && ctx.inListElement(1).field() != null) {
                String fromField = getFieldName(ctx.inListElement(0).field());
                String toField = getFieldName(ctx.inListElement(1).field());
                Path<String> path = (Path<String>) getPropertyPath(fieldName, root);
                Path<String> pathFromField = (Path<String>) getPropertyPath(fromField, root);
                Path<String> pathToField = (Path<String>) getPropertyPath(toField, root);
                return criteriaBuilder.between(path, pathFromField, pathToField);
            }

            throw new SyntaxErrorException(
                "Invalid arguments for BETWEEN clause: " + ctx.inListElement(0).getText() + "," + ctx.inListElement(1).getText()
            );
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionNotBetween(RsqlWhereParser.SingleConditionNotBetweenContext ctx) {
        String fieldName = getFieldName(ctx.field());

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            if (ctx.inListElement(0).STRING_LITERAL() != null && ctx.inListElement(1).STRING_LITERAL() != null) {
                String from = getStringFromStringLiteral(ctx.inListElement(0).STRING_LITERAL());
                String to = getStringFromStringLiteral(ctx.inListElement(1).STRING_LITERAL());
                Path<String> path = (Path<String>) getPropertyPath(fieldName, root);

                return criteriaBuilder.not(criteriaBuilder.between(path, from, to));
            } else if (ctx.inListElement(0).DECIMAL_LITERAL() != null && ctx.inListElement(1).DECIMAL_LITERAL() != null) {
                Long from = Long.valueOf(ctx.inListElement(0).DECIMAL_LITERAL().getText());
                Long to = Long.valueOf(ctx.inListElement(1).DECIMAL_LITERAL().getText());
                Path<Long> path = (Path<Long>) getPropertyPath(fieldName, root);
                return criteriaBuilder.not(criteriaBuilder.between(path, from, to));
            } else if (ctx.inListElement(0).REAL_LITERAL() != null && ctx.inListElement(1).REAL_LITERAL() != null) {
                BigDecimal from = new BigDecimal(ctx.inListElement(0).REAL_LITERAL().getText());
                BigDecimal to = new BigDecimal(ctx.inListElement(1).REAL_LITERAL().getText());
                Path<BigDecimal> path = (Path<BigDecimal>) getPropertyPath(fieldName, root);
                return criteriaBuilder.not(criteriaBuilder.between(path, from, to));
            } else if (ctx.inListElement(0).DATE_LITERAL() != null && ctx.inListElement(1).DATE_LITERAL() != null) {
                LocalDate from = RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.inListElement(0).DATE_LITERAL());
                LocalDate to = RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.inListElement(1).DATE_LITERAL());
                Path<LocalDate> path = (Path<LocalDate>) getPropertyPath(fieldName, root);
                return criteriaBuilder.not(criteriaBuilder.between(path, from, to));
            } else if (ctx.inListElement(0).DATETIME_LITERAL() != null && ctx.inListElement(1).DATETIME_LITERAL() != null) {
                Instant from = RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.inListElement(0).DATETIME_LITERAL());
                Instant to = RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.inListElement(1).DATETIME_LITERAL());
                Path<Instant> path = (Path<Instant>) getPropertyPath(fieldName, root);
                return criteriaBuilder.not(criteriaBuilder.between(path, from, to));
            } else if (ctx.inListElement(0).PARAM_LITERAL() != null && ctx.inListElement(1).PARAM_LITERAL() != null) {
                String fromParam = getParamFromLiteral(ctx.inListElement(0).PARAM_LITERAL());
                String toParam = getParamFromLiteral(ctx.inListElement(1).PARAM_LITERAL());
                Path<String> path = (Path<String>) getPropertyPath(fieldName, root);
                final ParameterExpression<? extends String> parameterFrom = criteriaBuilder.parameter(path.getJavaType(), fromParam);
                final ParameterExpression<? extends String> parameterTo = criteriaBuilder.parameter(path.getJavaType(), toParam);
                return criteriaBuilder.not(criteriaBuilder.between(path, parameterFrom, parameterTo));
            } else if (ctx.inListElement(0).field() != null && ctx.inListElement(1).field() != null) {
                String fromField = getFieldName(ctx.inListElement(0).field());
                String toField = getFieldName(ctx.inListElement(1).field());
                Path<String> path = (Path<String>) getPropertyPath(fieldName, root);
                Path<String> pathFromField = (Path<String>) getPropertyPath(fromField, root);
                Path<String> pathToField = (Path<String>) getPropertyPath(toField, root);
                return criteriaBuilder.not(criteriaBuilder.between(path, pathFromField, pathToField));
            }

            throw new SyntaxErrorException(
                    "Invalid arguments for BETWEEN clause: " + ctx.inListElement(0).getText() + "," + ctx.inListElement(1).getText()
            );
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionDate(RsqlWhereParser.SingleConditionDateContext ctx) {
        String fieldName = getFieldName(ctx.field());
        LocalDate value = RsqlWhereHelper.getLocalDateFromDateLiteral((ctx.DATE_LITERAL()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<LocalDate> path = (Path<LocalDate>) getPropertyPath(fieldName, root);
            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(path, value);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(path, value);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, value);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(path, value);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(path, value);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(path, value);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionDatetime(RsqlWhereParser.SingleConditionDatetimeContext ctx) {
        String fieldName = getFieldName(ctx.field());
        Instant value = RsqlWhereHelper.getInstantFromDatetimeLiteral((ctx.DATETIME_LITERAL()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<Instant> path = (Path<Instant>) getPropertyPath(fieldName, root);
            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(path, value);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(path, value);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, value);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(path, value);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(path, value);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(path, value);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };
        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionDecimal(RsqlWhereParser.SingleConditionDecimalContext ctx) {
        String fieldName = getFieldName(ctx.field());
        Long value = Long.valueOf((ctx.DECIMAL_LITERAL().getText()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<Long> path = (Path<Long>) getPropertyPath(fieldName, root);
            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(path, value);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(path, value);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, value);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(path, value);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(path, value);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(path, value);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };
        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionEnum(RsqlWhereParser.SingleConditionEnumContext ctx) {
        String fieldName = getFieldName(ctx.field());
        String value = getStringFromStringLiteral((ctx.ENUM_LITERAL()));
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<Enum> pathField = (Path<Enum>) getPropertyPath(fieldName, root);

            Object enumValue = RsqlWhereHelper.getEnum(value, pathField.getJavaType());
            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(pathField, enumValue);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(pathField, enumValue);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };

        return spec;
    }


    @Override
    public Specification<T> visitSingleConditionIn(RsqlWhereParser.SingleConditionInContext ctx) {
        String fieldName = getFieldName(ctx.field());

        Specification<T> spec = getSpecificationForInCondition(fieldName, ctx.inList());
        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionNotIn(RsqlWhereParser.SingleConditionNotInContext ctx) {
        String fieldName = getFieldName(ctx.field());

        Specification<T> spec = getSpecificationForInCondition(fieldName, ctx.inList());

        return Specification.not(spec);
    }

    @Override
    public Specification<T> visitSingleConditionNull(RsqlWhereParser.SingleConditionNullContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> pathField = (Path<String>) getPropertyPath(fieldName, root);

            if (operator.operatorEQ() != null) {
                return criteriaBuilder.isNull(pathField);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.isNotNull(pathField);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionTrue(RsqlWhereParser.SingleConditionTrueContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> pathField = (Path<String>) getPropertyPath(fieldName, root);

            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(pathField, true);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(pathField, true);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionFalse(RsqlWhereParser.SingleConditionFalseContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> pathField = (Path<String>) getPropertyPath(fieldName, root);

            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(pathField, false);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(pathField, false);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionOtherField(RsqlWhereParser.SingleConditionOtherFieldContext ctx) {
        String fieldName = getFieldName(ctx.field(0));
        String value = getFieldName(ctx.field(1));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> pathField1 = (Path<String>) getPropertyPath(fieldName, root);
            Path<String> pathField2 = (Path<String>) getPropertyPath(value, root);

            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(pathField1, pathField2);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(pathField1, pathField2);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(pathField1, pathField2);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(pathField1, pathField2);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(pathField1, pathField2);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(pathField1, pathField2);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };

        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionReal(RsqlWhereParser.SingleConditionRealContext ctx) {
        String fieldName = getFieldName(ctx.field());
        BigDecimal value = new BigDecimal((ctx.REAL_LITERAL().getText()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<BigDecimal> path = (Path<BigDecimal>) getPropertyPath(fieldName, root);
            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(path, value);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(path, value);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, value);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(path, value);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(path, value);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(path, value);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };
        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionString(RsqlWhereParser.SingleConditionStringContext ctx) {
        String fieldName = getFieldName(ctx.field());
        String value = getStringFromStringLiteral(ctx.STRING_LITERAL());
        RsqlWhereParser.OperatorContext operator = ctx.operator();
        Specification<T> spec = (Specification<T>) (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> path = (Path<String>) getPropertyPath(fieldName, root);

            boolean isUuid = isFieldUuidType(path);

            // If fieldType is UUID, convert the value to UUID
            if (isUuid) {
                UUID uuidValue;
                try {
                    uuidValue = UUID.fromString(value);
                } catch (IllegalArgumentException e) {
                    throw new SyntaxErrorException("Invalid UUID format: " + value);
                }

                if (operator.operatorEQ() != null) {
                    return criteriaBuilder.equal(path, uuidValue);
                } else if (operator.operatorNEQ() != null) {
                    return criteriaBuilder.notEqual(path, uuidValue);
                }
                // Add other UUID-compatible operators here if needed
                throw new SyntaxErrorException("Unknown operator for UUID: " + operator.getText());
            }

            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(path, value);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(path, value);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, value);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(path, value);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(path, value);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(path, value);
            } else if (operator.operatorLIKE() != null) {
                final String likeString = value.replace('*', '%').toLowerCase(Locale.ROOT);

                return criteriaBuilder.like(criteriaBuilder.lower(path), likeString);
                //                return criteriaBuilder.like(path, likeString);
            } else if (operator.operatorNLIKE() != null) {
                final String likeString = value.replace('*', '%').toLowerCase(Locale.ROOT);

                return criteriaBuilder.notLike(criteriaBuilder.lower(path), likeString);
                //                return criteriaBuilder.like(path, likeString);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };
        return spec;
    }

    @Override
    public Specification<T> visitSingleConditionParam(RsqlWhereParser.SingleConditionParamContext ctx) {
        String fieldName = getFieldName(ctx.field());
        String parameterName = getParamFromLiteral(ctx.PARAM_LITERAL());
        RsqlWhereParser.OperatorContext operator = ctx.operator();
        Specification<T> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> path = (Path<String>) getPropertyPath(fieldName, root);
            final ParameterExpression<? extends String> parameter = criteriaBuilder.parameter(path.getJavaType(), parameterName);
            if (operator.operatorEQ() != null) {
                return criteriaBuilder.equal(path, parameter);
            } else if (operator.operatorGT() != null) {
                return criteriaBuilder.greaterThan(path, parameter);
            } else if (operator.operatorGE() != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, parameter);
            } else if (operator.operatorLT() != null) {
                return criteriaBuilder.lessThan(path, parameter);
            } else if (operator.operatorLE() != null) {
                return criteriaBuilder.lessThanOrEqualTo(path, parameter);
            } else if (operator.operatorNEQ() != null) {
                return criteriaBuilder.notEqual(path, parameter);
            }  else if (operator.operatorLIKE() != null) {
                // final String likeString = parameter.replace('*', '%').toLowerCase(Locale.ROOT);
                // return criteriaBuilder.like(criteriaBuilder.lower(path), criteriaBuilder.lower(parameter));
                throw new SyntaxErrorException("Not supported like with parameter :" + parameterName);
            }

            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        };
        return spec;

    }
}
