package com.nomendi6.rsql.demo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.nomendi6.rsql.demo.web.rest.TestUtil;

class AppObjectDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppObjectDTO.class);
        AppObjectDTO appObjectDTO1 = new AppObjectDTO();
        appObjectDTO1.setId(1L);
        AppObjectDTO appObjectDTO2 = new AppObjectDTO();
        assertThat(appObjectDTO1).isNotEqualTo(appObjectDTO2);
        appObjectDTO2.setId(appObjectDTO1.getId());
        assertThat(appObjectDTO1).isEqualTo(appObjectDTO2);
        appObjectDTO2.setId(2L);
        assertThat(appObjectDTO1).isNotEqualTo(appObjectDTO2);
        appObjectDTO1.setId(null);
        assertThat(appObjectDTO1).isNotEqualTo(appObjectDTO2);
    }
}
