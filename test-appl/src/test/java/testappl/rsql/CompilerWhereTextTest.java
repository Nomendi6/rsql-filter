package testappl.rsql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import javax.transaction.Transactional;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
public class CompilerWhereTextTest {

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

    @Test
    void fieldEqNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq==10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq=:p1");
    }

    @Test
    void fieldEqNegNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq==-10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq=:p1");
    }

    @Test
    void fieldNotEqNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq!=10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq!=:p1");
    }

    @Test
    void fieldNotEqNegNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq!=-10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq!=:p1");
    }

    @Test
    void fieldGtNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=gt=10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq>:p1");
    }
    @Test
    void fieldGeNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=ge=10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq>=:p1");
    }
    @Test
    void fieldLtNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=lt=10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq<:p1");
    }
    @Test
    void fieldLeNum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=le=10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq<=:p1");
    }

    @Test
    void errorMissingClosingParenthesis() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("(seq==10 or seq==11", rsqlContext);
        });
//        assertTrue(thrown.getMessage().contains("Missing closing parenthesis"));
    }

    @Test
    void errorMissingOpeningParenthesis() {
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq==10 or seq==11)", rsqlContext);
        });
        assertTrue(thrown.getMessage().contains("Missing opening parenthesis"));
    }

    @Test
    void errorMissingQuote1() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=='text", rsqlContext);
        });
    }

    @Test
    void seqNotEq() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq!=10", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq!=:p1");
    }

    @Test
    void fieldEqString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=='text'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=:p1");
    }
    @Test
    void fieldNotEqString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name!='text'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name!=:p1");
    }
    @Test
    void fieldGtString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=gt='text'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name>:p1");
    }
    @Test
    void fieldGeString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=ge='text'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name>=:p1");
    }
    @Test
    void fieldLtString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=lt='text'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name<:p1");
    }
    @Test
    void fieldLeString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=le='text'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name<=:p1");
    }

    @Test
    void fieldEqReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity==1.0", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity=:p1");
    }

    @Test
    void fieldEqNegReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity==-1.0", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity=:p1");
    }

    @Test
    void fieldGtReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity=gt=1.0", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity>:p1");
    }
    @Test
    void fieldGeReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity=gE=1.0", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity>=:p1");
    }
    @Test
    void fieldLtReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity=lt=1.0", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity<:p1");
    }
    @Test
    void fieldLeReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity=le=1.0", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity<=:p1");
    }


    @Test
    void fieldEqDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom==#2023-01-01T14:01:01Z#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom=:p1");
    }

    @Test
    void fieldNotEqDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom!=#2023-01-01T14:01:01Z#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom!=:p1");
    }

    @Test
    void fieldGtDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom=gt=#2023-01-01T14:01:01Z#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom>:p1");
    }
    @Test
    void fieldGeDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom=ge=#2023-01-01T14:01:01Z#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom>=:p1");
    }
    @Test
    void fieldLtDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom=lt=#2023-01-01T14:01:01Z#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom<:p1");
    }

    @Test
    void fieldLeDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom=le=#2023-01-01T14:01:01Z#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom<=:p1");
    }

    @Test
    void fieldEqDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate==#2023-01-01#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate=:p1");
    }
    @Test
    void fieldNotEqDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate!=#2023-01-01#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate!=:p1");
    }
    @Test
    void fieldGtDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate=Gt=#2023-01-01#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate>:p1");
    }
    @Test
    void fieldGeDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate=ge=#2023-01-01#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate>=:p1");
    }
    @Test
    void fieldLtDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate=lt=#2023-01-01#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate<:p1");
    }

    @Test
    void fieldLeDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate=le=#2023-01-01#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate<=:p1");
    }

    @Test
    void errorDateMissingHash() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom==#2023-01-01T14:01:01Z", rsqlContext);
        });
    }

    @Test
    void fieldsAnd1() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=='text' and code=='code'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=:p1 and a0.code=:p2");
    }

    @Test
    void fieldsAnd2() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=='text';code=='code'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=:p1 and a0.code=:p2");
    }

    @Test
    void fieldsAnd3() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("(name=='text') and (code=='code')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("(a0.name=:p1) and (a0.code=:p2)");
    }

    @Test
    void fieldsAnd4() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("(name=='text');(code=='code')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("(a0.name=:p1) and (a0.code=:p2)");
    }

    @Test
    void fieldsAnd5() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("(((name=='text') and (code=='code')) and (description=='a'))", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("(((a0.name=:p1) and (a0.code=:p2)) and (a0.description=:p3))");
    }

    @Test
    void fieldsOr1() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=='text' or code=='code'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=:p1 or a0.code=:p2");
    }

    @Test
    void fieldsOr2() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=='text',code=='code'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=:p1 or a0.code=:p2");
    }

    @Test
    void fieldsOr3() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("(name=='text')or(code=='code')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("(a0.name=:p1) or (a0.code=:p2)");
    }

    @Test
    void fieldsOr4() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("(name=='text'),(code=='code')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("(a0.name=:p1) or (a0.code=:p2)");
    }

    @Test
    void fieldEqField() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name==code", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=a0.code");
    }

    @Test
    void fieldNotEqField() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name!=code", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name!=a0.code");
    }

    @Test
    void fieldWithDotsEqField() {
        final RsqlQuery rsqlQuery = compilerForProduct.compileToRsqlQuery("tproduct.code==name", rsqlContextProduct);
        assertThat(rsqlQuery.where).isEqualTo("a1.code=a0.name");
    }

    @Test
    void fieldGeField() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=ge=code", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name>=a0.code");
    }

    @Test
    void fieldGtField() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=gt=code", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name>a0.code");
    }

    @Test
    void fieldLtField() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=lt=code", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name<a0.code");
    }

    @Test
    void fieldLeField() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=le=code", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name<=a0.code");
    }

    @Test
    void fieldIsNull() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name==NULL", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name is null");
    }

    @Test
    void fieldIsNotNull() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name!=NULL", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name is not null");
    }

    @Test
    void fieldEqEnum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("status==#ACTIVE#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.status=:p1");
    }

    @Test
    void fieldNotEqEnum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("status!=#ACTIVE#", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.status!=:p1");
    }

    @Test
    void errorEnumMissingHash1() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("status==ACTIVE#", rsqlContext);
        });
    }

    @Test
    void errorEnumMissingHash2() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("status==#ACTIVE", rsqlContext);
        });
    }

    @Test
    void fieldEqTrue() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("isValid==true", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.isValid=true");
    }

    @Test
    void errorGtTrue() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("isValid=gt=true", rsqlContext);
        });
    }

    @Test
    void errorGtNull() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("isValid=gt=null", rsqlContext);
        });
    }

    @Test
    void errorGtEnum() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("status=gt=#ACTIVE#", rsqlContext);
        });
    }

    @Test
    void fieldNotEqTrue() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("isValid!=true", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.isValid!=true");
    }

    @Test
    void fieldEqFalse() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("isValid==false", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.isValid=false");
    }

    @Test
    void fieldNotEqFalse() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("isValid!=false", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.isValid!=false");
    }

    @Test
    void fieldEqParam() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name==:param", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name=:param");
    }
    @Test
    void fieldNotEqParam() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name!=:param", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name!=:param");
    }
    @Test
    void fieldGtParam() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=gt=:param", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name>:param");
    }
    @Test
    void fieldGeParam() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=ge=:param", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name>=:param");
    }
    @Test
    void fieldLtParam() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=lt=:param", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name<:param");
    }
    @Test
    void fieldLeParam() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=le=:param", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name<=:param");
    }

    @Test
    void fieldBetweenDec() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=bt=(1,2)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq between :p1 and :p2");
    }

    @Test
    void fieldBetweenReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity=bt=(1.0,2.0)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity between :p1 and :p2");
    }

    @Test
    void fieldBetweenString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=bt=('A','B')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name between :p1 and :p2");
    }

    @Test
    void fieldBetweenDatetime() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("validFrom=bt=(#2023-01-01T12:00:00Z#,#2023-01-02T12:00:00Z#)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.validFrom between :p1 and :p2");
    }

    @Test
    void fieldBetweenDate() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("creationDate=bt=(#2023-01-01#,#2023-01-02#)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.creationDate between :p1 and :p2");
    }

    @Test
    void fieldBetweenFields() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=bt=(code,description)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name between code and description");
    }

    @Test
    void fieldBetweenParams() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=bt=(:param1,:param2)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.name between :param1 and :param2");
    }

    @Test
    void errorBtClauseMissingParentheses1() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=bt=(1,2", rsqlContext);
        });
    }

    @Test
    void errorBtClauseMissingParentheses2() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=bt=1,2)", rsqlContext);
        });
    }

    @Test
    void fieldInNumbers() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=in=(1,2,3)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq in (:p1)");
    }

    @Test
    void fieldInReal() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("quantity=in=(1.0,2.0,3.0)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.quantity in (:p1)");
    }

    @Test
    void fieldInString() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("code=in=('A','B','C')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.code in (:p1)");
    }

    @Test
    void fieldInEnum() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("status=in=('ACTIVE','NOT_ACTIVE')", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.status in (:p1)");
    }

    @Test
    void errorInClauseMissingParentheses1() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=in=(1,2,3", rsqlContext);
        });
    }

    @Test
    void errorInClauseMissingParentheses2() {
        assertThrows(SyntaxErrorException.class, () -> {
            final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=in=1,2,3)", rsqlContext);
        });
    }

    @Test
    void fieldNotInNumbers() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("seq=nin=(1,2,3)", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("a0.seq not in (:p1)");
    }

    @Test
    void fieldLikeString1() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=*'A*'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("lower(a0.name) like :p1");
    }

    @Test
    void fieldLikeString2() {
        final RsqlQuery rsqlQuery = compiler.compileToRsqlQuery("name=like='A*'", rsqlContext);
        assertThat(rsqlQuery.where).isEqualTo("lower(a0.name) like :p1");
    }


}
