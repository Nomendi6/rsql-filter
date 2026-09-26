package rsql.where;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;

/**
 * A string literal compared with an embeddable becomes a value through the type's own
 * {@code public static valueOf(String)} - and everything that can go wrong on the way is a
 * {@link SyntaxErrorException}, except an {@link Error}, which is not a bad literal.
 */
class ValueOfLiteralTest {

    public static class Key {
        final String text;

        Key(String text) {
            this.text = text;
        }

        public static Key valueOf(String text) {
            if (text.isEmpty()) {
                throw new IllegalArgumentException("Empty key");
            }
            if (text.equals("null")) {
                return null;
            }
            if (text.equals("error")) {
                throw new AssertionError("not a literal problem");
            }
            return new Key(text);
        }
    }

    public static class NoValueOf {}

    public static class InstanceValueOf {
        public InstanceValueOf valueOf(String text) {
            return this;
        }
    }

    public static class ValueOfOfAnotherType {
        public static String valueOf(String text) {
            return text;
        }
    }

    public static class Base {
        public static Base valueOf(String text) {
            return new Base();
        }
    }

    /** Inherits Base.valueOf, which makes a Base - not a Derived. */
    public static class Derived extends Base {}

    public static class NarrowerValueOf {
        public static NarrowerSubtype valueOf(String text) {
            return new NarrowerSubtype();
        }
    }

    public static class NarrowerSubtype extends NarrowerValueOf {}

    public static class CheckedValueOf {
        public static CheckedValueOf valueOf(String text) throws java.io.IOException {
            throw new java.io.IOException("unreadable: " + text);
        }
    }

    @Test
    @DisplayName("the type's valueOf makes the value")
    void convertsThroughValueOf() {
        Object value = RsqlWhereHelper.valueOfLiteral(Key.class, "ACME~2024~17");
        assertThat(value).isInstanceOf(Key.class);
        assertThat(((Key) value).text).isEqualTo("ACME~2024~17");
        // A valueOf may return a subtype of the type it is declared on.
        assertThat(RsqlWhereHelper.valueOfLiteral(NarrowerValueOf.class, "x")).isInstanceOf(NarrowerSubtype.class);
    }

    @Test
    @DisplayName("a type without a usable valueOf(String) is refused by name")
    void typeWithoutValueOf() {
        for (Class<?> type : new Class<?>[] { NoValueOf.class, InstanceValueOf.class, ValueOfOfAnotherType.class, Derived.class }) {
            assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(type, "x"))
                .as(type.getSimpleName())
                .isInstanceOf(SyntaxErrorException.class)
                .hasMessage("Cannot compare " + type.getName() + " with a string: it has no public static valueOf(String)");
        }
    }

    @Test
    @DisplayName("an exception from valueOf is a SyntaxErrorException with its reason, and the cause is kept")
    void valueOfThrows() {
        assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(Key.class, ""))
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessage("Invalid value for Key:  (Empty key)")
            .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a checked exception from valueOf is a SyntaxErrorException too")
    void valueOfThrowsChecked() {
        assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(CheckedValueOf.class, "x"))
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessage("Invalid value for CheckedValueOf: x (unreadable: x)")
            .hasCauseInstanceOf(java.io.IOException.class);
    }

    @Test
    @DisplayName("a valueOf inherited from a base that is not public is called as Java would call it")
    void valueOfInheritedFromHiddenBase() {
        assertThat(RsqlWhereHelper.valueOfLiteral(rsql.where.keys.PublicSubKey.class, "k").toString()).isEqualTo("k");
    }

    @Test
    @DisplayName("a null from valueOf is a SyntaxErrorException, never a null bound into the query")
    void valueOfReturnsNull() {
        assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(Key.class, "null"))
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessage("Invalid value for Key: null (valueOf returned null)");
    }

    @Test
    @DisplayName("an Error from valueOf is rethrown as it is")
    void errorIsNotWrapped() {
        assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(Key.class, "error"))
            .isExactlyInstanceOf(AssertionError.class)
            .hasMessage("not a literal problem");
    }

    @Test
    @DisplayName("a key class the library cannot reach is a SyntaxErrorException")
    void inaccessibleClass() throws ClassNotFoundException {
        Class<?> hidden = Class.forName("rsql.where.keys.PackagePrivateKey");
        assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(hidden, "x"))
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessageStartingWith("Cannot call rsql.where.keys.PackagePrivateKey.valueOf(String)")
            .hasCauseInstanceOf(IllegalAccessException.class);
    }

    @Test
    @DisplayName("the lookup is repeatable and does not depend on the literal")
    void lookupIsStable() {
        for (int i = 0; i < 3; i++) {
            assertThat(((Key) RsqlWhereHelper.valueOfLiteral(Key.class, "k" + i)).text).isEqualTo("k" + i);
            assertThatThrownBy(() -> RsqlWhereHelper.valueOfLiteral(NoValueOf.class, "x")).isInstanceOf(SyntaxErrorException.class);
        }
    }
}
