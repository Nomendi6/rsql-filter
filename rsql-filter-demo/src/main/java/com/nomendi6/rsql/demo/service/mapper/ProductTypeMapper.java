// Template: EntityMapper | v3.0
package com.nomendi6.rsql.demo.service.mapper;

import org.mapstruct.*;
import com.nomendi6.rsql.demo.domain.ProductType;
import com.nomendi6.rsql.demo.service.dto.ProductTypeDTO;

/**
 * Mapper for the entity {@link ProductType} and its DTO {@link ProductTypeDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductTypeMapper extends EntityMapper<ProductTypeDTO, ProductType> {}
