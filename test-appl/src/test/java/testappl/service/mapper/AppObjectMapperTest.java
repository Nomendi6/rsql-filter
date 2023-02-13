package testappl.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppObjectMapperTest {

    private AppObjectMapper appObjectMapper;

    @BeforeEach
    public void setUp() {
        appObjectMapper = new AppObjectMapperImpl();
    }
}
