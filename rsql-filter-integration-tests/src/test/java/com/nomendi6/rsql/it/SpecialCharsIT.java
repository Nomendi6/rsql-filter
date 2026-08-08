package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;

import java.math.BigDecimal;
import java.util.List;

import rsql.exceptions.SyntaxErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end tests for un-escaping of a doubled delimiter on the WHERE path.
 *
 * <p>The grammar lets a delimiter be escaped by doubling it ({@code "say ""hi"""}, {@code 'it''s'},
 * {@code `a``b`}). Before this fix the WHERE path kept the doubled delimiter in the value, so such a
 * filter silently matched nothing, while the HAVING path already un-escaped it.</p>
 *
 * <p>These are execution tests: they go through {@code compileToSpecification} and hit the database,
 * so they cover every branch that reads a string literal - not just the shared helper.</p>
 */
@IntegrationTest
public class SpecialCharsIT {

    private static final String SAY_HI = "say \"hi\"";
    private static final String ITS = "it's";
    private static final String A_B = "a\"b";
    private static final String A_TICK_B = "a`b";
    private static final String PLAIN = "plain";
    /** value ending with a single backslash - not expressible before 0.7.4 */
    private static final String TRAILING_BS = "C:\\dir\\";

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductMapper productMapper;

    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

    private final Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

    @BeforeEach
    @Transactional
    void setup() {
        queryService = new RsqlQueryService<>(productRepository, productMapper, em, Product.class);

        ProductType type = productTypeRepository.save(
            new ProductType().withCode("T").withName("Type").withDescription("t"));

        // seeded through the repository, not through RSQL, so the values are exactly these
        for (String code : new String[] { SAY_HI, ITS, A_B, A_TICK_B, PLAIN, TRAILING_BS }) {
            productRepository.save(new Product()
                .withCode(code)
                .withName(code)
                .withDescription(code)
                .withPrice(new BigDecimal("1.00"))
                .withStatus(StandardRecordStatus.ACTIVE)
                .withProductType(type));
        }
    }

    @AfterEach
    @Transactional
    void cleanup() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    private List<String> codes(String filter) {
        return queryService.findByFilterAndSort(filter, pageable)
            .stream().map(ProductDTO::getCode).toList();
    }

    @Test
    void equalsWithDoubledDoubleQuote() {
        assertThat(codes("code==\"say \"\"hi\"\"\"")).containsExactly(SAY_HI);
    }

    @Test
    void equalsWithDoubledSingleQuote() {
        assertThat(codes("code=='it''s'")).containsExactly(ITS);
    }

    @Test
    void equalsWithDoubledBacktick() {
        // a doubled backtick inside a backtick-delimited literal collapses to a single one
        assertThat(codes("code==`a``b`")).containsExactly(A_TICK_B);
    }

    @Test
    void backtickDelimiterWithEmbeddedDoubleQuote() {
        // no doubling needed - a double quote is an ordinary character inside a backtick literal
        assertThat(codes("code==`a\"b`")).containsExactly(A_B);
    }

    @Test
    void notEqualsWithDoubledDelimiter() {
        assertThat(codes("code!=\"say \"\"hi\"\"\""))
            .containsExactlyInAnyOrder(ITS, A_B, A_TICK_B, PLAIN, TRAILING_BS);
    }

    @Test
    void inListWithDoubledDelimiter() {
        assertThat(codes("code=in=(\"say \"\"hi\"\"\",'it''s')"))
            .containsExactlyInAnyOrder(SAY_HI, ITS);
    }

    @Test
    void notInListWithDoubledDelimiter() {
        assertThat(codes("code=nin=(\"say \"\"hi\"\"\",'it''s')"))
            .containsExactlyInAnyOrder(A_B, A_TICK_B, PLAIN, TRAILING_BS);
    }

    @Test
    void betweenWithDoubledDelimiter() {
        // the bounds must be chosen so that the outcome differs with and without un-escaping:
        // un-escaped the range is a"a .. a"c and contains a"b; left as a""a .. a""c it does not
        assertThat(codes("code=bt=(\"a\"\"a\",\"a\"\"c\")")).containsExactly(A_B);
    }

    @Test
    void notBetweenWithDoubledDelimiter() {
        assertThat(codes("code=nbt=(\"a\"\"a\",\"a\"\"c\")"))
            .containsExactlyInAnyOrder(SAY_HI, ITS, A_TICK_B, PLAIN, TRAILING_BS);
    }

    @Test
    void likeWithDoubledDelimiter() {
        assertThat(codes("code=*\"*say \"\"hi\"\"*\"")).containsExactly(SAY_HI);
    }

    @Test
    void notLikeWithDoubledDelimiter() {
        assertThat(codes("code=!*\"*say \"\"hi\"\"*\""))
            .containsExactlyInAnyOrder(ITS, A_B, A_TICK_B, PLAIN, TRAILING_BS);
    }

    @Test
    void caseSensitiveLikeWithDoubledDelimiter() {
        assertThat(codes("code=clike=\"*say \"\"hi\"\"*\"")).containsExactly(SAY_HI);
    }

    @Test
    void valueWithSingleQuoteInDoubleQuotedLiteral() {
        // no doubling needed - a single quote is an ordinary character inside a double-quoted literal
        assertThat(codes("code==\"it's\"")).containsExactly(ITS);
    }

    @Test
    void enumLiteralIsNotAffected() {
        // regression guard (R5): the helper is shared with ENUM_LITERAL and must leave # alone
        assertThat(codes("status==#ACTIVE#"))
            .containsExactlyInAnyOrder(SAY_HI, ITS, A_B, A_TICK_B, PLAIN, TRAILING_BS);
    }

    @Test
    void valueEndingWithBackslashIsMatched() {
        // not expressible before 0.7.4 - the filter used to fail on the lexer
        assertThat(codes("code==\"C:\\dir\\\"")).containsExactly(TRAILING_BS);
    }

    @Test
    void valueEndingWithBackslashInList() {
        assertThat(codes("code=in=(\"C:\\dir\\\",'plain')"))
            .containsExactlyInAnyOrder(TRAILING_BS, PLAIN);
    }

    @Test
    void conditionsWithoutLogicalOperatorAreRejected() {
        // used to silently drop the first condition, so the query returned more rows than the filter asked for
        assertThatThrownBy(() -> codes("code=='plain' name=='plain'"))
            .isInstanceOf(SyntaxErrorException.class);
    }

    @Test
    void likeMatchesLiteralBackslash() {
        // the pattern is backslash-escaped and carries ESCAPE '\\', so \\ matches a literal backslash
        assertThat(codes("code=*\"*C:\\dir*\"")).containsExactly(TRAILING_BS);
        assertThat(codes("code=clike=\"*C:\\dir*\"")).containsExactly(TRAILING_BS);
    }

    @Test
    void likePatternEndingWithBackslash() {
        // not expressible before 0.7.4, and a database error on the native path before 0.7.5
        assertThat(codes("code=*\"*dir\\*\"")).containsExactly(TRAILING_BS);
    }

    @Test
    void percentStaysAWildcard() {
        // documented behaviour: % and _ are NOT escaped, they stay SQL wildcards
        assertThat(codes("code=*'%'")).hasSize(6);
    }

    @Test
    void plainValueIsUnchanged() {
        // backward compatibility: a value without a doubled delimiter must behave exactly as before
        assertThat(codes("code=='plain'")).containsExactly(PLAIN);
        assertThat(codes("code=*'pla*'")).containsExactly(PLAIN);
    }
}
