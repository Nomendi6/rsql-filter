package rsql.where;

import java.util.ArrayList;
import java.util.List;

public class RsqlQueryParamList {

    public String name;
    public String entity;
    public String alias;
    public List<Object> list = new ArrayList<>();

    public RsqlQueryParamList(String name, List<Object> list) {
        this.name = name;
        this.list.addAll(list);
    }

    public RsqlQueryParamList(String name, String entity, String alias, List<Object> list) {
        this.name = name;
        this.entity = entity;
        this.alias = alias;
        this.list.addAll(list);
    }

    @Override
    public String toString() {
        return "RsqlQueryParam{" + "name='" + name + '\'' + ", list.size=" + list.size() + '}';
    }
}
