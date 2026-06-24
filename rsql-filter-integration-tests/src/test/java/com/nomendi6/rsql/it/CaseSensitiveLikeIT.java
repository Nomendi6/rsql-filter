package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for the case-sensitive LIKE operators
 * (=clike= / =^* and =cnlike= / =!^* / !=^*).
 *
 * <p>Seeds product codes {@code ABC}, {@code Abc}, {@code abc} and verifies that the
 * database actually discriminates case for the new operators, while the existing
 * case-insensitive {@code =like=} still matches all three (no regression).</p>
 *
 * <p>NOTE: this relies on a case-sensitive LIKE at the DB level (H2 default / PostgreSQL).</p>
 */
@IntegrationTest
public class CaseSensitiveLikeIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductMapper productMapper;

    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

    private final Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

    @BeforeEach
    @Transactional
    void setup() {
        queryService = new RsqlQueryService<>(productRepository, productMapper, em, Product.class);

        ProductType type = productTypeRepository.save(
            new ProductType().withCode("T").withName("Type").withDescription("t"));

        for (String code : new String[] { "ABC", "Abc", "abc" }) {
            productRepository.save(new Product()
                .withCode(code)
                .withName(code)
                .withDescription(code)
                .withPrice(new BigDecimal("1.00"))
                .withStatus(StandardRecordStatus.ACTIVE)
                .withProductType(type));
        }
    }

    @AfterEach
    @Transactional
    void cleanup() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    @Test
    void existingLikeRemainsCaseInsensitive() {
        // =like= lowercases both column and pattern -> matches ABC, Abc, abc
        List<ProductDTO> result = queryService.findByFilterAndSort("code=like='abc'", pageable);
        assertThat(result).extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("ABC", "Abc", "abc");
    }

    @Test
    void clikeExactMatchIsCaseSensitive() {
        // =clike='Abc' -> code LIKE 'Abc' (no lower) -> only the exact-case row
        List<ProductDTO> result = queryService.findByFilterAndSort("code=clike='Abc'", pageable);
        assertThat(result).extracting(ProductDTO::getCode).containsExactly("Abc");
    }

    @Test
    void clikePrefixIsCaseSensitive() {
        // =clike='A*' -> code LIKE 'A%' (uppercase A) -> ABC, Abc ; NOT abc
        List<ProductDTO> result = queryService.findByFilterAndSort("code=clike='A*'", pageable);
        assertThat(result).extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("ABC", "Abc");
    }

    @Test
    void symbolicClikeEqualsKeyword() {
        // =^* is an alias of =clike=
        List<ProductDTO> result = queryService.findByFilterAndSort("code=^*'A*'", pageable);
        assertThat(result).extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("ABC", "Abc");
    }

    @Test
    void cnlikeIsComplementCaseSensitive() {
        // =cnlike='A*' -> code NOT LIKE 'A%' -> only abc
        List<ProductDTO> result = queryService.findByFilterAndSort("code=cnlike='A*'", pageable);
        assertThat(result).extracting(ProductDTO::getCode).containsExactly("abc");
    }

    @Test
    void symbolicCnlikeEqualsKeyword() {
        assertThat(queryService.findByFilterAndSort("code=!^*'A*'", pageable))
            .extracting(ProductDTO::getCode).containsExactly("abc");
        assertThat(queryService.findByFilterAndSort("code!=^*'A*'", pageable))
            .extracting(ProductDTO::getCode).containsExactly("abc");
    }
}
