package com.nomendi6.rsql.demo.rsql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.converter.PredicateToText;
import rsql.where.RsqlContext;
import com.nomendi6.rsql.demo.IntegrationTest;
import com.nomendi6.rsql.demo.domain.AppObject;
import com.nomendi6.rsql.demo.domain.Product;
import com.nomendi6.rsql.demo.domain.ProductType;
import com.nomendi6.rsql.demo.domain.enumeration.StandardRecordStatus;
import com.nomendi6.rsql.demo.repository.AppObjectRepository;
import com.nomendi6.rsql.demo.repository.ProductRepository;
import com.nomendi6.rsql.demo.repository.ProductTypeRepository;

import java.time.Instant;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
public class PredicateToTextIT {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppObjectRepository appObjectRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    RsqlContext<AppObject> rsqlContext;
    RsqlContext<Product> rsqlContextProduct;
    RsqlCompiler<AppObject> compiler;
    RsqlCompiler<Product> compilerForProduct;

    Instant expectedInstant = Instant.parse("2023-01-01T14:01:01Z");
    LocalDate expectedDate = LocalDate.parse("2023-01-01");

    @BeforeEach
    @Transactional
    void setUp() {
        setupData();
        setupRsqlCompiler();
    }

    private void setupRsqlCompiler() {
        rsqlContext = new RsqlContext<>(AppObject.class)
            .defineEntityManager(entityManager);
        compiler = new RsqlCompiler<>();

        rsqlContextProduct = new RsqlContext<>(Product.class)
            .defineEntityManager(entityManager);
        compilerForProduct = new RsqlCompiler<>();
    }

    private void setupData() {
        if (appObjectRepository.count()<1) {
            AppObject a = new AppObject()
                .withName("AAAAA")
                .withCode("AAAAA")
                .withStatus(StandardRecordStatus.ACTIVE)
                .withValidFrom(Instant.now().minus(1, DAYS));

            appObjectRepository.save(a);
            AppObject b = new AppObject()
                .withName("BBBB")
                .withCode("BBBB")
                .withStatus(StandardRecordStatus.NOT_ACTIVE)
                .withValidFrom(Instant.now())
                .withParent(a);
            appObjectRepository.save(b);
            ProductType t1 = new ProductType()
                .withCode("T1")
                .withName("T1")
                ;
            productTypeRepository.save(t1);

            ProductType t2 = new ProductType()
                .withCode("T2")
                .withName("T2")
                ;
            productTypeRepository.save(t2);

            ProductType t3 = new ProductType()
                .withCode("T3")
                .withName("T3")
                ;
            productTypeRepository.save(t3);

            Product p1 = new Product()
                .withCode("P1")
                .withName("P1")
                .withTproduct(t1)
                ;
            productRepository.save(p1);

            Product p2 = new Product()
                .withCode("P2")
                .withName("P2")
                .withParent(p1)
                .withTproduct(t2)
                ;
            productRepository.save(p2);

            Product p3 = new Product()
                .withCode("P3")
                .withName("P3")
                .withParent(p2)
                .withTproduct(t3)
                ;
            productRepository.save(p3);

        }
    }

