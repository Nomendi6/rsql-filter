package rsql.select;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsql.antlr.select.RsqlSelectBaseVisitor;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.where.RsqlContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static rsql.select.SelectHelper.getLastFieldName;
import static rsql.where.RsqlWhereHelper.*;

/**
 * Visitor for converting aggregate SELECT clauses into JPA Criteria List<Selection<?>>.
 * Supports both simple fields (GROUP BY) and aggregate functions (SUM, AVG, COUNT, etc.).
 */
public class SelectAggregateSelectionVisitor extends RsqlSelectBaseVisitor<List<Selection<?>>> {

    private static final Logger log = LoggerFactory.getLogger(SelectAggregateSelectionVisitor.class);

    private RsqlContext<?> rsqlContext;
    private CriteriaBuilder builder;
    private Root<?> root;

    /**
     * Sets the context for JPA Criteria API operations.
     * Uses shared joinsMap and classMetadataMap from RsqlContext to ensure
     * consistency across SELECT, WHERE, GROUP BY, HAVING, and ORDER BY clauses.
     *
     * @param rsqlContext The RSQL context with shared JOIN and metadata caches
     * @param builder The CriteriaBuilder
     * @param root The query root
     */
    public void setContext(RsqlContext<?> rsqlContext, CriteriaBuilder builder, Root<?> root) {
        this.rsqlContext = rsqlContext;
        this.builder = builder;
        this.root = root;
    }

    @Override
    public List<Selection<?>> visitSelect(RsqlSelectParser.SelectContext ctx) {
        List<Selection<?>> allSelections = new ArrayList<>();

        for (RsqlSelectParser.SelectElementsContext elementsCtx : ctx.selectElements()) {
            List<Selection<?>> selections = visit(elementsCtx);
            if (selections != null) {
                allSelections.addAll(selections);
            }
        }

        return allSelections;
    }

    @Override
    public List<Selection<?>> visitSelectElements(RsqlSelectParser.SelectElementsContext ctx) {
        // If SELECT *, return all fields as GROUP BY
        if (ctx.star != null) {
            return getAllSelectionsAsGroupBy("");
        }

        List<Selection<?>> selections = new ArrayList<>();
        for (RsqlSelectParser.SelectElementContext elemCtx : ctx.selectElement()) {
            List<Selection<?>> elemSelections = visit(elemCtx);
            if (elemSelections != null) {
                selections.addAll(elemSelections);
            }
        }

        return selections;
    }

    @Override
    public List<Selection<?>> visitSeField(RsqlSelectParser.SeFieldContext ctx) {
        // Plain field → GROUP BY
        String fieldPath = getFieldPath(ctx.field());
        String alias = ctx.simpleField() != null ? ctx.simpleField().getText() : null;

        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);

        // Set alias: use provided alias, or default to the last part of fieldPath (e.g., "productType.name" -> "name")
        String finalAlias = (alias != null && !alias.isEmpty()) ? alias : getLastFieldName(fieldPath);
        Selection<?> selection = path.alias(finalAlias);

