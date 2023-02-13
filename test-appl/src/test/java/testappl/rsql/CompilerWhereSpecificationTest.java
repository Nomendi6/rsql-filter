package testappl.rsql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.RsqlContext;
import rsql.where.RsqlQuery;
import testappl.IntegrationTest;
import testappl.domain.AppObject;
import testappl.domain.Product;
import testappl.domain.ProductType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.repository.AppObjectRepository;
import testappl.repository.ProductRepository;
import testappl.repository.ProductTypeRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@IntegrationTest
public class CompilerWhereSpecificationTest {

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
                .name("AAAAA")
                .code("AAAAA")
                .status(StandardRecordStatus.ACTIVE)
                .validFrom(Instant.now().minus(1, DAYS));

            appObjectRepository.save(a);
            AppObject b = new AppObject()
                .name("BBBB")
                .code("BBBB")
                .status(StandardRecordStatus.NOT_ACTIVE)
                .validFrom(Instant.now())
                .parent(a);
            appObjectRepository.save(b);
            ProductType t1 = new ProductType()
                .code("T1")
                .name("T1")
                ;
            productTypeRepository.save(t1);

            ProductType t2 = new ProductType()
                .code("T2")
                .name("T2")
                ;
            productTypeRepository.save(t2);

            ProductType t3 = new ProductType()
                .code("T3")
                .name("T3")
                ;
            productTypeRepository.save(t3);

            Product p1 = new Product()
                .code ("P1")
                .name ("P1")
                .tproduct(t1)
                ;
            productRepository.save(p1);

            Product p2 = new Product()
                .code ("P2")
                .name ("P2")
                .parent(p1)
                .tproduct(t2)
                ;
            productRepository.save(p2);

