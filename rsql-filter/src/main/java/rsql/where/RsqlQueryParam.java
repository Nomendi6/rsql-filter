package rsql.where;

public class RsqlQueryParam {

    public String name;
    public String entity;
    public String alias;
    public Object value;

    public RsqlQueryParam(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public RsqlQueryParam(String name, String entity, String alias, Object value) {
        this.name = name;
        this.entity = entity;
        this.alias = alias;
        this.value = value;
    }

    @Override
    public String toString() {
        return "RsqlQueryParam{" + "name='" + name + '\'' + ", value=" + value + '}';
    }
}
