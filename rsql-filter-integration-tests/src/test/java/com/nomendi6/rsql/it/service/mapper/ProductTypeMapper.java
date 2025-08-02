package com.nomendi6.rsql.it.service.mapper;

import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.service.dto.ProductTypeDTO;
import org.mapstruct.Mapper;
import rsql.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface ProductTypeMapper extends EntityMapper<ProductTypeDTO, ProductType> {
}