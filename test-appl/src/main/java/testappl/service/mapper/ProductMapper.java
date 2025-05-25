// Template: EntityMapper | v3.0
package testappl.service.mapper;

import org.mapstruct.*;
import testappl.domain.Product;
import testappl.domain.ProductType;
import testappl.service.dto.ProductDTO;
import testappl.service.dto.ProductTypeDTO;

/**
 * Mapper for the entity {@link Product} and its DTO {@link ProductDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper extends EntityMapper<ProductDTO, Product> {
    @Mapping(target = "tproduct", source = "tproduct", qualifiedByName = "productTypeName")
    @Mapping(target = "parent", source = "parent", qualifiedByName = "productName")
    ProductDTO toDto(Product s);

    @Named("productTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    ProductTypeDTO toDtoProductTypeName(ProductType productType);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    ProductDTO toDtoProductName(Product product);
}
