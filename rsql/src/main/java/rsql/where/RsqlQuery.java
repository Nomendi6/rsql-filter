package rsql.where;

import org.antlr.v4.runtime.tree.SyntaxTree;
import org.antlr.v4.runtime.tree.Tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents an RSQL query.
 * It contains information about the select, from, where, orderBy, group, having, alias, parameters, parameter lists, and joins.
 */
public class RsqlQuery {

    public String select = "a0";
    public String from = "";
    public String where;
    public String orderBy = "";
    public String group = "";
    public String having = "";
    public String alias = "a0";
    public List<RsqlQueryParam> params;
    public List<RsqlQueryParamList> paramLists;
    public Map<String, RsqlJoin> joins;

    /**
     * Overrides the toString method to provide a string representation of the RsqlQuery object.
     *
     * @return The string representation of the RsqlQuery object.
     */
    @Override
    public String toString() {
        return (
            "RsqlQuery{" +
            "select='" +
            select +
            '\'' +
            ", from='" +
            from +
            '\'' +
            ", where='" +
            where +
            '\'' +
            ", orderBy='" +
            orderBy +
            '\'' +
            ", group='" +
            group +
            '\'' +
            ", having='" +
            having +
            '\'' +
            ", alias='" +
            alias +
            '\'' +
            ", params=" +
            params +
            ", paramLists=" +
            paramLists +
            ", joins=" +
            joins +
            '}'
        );
    }

    /**
     * Default constructor for the RsqlQuery class.
     * Initializes the where clause to an empty string.
     */
    public RsqlQuery() {
        this("");
    }

    /**
     * Constructor for the RsqlQuery class.
     *
     * @param where The where clause to be set.
     */
    public RsqlQuery(String where) {
        this.where = where;
        params = new ArrayList<>();
        paramLists = new ArrayList<>();
        joins = new LinkedHashMap<>();
    }

    /**
     * Adds another RsqlQuery to this one using the AND operator.
     *
     * @param other The other RsqlQuery to be added.
     * @return This RsqlQuery with the other one added.
     */
    public RsqlQuery and(RsqlQuery other) {
        this.where += " and " + other.where;
        this.params.addAll(other.params);
        this.paramLists.addAll(other.paramLists);
        this.joins.putAll(other.joins);
        return this;
    }

    /**
     * Adds another RsqlQuery to this one using the OR operator.
     *
     * @param other The other RsqlQuery to be added.
     * @return This RsqlQuery with the other one added.
     */
    public RsqlQuery or(RsqlQuery other) {
        this.where += " or " + other.where;
        this.params.addAll(other.params);
        this.paramLists.addAll(other.paramLists);
        this.joins.putAll(other.joins);
        return this;
    }

    /**
     * Adds parentheses to the where clause of this RsqlQuery.
     *
     * @return This RsqlQuery with parentheses added to the where clause.
     */
    public RsqlQuery addParens() {
        this.where = "(" + this.where + ")";
        return this;
    }

    /**
     * Adds a join to this RsqlQuery.
     *
     * @param path The path of the join.
     * @param join The join to be added.
     */
    public void addJoin(String path, RsqlJoin join) {
        joins.put(path, join);
    }

    /**
     * Adds multiple joins to this RsqlQuery.
     *
     * @param otherJoins The joins to be added.
     */
    public void addJoins(Map<String, RsqlJoin> otherJoins) {
        joins.putAll(otherJoins);
    }

    /**
     * Finds a join in this RsqlQuery.
     *
     * @param path The path of the join.
     * @return The join found, or null if no join was found.
     */
    public RsqlJoin findJoin(String path) {
        return joins.get(path);
    }

    /**
     * Checks if this RsqlQuery contains a certain path.
     *
     * @param path The path to be checked.
     * @return True if the path is contained in this RsqlQuery, false otherwise.
     */
    public boolean containsPath(String path) {
        return joins.containsKey(path);
    }

    /**
     * Returns the SQL representation of this RsqlQuery.
     *
     * @return The SQL representation of this RsqlQuery.
     */
    public String getSql() {
        String sql = "select ";
        sql += this.select != null ? this.select : "a0";
        sql += " from " + this.from;
        if (this.where != null && this.where.length() > 0) {
            sql += " where " + this.where;
        }
        if (this.orderBy != null && this.orderBy.length() > 0) {
            sql += " order by " + this.orderBy;
        }
        if (this.group != null && this.group.length() > 0) {
            sql += " group by " + this.group;
        }
        if (this.having != null && this.having.length() > 0) {
            sql += " having " + this.having;
        }

        return sql;
    }

    /**
     * Returns the FROM clause of this RsqlQuery.
     *
     * @param rootEntity The root entity of the FROM clause.
     * @param rootEntityAlias The alias of the root entity.
     * @return The FROM clause of this RsqlQuery.
     */
    public String getFromClause(String rootEntity, String rootEntityAlias) {
        String fromClause = rootEntity + " " + rootEntityAlias;

        for (Map.Entry<String, RsqlJoin> entry : this.joins.entrySet()) {
            RsqlJoin join = entry.getValue();
            fromClause += " " + join.joinType + " " + join.parentAlias + "." + join.attribute + " " + join.alias;
        }

        return fromClause;
    }
}
