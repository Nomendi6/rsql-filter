package rsql.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * This interface provides a contract for a generic DTO to entity mapper.
 * It defines methods for mapping to and from DTOs and entities, as well as for handling lists of DTOs and entities.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 */
public interface EntityMapper<D, E> {

    /**
     * Maps a DTO to an entity.
     *
     * @param dto The DTO to be mapped to an entity.
     * @return The mapped entity.
     */
    E toEntity(D dto);

    /**
     * Maps an entity to a DTO.
     *
     * @param entity The entity to be mapped to a DTO.
     * @return The mapped DTO.
     */
    D toDto(E entity);

    /**
     * Maps a list of DTOs to a list of entities.
     *
     * @param dtoList The list of DTOs to be mapped to entities.
     * @return The list of mapped entities.
     */
    List<E> toEntity(List<D> dtoList);

    /**
     * Maps a list of entities to a list of DTOs.
     *
     * @param entityList The list of entities to be mapped to DTOs.
     * @return The list of mapped DTOs.
     */
    List<D> toDto(List<E> entityList);

    /**
     * Performs a partial update on an entity using a DTO.
     * Only non-null properties of the DTO are mapped to the entity.
     *
     * @param entity The target entity to be updated.
     * @param dto The DTO to be used for the update.
     */
    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}
