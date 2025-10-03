package rsql.helper;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.data.support.PageableExecutionUtils;
import rsql.RsqlCompiler;
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
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // Parse SELECT string and create Selections using SelectFieldSelectionVisitor
        SelectTreeParser selectParser = new SelectTreeParser();
        ParseTree tree = selectParser.parseStream(CharStreams.fromString(selectString));

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, builder, root);
        List<Selection<?>> selectionList = visitor.visit(tree);

        query.multiselect(selectionList);

        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, query, builder);
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
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // Parse SELECT string and create Selections using SelectFieldSelectionVisitor
        SelectTreeParser selectParser = new SelectTreeParser();
        ParseTree tree = selectParser.parseStream(CharStreams.fromString(selectString));

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, builder, root);
        List<Selection<?>> selectionList = visitor.visit(tree);

        query.multiselect(selectionList);

        Predicate predicate = null;
        if (specification != null) {
            predicate = specification.toPredicate(root, query, builder);
            query.where(predicate);
        }

        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        if (isUnpaged(pageable)) {
            return new PageImpl<>((List<RESULT>) rsqlContext.entityManager.createQuery(query).getResultList());
        }

        long totalRecords = 0L;
        if (specification == null) {
            totalRecords = repository.count();
        } else {
            totalRecords = repository.count(specification);
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
     * Executes an aggregate query with SELECT string parsing, supporting aliases, aggregate functions, and GROUP BY.
     * Uses SelectAggregateSelectionVisitor for creating JPA Criteria Selections.
     * Automatically extracts GROUP BY fields from SELECT string (fields without aggregate functions).
     *
     * @param entityClass The entity class to query
     * @param resultClass The result class (usually Tuple.class)
     * @param selectString SELECT clause string with aggregate functions (e.g., "productType.name, COUNT(*):count, SUM(price):total")
     * @param filter RSQL filter string
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
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        // First, parse SELECT string to extract GROUP BY fields using SelectAggregateVisitor
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

        // Now create Selections using SelectAggregateSelectionVisitor
        SelectAggregateSelectionVisitor selectionVisitor = new SelectAggregateSelectionVisitor();
        selectionVisitor.setContext(rsqlContext, builder, root);
        List<Selection<?>> selectionList = selectionVisitor.visit(tree);

        query.multiselect(selectionList);

        // Add WHERE clause
        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, query, builder);
            query.where(predicate);
        }

        // Add GROUP BY clause (reuse getPropertyPathRecursive to create proper Path objects)
        if (!groupByFields.isEmpty()) {
            Map<String, Path<?>> joinsMap = new HashMap<>();
            Map<String, ManagedType<?>> classMetadataMap = new HashMap<>();
            List<Expression<?>> groupByExpressions = new ArrayList<>();
            for (String groupByField : groupByFields) {
                Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, joinsMap, classMetadataMap);
                groupByExpressions.add(groupByPath);
            }
            query.groupBy(groupByExpressions);
        }

        // Add ORDER BY clause
        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        return rsqlContext.entityManager.createQuery(query).getResultList();
    }

    public static <ENTITY, RESULT> List<RESULT> getAggregateQueryResult(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        List<AggregateField> selectFields,
        List<String> groupByFields,
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
        Specification<ENTITY> specification = createSpecification(filter, rsqlContext, compiler);

        CriteriaBuilder builder = rsqlContext.entityManager.getCriteriaBuilder();
        CriteriaQuery<RESULT> query = builder.createQuery(resultClass);
        Root<ENTITY> root = query.from(entityClass);

        Map<String, Path<?>> joinsMap = new HashMap<>();
        Map<String, ManagedType<?>> classMetadataMap = new HashMap<>();

        // Build select clause with aggregate functions
        List<Selection<?>> selectionList = new ArrayList<>();
        for (AggregateField field : selectFields) {
            Path<?> path = getPropertyPathRecursive(field.getFieldPath(), root, rsqlContext, joinsMap, classMetadataMap);
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
        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, query, builder);
            query.where(predicate);
        }

        // Add GROUP BY clause
        if (groupByFields != null && !groupByFields.isEmpty()) {
            List<Expression<?>> groupByExpressions = new ArrayList<>();
            for (String groupByField : groupByFields) {
                Path<?> groupByPath = getPropertyPathRecursive(groupByField, root, rsqlContext, joinsMap, classMetadataMap);
                groupByExpressions.add(groupByPath);
            }
            query.groupBy(groupByExpressions);
        }

        // Add ORDER BY clause
        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        return rsqlContext.entityManager.createQuery(query).getResultList();
    }
}
