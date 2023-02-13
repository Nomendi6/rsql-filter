package rsql.where;

import org.antlr.v4.runtime.tree.SyntaxTree;
import org.antlr.v4.runtime.tree.Tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public RsqlQuery() {
        this("");
    }

    public RsqlQuery(String where) {
        this.where = where;
        params = new ArrayList<>();
        paramLists = new ArrayList<>();
        joins = new LinkedHashMap<>();
    }

    public RsqlQuery and(RsqlQuery other) {
        this.where += " and " + other.where;
        this.params.addAll(other.params);
        this.paramLists.addAll(other.paramLists);
        this.joins.putAll(other.joins);
        return this;
    }

    public RsqlQuery or(RsqlQuery other) {
        this.where += " or " + other.where;
        this.params.addAll(other.params);
        this.paramLists.addAll(other.paramLists);
        this.joins.putAll(other.joins);
        return this;
    }

    public RsqlQuery addParens() {
        this.where = "(" + this.where + ")";
        return this;
    }

    public void addJoin(String path, RsqlJoin join) {
        joins.put(path, join);
    }

    public void addJoins(Map<String, RsqlJoin> otherJoins) {
        joins.putAll(otherJoins);
    }

    public RsqlJoin findJoin(String path) {
        return joins.get(path);
    }

    public boolean containsPath(String path) {
        return joins.containsKey(path);
    }

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

    public String getFromClause(String rootEntity, String rootEntityAlias) {
        String fromClause = rootEntity + " " + rootEntityAlias;

        for (Map.Entry<String, RsqlJoin> entry : this.joins.entrySet()) {
            RsqlJoin join = entry.getValue();
            fromClause += " " + join.joinType + " " + join.parentAlias + "." + join.attribute + " " + join.alias;
        }

        return fromClause;
    }
}
