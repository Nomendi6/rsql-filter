package testappl.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testappl.domain.AppObject;
import testappl.domain.enumeration.AppObjectType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.service.dto.AppObjectDTO;

class AppObjectMapperTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";

    private static final AppObjectType DEFAULT_OBJECT_TYPE = AppObjectType.FUNCTIONAL_MODULE;

    private static final Instant DEFAULT_LAST_CHANGE = Instant.ofEpochMilli(0L);

    private static final Long DEFAULT_SEQ = 1L;

    private static final StandardRecordStatus DEFAULT_STATUS = StandardRecordStatus.ACTIVE;

    private static final Double DEFAULT_QUANTITY = 1D;

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);

    private static final Instant DEFAULT_VALID_UNTIL = Instant.ofEpochMilli(0L);

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    private AppObjectMapper appObjectMapper;

    private AppObjectDTO createNewAppObjectDTO() {
        AppObjectDTO appObjectDTO = new AppObjectDTO();
        appObjectDTO.setCode(DEFAULT_CODE);
        appObjectDTO.setName(DEFAULT_NAME);
        appObjectDTO.setDescription(DEFAULT_DESCRIPTION);
        appObjectDTO.setObjectType(DEFAULT_OBJECT_TYPE);
        appObjectDTO.setLastChange(DEFAULT_LAST_CHANGE);
        appObjectDTO.setSeq(DEFAULT_SEQ);
        appObjectDTO.setStatus(DEFAULT_STATUS);
        appObjectDTO.setQuantity(DEFAULT_QUANTITY);
        appObjectDTO.setValidFrom(DEFAULT_VALID_FROM);
        appObjectDTO.setValidUntil(DEFAULT_VALID_UNTIL);

        return appObjectDTO;
    }

    private AppObject createNewAppObject() {
        AppObject appObject = new AppObject();
        appObject.setCode(DEFAULT_CODE);
        appObject.setName(DEFAULT_NAME);
        appObject.setDescription(DEFAULT_DESCRIPTION);
        appObject.setObjectType(DEFAULT_OBJECT_TYPE);
        appObject.setLastChange(DEFAULT_LAST_CHANGE);
        appObject.setSeq(DEFAULT_SEQ);
        appObject.setStatus(DEFAULT_STATUS);
        appObject.setQuantity(DEFAULT_QUANTITY);
        appObject.setValidFrom(DEFAULT_VALID_FROM);
        appObject.setValidUntil(DEFAULT_VALID_UNTIL);

        return appObject;
    }

    @BeforeEach
    public void setUp() {
        appObjectMapper = new AppObjectMapperImpl();
    }

    @Test
    void entityToDtoMapping() {
        AppObject appObject = createNewAppObject();
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        assertThat(appObjectDTO).isNotNull();
        assertThat(appObjectDTO.getCode()).isEqualTo(appObject.getCode());
        assertThat(appObjectDTO.getName()).isEqualTo(appObject.getName());
        assertThat(appObjectDTO.getDescription()).isEqualTo(appObject.getDescription());
        assertThat(appObjectDTO.getObjectType()).isEqualTo(appObject.getObjectType());
        assertThat(appObjectDTO.getLastChange()).isEqualTo(appObject.getLastChange());
        assertThat(appObjectDTO.getSeq()).isEqualTo(appObject.getSeq());
        assertThat(appObjectDTO.getStatus()).isEqualTo(appObject.getStatus());
        assertThat(appObjectDTO.getQuantity()).isEqualTo(appObject.getQuantity());
        assertThat(appObjectDTO.getValidFrom()).isEqualTo(appObject.getValidFrom());
        assertThat(appObjectDTO.getValidUntil()).isEqualTo(appObject.getValidUntil());
    }

    @Test
    void dtoToEntityMapping() {
        AppObjectDTO appObjectDTO = createNewAppObjectDTO();
        AppObject appObject = appObjectMapper.toEntity(appObjectDTO);

        assertThat(appObject).isNotNull();
        assertThat(appObject.getCode()).isEqualTo(appObjectDTO.getCode());
        assertThat(appObject.getName()).isEqualTo(appObjectDTO.getName());
        assertThat(appObject.getDescription()).isEqualTo(appObjectDTO.getDescription());
        assertThat(appObject.getObjectType()).isEqualTo(appObjectDTO.getObjectType());
        assertThat(appObject.getLastChange()).isEqualTo(appObjectDTO.getLastChange());
        assertThat(appObject.getSeq()).isEqualTo(appObjectDTO.getSeq());
        assertThat(appObject.getStatus()).isEqualTo(appObjectDTO.getStatus());
        assertThat(appObject.getQuantity()).isEqualTo(appObjectDTO.getQuantity());
        assertThat(appObject.getValidFrom()).isEqualTo(appObjectDTO.getValidFrom());
        assertThat(appObject.getValidUntil()).isEqualTo(appObjectDTO.getValidUntil());
    }
}
