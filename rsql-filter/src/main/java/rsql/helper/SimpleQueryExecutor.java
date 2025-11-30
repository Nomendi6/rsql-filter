package rsql.helper;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.data.support.PageableExecutionUtils;
import rsql.RsqlCompiler;
import rsql.having.HavingCompiler;
import rsql.having.HavingContext;
import rsql.select.SelectAggregateSelectionVisitor;
import rsql.select.SelectAggregateVisitor;
import rsql.select.SelectFieldSelectionVisitor;
import rsql.select.SelectTreeParser;
import rsql.where.RsqlContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.Assert;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import rsql.where.RsqlQuery;

import java.util.*;

import static rsql.where.RsqlWhereHelper.*;

public class SimpleQueryExecutor {

    private static <ENTITY> Specification<ENTITY> createSpecification(
        String filter,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        return compiler.compileToSpecification(filter, rsqlContext);
    }

    private static <ENTITY> RsqlQuery createWhereClause(
        String filter,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        return compiler.compileToRsqlQuery(filter, rsqlContext);
    }

    /**
     * Creates JPA Criteria Selections from a SELECT string (non-aggregate queries).
     * This method is analogous to createSpecification() but for SELECT clauses.
     *
     * <p>Example usage:</p>
     * <pre>
     * List&lt;Selection&lt;?&gt;&gt; selections = SimpleQueryExecutor.createSelectionsFromString(
     *     "code, name, productType.name:typeName",
     *     builder, root, rsqlContext
     * );
     * query.multiselect(selections);
     * </pre>
     *
     * @param selectString SELECT clause string (e.g., "code, name, productType.name:typeName")
     * @param builder CriteriaBuilder for creating selections
     * @param root Query root
     * @param rsqlContext RSQL context with EntityManager
     * @param <ENTITY> Entity type
     * @return List of Selection<?> for use with CriteriaQuery.multiselect()
     * @throws rsql.exceptions.SyntaxErrorException if SELECT string is invalid or contains aggregate functions
     */
    public static <ENTITY> List<Selection<?>> createSelectionsFromString(
        String selectString,
        CriteriaBuilder builder,
        Root<ENTITY> root,
        RsqlContext<ENTITY> rsqlContext
    ) {
        if (selectString == null || selectString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Parse SELECT string and create Selections using SelectFieldSelectionVisitor
        SelectTreeParser selectParser = new SelectTreeParser();
        ParseTree tree = selectParser.parseStream(CharStreams.fromString(selectString));

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, builder, root);
        List<Selection<?>> selectionList = visitor.visit(tree);

        return selectionList != null ? selectionList : new ArrayList<>();
    }

    /**
     * Creates an aggregate query builder from a SELECT string containing aggregate functions.
     * This method is analogous to createSpecification() but for aggregate queries.
     *
     * <p>Returns an AggregateQueryBuilder containing:</p>
     * <ul>
     *   <li>SELECT selections (aggregate functions + grouping fields)</li>
     *   <li>GROUP BY expressions</li>
     *   <li>Internal state for creating HAVING predicates</li>
     * </ul>
     *
     * <p>Example usage:</p>
     * <pre>
     * AggregateQueryBuilder&lt;Product&gt; aggQuery = SimpleQueryExecutor.createAggregateQuery(
     *     "productType.name:category, COUNT(*):count, SUM(price):total",
     *     builder, root, rsqlContext
     * );
     *
     * query.multiselect(aggQuery.getSelections());
     * query.groupBy(aggQuery.getGroupByExpressions());
     * query.having(aggQuery.createHavingPredicate("total=gt=50000;count=ge=10", compiler));
     * </pre>
     *
     * @param selectString Aggregate SELECT clause string (e.g., "category, COUNT(*):count, SUM(price):total")
     * @param builder CriteriaBuilder for creating selections and expressions
     * @param root Query root
     * @param rsqlContext RSQL context with EntityManager
     * @param <ENTITY> Entity type
     * @return AggregateQueryBuilder with selections, GROUP BY expressions, and HAVING state
     * @throws rsql.exceptions.SyntaxErrorException if SELECT string is invalid
     */
    public static <ENTITY> AggregateQueryBuilder<ENTITY> createAggregateQuery(
        String selectString,
        CriteriaBuilder builder,
        Root<ENTITY> root,
        RsqlContext<ENTITY> rsqlContext
    ) {
        if (selectString == null || selectString.trim().isEmpty()) {
            return new AggregateQueryBuilder<>(
                new ArrayList<>(),
                new ArrayList<>(),
                builder,
                root,
                new ArrayList<>(),
                rsqlContext,
                new ArrayList<>()
            );
        }

        // Parse SELECT string to extract aggregate field metadata
        SelectTreeParser selectParser = new SelectTreeParser();
        ParseTree tree = selectParser.parseStream(CharStreams.fromString(selectString));

        // Extract metadata about aggregate fields (for HAVING clause)
        SelectAggregateVisitor aggregateVisitor = new SelectAggregateVisitor();
        aggregateVisitor.setContext(rsqlContext);
        List<AggregateField> selectFields = aggregateVisitor.visit(tree);

        if (selectFields == null) {
            selectFields = new ArrayList<>();
        }

        // Build SELECT selections using visitor
        // The visitor uses shared joinsMap and classMetadataMap from rsqlContext
        // to ensure consistency with WHERE, GROUP BY, and HAVING clauses
        SelectAggregateSelectionVisitor selectionVisitor = new SelectAggregateSelectionVisitor();
        selectionVisitor.setContext(rsqlContext, builder, root);
        List<Selection<?>> selectionList = selectionVisitor.visit(tree);

        if (selectionList == null) {
            selectionList = new ArrayList<>();
        }

        // Extract GROUP BY field names (fields without aggregate functions)
        List<String> groupByFieldNames = new ArrayList<>();
        for (AggregateField field : selectFields) {
            if (field.getFunction() == null ||
                field.getFunction() == AggregateField.AggregateFunction.NONE) {
                groupByFieldNames.add(field.getFieldPath());
            }
        }

        // Build GROUP BY expressions (using shared joinsMap from rsqlContext for consistency)
        List<Expression<?>> groupByExpressions = new ArrayList<>();
        for (String groupByField : groupByFieldNames) {
            Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
            groupByExpressions.add(groupByPath);
        }

        // Return builder with all components
        // Note: joinsMap and classMetadataMap are now managed by rsqlContext
        return new AggregateQueryBuilder<>(
            selectionList,
            groupByExpressions,
            builder,
            root,
            selectFields,
            rsqlContext,
            groupByFieldNames
        );
    }

    /**
     * Executes a query with SELECT properties array.
     * This method delegates to getQueryResultWithSelect for better support of navigation properties.
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param properties Array of property names (e.g., {"code", "name", "price"})
     * @param filter RSQL filter string
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @return List of query results
     */
    public static <ENTITY, RESULT> List<RESULT> getQueryResult(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String[] properties,
        String filter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        // Convert String[] to SELECT string (comma-separated list)
        String selectString = String.join(", ", properties);

        // Delegate to new method that supports navigation properties
        return getQueryResultWithSelect(entityClass, resultClass, selectString, filter, pageable, rsqlContext, compiler);
    }

