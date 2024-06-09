package rsql.where;

import jakarta.persistence.criteria.Path;

import java.util.Objects;

/**
 * This class represents a property path in an RSQL query.
 * It contains information about the join and the property index.
 */
public class RsqlProperyPath {
    public RsqlJoin join;
    public int propertyIndex;

    /**
     * Returns the join of the property path.
     *
     * @return The join of the property path.
     */
    public RsqlJoin getJoin() {
        return join;
    }

    /**
     * Sets the join of the property path.
     *
     * @param join The join to be set.
     */
    public void setJoin(RsqlJoin join) {
        this.join = join;
    }

    /**
     * Returns the property index of the property path.
     *
     * @return The property index of the property path.
     */
    public int getPropertyIndex() {
        return propertyIndex;
    }

    /**
     * Sets the property index of the property path.
     *
     * @param propertyIndex The property index to be set.
     */
    public void setPropertyIndex(int propertyIndex) {
        this.propertyIndex = propertyIndex;
    }

    /**
     * Default constructor for the RsqlProperyPath class.
     * Initializes the join to null and the property index to 0.
     */
    RsqlProperyPath () {
        this.join = null;
        this.propertyIndex = 0;
    }

    /**
     * Constructor for the RsqlProperyPath class.
     *
     * @param join The join to be set.
     * @param propertyIndex The property index to be set.
     */
    RsqlProperyPath (RsqlJoin join, int propertyIndex) {
        this.join = join;
        this.propertyIndex = propertyIndex;
    }

    /**
     * Constructor for the RsqlProperyPath class.
     *
     * @param root The root of the join.
     * @param alias The alias of the join.
     * @param propertyIndex The property index to be set.
     */
    RsqlProperyPath (Path<?> root, String alias, int propertyIndex) {
        this.join = new RsqlJoin();
        this.join.root = root;
        this.join.alias = alias;
        this.propertyIndex = propertyIndex;
    }

    /**
     * Overrides the equals method to provide a custom equality check for RsqlProperyPath objects.
     *
     * @param o The object to be compared for equality.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RsqlProperyPath that = (RsqlProperyPath) o;
        return propertyIndex == that.propertyIndex && Objects.equals(join, that.join);
    }

    /**
     * Overrides the hashCode method to provide a custom hash code for RsqlProperyPath objects.
     *
     * @return The hash code of the RsqlProperyPath object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(join, propertyIndex);
    }

    /**
     * Overrides the toString method to provide a string representation of the RsqlProperyPath object.
     *
     * @return The string representation of the RsqlProperyPath object.
     */
    @Override
    public String toString() {
        return "RsqlProperyPath{" +
                "join=" + join +
                ", propertyIndex=" + propertyIndex +
                '}';
    }
}
