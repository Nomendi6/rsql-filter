package testappl.rsql;

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
import testappl.IntegrationTest;
import testappl.domain.AppObject;
import testappl.domain.Product;
import testappl.domain.ProductType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.repository.AppObjectRepository;
import testappl.repository.ProductRepository;
import testappl.repository.ProductTypeRepository;

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
        String pathString = getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("name");
    }

    @Test
    void parentRel() {
        Path<?> path = visitor.getPropertyPathRecursive("parent.name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("parent.name");
    }

    @Test
    void parentRel2() {
        Path<?> path = visitor.getPropertyPathRecursive("parent.parent.name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("parent.parent.name");
    }

    @Test
    void preductRel() {
        Path<?> path = visitor.getPropertyPathRecursive("product.name", rsqlContext.root);
        assertThat(path).isNotNull();
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath) path).getNavigablePath();
        String pathString = getFullPath(navigablePath);
        assertThat(pathString).isEqualTo("product.name");
    }

    private String getFullPath(NavigablePath navigablePath) {
        String rootPath = getRootPath(navigablePath);
        int rootPathLength = rootPath.length();
        String fullPath = navigablePath.getIdentifierForTableGroup().toString().substring(rootPathLength+1);
        return fullPath;
    }

    private String getRootPath(NavigablePath navigablePath) {
        if (navigablePath.getParent() == null) {
            return navigablePath.getIdentifierForTableGroup().toString();
        } else {
            return getRootPath(navigablePath.getParent());
        }
    }

}