    /**
     * Executes a query with SELECT string parsing, supporting aliases and navigation properties.
     * Uses SelectFieldSelectionVisitor for creating JPA Criteria Selections.
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string (e.g., "code, name:productName, productType.name")
     * @param filter RSQL filter string
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @return List of query results
     */
    public static <ENTITY, RESULT> List<RESULT> getQueryResultWithSelect(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        Sort sort;
        if (pageable == null) {
            sort = null;
        } else {
            sort = pageable.getSort();
        }

        // FIRST: Create CriteriaQuery and Root
        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);

        // Preserve the existing root alias (set by RsqlQueryService or user's custom JPQL)
        String rootAlias = rsqlContext.root.getAlias();
        if (rootAlias == null) {
            rootAlias = "a0";  // Fallback to default alias
        }

        Root<ENTITY> root = query.from(entityClass);
        root.alias(rootAlias);  // Set the preserved alias on the new root

        // SECOND: Update rsqlContext with new root and clear JOIN caches
        // This ensures that WHERE and SELECT use the SAME root and share JOINs
        rsqlContext.criteriaBuilder = builder;
        rsqlContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) query;
        rsqlContext.root = root;
        rsqlContext.joinsMap.clear();
        rsqlContext.classMetadataMap.clear();

        // THIRD: Create WHERE specification (uses shared joinsMap from rsqlContext)
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        // FOURTH: Create WHERE predicate FIRST (this creates JOINs in joinsMap)
        // This ensures that JOINs created by WHERE clause are registered first,
        // allowing SELECT clause to reuse them instead of creating duplicates
        Predicate predicate = null;
        if (specification != null) {
            predicate = specification.toPredicate(root, query, builder);
        }

        // FIFTH: Create SELECT selections (reuses JOINs from WHERE clause via joinsMap)
        List<Selection<?>> selectionList = createSelectionsFromString(
            selectString, builder, root, rsqlContext
        );

        query.multiselect(selectionList);

        // Apply WHERE clause
        if (predicate != null) {
            query.where(predicate);
        }
        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }
        return rsqlContext.entityManager.createQuery(query).getResultList();
    }

    /**
     * Executes a paginated query with SELECT string parsing, supporting aliases and navigation properties.
     * Uses SelectFieldSelectionVisitor for creating JPA Criteria Selections.
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string (e.g., "code, name:productName, productType.name")
     * @param filter RSQL filter string
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @param repository JPA repository for count operations
     * @return Page of query results
     */
    public static <
        ENTITY, RESULT, REPOS extends JpaRepository<ENTITY, Long> & JpaSpecificationExecutor<ENTITY>
    > Page<RESULT> getQueryResultAsPageWithSelect(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler,
        REPOS repository
    ) {
        Sort sort;
        if (pageable == null) {
            sort = null;
        } else {
            sort = pageable.getSort();
        }

        // FIRST: Create CriteriaQuery and Root
        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);

        // Preserve the existing root alias (set by RsqlQueryService or user's custom JPQL)
        String rootAlias = rsqlContext.root.getAlias();
        if (rootAlias == null) {
            rootAlias = "a0";  // Fallback to default alias
        }

        Root<ENTITY> root = query.from(entityClass);
        root.alias(rootAlias);  // Set the preserved alias on the new root

        // SECOND: Update rsqlContext with new root and clear JOIN caches
        // This ensures that WHERE and SELECT use the SAME root and share JOINs
        rsqlContext.criteriaBuilder = builder;
        rsqlContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) query;
        rsqlContext.root = root;
        rsqlContext.joinsMap.clear();
        rsqlContext.classMetadataMap.clear();

        // THIRD: Create WHERE specification (uses shared joinsMap from rsqlContext)
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        // FOURTH: Create WHERE predicate FIRST (this creates JOINs in joinsMap)
        // This ensures that JOINs created by WHERE clause are registered first,
        // allowing SELECT clause to reuse them instead of creating duplicates
        Predicate predicate = null;
        if (specification != null) {
            predicate = specification.toPredicate(root, query, builder);
        }

        // FIFTH: Create SELECT selections (reuses JOINs from WHERE clause via joinsMap)
        List<Selection<?>> selectionList = createSelectionsFromString(
            selectString, builder, root, rsqlContext
        );

        query.multiselect(selectionList);

        // Apply WHERE clause
        if (predicate != null) {
            query.where(predicate);
        }

        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        if (isUnpaged(pageable)) {
            return new PageImpl<>((List<RESULT>) rsqlContext.entityManager.createQuery(query).getResultList());
        }

        // Calculate total count
        // We create our own count query instead of using repository.count(specification)
        // because the Specification's JOINs are tied to the main query's Root and cannot be reused
        long totalRecords;
        if (specification == null) {
            totalRecords = repository.count();
        } else {
            // Create a new count query with fresh RsqlContext to avoid JOIN conflicts
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<ENTITY> countRoot = countQuery.from(entityClass);
            countQuery.select(builder.count(countRoot));

            // Create a new RsqlContext for count query to avoid reusing cached joins from main query
            // The main query's joins are tied to a different Root and cannot be reused here
            RsqlContext<ENTITY> countContext = new RsqlContext<>(rsqlContext.entityClass);
            countContext.entityManager = rsqlContext.entityManager;
            countContext.criteriaBuilder = builder;
            countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) countQuery;
            countContext.root = countRoot;
            countContext.joinsMap = new HashMap<>();
            countContext.classMetadataMap = new HashMap<>();

            // Create a new specification with the count context (this will create its own joins)
            Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
            if (countSpecification != null) {
                Predicate countPredicate = countSpecification.toPredicate(countRoot, countQuery, builder);
                countQuery.where(countPredicate);
            }

            totalRecords = rsqlContext.entityManager.createQuery(countQuery).getSingleResult();
        }

        return readPage(query, pageable, predicate, rsqlContext, entityClass, totalRecords);
    }

    public static <ENTITY, RESULT> List<RESULT> getJpqlQueryResult(
            Class<ENTITY> entityClass,
            Class<RESULT> resultClass,
            String jpqlQueryString,
            String alias, String filter,
            Pageable pageable,
            RsqlContext<ENTITY> rsqlContext,
            RsqlCompiler<ENTITY> compiler) {
        Sort sort;
        if (pageable == null) {
            sort = null;
        } else {
            sort = pageable.getSort();
        }
        RsqlQuery rsqlQuery = createWhereClause(filter, rsqlContext, compiler);

        if (rsqlQuery != null) {
            jpqlQueryString = jpqlQueryString.concat(" where ").concat(rsqlQuery.where);
        }

        jpqlQueryString = jpqlQueryString.concat(getOrderByWithAlias(sort, alias));
        TypedQuery<RESULT> query = rsqlContext.entityManager.createQuery(jpqlQueryString, resultClass);
        if (rsqlQuery != null) {
            RsqlCompiler.bindImplicitParametersForTypedQuery(rsqlQuery, query);
        }

        return query.getResultList();
    }

    public static String getOrderByWithAlias(Sort sort, String alias) {
        if (sort == null || !sort.isSorted()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" order by ");
        Iterator<Sort.Order> iterator = sort.iterator();
        while (iterator.hasNext()) {
            Sort.Order order = iterator.next();
            sb.append(alias).append(".").append(order.getProperty()).append(" ").append(order.getDirection().name());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static <ENTITY, RESULT> Page<RESULT> getJpqlQueryResultAsPage(
            Class<ENTITY> entityClass,
            Class<RESULT> resultClass,
            String jpqlQueryString, String selectAlias,
            String countQueryString, String countAlias,
            String filter,
            Pageable pageable,
            RsqlContext<ENTITY> rsqlContext,
            RsqlCompiler<ENTITY> compiler
    ) {
        Sort sort;
        if (pageable == null) {
            sort = null;
        } else {
            sort = pageable.getSort();
        }
        RsqlQuery rsqlQuery = createWhereClause(filter, rsqlContext, compiler);

        if (rsqlQuery != null) {
            jpqlQueryString = jpqlQueryString.concat(" where ").concat(rsqlQuery.where);
            countQueryString = countQueryString.concat(" where ").concat(rsqlQuery.where);
        }

        jpqlQueryString = jpqlQueryString.concat(getOrderByWithAlias(sort, selectAlias));

        TypedQuery<RESULT> query = null;
        TypedQuery<Long> countQuery = null;
        try {
            query = rsqlContext.entityManager.createQuery(jpqlQueryString, resultClass);
            countQuery = rsqlContext.entityManager.createQuery(countQueryString, Long.class);
        } catch (Exception e) {
            System.out.println("Error compiling JPQL expression: ");
            System.out.println("------ SELECT QUERY ------");
            System.out.println(jpqlQueryString);
            System.out.println("------ COUNT QUERY ------");
            System.out.println(countQueryString);
            System.out.println("------");
            throw new RuntimeException(e);
        }
        if (rsqlQuery != null) {
            RsqlCompiler.bindImplicitParametersForTypedQuery(rsqlQuery, query);
            RsqlCompiler.bindImplicitParametersForTypedQuery(rsqlQuery, countQuery);
        }

        if (isUnpaged(pageable)) {
            return new PageImpl<>(query.getResultList());
        }

        Long totalRecords = countQuery.getSingleResult();

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return PageableExecutionUtils.getPage(
                query.getResultList(),
                pageable,
                () -> {
                    return totalRecords;
                }
        );

    }

    public static <ENTITY> Long getJpqlQueryCount(
            Class<ENTITY> entityClass,
            String countQueryString,
            String countAlias,
            String filter,
            RsqlContext<ENTITY> rsqlContext,
            RsqlCompiler<ENTITY> compiler
    ) {
        RsqlQuery rsqlQuery = createWhereClause(filter, rsqlContext, compiler);

        if (rsqlQuery != null) {
            if (!Objects.equals(countAlias, "a0")) {
                RsqlCompiler.replaceAlias(rsqlQuery, "a0", countAlias);
            }
            countQueryString = countQueryString.concat(" where ").concat(rsqlQuery.where);
        }
        TypedQuery<Long> countQuery = rsqlContext.entityManager.createQuery(countQueryString, Long.class);
        if (rsqlQuery != null) {
            RsqlCompiler.bindImplicitParametersForTypedQuery(rsqlQuery, countQuery);
        }

        return countQuery.getSingleResult();
    }


    /**
     * Executes a paginated query with SELECT properties array.
     * This method delegates to getQueryResultAsPageWithSelect for better support of navigation properties.
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param properties Array of property names (e.g., {"code", "name", "price"})
     * @param filter RSQL filter string
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @param repository JPA repository for count operations
     * @return Page of query results
     */
    public static <
        ENTITY, RESULT, REPOS extends JpaRepository<ENTITY, Long> & JpaSpecificationExecutor<ENTITY>
    > Page<RESULT> getQueryResultAsPage(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String[] properties,
        String filter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler,
        REPOS repository
    ) {
        // Convert String[] to SELECT string (comma-separated list)
        String selectString = String.join(", ", properties);

        // Delegate to new method that supports navigation properties
        return getQueryResultAsPageWithSelect(entityClass, resultClass, selectString, filter, pageable, rsqlContext, compiler, repository);
    }

    private static boolean isUnpaged(Pageable pageable) {
        if (pageable == null) {
            return true;
        }
        return pageable.isUnpaged();
    }

    private static <RESULT, ENTITY> Page<RESULT> readPage(
        CriteriaQuery<RESULT> query,
        Pageable pageable,
        Predicate predicate,
        RsqlContext<ENTITY> rsqlContext,
        Class<ENTITY> entityClass,
        long totalRecords
    ) {
        final TypedQuery<RESULT> typedQuery = rsqlContext.entityManager.createQuery(query);
        if (pageable.isPaged()) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }
        return PageableExecutionUtils.getPage(
            typedQuery.getResultList(),
            pageable,
            () -> {
                return totalRecords;
            }
        );
    }

    private static <ENTITY> TypedQuery<Long> getCountQuery(Predicate predicate, Class<ENTITY> entityClass, EntityManager entityManager) {
        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(entityClass)));
        cq.where(predicate);
        return entityManager.createQuery(cq);
    }

    private static long executeCountQuery(TypedQuery<Long> query) {
        Assert.notNull(query, "TypedQuery must not be null!");
        List<Long> totals = query.getResultList();
        long total = 0L;

        Long element;
        for (Iterator<Long> var4 = totals.iterator(); var4.hasNext(); total += element == null ? 0L : element) {
            element = var4.next();
        }

        return total;
    }

    public static <ENTITY> Path<?> getPropertyPathRecursive(
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
                    String previousClass = classMetadata.getJavaType().getName();
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

    /**
     * Executes an aggregate query with SELECT string parsing, supporting aliases, aggregate functions, GROUP BY, and HAVING.
     * Uses SelectAggregateVisitor to parse the SELECT string and delegates to getAggregateQueryResult() for execution.
     * Automatically extracts GROUP BY fields from SELECT string (fields without aggregate functions).
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string with aggregate functions (e.g., "productType.name, COUNT(*):count, SUM(price):total")
     * @param filter RSQL filter string for WHERE clause (applied before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (applied after aggregation)
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @return List of aggregate query results
     */
    public static <ENTITY, RESULT> List<RESULT> getAggregateQueryResultWithSelect(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        // Parse SELECT string to extract aggregate fields and GROUP BY fields
        SelectTreeParser selectParser = new SelectTreeParser();
        ParseTree tree = selectParser.parseStream(CharStreams.fromString(selectString));

        SelectAggregateVisitor aggregateVisitor = new SelectAggregateVisitor();
        aggregateVisitor.setContext(rsqlContext);
        List<AggregateField> selectFields = aggregateVisitor.visit(tree);

        // Extract GROUP BY fields (fields without aggregate functions)
        List<String> groupByFields = new ArrayList<>();
        for (AggregateField field : selectFields) {
            if (field.getFunction() == null ||
                field.getFunction() == AggregateField.AggregateFunction.NONE) {
                groupByFields.add(field.getFieldPath());
            }
        }

        // Delegate to getAggregateQueryResult which has full HAVING support
        return getAggregateQueryResult(
            entityClass,
            resultClass,
            selectFields,
            groupByFields,
            filter,
            havingFilter,
            pageable,
            rsqlContext,
            compiler
        );
    }

    /**
     * Executes a paginated aggregate query with SELECT string syntax, supporting GROUP BY, WHERE, and HAVING.
     * Returns Page with pagination info (totalElements, totalPages, etc.).
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string with aggregate functions (e.g., "category, COUNT(*):count, SUM(price):total")
     * @param filter RSQL filter string for WHERE clause (filters rows before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (filters groups after aggregation)
     * @param pageable Pagination and sorting (offset, limit, sort)
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @return Page of aggregate query results with pagination metadata
     */
    public static <ENTITY, RESULT> Page<RESULT> getAggregateQueryResultAsPageWithSelect(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        // Parse SELECT string to extract aggregate fields and GROUP BY fields
        SelectTreeParser selectParser = new SelectTreeParser();
        ParseTree tree = selectParser.parseStream(CharStreams.fromString(selectString));

        SelectAggregateVisitor aggregateVisitor = new SelectAggregateVisitor();
        aggregateVisitor.setContext(rsqlContext);
        List<AggregateField> selectFields = aggregateVisitor.visit(tree);

        // Extract GROUP BY fields (fields without aggregate functions)
        List<String> groupByFields = new ArrayList<>();
        for (AggregateField field : selectFields) {
            if (field.getFunction() == null ||
                field.getFunction() == AggregateField.AggregateFunction.NONE) {
                groupByFields.add(field.getFieldPath());
            }
        }

        // Delegate to getAggregateQueryResultAsPage which has full HAVING and pagination support
        return getAggregateQueryResultAsPage(
            entityClass,
            resultClass,
            selectFields,
            groupByFields,
            filter,
            havingFilter,
            pageable,
            rsqlContext,
            compiler
        );
    }

    /**
     * Executes an aggregate query with SELECT fields, GROUP BY, WHERE, and optional HAVING clause.
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectFields List of AggregateFields from SELECT clause (with aliases)
     * @param groupByFields List of GROUP BY field paths
     * @param filter RSQL filter string for WHERE clause (filters rows before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (filters groups after aggregation)
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @return List of aggregate query results
     */
    public static <ENTITY, RESULT> List<RESULT> getAggregateQueryResult(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        List<AggregateField> selectFields,
        List<String> groupByFields,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        Sort sort;
        if (pageable == null) {
            sort = null;
        } else {
            sort = pageable.getSort();
        }
        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // Update rsqlContext with new root and clear JOIN caches
        rsqlContext.criteriaBuilder = builder;
        rsqlContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) query;
        rsqlContext.root = root;
        rsqlContext.joinsMap.clear();
        rsqlContext.classMetadataMap.clear();

        // Create WHERE specification (uses shared joinsMap from rsqlContext)
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        // Create WHERE predicate FIRST (this creates JOINs in joinsMap)
        // This ensures that JOINs created by WHERE clause are registered first,
        // allowing SELECT clause to reuse them instead of creating duplicates
        Predicate wherePredicate = null;
        if (specification != null) {
            wherePredicate = specification.toPredicate(root, query, builder);
        }

        // Build select clause with aggregate functions (uses shared joinsMap from rsqlContext)
        List<Selection<?>> selectionList = new ArrayList<>();
        for (AggregateField field : selectFields) {
            Path<?> path = getPropertyPathRecursive(field.getFieldPath(), root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
            Selection<?> selection;

            selection = switch (field.getFunction()) {
                case SUM -> builder.sum((Expression<Number>) path);
                case AVG -> builder.avg((Expression<Number>) path);
                case COUNT -> builder.count(path);
                case COUNT_DISTINCT -> builder.countDistinct(path);
                case MIN -> builder.least((Expression) path);
                case MAX -> builder.greatest((Expression) path);
                case NONE -> path;
            };

            if (field.getAlias() != null && !field.getAlias().isEmpty()) {
                selection = selection.alias(field.getAlias());
            }
            selectionList.add(selection);
        }
        query.multiselect(selectionList);

        // Add WHERE clause
        if (wherePredicate != null) {
            query.where(wherePredicate);
        }

        // Add GROUP BY clause (uses shared joinsMap from rsqlContext)
        if (groupByFields != null && !groupByFields.isEmpty()) {
            List<Expression<?>> groupByExpressions = new ArrayList<>();
            for (String groupByField : groupByFields) {
                Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                groupByExpressions.add(groupByPath);
            }
            query.groupBy(groupByExpressions);
        }

        // Add HAVING clause
        if (havingFilter != null && !havingFilter.trim().isEmpty()) {
            // Create HavingContext with alias mappings from SELECT
            // Uses shared joinsMap and classMetadataMap from rsqlContext
            HavingContext<ENTITY> havingContext = HavingContext.fromAggregateFields(
                builder,
                root,
                selectFields,
                rsqlContext
            );

            // Compile HAVING filter to Predicate
            HavingCompiler havingCompiler = new HavingCompiler();
            Predicate havingPredicate = havingCompiler.compile(
                havingFilter,
                havingContext,
                rsqlContext,
                groupByFields
            );

            if (havingPredicate != null) {
                query.having(havingPredicate);
            }
        }

        // Add ORDER BY clause
        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        return rsqlContext.entityManager.createQuery(query).getResultList();
    }

    /**
     * Executes a paginated aggregate query with SELECT fields, GROUP BY, WHERE, and optional HAVING clause.
     * Returns Page with pagination info (totalElements, totalPages, etc.).
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectFields List of AggregateFields from SELECT clause (with aliases)
     * @param groupByFields List of GROUP BY field paths
     * @param filter RSQL filter string for WHERE clause (filters rows before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (filters groups after aggregation)
     * @param pageable Pagination and sorting (offset, limit, sort)
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @return Page of aggregate query results with pagination metadata
     */
    public static <ENTITY, RESULT> Page<RESULT> getAggregateQueryResultAsPage(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        List<AggregateField> selectFields,
        List<String> groupByFields,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        if (pageable == null) {
            pageable = Pageable.unpaged();
        }

        Sort sort = pageable.getSort();
        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();

        // ========== BUILD MAIN QUERY ==========
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // Update rsqlContext with new root and clear JOIN caches
        rsqlContext.criteriaBuilder = builder;
        rsqlContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) query;
        rsqlContext.root = root;
        rsqlContext.joinsMap.clear();
        rsqlContext.classMetadataMap.clear();

        // Create WHERE specification
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        // Create WHERE predicate FIRST (this creates JOINs in joinsMap)
        // This ensures that JOINs created by WHERE clause are registered first,
        // allowing SELECT clause to reuse them instead of creating duplicates
        Predicate wherePredicate = null;
        if (specification != null) {
            wherePredicate = specification.toPredicate(root, query, builder);
        }

        // Build select clause with aggregate functions and create maps for sorting
        // - aliasToExpressionMap: maps aliases to expressions (e.g., "sumaDuguje" -> SUM(duguje))
        // - fieldPathToExpressionMap: maps field paths to expressions (e.g., "konto.oznaka" -> Path)
        List<Selection<?>> selectionList = new ArrayList<>();
        Map<String, Expression<?>> aliasToExpressionMap = new HashMap<>();
        Map<String, Expression<?>> fieldPathToExpressionMap = new HashMap<>();
        for (AggregateField field : selectFields) {
            Path<?> path = getPropertyPathRecursive(field.getFieldPath(), root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
            Expression<?> expression = switch (field.getFunction()) {
                case SUM -> builder.sum((Expression<Number>) path);
                case AVG -> builder.avg((Expression<Number>) path);
                case COUNT -> builder.count(path);
                case COUNT_DISTINCT -> builder.countDistinct(path);
                case MIN -> builder.least((Expression) path);
                case MAX -> builder.greatest((Expression) path);
                case NONE -> path;
            };

            Selection<?> selection = expression;
            if (field.getAlias() != null && !field.getAlias().isEmpty()) {
                selection = selection.alias(field.getAlias());
                aliasToExpressionMap.put(field.getAlias(), expression);
            }

            // Map field path to expression for NONE function (simple fields without aggregates)
            // This allows sorting by field path (e.g., "konto.oznaka")
            if (field.getFunction() == AggregateField.AggregateFunction.NONE) {
                fieldPathToExpressionMap.put(field.getFieldPath(), expression);
            }

            selectionList.add(selection);
        }
        query.multiselect(selectionList);

        // Add WHERE clause
        if (wherePredicate != null) {
            query.where(wherePredicate);
        }

        // Add GROUP BY clause
        List<Expression<?>> groupByExpressions = new ArrayList<>();
        if (groupByFields != null && !groupByFields.isEmpty()) {
            for (String groupByField : groupByFields) {
                Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                groupByExpressions.add(groupByPath);
            }
            query.groupBy(groupByExpressions);
        }

        // Add HAVING clause
        Predicate havingPredicate = null;
        if (havingFilter != null && !havingFilter.trim().isEmpty()) {
            HavingContext<ENTITY> havingContext = HavingContext.fromAggregateFields(
                builder,
                root,
                selectFields,
                rsqlContext
            );

            HavingCompiler havingCompiler = new HavingCompiler();
            havingPredicate = havingCompiler.compile(
                havingFilter,
                havingContext,
                rsqlContext,
                groupByFields
            );

            if (havingPredicate != null) {
                query.having(havingPredicate);
            }
        }

        // Add ORDER BY clause - map sort properties to expressions via aliases or field paths
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                String property = sortOrder.getProperty();
                Expression<?> sortExpression;

                // Priority order for resolving sort property:
                // 1. Check if sort property is an alias in SELECT (e.g., "sumaDuguje")
                if (aliasToExpressionMap.containsKey(property)) {
                    sortExpression = aliasToExpressionMap.get(property);
                }
                // 2. Check if sort property is a field path in SELECT (e.g., "konto.oznaka")
                else if (fieldPathToExpressionMap.containsKey(property)) {
                    sortExpression = fieldPathToExpressionMap.get(property);
                }
                // 3. Fall back to entity property path (create new Path - use with caution)
                else {
                    sortExpression = getPropertyPathRecursive(property, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                }

                Order order = sortOrder.isAscending()
                    ? builder.asc(sortExpression)
                    : builder.desc(sortExpression);
                orders.add(order);
            }
            query.orderBy(orders);
        }

        // ========== HANDLE UNPAGED ==========
        if (isUnpaged(pageable)) {
            List<RESULT> results = rsqlContext.entityManager.createQuery(query).getResultList();
            return new PageImpl<>(results);
        }

        // ========== CALCULATE TOTAL COUNT ==========
        // For aggregate queries with GROUP BY, count must be the number of groups
        long totalCount;

        if (groupByFields == null || groupByFields.isEmpty()) {
            // No GROUP BY - simple count
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<ENTITY> countRoot = countQuery.from(entityClass);
            countQuery.select(builder.count(countRoot));

            if (specification != null) {
                // Create a new RsqlContext for count query to avoid reusing cached joins from main query
                // The main query's joins are tied to a different Root and cannot be reused here
                RsqlContext<ENTITY> countContext = new RsqlContext<>(rsqlContext.entityClass);
                countContext.entityManager = rsqlContext.entityManager;
                countContext.criteriaBuilder = builder;
                countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) countQuery;
                countContext.root = countRoot;
                countContext.joinsMap = new HashMap<>();
                countContext.classMetadataMap = new HashMap<>();

                // Create a new specification with the count context (this will create its own joins)
                Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
                if (countSpecification != null) {
                    Predicate countPredicate = countSpecification.toPredicate(countRoot, countQuery, builder);
                    countQuery.where(countPredicate);
                }
            }

            totalCount = rsqlContext.entityManager.createQuery(countQuery).getSingleResult();
        } else {
            // With GROUP BY - count number of groups
            // We need to execute a query that groups and counts
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<ENTITY> countRoot = countQuery.from(entityClass);

            // Use the first GROUP BY field for count (any will do)
            // Use fresh HashMap to avoid JOIN conflicts with main query
            Path<?> firstGroupByPath = getPropertyPathRecursive(groupByFields.get(0), countRoot, rsqlContext, new HashMap<>(), new HashMap<>());
            countQuery.select(builder.countDistinct(firstGroupByPath));

            if (specification != null) {
                // Create a new RsqlContext for count query to avoid reusing cached joins from main query
                RsqlContext<ENTITY> countContext = new RsqlContext<>(rsqlContext.entityClass);
                countContext.entityManager = rsqlContext.entityManager;
                countContext.criteriaBuilder = builder;
                countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) countQuery;
                countContext.root = countRoot;
                countContext.joinsMap = new HashMap<>();
                countContext.classMetadataMap = new HashMap<>();

                // Create a new specification with the count context (this will create its own joins)
                Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
                if (countSpecification != null) {
                    Predicate countPredicate = countSpecification.toPredicate(countRoot, countQuery, builder);
                    countQuery.where(countPredicate);
                }
            }

            // Note: HAVING doesn't affect count query because we want total groups before HAVING filter
            // If you want count AFTER HAVING, we'd need to execute the full query and count results

            // For precise count WITH HAVING, we execute a subquery
            if (havingPredicate != null) {
                // Execute the full query without pagination to count
                // Use fresh RsqlContext to avoid JOIN conflicts
                CriteriaQuery<RESULT> fullQuery = builder.createQuery(resultClass);
                Root<ENTITY> fullRoot = fullQuery.from(entityClass);

                // Create fresh context to avoid JOIN conflicts
                RsqlContext<ENTITY> countContext = new RsqlContext<>(entityClass);
                countContext.entityManager = rsqlContext.entityManager;
                countContext.criteriaBuilder = builder;
                countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) fullQuery;
                countContext.root = fullRoot;
                countContext.joinsMap = new HashMap<>();
                countContext.classMetadataMap = new HashMap<>();

                List<Selection<?>> countSelectionList = new ArrayList<>();
                for (AggregateField field : selectFields) {
                    Path<?> path = getPropertyPathRecursive(field.getFieldPath(), fullRoot, countContext, countContext.joinsMap, countContext.classMetadataMap);
                    Selection<?> selection = switch (field.getFunction()) {
                        case SUM -> builder.sum((Expression<Number>) path);
                        case AVG -> builder.avg((Expression<Number>) path);
                        case COUNT -> builder.count(path);
                        case COUNT_DISTINCT -> builder.countDistinct(path);
                        case MIN -> builder.least((Expression) path);
                        case MAX -> builder.greatest((Expression) path);
                        case NONE -> path;
                    };
                    countSelectionList.add(selection);
                }
                fullQuery.multiselect(countSelectionList);

                if (specification != null) {
                    // Create a new specification with the count context (this will create its own joins)
                    Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
                    if (countSpecification != null) {
                        Predicate fullPredicate = countSpecification.toPredicate(fullRoot, fullQuery, builder);
                        fullQuery.where(fullPredicate);
                    }
                }

                List<Expression<?>> fullGroupByExpressions = new ArrayList<>();
                for (String groupByField : groupByFields) {
                    Path<?> groupByPath = getPropertyPathRecursive(groupByField, fullRoot, countContext, countContext.joinsMap, countContext.classMetadataMap);
                    fullGroupByExpressions.add(groupByPath);
                }
                fullQuery.groupBy(fullGroupByExpressions);

                // Re-apply HAVING with fresh context
                HavingContext<ENTITY> fullHavingContext = HavingContext.fromAggregateFields(
                    builder,
                    fullRoot,
                    selectFields,
                    countContext
                );
                HavingCompiler fullHavingCompiler = new HavingCompiler();
                Predicate fullHavingPredicate = fullHavingCompiler.compile(
                    havingFilter,
                    fullHavingContext,
                    countContext,
                    groupByFields
                );
                if (fullHavingPredicate != null) {
                    fullQuery.having(fullHavingPredicate);
                }

                totalCount = rsqlContext.entityManager.createQuery(fullQuery).getResultList().size();
            } else {
                totalCount = rsqlContext.entityManager.createQuery(countQuery).getSingleResult();
            }
        }

        // ========== EXECUTE PAGINATED QUERY ==========
        List<RESULT> results = rsqlContext.entityManager.createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        return new PageImpl<>(results, pageable, totalCount);
    }

    /**
     * Executes an aggregate query with SELECT expressions (supports arithmetic operations),
     * GROUP BY, WHERE, and optional HAVING clause.
     *
     * <p>This method extends aggregate query capabilities to support arithmetic expressions
     * in the SELECT clause, such as:</p>
     * <ul>
     *   <li>SUM(revenue) - SUM(cost):profit</li>
     *   <li>(SUM(price) * 1.2):priceWithTax</li>
     *   <li>COUNT(*) / SUM(quantity):avgPerItem</li>
     * </ul>
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectExpressions List of SelectExpression objects from SELECT clause (with aliases)
     * @param groupByFields List of GROUP BY field paths
     * @param filter RSQL filter string for WHERE clause (filters rows before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (filters groups after aggregation)
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @param <ENTITY> Entity type
     * @param <RESULT> Result type (usually Tuple)
     * @return List of aggregate query results
     */
    public static <ENTITY, RESULT> List<RESULT> getAggregateQueryResultWithExpressions(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        List<rsql.helper.SelectExpression> selectExpressions,
        List<String> groupByFields,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        Sort sort;
        if (pageable == null) {
            sort = null;
        } else {
            sort = pageable.getSort();
        }
        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // Update rsqlContext with new root and clear JOIN caches
        rsqlContext.criteriaBuilder = builder;
        rsqlContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) query;
        rsqlContext.root = root;
        rsqlContext.joinsMap.clear();
        rsqlContext.classMetadataMap.clear();

        // Create WHERE specification (uses shared joinsMap from rsqlContext)
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        // Create WHERE predicate FIRST (this creates JOINs in joinsMap)
        // This ensures that JOINs created by WHERE clause are registered first,
        // allowing SELECT clause to reuse them instead of creating duplicates
        Predicate wherePredicate = null;
        if (specification != null) {
            wherePredicate = specification.toPredicate(root, query, builder);
        }

        // Build select clause with expressions and create maps for sorting
        // - aliasToExpressionMap: maps aliases to expressions (e.g., "sumaDuguje" -> SUM(duguje))
        // - fieldPathToExpressionMap: maps field paths to expressions (e.g., "konto.oznaka" -> Path)
        List<Selection<?>> selectionList = new ArrayList<>();
        Map<String, Expression<?>> aliasToExpressionMap = new HashMap<>();
        Map<String, Expression<?>> fieldPathToExpressionMap = new HashMap<>();
        for (rsql.helper.SelectExpression expr : selectExpressions) {
            // Convert SelectExpression to JPA Expression
            Expression<?> jpaExpression = expr.toJpaExpression(builder, root, rsqlContext);

            // Create Selection and apply alias if present
            Selection<?> selection = jpaExpression;
            if (expr.hasAlias()) {
                selection = selection.alias(expr.getAlias());
                aliasToExpressionMap.put(expr.getAlias(), jpaExpression);
            }

            // Map field path to expression for FieldExpression (allows sorting by field path)
            if (expr instanceof rsql.helper.FieldExpression) {
                rsql.helper.FieldExpression fieldExpr = (rsql.helper.FieldExpression) expr;
                fieldPathToExpressionMap.put(fieldExpr.getFieldPath(), jpaExpression);
            }

            selectionList.add(selection);
        }
        query.multiselect(selectionList);

        // Add WHERE clause
        if (wherePredicate != null) {
            query.where(wherePredicate);
        }

        // Add GROUP BY clause (uses shared joinsMap from rsqlContext)
        if (groupByFields != null && !groupByFields.isEmpty()) {
            List<Expression<?>> groupByExpressions = new ArrayList<>();
            for (String groupByField : groupByFields) {
                Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                groupByExpressions.add(groupByPath);
            }
            query.groupBy(groupByExpressions);
        }

        // Add HAVING clause
        if (havingFilter != null && !havingFilter.trim().isEmpty()) {
            HavingContext<ENTITY> havingContext = HavingContext.fromSelectExpressions(
                builder,
                root,
                selectExpressions,
                rsqlContext
            );

            HavingCompiler havingCompiler = new HavingCompiler();
            Predicate havingPredicate = havingCompiler.compile(
                havingFilter,
                havingContext,
                rsqlContext,
                groupByFields
            );

            if (havingPredicate != null) {
                query.having(havingPredicate);
            }
        }

        // Add ORDER BY clause - map sort properties to expressions via aliases or field paths
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                String property = sortOrder.getProperty();
                Expression<?> sortExpression;

                // Priority order for resolving sort property:
                // 1. Check if sort property is an alias in SELECT (e.g., "sumaDuguje")
                if (aliasToExpressionMap.containsKey(property)) {
                    sortExpression = aliasToExpressionMap.get(property);
                }
                // 2. Check if sort property is a field path in SELECT (e.g., "konto.oznaka")
                else if (fieldPathToExpressionMap.containsKey(property)) {
                    sortExpression = fieldPathToExpressionMap.get(property);
                }
                // 3. Fall back to entity property path (create new Path - use with caution)
                else {
                    sortExpression = getPropertyPathRecursive(property, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                }

                Order order = sortOrder.isAscending()
                    ? builder.asc(sortExpression)
                    : builder.desc(sortExpression);
                orders.add(order);
            }
            query.orderBy(orders);
        }

        return rsqlContext.entityManager.createQuery(query).getResultList();
    }

    /**
     * Executes a paginated aggregate query with SELECT expressions (supports arithmetic operations),
     * GROUP BY, WHERE, and optional HAVING clause. Returns Page with pagination info.
     *
     * <p>This method extends aggregate query capabilities to support arithmetic expressions
     * in the SELECT clause with pagination, such as:</p>
     * <ul>
     *   <li>SUM(revenue) - SUM(cost):profit</li>
     *   <li>(SUM(price) * 1.2):priceWithTax</li>
     *   <li>COUNT(*) / SUM(quantity):avgPerItem</li>
     * </ul>
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectExpressions List of SelectExpression objects from SELECT clause (with aliases)
     * @param groupByFields List of GROUP BY field paths
     * @param filter RSQL filter string for WHERE clause (filters rows before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (filters groups after aggregation)
     * @param pageable Pagination and sorting (offset, limit, sort)
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @param <ENTITY> Entity type
     * @param <RESULT> Result type (usually Tuple)
     * @return Page of aggregate query results with pagination metadata
     */
    public static <ENTITY, RESULT> Page<RESULT> getAggregateQueryResultAsPageWithExpressions(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        List<rsql.helper.SelectExpression> selectExpressions,
        List<String> groupByFields,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        if (pageable == null) {
            pageable = Pageable.unpaged();
        }

        Sort sort = pageable.getSort();
        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();

        // ========== BUILD MAIN QUERY ==========
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // Update rsqlContext with new root and clear JOIN caches
        rsqlContext.criteriaBuilder = builder;
        rsqlContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) query;
        rsqlContext.root = root;
        rsqlContext.joinsMap.clear();
        rsqlContext.classMetadataMap.clear();

        // Create WHERE specification
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        // Create WHERE predicate FIRST (this creates JOINs in joinsMap)
        // This ensures that JOINs created by WHERE clause are registered first,
        // allowing SELECT clause to reuse them instead of creating duplicates
        Predicate wherePredicate = null;
        if (specification != null) {
            wherePredicate = specification.toPredicate(root, query, builder);
        }

        // Build select clause with expressions and create maps for sorting
        // - aliasToExpressionMap: maps aliases to expressions (e.g., "sumaDuguje" -> SUM(duguje))
        // - fieldPathToExpressionMap: maps field paths to expressions (e.g., "konto.oznaka" -> Path)
        List<Selection<?>> selectionList = new ArrayList<>();
        Map<String, Expression<?>> aliasToExpressionMap = new HashMap<>();
        Map<String, Expression<?>> fieldPathToExpressionMap = new HashMap<>();
        for (rsql.helper.SelectExpression expr : selectExpressions) {
            Expression<?> jpaExpression = expr.toJpaExpression(builder, root, rsqlContext);
            Selection<?> selection = jpaExpression;

            // Map alias to expression if present
            if (expr.hasAlias()) {
                selection = selection.alias(expr.getAlias());
                aliasToExpressionMap.put(expr.getAlias(), jpaExpression);
            }

            // Map field path to expression for FieldExpression (allows sorting by field path)
            if (expr instanceof rsql.helper.FieldExpression) {
                rsql.helper.FieldExpression fieldExpr = (rsql.helper.FieldExpression) expr;
                fieldPathToExpressionMap.put(fieldExpr.getFieldPath(), jpaExpression);
            }

            selectionList.add(selection);
        }
        query.multiselect(selectionList);

        // Add WHERE clause
        if (wherePredicate != null) {
            query.where(wherePredicate);
        }

        // Add GROUP BY clause
        List<Expression<?>> groupByExpressions = new ArrayList<>();
        if (groupByFields != null && !groupByFields.isEmpty()) {
            for (String groupByField : groupByFields) {
                Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                groupByExpressions.add(groupByPath);
            }
            query.groupBy(groupByExpressions);
        }

        // Add HAVING clause
        Predicate havingPredicate = null;
        if (havingFilter != null && !havingFilter.trim().isEmpty()) {
            HavingContext<ENTITY> havingContext = HavingContext.fromSelectExpressions(
                builder,
                root,
                selectExpressions,
                rsqlContext
            );

            HavingCompiler havingCompiler = new HavingCompiler();
            havingPredicate = havingCompiler.compile(
                havingFilter,
                havingContext,
                rsqlContext,
                groupByFields
            );

            if (havingPredicate != null) {
                query.having(havingPredicate);
            }
        }

        // Add ORDER BY clause - map sort properties to expressions via aliases or field paths
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                String property = sortOrder.getProperty();
                Expression<?> sortExpression;

                // Priority order for resolving sort property:
                // 1. Check if sort property is an alias in SELECT (e.g., "sumaDuguje")
                if (aliasToExpressionMap.containsKey(property)) {
                    sortExpression = aliasToExpressionMap.get(property);
                }
                // 2. Check if sort property is a field path in SELECT (e.g., "konto.oznaka")
                else if (fieldPathToExpressionMap.containsKey(property)) {
                    sortExpression = fieldPathToExpressionMap.get(property);
                }
                // 3. Fall back to entity property path (create new Path - use with caution)
                else {
                    sortExpression = getPropertyPathRecursive(property, root, rsqlContext, rsqlContext.joinsMap, rsqlContext.classMetadataMap);
                }

                Order order = sortOrder.isAscending()
                    ? builder.asc(sortExpression)
                    : builder.desc(sortExpression);
                orders.add(order);
            }
            query.orderBy(orders);
        }

        // ========== HANDLE UNPAGED ==========
        if (isUnpaged(pageable)) {
            List<RESULT> results = rsqlContext.entityManager.createQuery(query).getResultList();
            return new PageImpl<>(results);
        }

        // ========== CALCULATE TOTAL COUNT ==========
        long totalCount;

        if (groupByFields == null || groupByFields.isEmpty()) {
            // No GROUP BY - simple count
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<ENTITY> countRoot = countQuery.from(entityClass);
            countQuery.select(builder.count(countRoot));

            if (specification != null) {
                // Create a new RsqlContext for count query to avoid reusing cached joins from main query
                // The main query's joins are tied to a different Root and cannot be reused here
                RsqlContext<ENTITY> countContext = new RsqlContext<>(rsqlContext.entityClass);
                countContext.entityManager = rsqlContext.entityManager;
                countContext.criteriaBuilder = builder;
                countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) countQuery;
                countContext.root = countRoot;
                countContext.joinsMap = new HashMap<>();
                countContext.classMetadataMap = new HashMap<>();

                // Create a new specification with the count context (this will create its own joins)
                Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
                if (countSpecification != null) {
                    Predicate countPredicate = countSpecification.toPredicate(countRoot, countQuery, builder);
                    countQuery.where(countPredicate);
                }
            }

            totalCount = rsqlContext.entityManager.createQuery(countQuery).getSingleResult();
        } else {
            // With GROUP BY - count number of groups
            // If HAVING is present, we must execute the full query to count
            if (havingPredicate != null) {
                // Execute the full query without pagination to count
                // Use fresh RsqlContext to avoid JOIN conflicts
                CriteriaQuery<RESULT> fullQuery = builder.createQuery(resultClass);
                Root<ENTITY> fullRoot = fullQuery.from(entityClass);

                // Create fresh context to avoid JOIN conflicts
                RsqlContext<ENTITY> countContext = new RsqlContext<>(entityClass);
                countContext.entityManager = rsqlContext.entityManager;
                countContext.criteriaBuilder = builder;
                countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) fullQuery;
                countContext.root = fullRoot;
                countContext.joinsMap = new HashMap<>();
                countContext.classMetadataMap = new HashMap<>();

                List<Selection<?>> countSelectionList = new ArrayList<>();
                for (rsql.helper.SelectExpression expr : selectExpressions) {
                    Expression<?> jpaExpression = expr.toJpaExpression(builder, fullRoot, countContext);
                    countSelectionList.add(jpaExpression);
                }
                fullQuery.multiselect(countSelectionList);

                if (specification != null) {
                    // Create a new specification with the count context (this will create its own joins)
                    Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
                    if (countSpecification != null) {
                        Predicate fullPredicate = countSpecification.toPredicate(fullRoot, fullQuery, builder);
                        fullQuery.where(fullPredicate);
                    }
                }

                List<Expression<?>> fullGroupByExpressions = new ArrayList<>();
                for (String groupByField : groupByFields) {
                    Path<?> groupByPath = getPropertyPathRecursive(groupByField, fullRoot, countContext, countContext.joinsMap, countContext.classMetadataMap);
                    fullGroupByExpressions.add(groupByPath);
                }
                fullQuery.groupBy(fullGroupByExpressions);

                // Re-apply HAVING with fresh context
                HavingContext<ENTITY> fullHavingContext = HavingContext.fromSelectExpressions(
                    builder,
                    fullRoot,
                    selectExpressions,
                    countContext
                );
                HavingCompiler fullHavingCompiler = new HavingCompiler();
                Predicate fullHavingPredicate = fullHavingCompiler.compile(
                    havingFilter,
                    fullHavingContext,
                    countContext,
                    groupByFields
                );
                if (fullHavingPredicate != null) {
                    fullQuery.having(fullHavingPredicate);
                }

                totalCount = rsqlContext.entityManager.createQuery(fullQuery).getResultList().size();
            } else {
                // No HAVING - use simple count
                CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
                Root<ENTITY> countRoot = countQuery.from(entityClass);

                // Use the first GROUP BY field for count
                // Use fresh HashMap to avoid JOIN conflicts with main query
                Path<?> firstGroupByPath = getPropertyPathRecursive(groupByFields.get(0), countRoot, rsqlContext, new HashMap<>(), new HashMap<>());
                countQuery.select(builder.countDistinct(firstGroupByPath));

                if (specification != null) {
                    // Create a new RsqlContext for count query to avoid reusing cached joins from main query
                    RsqlContext<ENTITY> countContext = new RsqlContext<>(rsqlContext.entityClass);
                    countContext.entityManager = rsqlContext.entityManager;
                    countContext.criteriaBuilder = builder;
                    countContext.criteriaQuery = (CriteriaQuery<ENTITY>) (CriteriaQuery<?>) countQuery;
                    countContext.root = countRoot;
                    countContext.joinsMap = new HashMap<>();
                    countContext.classMetadataMap = new HashMap<>();

                    // Create a new specification with the count context (this will create its own joins)
                    Specification<ENTITY> countSpecification = createSpecification(filter, countContext, compiler);
                    if (countSpecification != null) {
                        Predicate countPredicate = countSpecification.toPredicate(countRoot, countQuery, builder);
                        countQuery.where(countPredicate);
                    }
                }

                totalCount = rsqlContext.entityManager.createQuery(countQuery).getSingleResult();
            }
        }

        // ========== EXECUTE PAGINATED QUERY ==========
        List<RESULT> results = rsqlContext.entityManager.createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        return new PageImpl<>(results, pageable, totalCount);
    }

    /**
     * Executes an aggregate query with SELECT expression string (supports arithmetic operations).
     * Convenience method that parses the SELECT string and delegates to getAggregateQueryResultWithExpressions().
     *
     * <p>Example usage:</p>
     * <pre>
     * List&lt;Tuple&gt; results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
     *     Product.class,
     *     Tuple.class,
     *     "productType.name, SUM(duguje)-SUM(potrazuje):saldo",
     *     "status=='ACTIVE'",
     *     null, // no HAVING
     *     null, // no pagination
     *     rsqlContext,
     *     compiler
     * );
     * </pre>
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string with expressions
     * @param filter RSQL filter string for WHERE clause
     * @param havingFilter RSQL filter string for HAVING clause (not supported with expressions yet)
     * @param pageable Pagination and sorting
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @param <ENTITY> Entity type
     * @param <RESULT> Result type
     * @return List of aggregate query results
     */
    public static <ENTITY, RESULT> List<RESULT> getAggregateQueryResultWithSelectExpression(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        // Parse SELECT string to extract expressions and GROUP BY fields
        List<rsql.helper.SelectExpression> selectExpressions = compiler.compileSelectToExpressions(selectString, rsqlContext);

        // Extract GROUP BY fields (only FieldExpression without functions)
        List<String> groupByFields = new ArrayList<>();
        for (rsql.helper.SelectExpression expr : selectExpressions) {
            if (expr instanceof rsql.helper.FieldExpression) {
                rsql.helper.FieldExpression fieldExpr = (rsql.helper.FieldExpression) expr;
                groupByFields.add(fieldExpr.getFieldPath());
            } else if (expr instanceof rsql.helper.FunctionExpression) {
                rsql.helper.FunctionExpression funcExpr = (rsql.helper.FunctionExpression) expr;
                if (funcExpr.getFunction() == AggregateField.AggregateFunction.NONE) {
                    groupByFields.add(funcExpr.getFieldPath());
                }
            }
            // BinaryOpExpression and LiteralExpression are not GROUP BY candidates
        }

        // Delegate to getAggregateQueryResultWithExpressions
        return getAggregateQueryResultWithExpressions(
            entityClass,
            resultClass,
            selectExpressions,
            groupByFields,
            filter,
            havingFilter,
            pageable,
            rsqlContext,
            compiler
        );
    }

    /**
     * Executes a paginated aggregate query with SELECT expression string (supports arithmetic operations).
     * Convenience method that parses the SELECT string and delegates to getAggregateQueryResultAsPageWithExpressions().
     * Returns Page with pagination info (totalElements, totalPages, etc.).
     *
     * <p>Example usage:</p>
     * <pre>
     * Page&lt;Tuple&gt; page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression(
     *     Product.class,
     *     Tuple.class,
     *     "productType.name, SUM(price) * 1.2:totalWithTax",
     *     "status=='ACTIVE'",
     *     null, // no HAVING
     *     PageRequest.of(0, 10), // first page, 10 items
     *     rsqlContext,
     *     compiler
     * );
     * </pre>
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string with arithmetic expressions
     * @param filter RSQL filter string for WHERE clause
     * @param havingFilter RSQL filter string for HAVING clause (not supported with expressions yet)
     * @param pageable Pagination and sorting (offset, limit, sort)
     * @param rsqlContext RSQL context with EntityManager
     * @param compiler RSQL compiler
     * @param <ENTITY> Entity type
     * @param <RESULT> Result type
     * @return Page of aggregate query results with pagination metadata
     */
    public static <ENTITY, RESULT> Page<RESULT> getAggregateQueryResultAsPageWithSelectExpression(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        String havingFilter,
        Pageable pageable,
        RsqlContext<ENTITY> rsqlContext,
        RsqlCompiler<ENTITY> compiler
    ) {
        // Parse SELECT string to extract expressions and GROUP BY fields
        List<rsql.helper.SelectExpression> selectExpressions = compiler.compileSelectToExpressions(selectString, rsqlContext);

        // Extract GROUP BY fields (only FieldExpression without functions)
        List<String> groupByFields = new ArrayList<>();
        for (rsql.helper.SelectExpression expr : selectExpressions) {
            if (expr instanceof rsql.helper.FieldExpression) {
                rsql.helper.FieldExpression fieldExpr = (rsql.helper.FieldExpression) expr;
                groupByFields.add(fieldExpr.getFieldPath());
            } else if (expr instanceof rsql.helper.FunctionExpression) {
                rsql.helper.FunctionExpression funcExpr = (rsql.helper.FunctionExpression) expr;
                if (funcExpr.getFunction() == AggregateField.AggregateFunction.NONE) {
                    groupByFields.add(funcExpr.getFieldPath());
                }
            }
            // BinaryOpExpression and LiteralExpression are not GROUP BY candidates
        }

        // Delegate to getAggregateQueryResultAsPageWithExpressions
        return getAggregateQueryResultAsPageWithExpressions(
            entityClass,
            resultClass,
            selectExpressions,
            groupByFields,
            filter,
            havingFilter,
            pageable,
            rsqlContext,
            compiler
        );
    }

}
