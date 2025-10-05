package com.nomendi6.rsql.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;
import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest

public class RsqlQueryServiceOnProductIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;
    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryServiceWithJoins;

    private String jpqlSelectAll = "SELECT new Product(a0.id, a0.code, a0.name, a0.productType.name) FROM Product a0";

    private String jpqlSelectAllCount = "SELECT count(distinct a0) FROM Product a0";

    // Custom JPQL with pre-existing JOINs (like user's setup)
    private String jpqlSelectAllWithJoins = "SELECT new Product(a0.id, a0.code, a0.name, a0.productType.name) FROM Product a0 left join a0.productType left join a0.parent";

    private String jpqlSelectAllCountWithJoins = "SELECT count(distinct a0) FROM Product a0";
    @BeforeEach
    void init() {
        queryService = new RsqlQueryService<>(productRepository, productMapper, em, Product.class, jpqlSelectAll, jpqlSelectAllCount);
        queryServiceWithJoins = new RsqlQueryService<>(productRepository, productMapper, em, Product.class, jpqlSelectAllWithJoins, jpqlSelectAllCountWithJoins);
    }

    @Test
    void testSelectAll() {
        String filter = "";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testSelectAllWithNameFilter() {
        String filter = "name=*'A*'";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testSelectAllWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void sortedListWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void pageableWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<ProductDTO> result = queryService.findByFilter(filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void jpqlWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.getJpqlQueryResult(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void jpqlPageWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<ProductDTO> result = queryService.getJpqlQueryResultAsPage(jpqlSelectAll, jpqlSelectAllCount, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void tupleWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<Tuple> result = queryService.getJpqlQueryResultAsTuple(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void countWithComplexFilter1() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        long countByFilter = queryService.countByFilter(filter);
        assertThat(countByFilter).isNotNull();
    }

    @Test
    void testFindEntitiesByFilter() {
        String filter = "";
        
        List<Product> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithNameFilter() {
        String filter = "name=*'A*'";
        
        List<Product> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithComplexFilter() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        
        List<Product> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithSorting() {
        String filter = "parent.id=gt=1 and productType.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        
        // Since findEntitiesByFilter doesn't support sorting directly, we'll use getJpqlQueryEntities
        List<Product> result = queryService.getJpqlQueryEntities(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
        // If there are results, verify they're properly sorted
        if (!result.isEmpty() && result.size() > 1) {
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getId() <= result.get(i + 1).getId());
            }
        }
    }

    @Test
    void testFindProductsByTProductId() {
        String filter = "productType.id==1";
        List<Product> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    /**
     * Test simple property filter with JPQL flow (findByFilterAndSort).
     * This test reproduces the bug where simple properties generate WHERE clause without alias prefix.
     * Expected: WHERE (a0.id=...)
     * Bug: WHERE (id=...) - missing a0 prefix
     */
    @Test
    void testJpqlFlowWithSimplePropertyFilter() {
        String filter = "id=gt=0";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    /**
     * Test that JPQL flow works with simple property equality filter.
     */
    @Test
    void testJpqlFlowWithSimplePropertyEqualityFilter() {
        String filter = "id==1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    /**
     * Test JPQL with pre-existing JOINs and simple property filter.
     * This reproduces the user's bug where:
     * - Custom JPQL has JOINs without explicit aliases: "left join a0.productType left join a0.parent"
     * - Filter uses simple property: "id==1"
     * - Expected: WHERE (a0.id=...)
     * - Bug: WHERE (id=...) - causing "Multiple from-elements expose unqualified attribute" error
     */
    @Test
    void testJpqlWithPreExistingJoinsAndSimplePropertyFilter() {
        String filter = "id==1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryServiceWithJoins.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    /**
     * Test JPQL with pre-existing JOINs and navigation property filter.
     */
    @Test
    void testJpqlWithPreExistingJoinsAndNavigationPropertyFilter() {
        String filter = "productType.id=gt=0";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryServiceWithJoins.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    /**
     * Test that reproduces the "id.id" bug where simple property filter generates
     * "id.id=:p1" instead of "a0.id=:p1" in WHERE clause.
     *
     * Bug scenario:
     * - Custom JPQL with multiple JOINs
     * - Filter: id==1
     * - Expected WHERE: (a0.id=:p1)
     * - Bug: (id.id=:p1)
     */
    @Test
    void testSimplePropertyIdFilterDoesNotGenerateIdDotId() {
        String filter = "id==1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        // This should generate WHERE (a0.id=:p1), NOT WHERE (id.id=:p1)
        List<ProductDTO> result = queryServiceWithJoins.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    /**
     * Test multiple simple property filters to ensure proper alias usage.
     */
    @Test
    void testMultipleSimplePropertiesWithJoins() {
        String filter = "id=gt=0;code=*'PROD*'";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryServiceWithJoins.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    /**
     * Test mix of simple and navigation properties with JPQL joins.
     */
    @Test
    void testMixedSimpleAndNavigationPropertiesWithJpqlJoins() {
        String filter = "id=gt=0;productType.code=='TYPE1'";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryServiceWithJoins.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }
}
