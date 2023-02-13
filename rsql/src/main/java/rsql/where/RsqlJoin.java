package rsql.where;

import javax.persistence.criteria.Path;

public class RsqlJoin {

    public String path;
    public String attribute;
    public String entity;
    public String alias;
    public String parentAlias;
    public Path root;
    public String joinType;

    public RsqlJoin(String entity, String joinType) {
        this.entity = entity;
        this.joinType = joinType;
    }

    public RsqlJoin(String entity, String attribute, String alias, String joinType) {
        this.entity = entity;
        this.attribute = attribute;
        this.alias = alias;
        this.joinType = joinType;
    }

    public RsqlJoin(String path, String attribute, String entity, String alias, String parentAlias, Path root, String joinType) {
        this.path = path;
        this.attribute = attribute;
        this.entity = entity;
        this.alias = alias;
        this.parentAlias = parentAlias;
        this.root = root;
        this.joinType = joinType;
    }

    @Override
    public String toString() {
        return (
            "RsqlJoin{" +
            "path='" +
            path +
            '\'' +
            ", attribute='" +
            attribute +
            '\'' +
            ", entity='" +
            entity +
            '\'' +
            ", alias='" +
            alias +
            '\'' +
            ", parentAlias='" +
            parentAlias +
            '\'' +
            '}'
        );
    }
}
