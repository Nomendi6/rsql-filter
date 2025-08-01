package com.nomendi6.rsql.demo.rsql;

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
import com.nomendi6.rsql.demo.IntegrationTest;
import com.nomendi6.rsql.demo.domain.Product;
import com.nomendi6.rsql.demo.repository.ProductRepository;
import com.nomendi6.rsql.demo.service.dto.ProductDTO;
import com.nomendi6.rsql.demo.service.mapper.ProductMapper;

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

    private String jpqlSelectAll = "SELECT new Product(a0.id, a0.code, a0.name, a0.tproduct.name) FROM Product a0";

    private String jpqlSelectAllCount = "SELECT count(distinct a0) FROM Product a0";
    @BeforeEach
    void init() {
        queryService = new RsqlQueryService<>(productRepository, productMapper, em, Product.class, jpqlSelectAll, jpqlSelectAllCount);
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
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void sortedListWithComplexFilter1() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void pageableWithComplexFilter1() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<ProductDTO> result = queryService.findByFilter(filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void jpqlWithComplexFilter1() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<ProductDTO> result = queryService.getJpqlQueryResult(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void jpqlPageWithComplexFilter1() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<ProductDTO> result = queryService.getJpqlQueryResultAsPage(jpqlSelectAll, jpqlSelectAllCount, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void tupleWithComplexFilter1() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<Tuple> result = queryService.getJpqlQueryResultAsTuple(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void countWithComplexFilter1() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
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
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
        
        List<Product> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithSorting() {
        String filter = "parent.id=gt=1 and tproduct.id=gt=1";
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
        String filter = "tproduct.id==1";
        List<Product> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }
}