            Product p3 = new Product()
                .code ("P3")
                .name ("P3")
                .parent(p2)
                .tproduct(t3)
                ;
            productRepository.save(p3);

        }
    }

    private String getSqlText(Specification<AppObject> specification) {
        // get sql text from specification in hibernate dialect
        final TypedQuery<AppObject> query = entityManager.createQuery(
            rsqlContext.criteriaQuery
                .select(rsqlContext.root)
                .where(specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder)));
        org.hibernate.Query hq = query.unwrap(org.hibernate.Query.class);
        String sql = hq.getQueryString();
        // from sql string get text after 'where'
        String whereClause = sql.substring(sql.indexOf("where") + 5).trim();
        return whereClause;
    }

    private String getSqlTextForProduct(Specification<Product> specification) {
        // get sql text from specification in hibernate dialect
        final TypedQuery<Product> query = entityManager.createQuery(
            rsqlContextProduct.criteriaQuery
                .select(rsqlContextProduct.root)
                .where(specification.toPredicate(rsqlContextProduct.root, rsqlContextProduct.criteriaQuery, rsqlContextProduct.criteriaBuilder)));
        org.hibernate.Query hq = query.unwrap(org.hibernate.Query.class);
        String sql = hq.getQueryString();
        // from sql string get text after 'where'
        String whereClause = sql.substring(sql.indexOf("where") + 5).trim();
        return whereClause;
    }

    private String compileToSpecificationAndGetWhere(String rsql) {
        final Specification<AppObject> specification = compiler.compileToSpecification(rsql, rsqlContext);
        return getSqlText(specification);
    }
    private String compileToSpecificationAndGetWhereForProduct(String rsql) {
        final Specification<Product> specification = compilerForProduct.compileToSpecification(rsql, rsqlContextProduct);
        return getSqlTextForProduct(specification);
    }
    @Test
    void fieldEqNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq==10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq=10L");
    }

    @Test
    void fieldEqNegNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq==-10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq=-10L");
    }
    @Test
    void fieldNotEqNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq!=10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq<>10L");
    }
    @Test
    void fieldNotEqNegNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq!=-10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq<>-10L");
    }

    @Test
    void fieldGtNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq=gt=10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq>10L");
    }
    @Test
    void fieldGeNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq=ge=10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq>=10L");
    }
    @Test
    void fieldLtNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq=lt=10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq<10L");
    }
    @Test
    void fieldLeNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq=le=10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq<=10L");
    }

    @Test
    void errorMissingClosingParenthesis() {
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq==10 or seq==11)");
        });
        assertTrue(thrown.getMessage().contains("Missing opening parenthesis"));
    }

    @Test
    void errorMissingOpeningParenthesis() {
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("seq==10 or seq==11)");
        });
        assertTrue(thrown.getMessage().contains("Missing opening parenthesis"));
    }

    @Test
    void errorMissingQuote1() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("name=='text");
        });
    }

    @Test
    void seqNotEq() {
        String whereClause = compileToSpecificationAndGetWhere("seq!=10");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq<>10L");
    }

    @Test
    void fieldEqString() {
        String whereClause = compileToSpecificationAndGetWhere("name=='text'");
        assertThat(whereClause).isEqualTo("generatedAlias0.name=:param0");
    }
    @Test
    void fieldNotEqString() {
        String whereClause = compileToSpecificationAndGetWhere("name!='text'");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<>:param0");
    }
    @Test
    void fieldGtString() {
        String whereClause = compileToSpecificationAndGetWhere("name=gt='text'");
        assertThat(whereClause).isEqualTo("generatedAlias0.name>:param0");
    }
    @Test
    void fieldGeString() {
        String whereClause = compileToSpecificationAndGetWhere("name=ge='text'");
        assertThat(whereClause).isEqualTo("generatedAlias0.name>=:param0");
    }
    @Test
    void fieldLtString() {
        String whereClause = compileToSpecificationAndGetWhere("name=lt='text'");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<:param0");
    }
    @Test
    void fieldLeString() {
        String whereClause = compileToSpecificationAndGetWhere("name=le='text'");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<=:param0");
    }

    @Test
    void fieldEqReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity==1.0");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity=1.0D");
    }

    @Test
    void fieldEqNegReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity==-1.0");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity=-1.0D");
    }

    @Test
    void fieldGtReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity=gt=1.0");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity>1.0D");
    }
    @Test
    void fieldGeReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity=gE=1.0");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity>=1.0D");
    }
    @Test
    void fieldLtReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity=lt=1.0");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity<1.0D");
    }
    @Test
    void fieldLeReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity=le=1.0");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity<=1.0D");
    }


    @Test
    void fieldEqDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom==#2023-01-01T14:01:01Z#");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom=:param0");
    }

    @Test
    void fieldNotEqDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom!=#2023-01-01T14:01:01Z#");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom<>:param0");
    }

    @Test
    void fieldGtDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom=gt=#2023-01-01T14:01:01Z#");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom>:param0");
    }
    @Test
    void fieldGeDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom=ge=#2023-01-01T14:01:01Z#");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom>=:param0");
    }
    @Test
    void fieldLtDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom=lt=#2023-01-01T14:01:01Z#");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom<:param0");
    }

    @Test
    void fieldLeDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom=le=#2023-01-01T14:01:01Z#");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom<=:param0");
    }

    @Test
    void fieldEqDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate==#2023-01-01#");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate=:param0");
    }
    @Test
    void fieldNotEqDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate!=#2023-01-01#");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate<>:param0");
    }
    @Test
    void fieldGtDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate=Gt=#2023-01-01#");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate>:param0");
    }
    @Test
    void fieldGeDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate=ge=#2023-01-01#");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate>=:param0");
    }
    @Test
    void fieldLtDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate=lt=#2023-01-01#");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate<:param0");
    }

    @Test
    void fieldLeDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate=le=#2023-01-01#");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate<=:param0");
    }

    @Test
    void errorDateMissingHash() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("validFrom==#2023-01-01T14:01:01Z");
        });
    }

    @Test
    void fieldsAnd1() {
        String whereClause = compileToSpecificationAndGetWhere("name=='text' and code=='code'");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) and ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsAnd2() {
        String whereClause = compileToSpecificationAndGetWhere("name=='text';code=='code'");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) and ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsAnd3() {
        String whereClause = compileToSpecificationAndGetWhere("(name=='text') and (code=='code')");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) and ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsAnd4() {
        String whereClause = compileToSpecificationAndGetWhere("(name=='text');(code=='code')");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) and ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsAnd5() {
        String whereClause = compileToSpecificationAndGetWhere("(((name=='text') and (code=='code')) and (description=='a'))");
        assertThat(whereClause).isEqualTo("( ( generatedAlias0.name=:param0 ) and ( generatedAlias0.code=:param1 ) ) and ( generatedAlias0.description=:param2 )");
    }

    @Test
    void fieldsOr1() {
        String whereClause = compileToSpecificationAndGetWhere("name=='text' or code=='code'");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) or ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsOr2() {
        String whereClause = compileToSpecificationAndGetWhere("name=='text',code=='code'");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) or ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsOr3() {
        String whereClause = compileToSpecificationAndGetWhere("(name=='text')or(code=='code')");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) or ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldsOr4() {
        String whereClause = compileToSpecificationAndGetWhere("(name=='text'),(code=='code')");
        assertThat(whereClause).isEqualTo("( generatedAlias0.name=:param0 ) or ( generatedAlias0.code=:param1 )");
    }

    @Test
    void fieldEqField() {
        String whereClause = compileToSpecificationAndGetWhere("name==code");
        assertThat(whereClause).isEqualTo("generatedAlias0.name=generatedAlias0.code");
    }

    @Test
    void fieldNotEqField() {
        String whereClause = compileToSpecificationAndGetWhere("name!=code");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<>generatedAlias0.code");
    }

    @Test
    void fieldWithDotsEqField() {
        String whereClause = compileToSpecificationAndGetWhereForProduct("tproduct.code==name");
        assertThat(whereClause).isEqualTo("generatedAlias2.code=generatedAlias0.name");
    }

    @Test
    void fieldGeField() {
        String whereClause = compileToSpecificationAndGetWhere("name=ge=code");
        assertThat(whereClause).isEqualTo("generatedAlias0.name>=generatedAlias0.code");
    }

    @Test
    void fieldGtField() {
        String whereClause = compileToSpecificationAndGetWhere("name=gt=code");
        assertThat(whereClause).isEqualTo("generatedAlias0.name>generatedAlias0.code");
    }

    @Test
    void fieldLtField() {
        String whereClause = compileToSpecificationAndGetWhere("name=lt=code");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<generatedAlias0.code");
    }

    @Test
    void fieldLeField() {
        String whereClause = compileToSpecificationAndGetWhere("name=le=code");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<=generatedAlias0.code");
    }

    @Test
    void fieldIsNull() {
        String whereClause = compileToSpecificationAndGetWhere("name==NULL");
        assertThat(whereClause).isEqualTo("generatedAlias0.name is null");
    }

    @Test
    void fieldIsNotNull() {
        String whereClause = compileToSpecificationAndGetWhere("name!=NULL");
        assertThat(whereClause).isEqualTo("generatedAlias0.name is not null");
    }

    @Test
    void fieldEqEnum() {
        String whereClause = compileToSpecificationAndGetWhere("status==#ACTIVE#");
        assertThat(whereClause).isEqualTo("generatedAlias0.status=:param0");
    }

    @Test
    void fieldNotEqEnum() {
        String whereClause = compileToSpecificationAndGetWhere("status!=#ACTIVE#");
        assertThat(whereClause).isEqualTo("generatedAlias0.status<>:param0");
    }

    @Test
    void errorEnumMissingHash1() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("status==ACTIVE#");
        });
    }

    @Test
    void errorEnumMissingHash2() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("status==#ACTIVE");
        });
    }

    @Test
    void fieldEqTrue() {
        String whereClause = compileToSpecificationAndGetWhere("isValid==true");
        assertThat(whereClause).isEqualTo("generatedAlias0.isValid=:param0");
    }

    @Test
    void errorGtTrue() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("isValid=gt=true");
        });
    }

    @Test
    void errorGtNull() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("isValid=gt=null");
        });
    }

    @Test
    void errorGtEnum() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("status=gt=#ACTIVE#");
        });
    }

    @Test
    void fieldNotEqTrue() {
        String whereClause = compileToSpecificationAndGetWhere("isValid!=true");
        assertThat(whereClause).isEqualTo("generatedAlias0.isValid<>:param0");
    }

    @Test
    void fieldEqFalse() {
        String whereClause = compileToSpecificationAndGetWhere("isValid==false");
        assertThat(whereClause).isEqualTo("generatedAlias0.isValid=:param0");
    }

    @Test
    void fieldNotEqFalse() {
        String whereClause = compileToSpecificationAndGetWhere("isValid!=false");
        assertThat(whereClause).isEqualTo("generatedAlias0.isValid<>:param0");
    }

    @Test
    void fieldEqParamString() {
        String whereClause = compileToSpecificationAndGetWhere("name==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.name=:param");
    }
    @Test
    void fieldEqParamNum() {
        String whereClause = compileToSpecificationAndGetWhere("seq==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq=:param");
    }

    @Test
    void fieldEqParamReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity=:param");
    }

    @Test
    void fieldEqParamBoolean() {
        String whereClause = compileToSpecificationAndGetWhere("isValid==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.isValid=:param");
    }

    @Test
    void fieldEqParamDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate=:param");
    }

    @Test
    void fieldEqParamDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom=:param");
    }

    @Test
    void fieldEqParamEnum() {
        String whereClause = compileToSpecificationAndGetWhere("status==:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.status=:param");
    }

    @Test
    void fieldNotEqParam() {
        String whereClause = compileToSpecificationAndGetWhere("name!=:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<>:param");
    }
    @Test
    void fieldGtParam() {
        String whereClause = compileToSpecificationAndGetWhere("name=gt=:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.name>:param");
    }
    @Test
    void fieldGeParam() {
        String whereClause = compileToSpecificationAndGetWhere("name=ge=:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.name>=:param");
    }
    @Test
    void fieldLtParam() {
        String whereClause = compileToSpecificationAndGetWhere("name=lt=:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<:param");
    }
    @Test
    void fieldLeParam() {
        String whereClause = compileToSpecificationAndGetWhere("name=le=:param");
        assertThat(whereClause).isEqualTo("generatedAlias0.name<=:param");
    }

    @Test
    void fieldBetweenDec() {
        String whereClause = compileToSpecificationAndGetWhere("seq=bt=(1,2)");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq between 1L and 2L");
    }

    @Test
    void fieldBetweenReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity=bt=(1.0,2.0)");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity between 1.0BD and 2.0BD");
    }

    @Test
    void fieldBetweenString() {
        String whereClause = compileToSpecificationAndGetWhere("name=bt=('A','B')");
        assertThat(whereClause).isEqualTo("generatedAlias0.name between :param0 and :param1");
    }

    @Test
    void fieldBetweenDatetime() {
        String whereClause = compileToSpecificationAndGetWhere("validFrom=bt=(#2023-01-01T12:00:00Z#,#2023-01-02T12:00:00Z#)");
        assertThat(whereClause).isEqualTo("generatedAlias0.validFrom between :param0 and :param1");
    }

    @Test
    void fieldBetweenDate() {
        String whereClause = compileToSpecificationAndGetWhere("creationDate=bt=(#2023-01-01#,#2023-01-02#)");
        assertThat(whereClause).isEqualTo("generatedAlias0.creationDate between :param0 and :param1");
    }

    @Test
    void fieldBetweenFields() {
        String whereClause = compileToSpecificationAndGetWhere("name=bt=(code,description)");
        assertThat(whereClause).isEqualTo("generatedAlias0.name between generatedAlias0.code and generatedAlias0.description");
    }

    @Test
    void fieldBetweenParams() {
        String whereClause = compileToSpecificationAndGetWhere("name=bt=(:param1,:param2)");
        assertThat(whereClause).isEqualTo("generatedAlias0.name between :param1 and :param2");
    }

    @Test
    void errorBtClauseMissingParentheses1() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("seq=bt=(1,2");
        });
    }

    @Test
    void errorBtClauseMissingParentheses2() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("seq=bt=1,2)");
        });
    }

    @Test
    void fieldInNumbers() {
        String whereClause = compileToSpecificationAndGetWhere("seq=in=(1,2,3)");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq in (1L, 2L, 3L)");
    }

    @Test
    void fieldInReal() {
        String whereClause = compileToSpecificationAndGetWhere("quantity=in=(1.0,2.0,3.0)");
        assertThat(whereClause).isEqualTo("generatedAlias0.quantity in (1.0BD, 2.0BD, 3.0BD)");
    }

    @Test
    void fieldInString() {
        String whereClause = compileToSpecificationAndGetWhere("code=in=('A','B','C')");
        assertThat(whereClause).isEqualTo("generatedAlias0.code in (:param0, :param1, :param2)");
    }

    @Test
    void fieldInEnum() {
        String whereClause = compileToSpecificationAndGetWhere("status=in=('ACTIVE','NOT_ACTIVE')");
        assertThat(whereClause).isEqualTo("generatedAlias0.status in (:param0, :param1)");
    }

    @Test
    void errorInClauseMissingParentheses1() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("seq=in=(1,2,3");
        });
    }

    @Test
    void errorInClauseMissingParentheses2() {
        assertThrows(SyntaxErrorException.class, () -> {
            String whereClause = compileToSpecificationAndGetWhere("seq=in=1,2,3)");
        });
    }

    @Test
    void fieldNotInNumbers() {
        String whereClause = compileToSpecificationAndGetWhere("seq=nin=(1,2,3)");
        assertThat(whereClause).isEqualTo("generatedAlias0.seq not in (1L, 2L, 3L)");
    }

    @Test
    void fieldLikeString1() {
        String whereClause = compileToSpecificationAndGetWhere("name=*'A*'");
        assertThat(whereClause).isEqualTo("lower(generatedAlias0.name) like :param0");
    }

    @Test
    void fieldLikeString2() {
        String whereClause = compileToSpecificationAndGetWhere("name=like='A*'");
        assertThat(whereClause).isEqualTo("lower(generatedAlias0.name) like :param0");
    }

}
