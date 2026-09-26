package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nomendi6.rsql.it.config.SqlStatementCapture;
import com.nomendi6.rsql.it.domain.compositekey.DocumentKey;
import com.nomendi6.rsql.it.domain.compositekey.DocumentPeriod;
import com.nomendi6.rsql.it.domain.compositekey.GenericKeyedDocument;
import com.nomendi6.rsql.it.domain.compositekey.KeyedDocument;
import com.nomendi6.rsql.it.domain.compositekey.KeyedLine;
import com.nomendi6.rsql.it.domain.compositekey.KeyedPayment;
import com.nomendi6.rsql.it.domain.compositekey.PaymentTag;
import com.nomendi6.rsql.it.repository.GenericKeyedDocumentRepository;
import com.nomendi6.rsql.it.repository.KeyedDocumentRepository;
import com.nomendi6.rsql.it.repository.KeyedLineRepository;
import com.nomendi6.rsql.it.repository.KeyedPaymentRepository;
import com.nomendi6.rsql.it.repository.PaymentTagRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.context.TestPropertySource;
import rsql.PagingStrategy;
import rsql.RsqlCompiler;
import rsql.RsqlQueryService;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.SimpleQueryExecutor;
import rsql.mapper.EntityMapper;
import rsql.where.RsqlContext;

/**
 * A composite key written as one string - {@code id=='ACME~2024~2'} - compared with an {@code @EmbeddedId}.
 *
 * <p>The library does not know the format: it asks the key class, through its {@code public static
 * valueOf(String)}, and compares the path with the value that comes back. {@link DocumentKey} joins its parts
 * with {@code ~} and escapes everything else as {@code !XX}, the way a generated application's key does.</p>
 *
 * <p>Every filter runs through both WHERE paths - the Specification one and the JPQL-text one a service built
 * with its own JPQL uses - and the two must agree, because a filter string must not mean one thing in one
 * service mode and another thing in the other.</p>
 *
 * <p>The subclasses run the contract under dialects that have no row values and make Hibernate emulate the
 * comparison of a whole key - the emulation is where a {@code =nin=} over a key used to go wrong.</p>
 */
@Transactional
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.nomendi6.rsql.it.config.SqlStatementCapture" })
abstract class EmbeddableKeyFilterContract {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private KeyedDocumentRepository documentRepository;

    @Autowired
    private KeyedPaymentRepository paymentRepository;

    @Autowired
    private KeyedLineRepository lineRepository;

    @Autowired
    private GenericKeyedDocumentRepository genericRepository;

    @Autowired
    private PaymentTagRepository tagRepository;

