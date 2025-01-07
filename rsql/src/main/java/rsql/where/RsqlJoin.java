package rsql.where;

import jakarta.persistence.criteria.Path;

/**
 * This class represents a join operation in an RSQL query.
 * It contains information about the path, attribute, entity, alias, parent alias, root, join type, and full path of the join.
 */
public class RsqlJoin {

    /**
     * The path of the join.
     */
    public String path;
    /**
     * The attribute to be joined on.
     */
    public String attribute;
    /**
     * The entity to be joined.
     */
    public String entity;
    /**
     * The alias of the join.
     */
    public String alias;
    /**
     * The parent alias of the join.
     */
    public String parentAlias;
    /**
     * The root of the join.
     */
    public String rootAlias;
    /**
     * The root of the join.
     */
    public Path root;
    /**
     * The type of the join operation.
     */
    public String joinType;
    /**
     * The full path of the join.
     */
    public String fullPath;

    /**
     * Default constructor for the RsqlJoin class.
     */
    public RsqlJoin() {
    }

    /**
     * Constructor for the RsqlJoin class.
     *
     * @param entity The entity to be joined.
     * @param joinType The type of the join operation.
     */
    public RsqlJoin(String entity, String joinType) {
        this.entity = entity;
        this.joinType = joinType;
    }

    /**
     * Constructor for the RsqlJoin class.
     *
     * @param entity The entity to be joined.
     * @param attribute The attribute to be joined on.
     * @param alias The alias of the join.
     * @param joinType The type of the join operation.
     */
    public RsqlJoin(String entity, String attribute, String alias, String joinType) {
        this.entity = entity;
        this.attribute = attribute;
        this.alias = alias;
        this.joinType = joinType;
    }

    /**
     * Constructor for the RsqlJoin class.
     *
     * @param path The path of the join.
     * @param attribute The attribute to be joined on.
     * @param entity The entity to be joined.
     * @param alias The alias of the join.
     * @param parentAlias The parent alias of the join.
     * @param root The root of the join.
     * @param joinType The type of the join operation.
     */
    public RsqlJoin(String path, String attribute, String entity, String alias, String parentAlias, Path<?> root, String joinType) {
        this.path = path;
        this.attribute = attribute;
        this.entity = entity;
        this.alias = alias;
        this.parentAlias = parentAlias;
        this.root = root;
        this.joinType = joinType;
    }

    /**
     * Copy constructor for the RsqlJoin class.
     *
     * @param otherJoin The RsqlJoin object to be copied.
     */
    public RsqlJoin(RsqlJoin otherJoin) {
        this.path = otherJoin.path;
        this.attribute = otherJoin.attribute;
        this.entity = otherJoin.entity;
        this.alias = otherJoin.alias;
        this.parentAlias = otherJoin.parentAlias;
        this.root = otherJoin.root;
        this.joinType = otherJoin.joinType;
        this.fullPath = otherJoin.fullPath;
    }

    /**
     * Overrides the toString method to provide a string representation of the RsqlJoin object.
     *
     * @return The string representation of the RsqlJoin object.
     */
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
