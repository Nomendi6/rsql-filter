// Template: EntityMapper | v3.0
package com.nomendi6.rsql.demo.service.mapper;

import org.mapstruct.*;
import com.nomendi6.rsql.demo.domain.Product;
import com.nomendi6.rsql.demo.domain.ProductType;
import com.nomendi6.rsql.demo.service.dto.ProductDTO;
import com.nomendi6.rsql.demo.service.dto.ProductTypeDTO;

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
