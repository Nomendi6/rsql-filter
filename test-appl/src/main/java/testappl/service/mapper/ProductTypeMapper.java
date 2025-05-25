// Template: EntityMapper | v3.0
package testappl.service.mapper;

import org.mapstruct.*;
import testappl.domain.ProductType;
import testappl.service.dto.ProductTypeDTO;

/**
 * Mapper for the entity {@link ProductType} and its DTO {@link ProductTypeDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductTypeMapper extends EntityMapper<ProductTypeDTO, ProductType> {}
