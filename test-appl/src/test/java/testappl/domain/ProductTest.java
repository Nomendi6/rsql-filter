package testappl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static testappl.domain.ProductTestSamples.*;
import static testappl.domain.ProductTestSamples.*;
import static testappl.domain.ProductTypeTestSamples.*;

import org.junit.jupiter.api.Test;
import testappl.web.rest.TestUtil;

class ProductTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Product.class);
        Product product1 = getProductSample1();
        Product product2 = new Product();
        assertThat(product1).isNotEqualTo(product2);

        product2.setId(product1.getId());
        assertThat(product1).isEqualTo(product2);

        product2 = getProductSample2();
        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void tproductTest() throws Exception {
        Product product = getProductRandomSampleGenerator();
        ProductType productTypeBack = getProductTypeRandomSampleGenerator();

        product.setTproduct(productTypeBack);
        assertThat(product.getTproduct()).isEqualTo(productTypeBack);

        product.tproduct(null);
        assertThat(product.getTproduct()).isNull();
    }

    @Test
    void parentTest() throws Exception {
        Product product = getProductRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        product.setParent(productBack);
        assertThat(product.getParent()).isEqualTo(productBack);

        product.parent(null);
        assertThat(product.getParent()).isNull();
    }
}
