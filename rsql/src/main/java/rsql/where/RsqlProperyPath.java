package rsql.where;

import jakarta.persistence.criteria.Path;

import java.util.Objects;

public class RsqlProperyPath {
    public RsqlJoin join;
    public int propertyIndex;

    public RsqlJoin getJoin() {
        return join;
    }

    public void setJoin(RsqlJoin join) {
        this.join = join;
    }

    public int getPropertyIndex() {
        return propertyIndex;
    }

    public void setPropertyIndex(int propertyIndex) {
        this.propertyIndex = propertyIndex;
    }

    RsqlProperyPath () {
        this.join = null;
        this.propertyIndex = 0;
    }

    RsqlProperyPath (RsqlJoin join, int propertyIndex) {
        this.join = join;
        this.propertyIndex = propertyIndex;
    }

    RsqlProperyPath (Path<?> root, String alias, int propertyIndex) {
        this.join = new RsqlJoin();
        this.join.root = root;
        this.join.alias = alias;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RsqlProperyPath that = (RsqlProperyPath) o;
        return propertyIndex == that.propertyIndex && Objects.equals(join, that.join);
    }

    @Override
    public int hashCode() {
        return Objects.hash(join, propertyIndex);
    }

    @Override
    public String toString() {
        return "RsqlProperyPath{" +
                "join=" + join +
                ", propertyIndex=" + propertyIndex +
                '}';
    }
}
