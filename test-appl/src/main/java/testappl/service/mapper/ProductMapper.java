package testappl.service.mapper;

import org.mapstruct.*;
import testappl.domain.Product;
import testappl.domain.ProductType;
import testappl.service.dto.ProductDTO;
import testappl.service.dto.ProductTypeDTO;

/**
 * Mapper for the entity {@link Product} and its DTO {@link ProductDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductDTO, Product> {
    @Mapping(target = "tproduct", source = "tproduct", qualifiedByName = "productTypeName")
    @Mapping(target = "parent", source = "parent", qualifiedByName = "productName")
    ProductDTO toDto(Product s);

    @Named("productTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductTypeDTO toDtoProductTypeName(ProductType productType);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);
}
