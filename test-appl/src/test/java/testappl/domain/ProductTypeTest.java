package testappl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static testappl.domain.ProductTypeTestSamples.*;

import org.junit.jupiter.api.Test;
import testappl.web.rest.TestUtil;

class ProductTypeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductType.class);
        ProductType productType1 = getProductTypeSample1();
        ProductType productType2 = new ProductType();
        assertThat(productType1).isNotEqualTo(productType2);

        productType2.setId(productType1.getId());
        assertThat(productType1).isEqualTo(productType2);

        productType2 = getProductTypeSample2();
        assertThat(productType1).isNotEqualTo(productType2);
    }
}
