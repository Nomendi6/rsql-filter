package com.nomendi6.rsql.it.service.mapper;

import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rsql.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductDTO, Product> {

    @Override
    @Mapping(source = "productType.id", target = "productTypeId")
    @Mapping(source = "productType.name", target = "productTypeName")
    ProductDTO toDto(Product entity);

    @Override
    @Mapping(source = "productTypeId", target = "productType.id")
    @Mapping(target = "productType.name", ignore = true)
    Product toEntity(ProductDTO dto);

    default ProductType fromId(Long id) {
        if (id == null) {
            return null;
        }
        ProductType productType = new ProductType();
        productType.setId(id);
        return productType;
    }
}