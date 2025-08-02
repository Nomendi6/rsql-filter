package com.nomendi6.rsql.demo.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.nomendi6.rsql.demo.domain.Product;
import com.nomendi6.rsql.demo.domain.enumeration.StandardRecordStatus;
import com.nomendi6.rsql.demo.service.dto.ProductDTO;

class ProductMapperTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    private static final Long DEFAULT_SEQ = 1L;

    private static final StandardRecordStatus DEFAULT_STATUS = StandardRecordStatus.ACTIVE;

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);

    private static final Instant DEFAULT_VALID_UNTIL = Instant.ofEpochMilli(0L);

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    private ProductMapper productMapper;

    private ProductDTO createNewProductDTO() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setCode(DEFAULT_CODE);
        productDTO.setName(DEFAULT_NAME);
        productDTO.setDescription(DEFAULT_DESCRIPTION);
        productDTO.setSeq(DEFAULT_SEQ);
        productDTO.setStatus(DEFAULT_STATUS);
        productDTO.setValidFrom(DEFAULT_VALID_FROM);
        productDTO.setValidUntil(DEFAULT_VALID_UNTIL);

        return productDTO;
    }

    private Product createNewProduct() {
        Product product = new Product();
        product.setCode(DEFAULT_CODE);
        product.setName(DEFAULT_NAME);
        product.setDescription(DEFAULT_DESCRIPTION);
        product.setSeq(DEFAULT_SEQ);
        product.setStatus(DEFAULT_STATUS);
        product.setValidFrom(DEFAULT_VALID_FROM);
        product.setValidUntil(DEFAULT_VALID_UNTIL);

        return product;
    }

    @BeforeEach
    public void setUp() {
        productMapper = new ProductMapperImpl();
    }

    @Test
    void entityToDtoMapping() {
        Product product = createNewProduct();
        ProductDTO productDTO = productMapper.toDto(product);

        assertThat(productDTO).isNotNull();
        assertThat(productDTO.getCode()).isEqualTo(product.getCode());
        assertThat(productDTO.getName()).isEqualTo(product.getName());
        assertThat(productDTO.getDescription()).isEqualTo(product.getDescription());
        assertThat(productDTO.getSeq()).isEqualTo(product.getSeq());
        assertThat(productDTO.getStatus()).isEqualTo(product.getStatus());
        assertThat(productDTO.getValidFrom()).isEqualTo(product.getValidFrom());
        assertThat(productDTO.getValidUntil()).isEqualTo(product.getValidUntil());
    }

    @Test
    void dtoToEntityMapping() {
        ProductDTO productDTO = createNewProductDTO();
        Product product = productMapper.toEntity(productDTO);

        assertThat(product).isNotNull();
        assertThat(product.getCode()).isEqualTo(productDTO.getCode());
        assertThat(product.getName()).isEqualTo(productDTO.getName());
        assertThat(product.getDescription()).isEqualTo(productDTO.getDescription());
        assertThat(product.getSeq()).isEqualTo(productDTO.getSeq());
        assertThat(product.getStatus()).isEqualTo(productDTO.getStatus());
        assertThat(product.getValidFrom()).isEqualTo(productDTO.getValidFrom());
        assertThat(product.getValidUntil()).isEqualTo(productDTO.getValidUntil());
    }
}
