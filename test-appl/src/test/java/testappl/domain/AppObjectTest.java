package testappl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static testappl.domain.AppObjectTestSamples.*;
import static testappl.domain.AppObjectTestSamples.*;

import org.junit.jupiter.api.Test;
import testappl.web.rest.TestUtil;

class AppObjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AppObject.class);
        AppObject appObject1 = getAppObjectSample1();
        AppObject appObject2 = new AppObject();
        assertThat(appObject1).isNotEqualTo(appObject2);

        appObject2.setId(appObject1.getId());
        assertThat(appObject1).isEqualTo(appObject2);

        appObject2 = getAppObjectSample2();
        assertThat(appObject1).isNotEqualTo(appObject2);
    }

    @Test
    void parentTest() throws Exception {
        AppObject appObject = getAppObjectRandomSampleGenerator();
        AppObject appObjectBack = getAppObjectRandomSampleGenerator();

        appObject.setParent(appObjectBack);
        assertThat(appObject.getParent()).isEqualTo(appObjectBack);

        appObject.parent(null);
        assertThat(appObject.getParent()).isNull();
    }
}
