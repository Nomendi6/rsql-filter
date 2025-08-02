package com.nomendi6.rsql.it.service.mapper;

import com.nomendi6.rsql.it.domain.AppObject;
import com.nomendi6.rsql.it.service.dto.AppObjectDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rsql.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface AppObjectMapper extends EntityMapper<AppObjectDTO, AppObject> {

    @Override
    @Mapping(source = "parent.id", target = "parentId")
    AppObjectDTO toDto(AppObject entity);

    @Override
    @Mapping(source = "parentId", target = "parent.id")
    AppObject toEntity(AppObjectDTO dto);

    default AppObject fromId(Long id) {
        if (id == null) {
            return null;
        }
        AppObject appObject = new AppObject();
        appObject.setId(id);
        return appObject;
    }
}