        return Arrays.asList(selection);
    }

    @Override
    public List<Selection<?>> visitSeAll(RsqlSelectParser.SeAllContext ctx) {
        String entityPath = getFieldPath(ctx.field());
        return getAllSelectionsAsGroupBy(entityPath);
    }

    @Override
    public List<Selection<?>> visitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx) {
        // Aggregate function with optional alias
        List<Selection<?>> selections = visit(ctx.functionCall());

        // Apply alias if present
        if (ctx.simpleField() != null && !selections.isEmpty()) {
            String alias = ctx.simpleField().getText();
            Selection<?> selection = selections.get(0);
            selections = Arrays.asList(selection.alias(alias));
        }

        return selections;
    }

    @Override
    public List<Selection<?>> visitFuncCall(RsqlSelectParser.FuncCallContext ctx) {
        // Detect function type
        AggregateFunction function;
        if (ctx.AVG() != null) function = AggregateFunction.AVG;
        else if (ctx.MAX() != null) function = AggregateFunction.MAX;
        else if (ctx.MIN() != null) function = AggregateFunction.MIN;
        else if (ctx.SUM() != null) function = AggregateFunction.SUM;
        else if (ctx.GRP() != null) function = AggregateFunction.NONE; // GRP() → GROUP BY
        else throw new SyntaxErrorException("Unknown aggregate function");

        // Check for DIST modifier (not supported for non-COUNT functions)
        if (ctx.aggregator != null && ctx.aggregator.getText().equalsIgnoreCase("DIST")) {
            throw new SyntaxErrorException(
                "DISTINCT modifier is only supported for COUNT function, not for " +
                (ctx.AVG() != null ? "AVG" : ctx.MAX() != null ? "MAX" : ctx.MIN() != null ? "MIN" : "SUM")
            );
        }

        // Extract field path
        String fieldPath = visitFunctionArgAsString(ctx.functionArg());
        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);

        // Create Selection using CriteriaBuilder
        // Same logic as SimpleQueryExecutor.getAggregateQueryResult() lines 430-438
        Selection<?> selection = switch (function) {
            case SUM -> builder.sum((Expression<Number>) path);
            case AVG -> builder.avg((Expression<Number>) path);
            case MIN -> builder.least((Expression) path);
            case MAX -> builder.greatest((Expression) path);
            case NONE -> path; // GRP() function → GROUP BY
            default -> throw new SyntaxErrorException("Unsupported aggregate function: " + function);
        };

        return Arrays.asList(selection);
    }

    @Override
    public List<Selection<?>> visitCountAll(RsqlSelectParser.CountAllContext ctx) {
        String fieldPath;

        // COUNT(*) or COUNT(field)
        if (ctx.starArg != null) {
            // COUNT(*) - use default "id" field
            fieldPath = "id"; // Convention - could use metamodel to get PK
        } else {
            // COUNT(field)
            fieldPath = visitFunctionArgAsString(ctx.functionArg());
        }

        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
        Selection<?> selection = builder.count(path);

        return Arrays.asList(selection);
    }

    @Override
    public List<Selection<?>> visitCountDist(RsqlSelectParser.CountDistContext ctx) {
        // COUNT(DIST field1, field2, ...) → COUNT DISTINCT
        List<String> fieldPaths = visitFunctionArgsAsStrings(ctx.functionArgs());

        List<Selection<?>> selections = new ArrayList<>();
        for (String fieldPath : fieldPaths) {
            Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
            Selection<?> selection = builder.countDistinct(path);
            selections.add(selection);
        }

        return selections;
    }

    // ==================== Helper Methods ====================

    /**
     * Extracts field path from FieldContext.
     */
    private String getFieldPath(RsqlSelectParser.FieldContext ctx) {
        StringBuilder sb = new StringBuilder(ctx.ID().getText());

        for (int i = 0; i < ctx.DOT_ID().size(); i++) {
            sb.append(ctx.DOT_ID(i).getText());
        }

        return sb.toString();
    }


    /**
     * Extracts field path string from FunctionArgContext.
     */
    private String visitFunctionArgAsString(RsqlSelectParser.FunctionArgContext ctx) {
        if (ctx.field() != null) {
            return getFieldPath(ctx.field());
        } else if (ctx.functionCall() != null) {
            throw new SyntaxErrorException("Nested function calls are not currently supported");
        }
        throw new SyntaxErrorException("Invalid function argument");
    }

    /**
     * Extracts list of field path strings from FunctionArgsContext.
     */
    private List<String> visitFunctionArgsAsStrings(RsqlSelectParser.FunctionArgsContext ctx) {
        List<String> fieldPaths = new ArrayList<>();
        for (RsqlSelectParser.FunctionArgContext argCtx : ctx.functionArg()) {
            fieldPaths.add(visitFunctionArgAsString(argCtx));
        }
        return fieldPaths;
    }

    /**
     * Gets all Selection objects as GROUP BY from entity.
     */
    private List<Selection<?>> getAllSelectionsAsGroupBy(String entityPath) {
        List<String> fieldPaths = getAllFieldPathsFromEntity(entityPath);
        List<Selection<?>> selections = new ArrayList<>();

        for (String fieldPath : fieldPaths) {
            Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
            selections.add(path);
        }

        return selections;
    }

    /**
     * Gets all field paths from a specific entity.
     */
    private List<String> getAllFieldPathsFromEntity(String entityPath) {
        if (rsqlContext == null || rsqlContext.entityManager == null) {
            throw new IllegalStateException("RsqlContext with EntityManager must be set before parsing");
        }

        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> managedType;

        if (entityPath == null || entityPath.isEmpty()) {
            // Root entity
            managedType = metamodel.managedType(rsqlContext.entityClass);
        } else {
            // Navigate to nested entity
            managedType = getManagedTypeForPath(entityPath, metamodel);
        }

        // Get all non-association attributes (basic fields only)
        return managedType.getAttributes().stream()
            .filter(attr -> attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC)
            .map(attr -> {
                if (entityPath == null || entityPath.isEmpty()) {
                    return attr.getName();
                } else {
                    return entityPath + "." + attr.getName();
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets the ManagedType for a given field path.
     */
    private ManagedType<?> getManagedTypeForPath(String entityPath, Metamodel metamodel) {
        String[] parts = entityPath.split("\\.");
        ManagedType<?> currentType = metamodel.managedType(rsqlContext.entityClass);

        for (String part : parts) {
            Attribute<?, ?> attribute = currentType.getAttribute(part);

            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {

                Class<?> javaType = attribute.getJavaType();
                currentType = metamodel.managedType(javaType);
            } else {
                throw new SyntaxErrorException(
                    "Field '" + part + "' in path '" + entityPath + "' is not an entity or embeddable"
                );
            }
        }

        return currentType;
    }

    /**
     * Recursively builds JPA Path for a field, creating LEFT JOINs as needed.
     * Same logic as SimpleQueryExecutor.getPropertyPathRecursive().
     */
    private <ENTITY> Path<?> getPropertyPathRecursive(
        String fieldName,
        Path<?> startRoot,
        RsqlContext<ENTITY> rsqlContext,
        Map<String, Path<?>> joinsMap,
        Map<String, ManagedType<?>> classMetadataMap
    ) {
        String[] graph = fieldName.split("\\.");
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> classMetadata = metamodel.managedType(startRoot.getJavaType());
        Path<?> root = startRoot;

        // Check if we already have a join for the path (except last element)
        String pathKey = "";
        if (graph.length > 1) {
            pathKey = joinArrayItems(graph, graph.length - 1, ".");
            if (joinsMap.containsKey(pathKey)) {
                Path<?> pathRoot = joinsMap.get(pathKey);
                ManagedType<?> pathClassMetadata = classMetadataMap.get(pathKey);
                String property = graph[graph.length - 1];
                root = pathRoot.get(property);
                if (isEmbeddedType(property, pathClassMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, pathClassMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
                return root;
            }
        }

        // Build the property path, creating joins as needed
        pathKey = "";
        for (String property : graph) {
            if (!hasPropertyName(property, classMetadata)) {
                throw new IllegalArgumentException(
                    "Unknown property: " + property + " from entity " + classMetadata.getJavaType().getName()
                );
            }

            if (isAssociationType(property, classMetadata)) {
                // Build path key for join caching
                if (pathKey.length() > 0) {
                    pathKey = pathKey.concat(".").concat(property);
                } else {
                    pathKey = property;
                }

                // Check if join already exists
                if (joinsMap.containsKey(pathKey)) {
                    classMetadata = classMetadataMap.get(pathKey);
                    root = joinsMap.get(pathKey);
                } else {
                    // Create new LEFT JOIN
                    Class<?> associationType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(associationType);

                    root = ((From) root).join(property, JoinType.LEFT);
                    joinsMap.put(pathKey, root);
                    classMetadataMap.put(pathKey, classMetadata);

                    log.trace("Created LEFT JOIN for: {}", pathKey);
                }
            } else {
                // Simple property access
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
     * Joins array elements into a string with delimiter.
     */
    private String joinArrayItems(String[] graph, int len, String delimiter) {
        String key = graph[0];
        for (int i = 1; i < len; i++) {
            key = key.concat(delimiter).concat(graph[i]);
        }
        return key;
    }
}