    /**
     * Five documents - one company code with a character that has to be escaped in the key - three payments
     * pointing at them through a three-column foreign key and one pointing at nothing, and three lines whose
     * key contains their document's key.
     */
    @BeforeEach
    void seed() {
        entityManager.clear();
        // Native, because SQL Server's dialect writes a JPQL bulk delete in a form H2 cannot run.
        for (String table : List.of("payment_tag", "keyed_line", "keyed_payment", "keyed_document", "generic_keyed_document")) {
            entityManager.createNativeQuery("delete from " + table).executeUpdate();
        }
        KeyedDocument a241 = new KeyedDocument(new DocumentKey("ACME", 2024, 1L), "A24-1", new DocumentPeriod(2024, 1));
        KeyedDocument a242 = new KeyedDocument(new DocumentKey("ACME", 2024, 2L), "A24-2", new DocumentPeriod(2024, 2));
        KeyedDocument a231 = new KeyedDocument(new DocumentKey("ACME", 2023, 1L), "A23-1", new DocumentPeriod(2023, 1));
        KeyedDocument b241 = new KeyedDocument(new DocumentKey("BETA", 2024, 1L), "B24-1", new DocumentPeriod(2024, 1));
        KeyedDocument xy = new KeyedDocument(new DocumentKey("X/Y", 2024, 9L), "XY-9", null);
        for (KeyedDocument document : List.of(a241, a242, a231, b241, xy)) {
            entityManager.persist(document);
        }
        KeyedPayment p1 = new KeyedPayment(1L, "P1", a241);
        entityManager.persist(p1);
        entityManager.persist(new KeyedPayment(2L, "P2", a231));
        entityManager.persist(new KeyedPayment(3L, "P3", b241));
        entityManager.persist(new KeyedPayment(4L, "P4", null));
        entityManager.persist(new KeyedLine(a241, 1, "a1-1"));
        entityManager.persist(new KeyedLine(a241, 2, "a1-2"));
        entityManager.persist(new KeyedLine(b241, 1, "b1-1"));
        entityManager.persist(new PaymentTag(p1, "urgent"));
        entityManager.persist(new GenericKeyedDocument(new DocumentKey("ACME", 2024, 1L), "G-A24-1", new DocumentPeriod(2024, 1)));
        entityManager.persist(new GenericKeyedDocument(new DocumentKey("ACME", 2024, 2L), "G-A24-2", new DocumentPeriod(2024, 2)));
        entityManager.persist(new GenericKeyedDocument(new DocumentKey("BETA", 2024, 1L), "G-B24-1", new DocumentPeriod(2024, 1)));
        entityManager.flush();
        entityManager.clear();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private <E> List<String> throughSpecification(Class<E> type, JpaSpecificationExecutor<E> repository, String filter, Function<E, String> name) {
        RsqlContext<E> context = new RsqlContext<>(type).defineEntityManager(entityManager);
        return repository.findAll(new RsqlCompiler<E>().compileToSpecification(filter, context)).stream().map(name).sorted().toList();
    }

    private <E> List<String> throughJpql(Class<E> type, String filter, Function<E, String> name) {
        RsqlContext<E> context = new RsqlContext<>(type).defineEntityManager(entityManager);
        context.root.alias("a0");
        return SimpleQueryExecutor
            .getJpqlQueryResult(type, type, "select a0 from " + type.getSimpleName() + " a0", "a0", filter, null, context, new RsqlCompiler<>())
            .stream()
            .map(name)
            .sorted()
            .toList();
    }

    /** The rows both paths return; fails when the paths disagree. */
    private <E> List<String> rows(Class<E> type, JpaSpecificationExecutor<E> repository, String filter, Function<E, String> name) {
        List<String> specification = throughSpecification(type, repository, filter, name);
        entityManager.clear();
        List<String> jpql = throughJpql(type, filter, name);
        entityManager.clear();
        assertThat(jpql).as("the JPQL path reads %s like the Specification path", filter).isEqualTo(specification);
        return specification;
    }

    private List<String> documents(String filter) {
        return rows(KeyedDocument.class, documentRepository, filter, KeyedDocument::getTitle);
    }

    private List<String> payments(String filter) {
        return rows(KeyedPayment.class, paymentRepository, filter, KeyedPayment::getReference);
    }

    private List<String> lines(String filter) {
        return rows(KeyedLine.class, lineRepository, filter, KeyedLine::getText);
    }

    private List<String> genericDocuments(String filter) {
        return rows(GenericKeyedDocument.class, genericRepository, filter, GenericKeyedDocument::getTitle);
    }

    /** A filter with named parameters, through the Specification path, the caller binding the parameters. */
    private List<String> documentsWithParameters(String filter, Map<String, Object> parameters) {
        RsqlContext<KeyedDocument> context = new RsqlContext<>(KeyedDocument.class).defineEntityManager(entityManager);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<KeyedDocument> query = builder.createQuery(KeyedDocument.class);
        Root<KeyedDocument> root = query.from(KeyedDocument.class);
        context.root = root;
        query.where(new RsqlCompiler<KeyedDocument>().compileToSpecification(filter, context).toPredicate(root, query, builder));
        TypedQuery<KeyedDocument> typed = entityManager.createQuery(query);
        parameters.forEach(typed::setParameter);
        return typed.getResultList().stream().map(KeyedDocument::getTitle).sorted().toList();
    }

    /** Both paths refuse the filter with a SyntaxErrorException whose message contains the given text. */
    private <E> void rejected(Class<E> type, JpaSpecificationExecutor<E> repository, String filter, String message) {
        assertThatThrownBy(() -> throughSpecification(type, repository, filter, Object::toString))
            .as("Specification path, %s", filter)
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessageContaining(message);
        assertThatThrownBy(() -> throughJpql(type, filter, Object::toString))
            .as("JPQL path, %s", filter)
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessageContaining(message);
    }

    private void documentRejected(String filter, String message) {
        rejected(KeyedDocument.class, documentRepository, filter, message);
    }

    /** The WHERE part of the query under test, from the Specification path. */
    private String whereClauseOf(String filter) {
        SqlStatementCapture.reset();
        throughSpecification(KeyedDocument.class, documentRepository, filter, KeyedDocument::getTitle);
        String sql = SqlStatementCapture.firstStatement();
        entityManager.clear();
        return sql.substring(sql.indexOf("where"));
    }

    // ------------------------------------------------------------------
    // The whole key
    // ------------------------------------------------------------------

    @Test
    @DisplayName("id=='<key>' selects exactly one row and compares every key column")
    void wholeKeyEquality() {
        assertThat(documents("id=='ACME~2024~2'")).containsExactly("A24-2");
        assertThat(whereClauseOf("id=='ACME~2024~2'")).contains("company_code", "doc_year", "doc_no");
        // The key class owns its escaping: '/' travels as !2F.
        assertThat(documents("id=='X!2FY~2024~9'")).containsExactly("XY-9");
        assertThat(documents("id=='ACME~2024~2';title=='A24-2'")).containsExactly("A24-2");
        assertThat(documents("id=='ACME~2024~2',title=='B24-1'")).containsExactly("A24-2", "B24-1");
        assertThat(documents("id=='ACME~1999~2'")).isEmpty();
    }

    @Test
    @DisplayName("id!='<key>' selects every other row")
    void wholeKeyInequality() {
        assertThat(documents("id!='ACME~2024~2'")).containsExactly("A23-1", "A24-1", "B24-1", "XY-9");
    }

    @Test
    @DisplayName("=in= and =nin= convert every string element")
    void inAndNotIn() {
        assertThat(documents("id=in=('ACME~2024~1','BETA~2024~1')")).containsExactly("A24-1", "B24-1");
        assertThat(documents("id=nin=('ACME~2024~1','BETA~2024~1')")).containsExactly("A23-1", "A24-2", "XY-9");
        assertThat(documents("id=in=('ACME~2024~1','ACME~2023~1');id.docYear==2024")).containsExactly("A24-1");
        assertThat(documents("id=in=('ACME~2024~1')")).containsExactly("A24-1");
        assertThat(documents("id=nin=('ACME~2024~1')")).containsExactly("A23-1", "A24-2", "B24-1", "XY-9");
    }

    @Test
    @DisplayName("=nin= over a key is one <> per element, never a row-value NOT IN")
    void notInIsAConjunction() {
        // Hibernate 6 emulates a row-value NOT IN on SQL Server and DB2 with OR between the groups, which excludes
        // nothing; a conjunction of <> means the same on every database.
        assertThat(whereClauseOf("id=nin=('ACME~2024~1','BETA~2024~1')")).doesNotContain("not in").contains(" and ");
        assertThat(documents("id=nin=('ACME~2024~1','BETA~2024~1');title!='XY-9'")).containsExactly("A23-1", "A24-2");
        assertThat(documents("title=='XY-9',id=nin=('ACME~2024~1','BETA~2024~1','ACME~2024~2','ACME~2023~1')")).containsExactly("XY-9");
    }

    @Test
    @DisplayName("=nin= keeps a row whose embedded value is partly NULL but differs from every element")
    void notInWithAPartlyNullValue() {
        KeyedDocument partly = new KeyedDocument(new DocumentKey("ZED", 2025, 1L), "Z25-1", new DocumentPeriod(2025, null));
        entityManager.persist(partly);
        entityManager.flush();
        entityManager.clear();
        // (2025, NULL) differs from (2024, 1) and from (2024, 2) - 2025 <> 2024 settles it whatever the month is.
        assertThat(documents("period=nin=('2024-01','2024-02')")).containsExactly("A23-1", "Z25-1");
        assertThat(documents("period!='2024-01';period!='2024-02'")).containsExactly("A23-1", "Z25-1");
    }

    @Test
    @DisplayName("<toOne>.id=='<key>' filters by a parent with a composite key")
    void throughToOneAssociation() {
        // Three-column foreign key.
        assertThat(payments("document.id=='ACME~2024~1'")).containsExactly("P1");
        assertThat(payments("document.id=in=('ACME~2024~1','ACME~2023~1')")).containsExactly("P1", "P2");
        assertThat(payments("document.id=nin=('ACME~2024~1','ACME~2023~1')")).containsExactly("P3");
        // A payment without a document is not "different from" a key, like any != over a missing association.
        assertThat(payments("document.id!='ACME~2024~1'")).containsExactly("P2", "P3");
        assertThat(payments("document.title=='A24-1';document.id=='ACME~2024~1'")).containsExactly("P1");
        // The foreign key shortcut declines a composite target, and switching it off changes nothing.
        RsqlContext<KeyedPayment> withoutShortcut = new RsqlContext<>(KeyedPayment.class).defineEntityManager(entityManager);
        withoutShortcut.useForeignKeyIdShortcut = false;
        assertThat(paymentRepository.findAll(new RsqlCompiler<KeyedPayment>().compileToSpecification("document.id=='ACME~2024~1'", withoutShortcut)))
            .extracting(KeyedPayment::getReference)
            .containsExactly("P1");

        // A key that contains the parent's key, shared with the association through @MapsId.
        assertThat(lines("document.id=='ACME~2024~1'")).containsExactly("a1-1", "a1-2");
        assertThat(lines("document.id=in=('BETA~2024~1')")).containsExactly("b1-1");
        // The embedded part of the key is an embeddable too, and is read off the line's own columns.
        assertThat(lines("id.documentKey=='ACME~2024~1'")).containsExactly("a1-1", "a1-2");
    }

    // ------------------------------------------------------------------
    // What is refused
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a literal valueOf refuses is a SyntaxErrorException carrying valueOf's reason")
    void invalidLiteral() {
        documentRejected("id=='ACME~2024'", "Invalid value for DocumentKey: ACME~2024 (Expected 3 key parts, got 2: ACME~2024)");
        documentRejected("id=='ACME~x~1'", "Invalid value for DocumentKey: ACME~x~1 (For input string: \"x\")");
        documentRejected("id=='ACME!2~2024~1'", "Invalid value for DocumentKey: ACME!2~2024~1");
        documentRejected("id!=''", "Invalid value for DocumentKey:");
        documentRejected("id=in=('ACME~2024~1','bad')", "Invalid value for DocumentKey: bad");
        documentRejected("id=nin=('bad')", "Invalid value for DocumentKey: bad");
        documentRejected("period=='2024'", "Invalid value for DocumentPeriod: 2024 (Expected yyyy-mm: 2024)");

        // The exception valueOf threw is kept as the cause.
        assertThatThrownBy(() -> throughSpecification(KeyedDocument.class, documentRepository, "id=='ACME~x~1'", KeyedDocument::getTitle))
            .isInstanceOf(SyntaxErrorException.class)
            .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("an operator other than ==, !=, =in=, =nin= is refused before the key is read")
    void operatorsOtherThanEquality() {
        for (String operator : List.of("=gt=", "=ge=", "=lt=", "=le=", "=like=", "=nlike=", "=clike=", "=cnlike=", "=*", "=!*", "=^*")) {
            documentRejected("id" + operator + "'ACME~2024~1'", "Unknown operator for DocumentKey: " + operator);
        }
        documentRejected("id=bt=('ACME~2024~1','ACME~2024~3')", "Unknown operator for DocumentKey: =bt=");
        documentRejected("id=nbt=('ACME~2024~1','ACME~2024~3')", "Unknown operator for DocumentKey: =nbt=");
        // The operator is checked first, so a wrong operator is not reported as a wrong key.
        documentRejected("id=gt='not a key'", "Unknown operator for DocumentKey: =gt=");
        documentRejected("period=lt='2024-01'", "Unknown operator for DocumentPeriod: =lt=");
        // Whatever the other side is: Hibernate would order the columns by attribute name, which is no order of the key.
        documentRejected("id=gt=id", "Unknown operator for DocumentKey: =gt=");
        documentRejected("id=le=:key", "Unknown operator for DocumentKey: =le=");
        documentRejected("id=bt=(:from,:to)", "Unknown operator for DocumentKey: =bt=");
        documentRejected("id=nbt=(id,id)", "Unknown operator for DocumentKey: =nbt=");
        documentRejected("period=bt=(1,2)", "Unknown operator for DocumentPeriod: =bt=");
    }

    @Test
    @DisplayName("a parameter or another field is compared with a key by equality, as before")
    void parametersAndFields() {
        assertThat(documentsWithParameters("id==:key", Map.of("key", new DocumentKey("ACME", 2024, 2L)))).containsExactly("A24-2");
        assertThat(documentsWithParameters("id!=:key", Map.of("key", new DocumentKey("ACME", 2024, 2L)))).hasSize(4);
        assertThat(documents("id==id")).hasSize(5);
        assertThat(documents("id!=id")).isEmpty();
    }

    @Test
    @DisplayName("an embeddable without a public static valueOf(String) cannot be written as a string")
    void embeddableWithoutValueOf() {
        rejected(KeyedLine.class, lineRepository, "id=='ACME~2024~1~1'", "LineKey with a string: it has no public static valueOf(String)");
        rejected(KeyedLine.class, lineRepository, "id=in=('ACME~2024~1~1')", "LineKey with a string: it has no public static valueOf(String)");
    }

    // ------------------------------------------------------------------
    // What did not change, and what the JPQL path can do now
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a condition on one part of the key is a plain scalar comparison, as before")
    void keyPartsStayScalar() {
        assertThat(documents("id.companyCode=='ACME'")).containsExactly("A23-1", "A24-1", "A24-2");
        assertThat(whereClauseOf("id.companyCode=='ACME'")).contains("company_code").doesNotContain("doc_year", "doc_no");
        assertThat(documents("id.companyCode=in=('ACME','BETA');id.docNo!=1")).containsExactly("A24-2");
        assertThat(documents("id.companyCode=bt=('ACME','ACME')")).containsExactly("A23-1", "A24-1", "A24-2");
        assertThat(documents("id.companyCode=nbt=('ACME','ACME')")).containsExactly("B24-1", "XY-9");
        assertThat(documents("id.docYear=lt=2024")).containsExactly("A23-1");
        assertThat(documents("title=like='*24-1'")).containsExactly("A24-1", "B24-1");
        assertThat(payments("document.id.docYear==2024")).containsExactly("P1", "P3");
        assertThat(lines("id.lineNo==2")).containsExactly("a1-2");
        assertThat(lines("id.documentKey.companyCode=='BETA'")).containsExactly("b1-1");
    }

    @Test
    @DisplayName("the JPQL path names an @EmbeddedId by its attribute, not by Hibernate's {id}")
    void jpqlPathNamesTheIdentifier() {
        RsqlContext<KeyedDocument> context = new RsqlContext<>(KeyedDocument.class).defineEntityManager(entityManager);
        context.root.alias("a0");
        assertThat(new RsqlCompiler<KeyedDocument>().compileToRsqlQuery("id.companyCode=='ACME'", context).where).isEqualTo("a0.id.companyCode=:p1");
        assertThat(new RsqlCompiler<KeyedDocument>().compileToRsqlQuery("id=='ACME~2024~2'", context).where).isEqualTo("a0.id=:p1");
    }

    @Test
    @DisplayName("an embeddable that is not an identifier works the same way")
    void nonIdentifierEmbeddable() {
        assertThat(documents("period=='2024-01'")).containsExactly("A24-1", "B24-1");
        assertThat(documents("period=in=('2024-01','2023-01')")).containsExactly("A23-1", "A24-1", "B24-1");
        // XY-9 has no period, and a missing value is not "different from" one.
        assertThat(documents("period!='2024-01'")).containsExactly("A23-1", "A24-2");
    }

    @Test
    @DisplayName("==null and !=null on an embeddable, on both paths")
    void nullChecks() {
        assertThat(documents("id==null")).isEmpty();
        assertThat(documents("id!=null")).containsExactly("A23-1", "A24-1", "A24-2", "B24-1", "XY-9");
        assertThat(documents("period==null")).containsExactly("XY-9");
        assertThat(payments("document.id==null")).containsExactly("P4");
    }

    @Test
    @DisplayName("a key declared as a type variable of a generic @MappedSuperclass is read by its concrete class")
    void genericKeyInMappedSuperclass() {
        // The path reports the erased bound, Serializable; the entity's key class is DocumentKey.
        assertThat(genericDocuments("id=='ACME~2024~2'")).containsExactly("G-A24-2");
        assertThat(genericDocuments("id=in=('ACME~2024~1','BETA~2024~1')")).containsExactly("G-A24-1", "G-B24-1");
        assertThat(genericDocuments("id=nin=('ACME~2024~1')")).containsExactly("G-A24-2", "G-B24-1");
        assertThat(genericDocuments("period=='2024-01'")).containsExactly("G-A24-1", "G-B24-1");
        rejected(GenericKeyedDocument.class, genericRepository, "id=gt='ACME~2024~1'", "Unknown operator for DocumentKey: =gt=");
        rejected(GenericKeyedDocument.class, genericRepository, "id=='ACME~2024'", "Invalid value for DocumentKey: ACME~2024");
    }

    @Test
    @DisplayName("a path through a derived identity has no identifier step on the JPQL path")
    void derivedIdentity() {
        // PaymentTag's key is (payment, tag), a virtual identifier with no attribute: a0.payment.reference, not a0.{id}.payment.reference.
        assertThat(rows(PaymentTag.class, tagRepository, "payment.reference=='P1'", PaymentTag::getTag)).containsExactly("urgent");
        assertThat(rows(PaymentTag.class, tagRepository, "tag=='urgent';payment.document.id=='ACME~2024~1'", PaymentTag::getTag)).containsExactly("urgent");
    }

    // ------------------------------------------------------------------
    // Through the service
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a typed RsqlQueryService over a composite-key repository, in both modes")
    void throughTheService() {
        // KeyedDocumentRepository is a JpaRepository<KeyedDocument, DocumentKey>: the service no longer needs a Long id.
        RsqlQueryService<KeyedDocument, KeyedDocument, KeyedDocumentRepository, EntityMapper<KeyedDocument, KeyedDocument>> specificationMode =
            new RsqlQueryService<>(documentRepository, null, entityManager, KeyedDocument.class);
        RsqlQueryService<KeyedDocument, KeyedDocument, KeyedDocumentRepository, EntityMapper<KeyedDocument, KeyedDocument>> jpqlMode =
            new RsqlQueryService<>(documentRepository, null, entityManager, KeyedDocument.class, "select a0 from KeyedDocument a0", "select count(a0) from KeyedDocument a0");
        RsqlQueryService<KeyedDocument, KeyedDocument, KeyedDocumentRepository, EntityMapper<KeyedDocument, KeyedDocument>> twoPhase =
            new RsqlQueryService<>(documentRepository, null, entityManager, KeyedDocument.class, "select a0 from KeyedDocument a0", "select count(a0) from KeyedDocument a0");
        twoPhase.setPagingStrategy(PagingStrategy.IDS_THEN_HYDRATE);

        for (RsqlQueryService<KeyedDocument, KeyedDocument, KeyedDocumentRepository, EntityMapper<KeyedDocument, KeyedDocument>> service : List.of(specificationMode, jpqlMode, twoPhase)) {
            assertThat(service.countByFilter("id=='ACME~2024~2'")).isEqualTo(1L);
            assertThat(service.countByFilter("id!='ACME~2024~2'")).isEqualTo(4L);
            // A composite identifier has no single id to page by, so two-phase paging runs the single statement.
            assertThat(service.findEntitiesByFilter("id=in=('ACME~2024~1','BETA~2024~1')", PageRequest.of(0, 10)).getContent())
                .extracting(KeyedDocument::getTitle)
                .containsExactlyInAnyOrder("A24-1", "B24-1");
            entityManager.clear();
            // A bad key reaches the caller as a SyntaxErrorException, not as a data-access exception.
            assertThatThrownBy(() -> service.countByFilter("id=='ACME~2024'")).isInstanceOf(SyntaxErrorException.class);
            assertThatThrownBy(() -> service.countByFilter("id=gt='ACME~2024~1'")).isInstanceOf(SyntaxErrorException.class);
        }
    }
}
