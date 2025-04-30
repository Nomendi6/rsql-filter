package testappl.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testappl.domain.ProductType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.service.dto.ProductTypeDTO;

class ProductTypeMapperTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    private static final Long DEFAULT_SEQ = 1L;

    private static final StandardRecordStatus DEFAULT_STATUS = StandardRecordStatus.ACTIVE;

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);

    private static final Instant DEFAULT_VALID_UNTIL = Instant.ofEpochMilli(0L);

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    private ProductTypeMapper productTypeMapper;

    private ProductTypeDTO createNewProductTypeDTO() {
        ProductTypeDTO productTypeDTO = new ProductTypeDTO();
        productTypeDTO.setCode(DEFAULT_CODE);
        productTypeDTO.setName(DEFAULT_NAME);
        productTypeDTO.setDescription(DEFAULT_DESCRIPTION);
        productTypeDTO.setSeq(DEFAULT_SEQ);
        productTypeDTO.setStatus(DEFAULT_STATUS);
        productTypeDTO.setValidFrom(DEFAULT_VALID_FROM);
        productTypeDTO.setValidUntil(DEFAULT_VALID_UNTIL);

        return productTypeDTO;
    }

    private ProductType createNewProductType() {
        ProductType productType = new ProductType();
        productType.setCode(DEFAULT_CODE);
        productType.setName(DEFAULT_NAME);
        productType.setDescription(DEFAULT_DESCRIPTION);
        productType.setSeq(DEFAULT_SEQ);
        productType.setStatus(DEFAULT_STATUS);
        productType.setValidFrom(DEFAULT_VALID_FROM);
        productType.setValidUntil(DEFAULT_VALID_UNTIL);

        return productType;
    }

    @BeforeEach
    public void setUp() {
        productTypeMapper = new ProductTypeMapperImpl();
    }

    @Test
    void entityToDtoMapping() {
        ProductType productType = createNewProductType();
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        assertThat(productTypeDTO).isNotNull();
        assertThat(productTypeDTO.getCode()).isEqualTo(productType.getCode());
        assertThat(productTypeDTO.getName()).isEqualTo(productType.getName());
        assertThat(productTypeDTO.getDescription()).isEqualTo(productType.getDescription());
        assertThat(productTypeDTO.getSeq()).isEqualTo(productType.getSeq());
        assertThat(productTypeDTO.getStatus()).isEqualTo(productType.getStatus());
        assertThat(productTypeDTO.getValidFrom()).isEqualTo(productType.getValidFrom());
        assertThat(productTypeDTO.getValidUntil()).isEqualTo(productType.getValidUntil());
    }

    @Test
    void dtoToEntityMapping() {
        ProductTypeDTO productTypeDTO = createNewProductTypeDTO();
        ProductType productType = productTypeMapper.toEntity(productTypeDTO);

        assertThat(productType).isNotNull();
        assertThat(productType.getCode()).isEqualTo(productTypeDTO.getCode());
        assertThat(productType.getName()).isEqualTo(productTypeDTO.getName());
        assertThat(productType.getDescription()).isEqualTo(productTypeDTO.getDescription());
        assertThat(productType.getSeq()).isEqualTo(productTypeDTO.getSeq());
        assertThat(productType.getStatus()).isEqualTo(productTypeDTO.getStatus());
        assertThat(productType.getValidFrom()).isEqualTo(productTypeDTO.getValidFrom());
        assertThat(productType.getValidUntil()).isEqualTo(productTypeDTO.getValidUntil());
    }
}
