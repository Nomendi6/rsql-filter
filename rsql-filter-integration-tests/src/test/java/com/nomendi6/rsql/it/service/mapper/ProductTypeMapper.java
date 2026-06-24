package com.nomendi6.rsql.it.service.mapper;

import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.service.dto.ProductTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import rsql.mapper.EntityMapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductTypeMapper extends EntityMapper<ProductTypeDTO, ProductType> {
}