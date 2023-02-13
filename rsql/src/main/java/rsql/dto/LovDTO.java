package rsql.dto;

import java.io.Serializable;
import java.util.Objects;

public class LovDTO implements Serializable {

    protected Long id;
    protected String code;
    protected String name;

    public LovDTO() {}

    public LovDTO(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public LovDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LovDTO)) return false;
        LovDTO lovDTO = (LovDTO) o;
        return id.equals(lovDTO.id) && Objects.equals(code, lovDTO.code) && Objects.equals(name, lovDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }

    @Override
    public String toString() {
        return "LovDTO{" + "id=" + id + ", code='" + code + '\'' + ", name='" + name + '\'' + '}';
    }
}