    private String compileToPredicate(String rsql) {
        final Specification<AppObject> specification = compiler.compileToSpecification(rsql, rsqlContext);
        Predicate predicate = specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder);
        return PredicateToText.convert(predicate);
    }

    private String compileToPredicateForProduct(String rsql) {
        final Specification<Product> specification = compilerForProduct.compileToSpecification(rsql, rsqlContextProduct);
        Predicate predicate = specification.toPredicate(rsqlContextProduct.root, rsqlContextProduct.criteriaQuery, rsqlContextProduct.criteriaBuilder);
        return PredicateToText.convert(predicate);
    }

    @Test
    void fieldEqNum() {
        String rsql = "seq==1";
        String expected = "seq = 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtNum() {
        String rsql = "seq=lt=1";
        String expected = "seq < 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeNum() {
        String rsql = "seq=le=1";
        String expected = "seq <= 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtNum() {
        String rsql = "seq=gt=1";
        String expected = "seq > 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeNum() {
        String rsql = "seq=ge=1";
        String expected = "seq >= 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNeNum() {
        String rsql = "seq!=1";
        String expected = "seq != 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNe2Num() {
        String rsql = "seq=!1";
        String expected = "seq != 1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqString() {
        String rsql = "code=='AAAAA'";
        String expected = "code = 'AAAAA'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqUuid() {
        String rsql = "uuidField=='f47ac10b-58cc-4372-a567-0e02b2c3d479'";
        String expected = "uuidField = UUID('f47ac10b-58cc-4372-a567-0e02b2c3d479')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqUuid() {
        String rsql = "uuidField!='f47ac10b-58cc-4372-a567-0e02b2c3d479'";
        String expected = "uuidField != UUID('f47ac10b-58cc-4372-a567-0e02b2c3d479')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqString() {
        String rsql = "code!='AAAAA'";
        String expected = "code != 'AAAAA'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtString() {
        String rsql = "code=gt='AAAAA'";
        String expected = "code > 'AAAAA'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeString() {
        String rsql = "code=ge='AAAAA'";
        String expected = "code >= 'AAAAA'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtString() {
        String rsql = "code=lt='AAAAA'";
        String expected = "code < 'AAAAA'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeString() {
        String rsql = "code=le='AAAAA'";
        String expected = "code <= 'AAAAA'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqReal() {
        String rsql = "quantity==1.1";
        String expected = "quantity = 1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqNegReal() {
        String rsql = "quantity==-1.1";
        String expected = "quantity = -1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtReal() {
        String rsql = "quantity=gt=1.1";
        String expected = "quantity > 1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeReal() {
        String rsql = "quantity=ge=1.1";
        String expected = "quantity >= 1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtReal() {
        String rsql = "quantity=lt=1.1";
        String expected = "quantity < 1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeReal() {
        String rsql = "quantity=le=1.1";
        String expected = "quantity <= 1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNeReal() {
        String rsql = "quantity!=1.1";
        String expected = "quantity != 1.1";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqDatetime() {
        String rsql = "validFrom==#2023-01-01T14:01:01Z#";
        String expected = "validFrom = TIMESTAMP('2023-01-01T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqDatetime() {
        String rsql = "validFrom!=#2023-01-01T14:01:01Z#";
        String expected = "validFrom != TIMESTAMP('2023-01-01T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtDatetime() {
        String rsql = "validFrom=gt=#2023-01-01T14:01:01Z#";
        String expected = "validFrom > TIMESTAMP('2023-01-01T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeDatetime() {
        String rsql = "validFrom=ge=#2023-01-01T14:01:01Z#";
        String expected = "validFrom >= TIMESTAMP('2023-01-01T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtDatetime() {
        String rsql = "validFrom=lt=#2023-01-01T14:01:01Z#";
        String expected = "validFrom < TIMESTAMP('2023-01-01T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeDatetime() {
        String rsql = "validFrom=le=#2023-01-01T14:01:01Z#";
        String expected = "validFrom <= TIMESTAMP('2023-01-01T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqDate() {
        String rsql = "validFrom==#2023-01-01#";
        String expected = "validFrom = DATE('2023-01-01')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqDate() {
        String rsql = "validFrom!=#2023-01-01#";
        String expected = "validFrom != DATE('2023-01-01')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtDate() {
        String rsql = "validFrom=gt=#2023-01-01#";
        String expected = "validFrom > DATE('2023-01-01')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeDate() {
        String rsql = "validFrom=ge=#2023-01-01#";
        String expected = "validFrom >= DATE('2023-01-01')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtDate() {
        String rsql = "validFrom=lt=#2023-01-01#";
        String expected = "validFrom < DATE('2023-01-01')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeDate() {
        String rsql = "validFrom=le=#2023-01-01#";
        String expected = "validFrom <= DATE('2023-01-01')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression1() {
        String rsql = "seq==1 and quantity=gt=1.1";
        String expected = "(seq = 1) and (quantity > 1.1)";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression2() {
        String rsql = "name=='name' and code=='code'";
        String expected = "(name = 'name') and (code = 'code')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression3() {
        String rsql = "(name=='name') and (code=='code')";
        String expected = "(name = 'name') and (code = 'code')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression4() {
        String rsql = "(name=='name');(code=='code')";
        String expected = "(name = 'name') and (code = 'code')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression5() {
        String rsql = "(name=='name') and (code=='code') and (description=='a')";
        String expected = "((name = 'name') and (code = 'code')) and (description = 'a')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression6() {
        String rsql = "name=='name' or code=='code'";
        String expected = "(name = 'name') or (code = 'code')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression7() {
        String rsql = "name=='name' , code=='code'";
        String expected = "(name = 'name') or (code = 'code')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinExpression8() {
        String rsql = "(name=='name') or (code=='code')";
        String expected = "(name = 'name') or (code = 'code')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqField() {
        String rsql = "name==code";
        String expected = "name = code";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqField() {
        String rsql = "name!=code";
        String expected = "name != code";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtField() {
        String rsql = "name=gt=code";
        String expected = "name > code";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeField() {
        String rsql = "name=ge=code";
        String expected = "name >= code";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtField() {
        String rsql = "name=lt=code";
        String expected = "name < code";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeField() {
        String rsql = "name=le=code";
        String expected = "name <= code";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldWithDotsEqField() {
        String rsql = "tproduct.code==code";
        String expected = "tproduct.code = code";
        String actual = compileToPredicateForProduct(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldWithDotsEqField2() {
        String rsql = "tproduct.code==tproduct.name";
        String expected = "tproduct.code = tproduct.name";
        String actual = compileToPredicateForProduct(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldIsNull() {
        String rsql = "name==null";
        String expected = "name is null";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }
    @Test
    void fieldIsNotNull() {
        String rsql = "name!=null";
        String expected = "name is not null";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqEnum() {
        String rsql = "status==#ACTIVE#";
        String expected = "status = 'ACTIVE'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqEnum() {
        String rsql = "status!=#ACTIVE#";
        String expected = "status != 'ACTIVE'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqTrue() {
        String rsql = "isValid==true";
        String expected = "isValid = true";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqTrue() {
        String rsql = "isValid!=true";
        String expected = "isValid != true";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqFalse() {
        String rsql = "isValid==false";
        String expected = "isValid = false";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqFalse() {
        String rsql = "isValid!=false";
        String expected = "isValid != false";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamString() {
        String rsql = "name==:param";
        String expected = "name = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamNum() {
        String rsql = "seq==:param";
        String expected = "seq = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamDatetime() {
        String rsql = "validFrom==:param";
        String expected = "validFrom = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamDate() {
        String rsql = "creationDate==:param";
        String expected = "creationDate = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamEnum() {
        String rsql = "status==:param";
        String expected = "status = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamBoolean() {
        String rsql = "isValid==:param";
        String expected = "isValid = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldEqParamReal() {
        String rsql = "quantity==:param";
        String expected = "quantity = :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotEqParamField() {
        String rsql = "name!=:param";
        String expected = "name != :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGtParamField() {
        String rsql = "name=gt=:param";
        String expected = "name > :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldGeParamField() {
        String rsql = "name=ge=:param";
        String expected = "name >= :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLtParamField() {
        String rsql = "name=lt=:param";
        String expected = "name < :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLeParamField() {
        String rsql = "name=le=:param";
        String expected = "name <= :param";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenNum() {
        String rsql = "seq=bt=(1,2)";
        String expected = "seq between 1 and 2";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenString() {
        String rsql = "code=bt=('A','B')";
        String expected = "code between 'A' and 'B'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenDatetime() {
        String rsql = "validFrom=bt=(#2023-01-01T14:01:01Z#,#2023-01-02T14:01:01Z#)";
        String expected = "validFrom between TIMESTAMP('2023-01-01T14:01:01Z') and TIMESTAMP('2023-01-02T14:01:01Z')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenDate() {
        String rsql = "validFrom=bt=(#2023-01-01#,#2023-01-02#)";
        String expected = "validFrom between DATE('2023-01-01') and DATE('2023-01-02')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenReal() {
        String rsql = "quantity=bt=(1.1,2.2)";
        String expected = "quantity between 1.1 and 2.2";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenField() {
        String rsql = "name=bt=(code,description)";
        String expected = "name between code and description";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    // ToDo: fix this test
//    @Test
    void fieldBetweenField2() {
        String rsql = "name=bt=(code,'text')";
        String expected = "name between code and description";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldBetweenParam() {
        String rsql = "seq=bt=(:param1,:param2)";
        String expected = "seq between :param1 and :param2";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInNum() {
        String rsql = "seq=in=(1,2)";
        String expected = "seq in (1, 2)";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInString() {
        String rsql = "code=in=('A','B')";
        String expected = "code in ('A', 'B')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInUuid() {
        String rsql = "uuidField=in=('f47ac10b-58cc-4372-a567-0e02b2c3d479','f47ac10b-58cc-4372-a567-0e02b2c3d479')";
        String expected = "uuidField in (UUID('f47ac10b-58cc-4372-a567-0e02b2c3d479'), UUID('f47ac10b-58cc-4372-a567-0e02b2c3d479'))";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInDatetime() {
        String rsql = "validFrom=in=(#2023-01-01T14:01:01Z#,#2023-01-02T14:01:01Z#)";
        String expected = "validFrom in (TIMESTAMP('2023-01-01T14:01:01Z'), TIMESTAMP('2023-01-02T14:01:01Z'))";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInDate() {
        String rsql = "validFrom=in=(#2023-01-01#,#2023-01-02#)";
        String expected = "validFrom in (DATE('2023-01-01'), DATE('2023-01-02'))";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInReal() {
        String rsql = "quantity=in=(1.1,2.2)";
        String expected = "quantity in (1.1, 2.2)";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInField() {
        String rsql = "name=in=(code,description)";
        String expected = "name in (code, description)";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    // ToDo: fix this test
//    @Test
    void fieldInParam() {
        String rsql = "seq=in=(:param1,:param2)";
        String expected = "seq in (:param1, :param2)";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldInEnum() {
        String rsql = "status=in=(#ACTIVE#,#NOT_ACTIVE#)";
        String expected = "status in ('ACTIVE', 'NOT_ACTIVE')";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotInNum() {
        String rsql = "seq=nin=(1,2)";
        String expected = "seq not in (1, 2)";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLikeString1() {
        String rsql = "code=*'A*'";
        String expected = "lower(code) like 'a%'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldLikeString2() {
        String rsql = "code=like='A*'";
        String expected = "lower(code) like 'a%'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotLikeString1() {
        String rsql = "code=!*'A*'";
        String expected = "lower(code) not like 'a%'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotLikeString2() {
        String rsql = "code!=*'A*'";
        String expected = "lower(code) not like 'a%'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void fieldNotLikeString3() {
        String rsql = "code=nlike='A*'";
        String expected = "lower(code) not like 'a%'";
        String actual = compileToPredicate(rsql);
        assertThat(actual).isEqualTo(expected);
    }

}
