package testappl.service.mapper;

import org.mapstruct.*;
import testappl.domain.AppObject;
import testappl.domain.Product;
import testappl.service.dto.AppObjectDTO;
import testappl.service.dto.ProductDTO;

/**
 * Mapper for the entity {@link AppObject} and its DTO {@link AppObjectDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppObjectMapper extends EntityMapper<AppObjectDTO, AppObject> {
    @Mapping(target = "parent", source = "parent", qualifiedByName = "appObjectName")
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "product2", source = "product2", qualifiedByName = "productName")
    @Mapping(target = "product3", source = "product3", qualifiedByName = "productName")
    AppObjectDTO toDto(AppObject s);

    @Named("appObjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    AppObjectDTO toDtoAppObjectName(AppObject appObject);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);
}
