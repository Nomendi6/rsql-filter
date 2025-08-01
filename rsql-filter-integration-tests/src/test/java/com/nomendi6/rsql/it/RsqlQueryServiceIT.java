package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.AppObject;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.repository.AppObjectRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.service.dto.AppObjectDTO;
import com.nomendi6.rsql.it.service.dto.ProductTypeDTO;
import com.nomendi6.rsql.it.service.mapper.AppObjectMapper;
import com.nomendi6.rsql.it.service.mapper.ProductTypeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;
import java.time.Instant;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest

public class RsqlQueryServiceIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private AppObjectRepository appObjectRepository;

    @Autowired
    private AppObjectMapper appObjectMapper;

    private RsqlQueryService<AppObject, AppObjectDTO, AppObjectRepository, AppObjectMapper> queryService;

    private String jpqlSelectAll = "SELECT new AppObject(a0.id, a0.code, a0.name) FROM AppObject a0";

    private String jpqlSelectAllCount = "SELECT count(distinct a0) FROM AppObject a0";
    @BeforeEach
    void init() {
        queryService = new RsqlQueryService<>(appObjectRepository, appObjectMapper, em, AppObject.class, jpqlSelectAll, jpqlSelectAllCount);
    }

    @Test
    void testSelectAll() {
        String filter = "";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<AppObjectDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testSelectAllWithNameFilter() {
        String filter = "name=*'A*'";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<AppObjectDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testSelectAllWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<AppObjectDTO> result = queryService.findByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void sortedListWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<AppObjectDTO> result = queryService.findByFilterAndSort(filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void pageableWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<AppObjectDTO> result = queryService.findByFilter(filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void jpqlWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<AppObjectDTO> result = queryService.getJpqlQueryResult(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void jpqlPageWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        Page<AppObjectDTO> result = queryService.getJpqlQueryResultAsPage(jpqlSelectAll, jpqlSelectAllCount, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void tupleWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<Tuple> result = queryService.getJpqlQueryResultAsTuple(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void countWithComplexFilter1() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);

        long countByFilter = queryService.countByFilter(filter);
        assertThat(countByFilter).isNotNull();
    }

    @Test
    void testFindEntitiesByFilter() {
        String filter = "";
        
        List<AppObject> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithNameFilter() {
        String filter = "name=*'A*'";
        
        List<AppObject> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithComplexFilter() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        
        List<AppObject> result = queryService.findEntitiesByFilter(filter);
        assertThat(result).isNotNull();
    }

    @Test
    void testFindEntitiesByFilterWithSorting() {
        String filter = "parent.id=gt=1 and product.id=gt=1";
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        
        // Since findEntitiesByFilter doesn't support sorting directly, we'll use getJpqlQueryEntities
        List<AppObject> result = queryService.getJpqlQueryEntities(jpqlSelectAll, filter, pageable);
        assertThat(result).isNotNull();
        // If there are results, verify they're properly sorted
        if (!result.isEmpty() && result.size() > 1) {
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getId() <= result.get(i + 1).getId());
            }
        }
    }
}
