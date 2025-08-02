package com.nomendi6.rsql.demo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.nomendi6.rsql.demo.web.rest.TestUtil;

class AppObjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppObject.class);
        AppObject appObject1 = new AppObject();
        appObject1.setId(1L);
        AppObject appObject2 = new AppObject();
        appObject2.setId(appObject1.getId());
        assertThat(appObject1).isEqualTo(appObject2);
        appObject2.setId(2L);
        assertThat(appObject1).isNotEqualTo(appObject2);
        appObject1.setId(null);
        assertThat(appObject1).isNotEqualTo(appObject2);
    }
}
