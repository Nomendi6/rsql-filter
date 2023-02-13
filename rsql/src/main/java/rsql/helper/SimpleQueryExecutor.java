package rsql.helper;

import rsql.RsqlCompiler;
import rsql.where.RsqlContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
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

    public static <ENTITY, RESULT> List<RESULT> getQueryResult(
        Class<ENTITY> entityClass,
        Class<RESULT> resultClass,
        String[] properties,
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

        List<Selection<? extends Object>> selectionList = new ArrayList<>();
        for (String property : properties) {
            Selection<? extends Object> selection = root.get(property);
            selectionList.add(selection);
        }
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

        List<Selection<? extends Object>> selectionList = new ArrayList<>();
        Map<String, Path<?>> joinsMap = new HashMap<>();
        Map<String, ManagedType<?>> classMetadataMap = new HashMap<>();
        for (String property : properties) {
            //            Selection<? extends Object> selection = root.get(property);
            Selection<? extends Object> selection = getPropertyPathRecursive(property, root, rsqlContext, joinsMap, classMetadataMap);
            selectionList.add(selection);
        }
        //        List<Join<? extends Object, ? extends Object>> joinsList = new ArrayList<>();
        //        for (String leftJoin : leftJoins) {
        //            final Join<Object, Object> join = root.join(leftJoin, JoinType.LEFT);
        //            joinsList.add(join);
        //        }

        query.multiselect(selectionList);

        Predicate predicate = null;
        if (specification != null) {
            predicate = specification.toPredicate(root, query, builder);
            query.where(predicate);
        }

        if (sort != null && sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        //        Page<RESULT> page = readPage(query, pageable, predicate, rsqlContext, entityClass);
        //        return page;

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
        //        return rsqlContext.entityManager.createQuery(query).getResultList();
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
}
