package testappl.service.mapper;

import org.junit.jupiter.api.BeforeEach;

class AppObjectMapperTest {

    private AppObjectMapper appObjectMapper;

    @BeforeEach
    public void setUp() {
        appObjectMapper = new AppObjectMapperImpl();
    }
}
