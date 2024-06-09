package rsql.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a Data Transfer Object (DTO) for the Lov entity.
 * It implements the Serializable interface to allow it to be sent over the network.
 */
public class LovDTO implements Serializable {

    /**
     * The id of the Lov entity.
     */
    protected Long id;
    /**
     *  The code of the Lov entity.
     */
    protected String code;
    /**
     * The name of the Lov entity.
     */
    protected String name;

    /**
     * Default constructor for the LovDTO class.
     */
    public LovDTO() {}

    /**
     * Constructor for the LovDTO class.
     *
     * @param id The id of the Lov entity.
     * @param code The code of the Lov entity.
     * @param name The name of the Lov entity.
     */
    public LovDTO(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    /**
     * Constructor for the LovDTO class.
     *
     * @param id The id of the Lov entity.
     * @param name The name of the Lov entity.
     */
    public LovDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters for the LovDTO class properties.

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

    /**
     * Overrides the equals method to compare LovDTO objects based on their properties.
     *
     * @param o The object to be compared with the current LovDTO object.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LovDTO)) return false;
        LovDTO lovDTO = (LovDTO) o;
        return id.equals(lovDTO.id) && Objects.equals(code, lovDTO.code) && Objects.equals(name, lovDTO.name);
    }

    /**
     * Overrides the hashCode method to generate a hash code for the LovDTO object based on its properties.
     *
     * @return The generated hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }

    /**
     * Overrides the toString method to provide a string representation of the LovDTO object.
     *
     * @return The string representation of the LovDTO object.
     */
    @Override
    public String toString() {
        return "LovDTO{" + "id=" + id + ", code='" + code + '\'' + ", name='" + name + '\'' + '}';
    }
}
