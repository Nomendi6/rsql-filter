package testappl.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductTypeMapperTest {

    private ProductTypeMapper productTypeMapper;

    @BeforeEach
    public void setUp() {
        productTypeMapper = new ProductTypeMapperImpl();
    }
}
