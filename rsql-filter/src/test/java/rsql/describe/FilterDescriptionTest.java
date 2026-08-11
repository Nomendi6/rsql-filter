package rsql.describe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.RsqlCompiler;
import rsql.exceptions.SyntaxErrorException;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FilterDescriptionTest {

    private RsqlFilterDescription describer;

    @BeforeEach
    void setUp() {
        describer = new RsqlFilterDescription();
    }

    // ============================================================ A. grammar coverage

    @Test
    void everyConditionAlternativeIsDescribed() {
        assertEquals(new RightSide.SingleValue("Ana"), rightSideOf("name=='Ana'"));
        assertEquals(new RightSide.SingleValue(1L), rightSideOf("a==1"));
        assertEquals(new RightSide.SingleValue(new BigDecimal("1.5")), rightSideOf("a==1.5"));
        assertEquals(new RightSide.SingleValue(LocalDate.parse("2020-01-01")), rightSideOf("d==#2020-01-01#"));
        assertEquals(new RightSide.SingleValue(Instant.parse("2020-01-01T10:00:00Z")),
            rightSideOf("d==#2020-01-01T10:00:00Z#"));
        assertEquals(new RightSide.SingleValue("ACTIVE"), rightSideOf("status==#ACTIVE#"));
        assertEquals(new RightSide.FieldRef("other"), rightSideOf("a==other"));
        assertEquals(new RightSide.Parameter("p1"), rightSideOf("a==:p1"));
        assertEquals(new RightSide.NoValue(), rightSideOf("a==null"));
        assertEquals(new RightSide.SingleValue(Boolean.TRUE), rightSideOf("a==true"));
        assertEquals(new RightSide.SingleValue(Boolean.FALSE), rightSideOf("a==false"));
        assertInstanceOf(RightSide.ValueList.class, rightSideOf("a=in=(1,2)"));
        assertInstanceOf(RightSide.ValueList.class, rightSideOf("a=nin=(1,2)"));
        assertInstanceOf(RightSide.Range.class, rightSideOf("a=bt=(1,2)"));
        assertInstanceOf(RightSide.Range.class, rightSideOf("a=nbt=(1,2)"));
    }

    /** All six combinations of operatorBasic with NULL / TRUE / FALSE stay distinguishable. */
    @Test
    void nullAndBooleanConditionsAreAllDistinct() {
        List<FilterCondition> six = Arrays.asList(
            conditionOf("a==null"), conditionOf("a!=null"),
            conditionOf("a==true"), conditionOf("a!=true"),
            conditionOf("a==false"), conditionOf("a!=false"));
        assertEquals(6, six.stream().distinct().count(), "the six must not collapse onto each other");

        assertEquals(FilterOperator.IS_NULL, six.get(0).operator());
        assertEquals(FilterOperator.IS_NOT_NULL, six.get(1).operator());
        assertEquals(FilterOperator.EQ, six.get(2).operator());
    }

    /** The grammar allows a field and a parameter inside IN and BETWEEN, and the helper throws on both. */
    @Test
    void listElementsMayBeFieldsAndParameters() {
        RightSide.ValueList list = (RightSide.ValueList) rightSideOf("code=in=('a',status,:p1)");
        assertEquals(List.of(
            new ListItem.ItemValue("a"),
            new ListItem.ItemField("status"),
            new ListItem.ItemParam("p1")), list.values());

        RightSide.Range range = (RightSide.Range) rightSideOf("price=bt=(minPrice,:upper)");
        assertEquals(new ListItem.ItemField("minPrice"), range.from());
        assertEquals(new ListItem.ItemParam("upper"), range.to());
    }

    /** Symbolic and worded operators, in any case, normalise onto the same constant. */
    @Test
    void operatorSpellingsNormalise() {
        assertEquals(FilterOperator.LIKE, conditionOf("a=*'x'").operator());
        assertEquals(FilterOperator.LIKE, conditionOf("a=LIKE='x'").operator());
        assertEquals(FilterOperator.NLIKE, conditionOf("a!=*'x'").operator());
        assertEquals(FilterOperator.GT, conditionOf("a=GT=1").operator());
        assertEquals(FilterOperator.NEQ, conditionOf("a=!1").operator());
    }

    @Test
    void groupsAreNormalised() {
        FilterGroup flat = (FilterGroup) describer.parse("a==1;b==2;c==3");
        assertEquals(FilterGroup.Junction.AND, flat.junction());
        assertEquals(3, flat.children().size(), "a;b;c is one group of three, not nested pairs");

        assertEquals(describer.parse("a==1;b==2;c==3"), describer.parse("a==1;(b==2;c==3)"),
            "redundant parentheses must not change the tree");

        FilterGroup mixed = (FilterGroup) describer.parse("a==1;b==2,c==3");
        assertEquals(FilterGroup.Junction.OR, mixed.junction(), "AND binds tighter than OR");
    }

    @Test
    void emptyFilterGivesAnEmptyDescription() {
        for (String blank : new String[] {null, "", "   "}) {
            assertNull(describer.parse(blank));
            FilterDescription description = describer.describe(blank);
            assertTrue(description.isEmpty());
            assertTrue(description.getRows().isEmpty());
            assertEquals("", description.getText());
        }
        assertTrue(describer.describeNode(null, FilterLabelResolver.TECHNICAL).isEmpty());
    }

    // ============================================================ B. model invariants

    @Test
    void contradictoryConditionsAreRejected() {
        Operand field = new Operand.FieldOperand("code");
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.IS_NULL, new RightSide.SingleValue("x"), PatternShape.NONE, null));
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.EQ, new RightSide.NoValue(), PatternShape.NONE, null));
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.IN, new RightSide.Range(
                new ListItem.ItemValue(1L), new ListItem.ItemValue(2L)), PatternShape.NONE, null));
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.EQ, new RightSide.SingleValue("x"), PatternShape.STARTS_WITH, "x"));
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.LIKE, new RightSide.FieldRef("other"), PatternShape.CONTAINS, "x"));
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.LIKE, new RightSide.SingleValue("A*"), PatternShape.STARTS_WITH, null));
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.EQ, new RightSide.SingleValue("x"), PatternShape.NONE, "unexpected"));
    }

    @Test
    void malformedPartsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Operand.FieldOperand(""));
        assertThrows(NullPointerException.class, () -> new Operand.FieldOperand(null));
        assertThrows(IllegalArgumentException.class, () -> new RightSide.ValueList(List.of()));
        assertThrows(NullPointerException.class,
            () -> new RightSide.Range(null, new ListItem.ItemValue(1L)));
        assertThrows(IllegalArgumentException.class,
            () -> new RightSide.SingleValue(new ArrayList<>()), "a mutable value would break equals later");
        assertThrows(IllegalArgumentException.class, () -> new FilterGroup(
            FilterGroup.Junction.AND, List.of(conditionOf("a==1"))), "a group needs at least two children");
        assertThrows(IllegalArgumentException.class, () -> new FilterGroup(
            FilterGroup.Junction.AND, List.of(
                conditionOf("a==1"),
                new FilterGroup(FilterGroup.Junction.AND, List.of(conditionOf("b==2"), conditionOf("c==3"))))),
            "a child must not repeat its parent's junction");
    }

    @Test
    void groupCopiesItsChildren() {
        List<FilterNode> children = new ArrayList<>(List.of(conditionOf("a==1"), conditionOf("b==2")));
        FilterGroup group = new FilterGroup(FilterGroup.Junction.AND, children);
        int hash = group.hashCode();

        children.add(conditionOf("c==3"));

        assertEquals(2, group.children().size());
        assertEquals(hash, group.hashCode(), "changing the caller's list must not change the group");
    }

    // ============================================================ C. rendering

    @Test
    void patternShapesAreDerivedAndRendered() {
        assertEquals("name starts with (ignoring case) \"A\"", text("name=*'A*'"));
        assertEquals("name ends with (ignoring case) \"A\"", text("name=*'*A'"));
        assertEquals("name contains (ignoring case) \"A\"", text("name=*'*A*'"));
        assertEquals("name equals (ignoring case) \"ABC\"", text("name=*'ABC'"));
        assertEquals("name does not contain (ignoring case) \"A\"", text("name=!*'*A*'"));
        assertEquals("name starts with (case-sensitive) \"A\"", text("name=^*'A*'"));
        assertEquals("name does not start with (case-sensitive) \"A\"", text("name=!^*'A*'"));
    }

    /**
     * {@code %} and {@code _} stay SQL wildcards and a {@code *} in the middle is neither a prefix nor a
     * suffix, so the description must not claim any of those - and must show the value as written.
     */
    @Test
    void patternsThatCannotBeDescribedTrulyFallBackToTheRawValue() {
        for (String raw : new String[] {"A*B", "50%", "A_C", "*", "**"}) {
            FilterCondition condition = conditionOf("name=*'" + raw + "'");
            assertEquals(PatternShape.CUSTOM, condition.patternShape(), raw);
            assertNull(condition.patternNeedle(), raw);
            assertEquals("name matches pattern (ignoring case) \"" + raw + "\"", text("name=*'" + raw + "'"));
        }
    }

    @Test
    void everyRightSideShapeRenders() {
        assertEquals("a is empty", text("a==null"));
        assertEquals("a is not empty", text("a!=null"));
        assertEquals("a is one of 1, 2", text("a=in=(1,2)"));
        assertEquals("a is between 1 and 2", text("a=bt=(1,2)"));
        assertEquals("a is other", text("a==other"));
        assertEquals("a is :p1", text("a==:p1"));
    }

    /** Two different filters must not produce the same rows - that was the whole point of the parentheses. */
    @Test
    void structurallyDifferentFiltersRenderDifferently() {
        String a = "a==1;(b==2,(c==3;d==4));((e==5;f==6),g==7)";
        String b = "a==1;(b==2,(c==3;d==4;e==5;f==6),g==7)";

        assertNotEquals(render(a), render(b), "these two filters mean different things");
        assertNotEquals(text(a), text(b));
    }

    @Test
    void connectorBelongsToTheLowestCommonAncestor() {
        List<FilterRow> rows = describer.describe("a==1;(b==2,c==3)").getRows();
        assertNull(rows.get(0).getConnector());
        assertEquals("and", rows.get(1).getConnector(), "b joins the outer group, not the inner one");
        assertEquals("or", rows.get(2).getConnector());
    }

    @Test
    void pureAndChainNeedsNoParentheses() {
        FilterDescription description = describer.describe("a==1;b==2;c==3");
        assertTrue(description.isPureAndChain());
        for (FilterRow row : description.getRows()) {
            assertEquals(0, row.getDepth());
            assertEquals(0, row.getOpenGroups());
            assertEquals(0, row.getCloseGroups());
        }
        assertFalse(describer.describe("a==1;b==2,c==3").isPureAndChain());
    }

    /** Jasper reads beans through Introspector, which does not see a record's accessors. */
    @Test
    void filterRowIsAJavaBean() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(FilterRow.class, Object.class);
        List<String> properties = Arrays.stream(info.getPropertyDescriptors())
            .map(PropertyDescriptor::getName).sorted().collect(Collectors.toList());
        assertEquals(List.of("closeGroups", "connector", "depth", "field", "openGroups", "operator", "value"),
            properties);
    }

    @Test
    void textCanBeTruncated() {
        FilterDescription description = describer.describe("a==1;b==2;c==3");
        assertTrue(description.getText().length() > 10);
        assertEquals(10, description.getText(10).length());
        assertTrue(description.getText(10).endsWith("…"));
        assertEquals(description.getText(), description.getText(1000));
        assertThrows(IllegalArgumentException.class, () -> description.getText(0));
    }

    // ============================================================ D. parameters

    @Test
    void parameterResolutionDistinguishesAbsentFromNull() {
        assertEquals("a is :p1", text("a==:p1"), "no map: show the placeholder");

        Map<String, Object> withNull = new HashMap<>();
        withNull.put("p1", null);
        assertEquals("a is null",
            describer.describe("a==:p1", FilterLabelResolver.TECHNICAL, withNull).getText(),
            "key present with a null value counts as supplied");

        assertEquals("a is :p1",
            describer.describe("a==:p1", FilterLabelResolver.TECHNICAL, Map.of("other", 1)).getText(),
            "a missing key is not supplied");

        assertEquals("a is 42",
            describer.describe("a==:p1", FilterLabelResolver.TECHNICAL, Map.of("p1", 42)).getText());
    }

    @Test
    void parametersInsideListsAndRangesAreResolvedToo() {
        Map<String, Object> values = Map.of("lo", 10, "hi", 20);
        assertEquals("a is between 10 and 20",
            describer.describe("a=bt=(:lo,:hi)", FilterLabelResolver.TECHNICAL, values).getText());
        assertEquals("a is one of 10, 20",
            describer.describe("a=in=(:lo,:hi)", FilterLabelResolver.TECHNICAL, values).getText());
    }

    // ============================================================ E. resolver and API

    @Test
    void resolverSuppliesEveryLabel() {
        FilterLabelResolver croatian = new FilterLabelResolver() {
            @Override
            public String operandLabel(Operand operand) {
                return operand instanceof Operand.FieldOperand field && field.fieldPath().equals("name")
                    ? "Naziv" : FilterLabelResolver.super.operandLabel(operand);
            }

            @Override
            public String operatorLabel(FilterCondition condition) {
                return condition.patternShape() == PatternShape.CONTAINS ? "sadrži"
                    : FilterLabelResolver.super.operatorLabel(condition);
            }

            @Override
            public String junctionLabel(FilterGroup.Junction junction) {
                return junction == FilterGroup.Junction.AND ? "I" : "ILI";
            }

            @Override
            public String valueLabel(Operand left, Object value) {
                return "\"" + value + "\"";
            }
        };
        assertEquals("Naziv sadrži \"abc\" I (status is \"A\" ILI code is \"B\")",
            describer.describe("name=*'*abc*';(status=='A',code=='B')", croatian).getText());
    }

    @Test
    void technicalResolverIsTheDefault() {
        assertEquals(describer.describe("a==1", FilterLabelResolver.TECHNICAL).getText(),
            describer.describe("a==1").getText());
    }

    @Test
    void nullResolverIsRejected() {
        assertThrows(NullPointerException.class, () -> describer.describe("a==1", null));
    }

    @Test
    void describingAnAlreadyBuiltTreeGivesTheSameResult() {
        FilterNode root = describer.parse("a==1;b==2");
        assertEquals(describer.describe("a==1;b==2").getText(),
            describer.describeNode(root, FilterLabelResolver.TECHNICAL).getText());
    }

    @Test
    void malformedFiltersStillFail() {
        assertThrows(SyntaxErrorException.class, () -> describer.parse("a==1 b==2"));
        assertThrows(SyntaxErrorException.class, () -> describer.parse("(a==1"));
        assertThrows(SyntaxErrorException.class, () -> describer.parse("a=="));
    }

    /** The description shares the tree parser, so it inherits the same recursion limits as the query path. */
    @Test
    void deepFiltersAreRejectedTheSameWayAsOnTheQueryPath() {
        String tooDeep = "(".repeat(20_000) + "a==1" + ")".repeat(20_000);
        assertThrows(SyntaxErrorException.class, () -> describer.parse(tooDeep));
    }


    /**
     * The default technical text must be unambiguous, not merely readable. Each of these used to produce a
     * line that could be read back as a different filter than the one described.
     */
    @Test
    void valuesThatWouldMakeTheTextAmbiguousAreQuotedAndEscaped() {
        // an empty value is a value, and must not look like an operator with no operand
        assertEquals("name is \"\"", text("name==''"));
        assertNotEquals(text("name==''"), text("name==NULL"));

        // a value that reads like a conjunction must not merge with the real ones
        assertEquals("name is \"A and status is B\" and status is \"C\"",
            text("name=='A and status is B';status=='C'"));

        // a comma inside a value must be distinguishable from the separator of the list
        assertEquals("code is one of \"A,B\", \"C\"", text("code=in=('A,B','C')"));

        // getText() promises one line
        String withNewline = text("name=='prvi\ndrugi'");
        assertEquals("name is \"prvi\\ndrugi\"", withNewline);
        assertFalse(withNewline.contains("\n"));

        // a quote or a backslash in the value must not close or escape the quoting itself
        assertEquals("name is \"say \\\"hi\\\"\"", text("name=='say \"hi\"'"));
        assertEquals("name is \"a\\\\b\"", text("name=='a\\b'"));
    }

    @Test
    void patternDataThatContradictsTheValueIsRejected() {
        Operand field = new Operand.FieldOperand("name");
        // the needle does not come from the value
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.LIKE, new RightSide.SingleValue("A*"), PatternShape.STARTS_WITH, "WRONG"));
        // the shape does not describe the value
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.LIKE, new RightSide.SingleValue("A*"), PatternShape.CONTAINS, "A"));
        // a LIKE pattern over something that is not a string
        assertThrows(IllegalArgumentException.class, () -> new FilterCondition(
            field, FilterOperator.LIKE, new RightSide.SingleValue(42L), PatternShape.EXACT, "42"));
        // and the consistent one is accepted
        assertEquals("name starts with (ignoring case) \"A\"",
            describer.describeNode(new FilterCondition(field, FilterOperator.LIKE,
                new RightSide.SingleValue("A*"), PatternShape.STARTS_WITH, "A"),
                FilterLabelResolver.TECHNICAL).getText());
    }

    /** RightSide is an extension point, so an unknown shape must still pass through the resolver. */
    @Test
    void anUnknownRightSideIsStillMasked() {
        record Secret() implements RightSide {
            @Override
            public String toString() {
                return "RAW_SECRET";
            }
        }
        FilterNode root = new FilterCondition(new Operand.FieldOperand("card"), FilterOperator.EQ,
            new Secret(), PatternShape.NONE, null);
        FilterLabelResolver masking = new FilterLabelResolver() {
            @Override
            public String valueLabel(Operand left, Object value) {
                return "***";
            }
        };
        assertEquals("card is ***", describer.describeNode(root, masking).getText());
        assertEquals("***", describer.describeNode(root, masking).getRows().get(0).getValue());
    }

    /** Both documented null inputs must compile without a cast - the overloads may not be ambiguous. */
    @Test
    void nullInputNeedsNoCast() {
        String noFilter = null;
        FilterNode noTree = null;
        assertTrue(describer.describe(noFilter).isEmpty());
        assertTrue(describer.describeNode(noTree, FilterLabelResolver.TECHNICAL).isEmpty());
    }

    /** The rows and the text are built from one rendering, so a stateful resolver cannot make them disagree. */
    @Test
    void eachConditionIsRenderedOnce() {
        int[] calls = {0};
        FilterLabelResolver counting = new FilterLabelResolver() {
            @Override
            public String valueLabel(Operand left, Object value) {
                return value + "#" + (++calls[0]);
            }
        };
        FilterDescription description = describer.describe("a==1", counting);
        assertEquals(1, calls[0]);
        assertEquals("1#1", description.getRows().get(0).getValue());
        assertEquals("a is 1#1", description.getText());
    }

    @Test
    void compilerProducesTheSameTreeAsTheDescriber() {
        RsqlCompiler<?> compiler = new RsqlCompiler<>();
        assertEquals(describer.parse("a==1;b=gt=2").toString(),
            compiler.compileToFilterNode("a==1;b=gt=2").toString());
        assertNull(compiler.compileToFilterNode(null));
        assertNull(compiler.compileToFilterNode("  "));
        assertThrows(SyntaxErrorException.class, () -> compiler.compileToFilterNode("a=="));
    }

    // ============================================================ G. supplied resolvers

    @Test
    void mapResolverNamesTheFieldsItKnows() {
        FilterLabelResolver labels = new MapFilterLabelResolver(Map.of(
            "productType.name", "Product type", "price", "Price"));
        assertEquals("Product type is \"A\" and Price is greater than 10 and code is \"C\"",
            describer.describe("productType.name=='A';price=gt=10;code=='C'", labels).getText());
    }

    @Test
    void mapResolverKeepsWhatItsDelegateDoes() {
        FilterLabelResolver masking = new FilterLabelResolver() {
            @Override
            public String valueLabel(Operand left, Object value) {
                return "***";
            }
        };
        FilterLabelResolver labels = new MapFilterLabelResolver(Map.of("card", "Card"), masking);
        assertEquals("Card is ***", describer.describe("card=='1234'", labels).getText());

        // and the map is copied, so the caller cannot change the resolver afterwards
        Map<String, String> mutable = new HashMap<>(Map.of("a", "A"));
        FilterLabelResolver copied = new MapFilterLabelResolver(mutable);
        mutable.put("b", "B");
        assertEquals("b is 1", describer.describe("b==1", copied).getText());
    }

    @Test
    void bundleResolverTranslatesFieldsOperatorsAndJunctions() {
        ResourceBundle bundle = bundleOf(Map.of(
            "field.name", "Naziv",
            "field.price", "Cijena",
            "operator.EQ", "je",
            "operator.GT", "je veći od",
            "operator.LIKE.STARTS_WITH", "počinje s",
            "junction.AND", "i",
            "junction.OR", "ili"));
        FilterLabelResolver labels = new ResourceBundleFilterLabelResolver(bundle);
        assertEquals("Naziv počinje s \"A\" i (Cijena je veći od 10 ili Cijena je 0)",
            describer.describe("name=*'A*';(price=gt=10,price==0)", labels).getText());
    }

    @Test
    void bundleResolverFallsBackForEveryKeyItIsMissing() {
        ResourceBundle bundle = bundleOf(Map.of("filter.field.name", "Naziv"));
        FilterLabelResolver labels = new ResourceBundleFilterLabelResolver(bundle, "filter.");
        // the prefixed field key is found, the operator and the untranslated field are not
        assertEquals("Naziv is \"A\" and code is not empty",
            describer.describe("name=='A';code!=NULL", labels).getText());
    }

    @Test
    void bundleKeysCanBeGeneratedRatherThanGuessed() {
        assertEquals("field.productType.name", ResourceBundleFilterLabelResolver.fieldKey("productType.name"));
        assertEquals("junction.AND", ResourceBundleFilterLabelResolver.junctionKey(FilterGroup.Junction.AND));
        assertEquals("operator.GT", ResourceBundleFilterLabelResolver.operatorKey(conditionOf("a=gt=1")));
        // the LIKE family is keyed by shape too, because one operator has several readings
        assertEquals("operator.LIKE.STARTS_WITH",
            ResourceBundleFilterLabelResolver.operatorKey(conditionOf("a=*'A*'")));
        assertEquals("operator.LIKE.CONTAINS",
            ResourceBundleFilterLabelResolver.operatorKey(conditionOf("a=*'*A*'")));
        assertEquals("operator.CNLIKE.EXACT",
            ResourceBundleFilterLabelResolver.operatorKey(conditionOf("a=!^*'A'")));
    }

    // ============================================================ helpers

    /** A bundle straight from a map, so the test does not need a properties file on the classpath. */
    private static ResourceBundle bundleOf(Map<String, String> entries) {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return entries.get(key);
            }

            @Override
            public Enumeration<String> getKeys() {
                return java.util.Collections.enumeration(entries.keySet());
            }
        };
    }

    private FilterCondition conditionOf(String filter) {
        return (FilterCondition) describer.parse(filter);
    }

    private RightSide rightSideOf(String filter) {
        return conditionOf(filter).rightSide();
    }

    private String text(String filter) {
        return describer.describe(filter).getText();
    }

    private String render(String filter) {
        return describer.describe(filter).getRows().stream()
            .map(FilterRow::toString).collect(Collectors.joining("\n"));
    }
}
