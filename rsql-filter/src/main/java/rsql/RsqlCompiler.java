package rsql;

import rsql.exceptions.SyntaxErrorException;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.select.SelectAggregateVisitor;
import rsql.select.SelectField;
import rsql.select.SelectFieldVisitor;
import rsql.select.SelectTreeParser;
import rsql.where.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is responsible for compiling RSQL queries into Specifications or RsqlQuery structures.
 *
 * @param <T> The type of the entity that the RSQL operations are targeting.
 */
public class RsqlCompiler<T> {

    /**
     * The RsqlWhereTreeParser used to parse the RSQL WHERE query.
     */
    private RsqlWhereTreeParser treeParser;

    /**
     * The SelectTreeParser used to parse the RSQL SELECT query.
     */
    private SelectTreeParser selectTreeParser;

    /**
     * Default constructor for the RsqlCompiler class.
     * It initializes the RsqlWhereTreeParser and SelectTreeParser.
     */
    public RsqlCompiler() {
        this.treeParser = new RsqlWhereTreeParser();
        this.selectTreeParser = new SelectTreeParser();
    }

    /**
     * Defines hibernate Specification (where condition) for filter defined in inputString
     *
     * @param inputString - filter defined in rsql language
     * @param rsqlContext - context
     * @return Specification with where conditions
     */
    public Specification<T> compileToSpecification(String inputString, RsqlContext<T> rsqlContext) {
        if (inputString != null && inputString.length() > 0) {
            CharStream inputStream = CharStreams.fromString(inputString);

            final ParseTree tree = this.treeParser.parseStream(inputStream);

            Specification<T> specification = null;

            if (tree != null && tree.getChildCount() > 0) {
                WhereSpecificationVisitor<T> visitor = new WhereSpecificationVisitor<>();
                visitor.setSpecificationContext(rsqlContext);

                specification = visitor.visit(tree);
            }
            if (specification == null) {
                throw new SyntaxErrorException("Error in rsql expression: " + inputString);
            }
            return specification;
        }
        return null;
    }

    /**
     * Create RsqlQuery structure from condition defined in inputString
     *
     * @param inputString - filter with condition
     * @param rsqlContext - context
     * @return - RsqlQuery structure
     */
    public RsqlQuery compileToRsqlQuery(String inputString, RsqlContext<T> rsqlContext) {
        if (inputString != null && inputString.length() > 0) {
            CharStream inputStream = CharStreams.fromString(inputString);

            final ParseTree tree = this.treeParser.parseStream(inputStream);

            RsqlQuery rsqlQuery = null;
            if (tree != null && tree.getChildCount() > 0) {
                WhereTextVisitor<T> visitor = new WhereTextVisitor<>();
                visitor.setSpecificationContext(rsqlContext);

                rsqlQuery = visitor.visit(tree);
            }

            if (rsqlQuery == null) {
                throw new SyntaxErrorException("Error in rsql expression: " + inputString);
            }

            rsqlQuery.select = "a0";
            rsqlQuery.from = rsqlQuery.getFromClause(rsqlContext.entityClass.getName(), "a0");

            return rsqlQuery;
        }
        return null;
    }

    /**
     * Bind parameters defined in RSqlQuery structure to the typed query.
     *
     * @param rsqlQuery - RsqlQuery structure
     * @param query - typed query where parameters will be bound
     * @param <T> - type of the typed query
     */
    public static <T> void bindImplicitParametersForTypedQuery(RsqlQuery rsqlQuery, TypedQuery<T> query) {
        for (int i = 0; i < rsqlQuery.params.size(); i++) {
            RsqlQueryParam param = rsqlQuery.params.get(i);
            query.setParameter(param.name, param.value);
        }

        for (int i = 0; i < rsqlQuery.paramLists.size(); i++) {
            RsqlQueryParamList paramList = rsqlQuery.paramLists.get(i);
            query.setParameter(paramList.name, paramList.list);
        }
    }

    /**
     * Bind parameters defined in RSqlQuery structure to the query.
     *
     * @param rsqlQuery - RsqlQuery structure
     * @param query - query to which parameters will be bound
     */
    public static void bindImplicitParametersForQuery(RsqlQuery rsqlQuery, Query query) {
        for (int i = 0; i < rsqlQuery.params.size(); i++) {
            RsqlQueryParam param = rsqlQuery.params.get(i);
            query.setParameter(param.name, param.value);
        }

        for (int i = 0; i < rsqlQuery.paramLists.size(); i++) {
            RsqlQueryParamList paramList = rsqlQuery.paramLists.get(i);
            query.setParameter(paramList.name, paramList.list);
        }
    }

