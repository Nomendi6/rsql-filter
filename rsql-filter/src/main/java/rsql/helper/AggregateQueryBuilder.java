package rsql.helper;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.ManagedType;
import rsql.RsqlCompiler;
import rsql.having.HavingCompiler;
import rsql.having.HavingContext;
import rsql.where.RsqlContext;

import java.util.List;
import java.util.Map;

/**
 * Builder class for aggregate queries containing SELECT selections, GROUP BY expressions,
 * and internal state for creating HAVING predicates.
 *
 * <p>This class encapsulates all components needed for building aggregate queries,
 * similar to how Specification encapsulates WHERE clause logic. It maintains shared
 * state (like joins maps) to ensure consistency between SELECT, GROUP BY, and HAVING clauses.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * AggregateQueryBuilder&lt;Product&gt; builder = SimpleQueryExecutor.createAggregateQuery(
 *     "productType.name:category, COUNT(*):count, SUM(price):total",
 *     criteriaBuilder, root, rsqlContext
 * );
 *
 * query.multiselect(builder.getSelections());
 * query.groupBy(builder.getGroupByExpressions());
 * query.having(builder.createHavingPredicate("total=gt=50000;count=ge=10", compiler));
 * </pre>
 *
 * @param <ENTITY> The entity type being queried
 */
public class AggregateQueryBuilder<ENTITY> {

    private final List<Selection<?>> selections;
    private final List<Expression<?>> groupByExpressions;

    // Internal state for HAVING clause generation
    private final CriteriaBuilder builder;
    private final Root<ENTITY> root;
    private final List<AggregateField> selectFields;
    private final RsqlContext<ENTITY> rsqlContext;
    private final List<String> groupByFieldNames;

    /**
     * Package-private constructor. Instances should be created via
     * {@link SimpleQueryExecutor#createAggregateQuery}.
     * Uses shared joinsMap and classMetadataMap from rsqlContext for consistency
     * across SELECT, WHERE, GROUP BY, and HAVING clauses.
     *
     * @param selections SELECT clause selections
     * @param groupByExpressions GROUP BY clause expressions
     * @param builder CriteriaBuilder
     * @param root Query root
     * @param selectFields Parsed aggregate fields from SELECT string
     * @param rsqlContext RSQL context with shared JOIN and metadata caches
     * @param groupByFieldNames GROUP BY field names
     */
    AggregateQueryBuilder(
        List<Selection<?>> selections,
        List<Expression<?>> groupByExpressions,
        CriteriaBuilder builder,
        Root<ENTITY> root,
        List<AggregateField> selectFields,
        RsqlContext<ENTITY> rsqlContext,
        List<String> groupByFieldNames
    ) {
        this.selections = selections;
        this.groupByExpressions = groupByExpressions;
        this.builder = builder;
        this.root = root;
        this.selectFields = selectFields;
        this.rsqlContext = rsqlContext;
        this.groupByFieldNames = groupByFieldNames;
    }

    /**
     * Returns the SELECT clause selections for use with {@code CriteriaQuery.multiselect()}.
     *
     * @return List of selections (aggregate functions and grouping fields)
     */
    public List<Selection<?>> getSelections() {
        return selections;
    }

    /**
     * Returns the GROUP BY clause expressions for use with {@code CriteriaQuery.groupBy()}.
     *
     * @return List of expressions representing GROUP BY fields
     */
    public List<Expression<?>> getGroupByExpressions() {
        return groupByExpressions;
    }

    /**
     * Creates a HAVING clause predicate from a HAVING filter string.
     * Uses internal state (SELECT fields, joins map, etc.) to ensure consistency
     * with SELECT and GROUP BY clauses.
     *
     * <p>The HAVING filter can reference:</p>
     * <ul>
     *   <li>Aliases from SELECT clause: {@code "totalPrice=gt=10000;productCount=ge=5"}</li>
     *   <li>Aggregate functions directly: {@code "SUM(price)=gt=50000;COUNT(*)=ge=10"}</li>
     *   <li>Logical operators: {@code ;} (AND), {@code ,} (OR), parentheses for grouping</li>
     * </ul>
     *
     * @param havingFilter RSQL HAVING filter string (can be null or empty)
     * @param compiler RSQL compiler for parsing the filter
     * @return HAVING Predicate, or null if havingFilter is null/empty
     * @throws rsql.exceptions.SyntaxErrorException if HAVING filter has syntax errors
     */
    public Predicate createHavingPredicate(String havingFilter, RsqlCompiler<ENTITY> compiler) {
        if (havingFilter == null || havingFilter.trim().isEmpty()) {
            return null;
        }

        // Create HavingContext with internal state
        // HavingContext will use shared joinsMap and classMetadataMap from rsqlContext
        HavingContext<ENTITY> havingContext = HavingContext.fromAggregateFields(
            builder,
            root,
            selectFields,
            rsqlContext
        );

        // Compile HAVING filter to Predicate using HavingCompiler
        HavingCompiler havingCompiler = new HavingCompiler();
        return havingCompiler.compile(
            havingFilter,
            havingContext,
            rsqlContext,
            groupByFieldNames
        );
    }

    /**
     * Returns the parsed aggregate fields from the SELECT string.
     * This is useful for advanced scenarios where you need access to field metadata.
     *
     * @return List of AggregateField objects with field paths, functions, and aliases
     */
    public List<AggregateField> getSelectFields() {
        return selectFields;
    }

    /**
     * Returns the GROUP BY field names.
     * This is useful for debugging or for advanced HAVING filter validation.
     *
     * @return List of GROUP BY field paths
     */
    public List<String> getGroupByFieldNames() {
        return groupByFieldNames;
    }
}
