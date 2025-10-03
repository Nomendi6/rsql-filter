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
 * Visitor for converting simple (non-aggregate) SELECT clauses into JPA Criteria List<Selection<?>>.
 * Does not support aggregate functions - throws SyntaxErrorException if aggregate function is encountered.
 */
public class SelectFieldSelectionVisitor extends RsqlSelectBaseVisitor<List<Selection<?>>> {

    private static final Logger log = LoggerFactory.getLogger(SelectFieldSelectionVisitor.class);

    private RsqlContext<?> rsqlContext;
    private CriteriaBuilder builder;
    private Root<?> root;
    private Map<String, Path<?>> joinsMap = new HashMap<>();
    private Map<String, ManagedType<?>> classMetadataMap = new HashMap<>();

    /**
     * Sets the context for JPA Criteria API operations.
     *
     * @param rsqlContext The RSQL context
     * @param builder The CriteriaBuilder
     * @param root The query root
     */
    public void setContext(RsqlContext<?> rsqlContext, CriteriaBuilder builder, Root<?> root) {
        this.rsqlContext = rsqlContext;
        this.builder = builder;
        this.root = root;
        this.joinsMap = new HashMap<>();
        this.classMetadataMap = new HashMap<>();
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
        // If SELECT *, return all fields from root entity
        if (ctx.star != null) {
            return getAllSelectionsFromRootEntity();
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
        String fieldPath = getFieldPath(ctx.field());
        String alias = ctx.simpleField() != null ? ctx.simpleField().getText() : null;

        // Create JPA Path using getPropertyPathRecursive
        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, joinsMap, classMetadataMap);

        // Set alias: use provided alias, or default to the last part of fieldPath (e.g., "productType.name" -> "name")
        String finalAlias = (alias != null && !alias.isEmpty()) ? alias : getLastFieldName(fieldPath);
        Selection<?> selection = path.alias(finalAlias);

        return Arrays.asList(selection);
    }

    @Override
    public List<Selection<?>> visitSeAll(RsqlSelectParser.SeAllContext ctx) {
        String entityPath = getFieldPath(ctx.field());
        return getAllSelectionsFromEntity(entityPath);
    }

    @Override
    public List<Selection<?>> visitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx) {
        throw new SyntaxErrorException(
            "Aggregate functions are not supported in simple SELECT queries. " +
            "Use SelectAggregateSelectionVisitor for aggregate queries."
        );
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
     * Gets all Selection objects from root entity.
     */
    private List<Selection<?>> getAllSelectionsFromRootEntity() {
        return getAllSelectionsFromEntity("");
    }

    /**
     * Gets all Selection objects from a specific entity.
     */
    private List<Selection<?>> getAllSelectionsFromEntity(String entityPath) {
        List<String> fieldPaths = getAllFieldPathsFromEntity(entityPath);
        List<Selection<?>> selections = new ArrayList<>();

        for (String fieldPath : fieldPaths) {
            Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, joinsMap, classMetadataMap);
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
     * This is the same logic as SimpleQueryExecutor.getPropertyPathRecursive().
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
