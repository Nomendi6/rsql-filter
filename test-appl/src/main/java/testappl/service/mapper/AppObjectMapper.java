package testappl.service.mapper;

import org.mapstruct.*;
import testappl.domain.AppObject;
import testappl.service.dto.AppObjectDTO;

/**
 * Mapper for the entity {@link AppObject} and its DTO {@link AppObjectDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppObjectMapper extends EntityMapper<AppObjectDTO, AppObject> {
    @Mapping(target = "parent", source = "parent", qualifiedByName = "appObjectName")
    AppObjectDTO toDto(AppObject s);

    @Named("appObjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    AppObjectDTO toDtoAppObjectName(AppObject appObject);
}
