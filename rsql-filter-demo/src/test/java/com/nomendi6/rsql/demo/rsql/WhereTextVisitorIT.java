package com.nomendi6.rsql.demo.rsql;

import jakarta.persistence.criteria.Path;
import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.hibernate.spi.NavigablePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rsql.RsqlCompiler;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.RsqlContext;
import rsql.where.RsqlQuery;
import rsql.where.WhereTextVisitor;
import com.nomendi6.rsql.demo.IntegrationTest;
import com.nomendi6.rsql.demo.domain.AppObject;
import com.nomendi6.rsql.demo.domain.Product;
import com.nomendi6.rsql.demo.domain.ProductType;
import com.nomendi6.rsql.demo.domain.enumeration.StandardRecordStatus;
import com.nomendi6.rsql.demo.repository.AppObjectRepository;
import com.nomendi6.rsql.demo.repository.ProductRepository;
import com.nomendi6.rsql.demo.repository.ProductTypeRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
public class WhereTextVisitorIT {

    @Autowired
    private EntityManager em;

    private WhereTextVisitor<AppObject> visitor;
    private RsqlContext<AppObject> rsqlContext;

    @BeforeEach
    void init() {
        visitor = new WhereTextVisitor<>();
        rsqlContext = new RsqlContext<>(AppObject.class);
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
    void parentRel2() {
        Path<?> path = visitor.getPropertyPathRecursive("parent.parent.name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = visitor.getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("parent.parent.name");
    }

    @Test
    void preductRel() {
        Path<?> path = visitor.getPropertyPathRecursive("product.name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = visitor.getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("product.name");
    }

}
