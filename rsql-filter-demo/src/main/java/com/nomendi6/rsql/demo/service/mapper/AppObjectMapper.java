// Template: EntityMapper | v3.0
package com.nomendi6.rsql.demo.service.mapper;

import org.mapstruct.*;
import com.nomendi6.rsql.demo.domain.AppObject;
import com.nomendi6.rsql.demo.service.dto.AppObjectDTO;

/**
 * Mapper for the entity {@link AppObject} and its DTO {@link AppObjectDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppObjectMapper extends EntityMapper<AppObjectDTO, AppObject> {
    @Mapping(target = "parent", source = "parent", qualifiedByName = "appObjectName")
    AppObjectDTO toDto(AppObject s);

    @Named("appObjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    AppObjectDTO toDtoAppObjectName(AppObject appObject);
}
