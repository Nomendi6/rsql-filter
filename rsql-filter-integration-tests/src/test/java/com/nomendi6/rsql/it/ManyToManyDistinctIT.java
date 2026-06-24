package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.Tag;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.TagRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.where.RsqlContext;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class ManyToManyDistinctIT {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    private RsqlContext<Product> rsqlContext;
    private RsqlCompiler<Product> compiler;

    @BeforeEach
    void init() {
        productRepository.deleteAll();
        tagRepository.deleteAll();

        Tag java = tagRepository.save(new Tag().withName("java"));
        Tag spring = tagRepository.save(new Tag().withName("spring"));
        Tag sql = tagRepository.save(new Tag().withName("sql"));

        productRepository.save(new Product()
            .withCode("P-JAVA-SPRING")
            .withName("Java Spring")
            .addTag(java)
            .addTag(spring));

        productRepository.save(new Product()
            .withCode("P-SQL")
            .withName("SQL")
            .addTag(sql));

        rsqlContext = new RsqlContext<>(Product.class)
            .defineEntityManager(entityManager);
        compiler = new RsqlCompiler<>();
    }

    @Test
    void manyToManyInFilterReturnsDistinctProducts() {
        Specification<Product> specification = compiler.compileToSpecification("tags.name=in=('java','spring')", rsqlContext);

        List<Product> result = productRepository.findAll(specification);

        assertThat(result)
            .extracting(Product::getCode)
            .containsExactly("P-JAVA-SPRING");
    }
}
