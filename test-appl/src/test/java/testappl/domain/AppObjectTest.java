package testappl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static testappl.domain.AppObjectTestSamples.*;
import static testappl.domain.AppObjectTestSamples.*;
import static testappl.domain.ProductTestSamples.*;

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

    @Test
    void productTest() throws Exception {
        AppObject appObject = getAppObjectRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        appObject.setProduct(productBack);
        assertThat(appObject.getProduct()).isEqualTo(productBack);

        appObject.product(null);
        assertThat(appObject.getProduct()).isNull();
    }

    @Test
    void product2Test() throws Exception {
        AppObject appObject = getAppObjectRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        appObject.setProduct2(productBack);
        assertThat(appObject.getProduct2()).isEqualTo(productBack);

        appObject.product2(null);
        assertThat(appObject.getProduct2()).isNull();
    }

    @Test
    void product3Test() throws Exception {
        AppObject appObject = getAppObjectRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        appObject.setProduct3(productBack);
        assertThat(appObject.getProduct3()).isEqualTo(productBack);

        appObject.product3(null);
        assertThat(appObject.getProduct3()).isNull();
    }
}