    /**
     * Replace alias in RsqlQuery structure.
     *
     * @param query - RsqlQuery structure
     * @param fromAlias - alias to be replaced
     * @param toAlias - new alias name
     */
    public static void replaceAlias(RsqlQuery query, String fromAlias, String toAlias) {
        if (Objects.equals(query.alias, fromAlias)) {
            query.alias = toAlias;
        }
        if (Objects.equals(query.select, fromAlias)) {
            query.select = toAlias;
        }
        query.where = query.where.replace(fromAlias + ".", toAlias + ".");
    }

    /**
     * Fix Id and .id in where condition so it can be used in native query.
     * @param query - RsqlQuery structure
     */
    public static void fixIdsForNativeQuery(RsqlQuery query) {
        String alias = query.alias;
        query.where = query.where.replace("Id", "_id");
        query.where = query.where.replace(".id", "_id");
        query.where = query.where.replace(alias + "_id", alias + ".id"); // this is not a reference field but a field on the entity
    }

    // ==================== SELECT Compilation Methods ====================

    /**
     * Compiles a simple SELECT string into a list of SelectField objects (fieldPath + alias).
     * Does not support aggregate functions.
     *
     * @param selectString SELECT clause (e.g., "field1, field2:alias2, productType.name")
     * @param rsqlContext JPA context
     * @return List of SelectField objects with fieldPath and optional alias
     * @throws SyntaxErrorException if syntax error or aggregate function found
     */
    public List<SelectField> compileSelectToFields(
        String selectString,
        RsqlContext<T> rsqlContext
    ) {
        if (selectString == null || selectString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        CharStream inputStream = CharStreams.fromString(selectString);
        ParseTree tree = selectTreeParser.parseStream(inputStream);

        SelectFieldVisitor visitor = new SelectFieldVisitor();
        visitor.setContext(rsqlContext);

        List<SelectField> fields = visitor.visit(tree);

        if (fields == null) {
            throw new SyntaxErrorException("Error parsing SELECT clause: " + selectString);
        }

        return fields;
    }

    /**
     * Compiles a simple SELECT string into a list of field paths (without aliases).
     * Helper method for backward compatibility with APIs that expect String[].
     *
     * @param selectString SELECT clause
     * @param rsqlContext JPA context
     * @return List of field paths (aliases ignored)
     */
    public List<String> compileSelectToFieldPaths(
        String selectString,
        RsqlContext<T> rsqlContext
    ) {
        List<SelectField> fields = compileSelectToFields(selectString, rsqlContext);
        return fields.stream()
            .map(SelectField::getFieldPath)
            .collect(Collectors.toList());
    }

    /**
     * Compiles an aggregate SELECT string into a list of AggregateField objects.
     * Supports both simple fields and aggregate functions.
     *
     * @param selectString SELECT clause (e.g., "productType.name:type, SUM(price):total")
     * @param rsqlContext JPA context
     * @return List of AggregateField objects
     * @throws SyntaxErrorException if syntax error
     */
    public List<AggregateField> compileSelectToAggregateFields(
        String selectString,
        RsqlContext<T> rsqlContext
    ) {
        if (selectString == null || selectString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        CharStream inputStream = CharStreams.fromString(selectString);
        ParseTree tree = selectTreeParser.parseStream(inputStream);

        SelectAggregateVisitor visitor = new SelectAggregateVisitor();
        visitor.setContext(rsqlContext);

        List<AggregateField> fields = visitor.visit(tree);

        if (fields == null) {
            throw new SyntaxErrorException("Error parsing SELECT clause: " + selectString);
        }

        return fields;
    }

    /**
     * Extracts GROUP BY field paths from an aggregate SELECT string.
     * Returns only non-aggregate fields (function == NONE).
     *
     * @param selectString SELECT clause
     * @param rsqlContext JPA context
     * @return List of field paths for GROUP BY
     */
    public List<String> compileSelectToGroupByFields(
        String selectString,
        RsqlContext<T> rsqlContext
    ) {
        List<AggregateField> fields = compileSelectToAggregateFields(selectString, rsqlContext);

        return fields.stream()
            .filter(f -> f.getFunction() == AggregateFunction.NONE)
            .map(AggregateField::getFieldPath)
            .collect(Collectors.toList());
    }

}
