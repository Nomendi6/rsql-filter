package rsql;

import rsql.exceptions.SyntaxErrorException;
import rsql.where.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Objects;

public class RsqlCompiler<T> {

    private RsqlWhereTreeParser treeParser;

    public RsqlCompiler() {
        this.treeParser = new RsqlWhereTreeParser();
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
        query.where = query.where.replace("Id", "_id");
        query.where = query.where.replace(".id", "_id");
    }

}
