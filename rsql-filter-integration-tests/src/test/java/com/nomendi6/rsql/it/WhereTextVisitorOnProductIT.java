package com.nomendi6.rsql.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Path;
import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.hibernate.spi.NavigablePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rsql.where.RsqlContext;
import rsql.where.WhereTextVisitor;
import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class WhereTextVisitorOnProductIT {

    @Autowired
    private EntityManager em;

    private WhereTextVisitor<Product> visitor;
    private RsqlContext<Product> rsqlContext;

    @BeforeEach
    void init() {
        visitor = new WhereTextVisitor<>();
        rsqlContext = new RsqlContext<>(Product.class);
        rsqlContext.defineEntityManager(em);
        rsqlContext.initContext();
        rsqlContext.root.alias("a0");
        visitor.setSpecificationContext(rsqlContext);
    }

    @Test
    void simpleFieldFromAppObjectPath() {
        Path<?> path = visitor.getPropertyPathRecursive("name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = visitor.getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("name");
    }

    @Test
    void parentRel() {
        Path<?> path = visitor.getPropertyPathRecursive("parent.name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = visitor.getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("parent.name");
    }

    @Test
    void productTypeId() {
        Path<?> path = visitor.getPropertyPathRecursive("productType.id", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = visitor.getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("productType.id");
    }
}
