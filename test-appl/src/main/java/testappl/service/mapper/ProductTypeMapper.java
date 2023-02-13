package testappl.service.mapper;

import org.mapstruct.*;
import testappl.domain.ProductType;
import testappl.service.dto.ProductTypeDTO;

/**
 * Mapper for the entity {@link ProductType} and its DTO {@link ProductTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProductTypeMapper extends EntityMapper<ProductTypeDTO, ProductType> {}
