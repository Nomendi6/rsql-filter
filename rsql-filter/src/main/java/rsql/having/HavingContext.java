package rsql.having;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import rsql.where.RsqlContext;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context for HAVING clause that maintains mapping between SELECT aliases and their corresponding JPA Expression objects.
 * This class builds Expression objects from SELECT AggregateFields and allows HAVING visitor to retrieve them by alias.
 * Uses shared JOIN and metadata caches from RsqlContext to ensure consistency with SELECT and WHERE clauses.
 */
public class HavingContext<ENTITY> {

    private final Map<String, Expression<?>> aliasToExpression;
    private final CriteriaBuilder builder;
    private final Root<ENTITY> root;
    private final RsqlContext<ENTITY> rsqlContext;

    /**
     * Constructs a HavingContext from SELECT aggregate fields.
     * Uses shared joinsMap and classMetadataMap from RsqlContext to ensure consistency
     * with SELECT, WHERE, and other clauses.
     *
     * @param builder CriteriaBuilder for creating expressions
     * @param root Query root
     * @param selectFields List of AggregateFields from SELECT clause
     * @param rsqlContext RSQL context with EntityManager and shared JOIN/metadata caches
     */
    public HavingContext(
        CriteriaBuilder builder,
        Root<ENTITY> root,
        List<AggregateField> selectFields,
        RsqlContext<ENTITY> rsqlContext
    ) {
        this.builder = builder;
        this.root = root;
        this.rsqlContext = rsqlContext;
        this.aliasToExpression = buildAliasMapping(selectFields);
    }

    /**
     * Retrieves the Expression object for a given alias.
     *
     * @param alias The alias from SELECT clause
     * @return Expression object mapped to this alias, or null if not found
     */
    public Expression<?> getExpressionForAlias(String alias) {
        return aliasToExpression.get(alias);
    }

    /**
     * Checks if an alias exists in the mapping.
     *
     * @param alias The alias to check
     * @return true if alias exists, false otherwise
     */
    public boolean hasAlias(String alias) {
        return aliasToExpression.containsKey(alias);
    }

    /**
     * Gets all available aliases.
     *
     * @return Set of all available aliases
     */
    public Set<String> getAvailableAliases() {
        return aliasToExpression.keySet();
    }

    /**
     * Gets the CriteriaBuilder.
     *
     * @return CriteriaBuilder
     */
    public CriteriaBuilder getBuilder() {
        return builder;
    }

    /**
     * Gets the query root.
     *
     * @return Root
     */
    public Root<ENTITY> getRoot() {
        return root;
    }

    /**
     * Gets the shared JOIN cache from RsqlContext.
     *
     * @return JOIN cache map
     */
    public Map<String, Path<?>> getJoinsMap() {
        return rsqlContext.joinsMap;
    }

    /**
     * Gets the shared metadata cache from RsqlContext.
     *
     * @return Metadata cache map
     */
    public Map<String, ManagedType<?>> getClassMetadataMap() {
        return rsqlContext.classMetadataMap;
    }

    /**
     * Gets the RSQL context.
     *
     * @return RSQL context
     */
    public RsqlContext<ENTITY> getRsqlContext() {
        return rsqlContext;
    }

    /**
     * Builds the alias-to-expression mapping from SELECT aggregate fields.
     * This method replicates the logic from SimpleQueryExecutor.getAggregateQueryResult()
     * to create Expression objects for each aliased field.
     *
     * @param selectFields List of AggregateFields from SELECT clause
     * @return Map of alias -> Expression
     */
    private Map<String, Expression<?>> buildAliasMapping(List<AggregateField> selectFields) {
        Map<String, Expression<?>> mapping = new HashMap<>();

        for (AggregateField field : selectFields) {
            // Skip fields without alias - they cannot be referenced in HAVING by name
            if (field.getAlias() == null || field.getAlias().isEmpty()) {
                continue;
            }

            // Create Path for fieldPath (same logic as SimpleQueryExecutor)
            // Uses shared joinsMap and classMetadataMap from rsqlContext
            Path<?> path = getPropertyPathRecursive(
                field.getFieldPath(),
                root,
                rsqlContext,
                rsqlContext.joinsMap,
                rsqlContext.classMetadataMap
            );

            // Create Expression depending on aggregate function (same logic as SimpleQueryExecutor)
            Expression<?> expression = switch (field.getFunction()) {
                case SUM -> builder.sum((Expression<Number>) path);
                case AVG -> builder.avg((Expression<Number>) path);
                case COUNT -> builder.count(path);
                case COUNT_DISTINCT -> builder.countDistinct(path);
                case MIN -> builder.least((Expression) path);
                case MAX -> builder.greatest((Expression) path);
                case NONE -> path;  // GROUP BY field without aggregation
            };

            // Map alias to expression
            mapping.put(field.getAlias(), expression);
        }

        return mapping;
    }

    /**
     * Recursively resolves a property path to a JPA Path object, creating LEFT JOINs as needed.
     * This is a copy of SimpleQueryExecutor.getPropertyPathRecursive() to ensure same behavior.
     *
     * @param fieldName Dot-separated field path (e.g., "productType.name")
     * @param startRoot Starting root path
     * @param rsqlContext RSQL context
     * @param joinsMap JOIN cache
     * @param classMetadataMap Metadata cache
     * @return Path object for the field
     */
    private static <ENTITY> Path<?> getPropertyPathRecursive(
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

        // try to build a property path
        pathKey = "";
        for (String property : graph) {
            if (!hasPropertyName(property, classMetadata)) {
                throw new IllegalArgumentException(
                    "Unknown property: " + property + " from entity " + classMetadata.getJavaType().getName()
                );
            }

            if (isAssociationType(property, classMetadata)) {
                if (pathKey.length() > 0) {
                    pathKey = pathKey.concat(".").concat(property);
                } else {
                    pathKey = property;
                }
                if (joinsMap.containsKey(pathKey)) {
                    classMetadata = classMetadataMap.get(pathKey);
                    root = joinsMap.get(pathKey);
                } else {
                    Class<?> associationType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(associationType);

                    root = ((From) root).join(property, JoinType.LEFT);
                    joinsMap.put(pathKey, root);
                    classMetadataMap.put(pathKey, classMetadata);
                }
            } else {
                root = root.get(property);
                if (isEmbeddedType(property, classMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
            }
        }
        return root;
    }

    private static String joinArrayItems(String[] graph, int len, String delimiter) {
        String key = graph[0];
        for (int i = 1; i < len; i++) {
            key = key.concat(delimiter).concat(graph[i]);
        }
        return key;
    }

    private static boolean hasPropertyName(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .anyMatch(attr -> attr.getName().equals(property));
    }

    private static boolean isAssociationType(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .filter(attr -> attr.getName().equals(property))
            .anyMatch(attr -> attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE
                || attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_MANY
                || attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_MANY);
    }

    private static boolean isEmbeddedType(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .filter(attr -> attr.getName().equals(property))
            .anyMatch(attr -> attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED);
    }

    private static Class<?> findPropertyType(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .filter(attr -> attr.getName().equals(property))
            .findFirst()
            .map(attr -> attr.getJavaType())
            .orElseThrow(() -> new IllegalArgumentException("Property not found: " + property));
    }
}
