package rsql.where;

import org.antlr.v4.runtime.tree.TerminalNode;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import rsql.antlr.where.RsqlWhereParser;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public class RsqlWhereHelper {

    /**
     * Escape character emitted with every generated LIKE predicate.
     * <p>
     * Without an explicit {@code ESCAPE} clause the meaning of a backslash in the pattern is left to the
     * database (and to the dialect), so the same filter could behave differently depending on where it runs.
     */
    public static final char LIKE_ESCAPE_CHARACTER = '\\';

    /**
     * Escape the characters that would otherwise be consumed by the escape character itself.
     * <p>
     * Only the backslash is doubled. {@code %} and {@code _} are deliberately left alone: they stay SQL
     * wildcards, which is the documented behaviour of this library.
     *
     * @param value The raw value
     * @return The value with every backslash doubled
     */
    public static String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\");
    }

    /**
     * Build a WHERE LIKE pattern: escape the backslash, then map the RSQL wildcard {@code *} to SQL {@code %}.
     * <p>
     * Note that HAVING does <em>not</em> map {@code *} - there the user writes {@code %} directly - so the
     * HAVING path uses {@link #escapeLikePattern(String)} on its own.
     *
     * @param value The value taken from the string literal
     * @return The pattern to pass to {@code like} / {@code notLike}
     */
    static String toLikePattern(String value) {
        return escapeLikePattern(value).replace('*', '%');
    }

    /**
     * Extract the field name (or the field path) from the FieldContext
     *
     * @param ctx The field context
     * @return Field name or field path (tbl1.tbl2.field2)
     */
    static String getFieldName(RsqlWhereParser.FieldContext ctx) {
        StringBuilder field = new StringBuilder(ctx.ID().getText());
        // hoisted: ctx.DOT_ID() is getRuleContexts(), which rebuilds the list by scanning every child,
        // so calling it per iteration makes this O(n^2) in the length of the field path
        for (TerminalNode segment : ctx.DOT_ID()) {
            field.append(segment.getText());
        }

        return field.toString();
    }

    /**
     * * Verifies if a class metamodel has the specified property.
     *
     * @param property      Property name.
     * @param classMetadata Class metamodel that may hold that property.
     * @param <T>           Class that we are working with
     * @return true if the class has that property, false otherwise.
     */
    public static <T> boolean hasPropertyName(String property, ManagedType<T> classMetadata) {
        Set<Attribute<? super T, ?>> names = classMetadata.getAttributes();
        for (Attribute<? super T, ?> name : names) {
            if (name.getName().equals(property)) return true;
        }
        return false;
    }

    /**
     * Verify if a property is an Association type.
     *
     * @param property      Property to verify.
     * @param classMetadata Metamodel of the class we want to check.
     * @param <T>           Class that we are working with
     * @return true if the property is an association, false otherwise.
     */
    public static <T> boolean isAssociationType(String property, ManagedType<T> classMetadata) {
        return classMetadata.getAttribute(property).isAssociation();
    }

    /**
     * Get the property Type out of the metamodel.
     *
     * @param property      Property name.
     * @param classMetadata Class metamodel that may hold that property.
     * @param <T>           Class that we are working with
     * @return true if the class has that property, false otherwise.
     */
    public static <T> Class<?> findPropertyType(String property, ManagedType<T> classMetadata) {
        Class<?> propertyType;
        if (classMetadata.getAttribute(property).isCollection()) {
            propertyType = ((PluralAttribute) classMetadata.getAttribute(property)).getBindableJavaType();
        } else {
            propertyType = classMetadata.getAttribute(property).getJavaType();
        }
        return propertyType;
    }

    /**
     * Verify if a property is an Embedded type.
     *
     * @param property      Property to verify.
     * @param classMetadata Metamodel of the class we want to check.
     * @param <T>           Class that we are working with
     * @return true if the property is an embedded attribute, false otherwise.
     */
    public static <T> boolean isEmbeddedType(String property, ManagedType<T> classMetadata) {
        return classMetadata.getAttribute(property).getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;
    }

    static Object getInListLiteral(RsqlWhereParser.InListElementContext ctx) {
        if (ctx.STRING_LITERAL() != null) {
            return getStringFromStringLiteral(ctx.STRING_LITERAL());
        } else if (ctx.DECIMAL_LITERAL() != null) {
            return Long.valueOf(ctx.DECIMAL_LITERAL().getText());
        } else if (ctx.REAL_LITERAL() != null) {
            return new BigDecimal(ctx.REAL_LITERAL().getText());
        } else if (ctx.DATE_LITERAL() != null) {
            return getLocalDateFromDateLiteral(ctx.DATE_LITERAL());
        } else if (ctx.DATETIME_LITERAL() != null) {
            return getInstantFromDatetimeLiteral(ctx.DATETIME_LITERAL());
        } else if (ctx.ENUM_LITERAL() != null) {
            return getStringFromStringLiteral(ctx.ENUM_LITERAL());
        }

        throw new IllegalArgumentException("Unknown property: " + ctx.getText());
    }

    /**
     * Extract the value of a string literal, removing the delimiters and un-escaping a doubled delimiter.
     * <p>
     * The grammar allows a delimiter to be escaped by doubling it ({@code 'it''s'}, {@code "say ""hi"""},
     * {@code `a``b`}), so the doubled delimiter has to be collapsed back into a single character. This mirrors
     * the semantics of the HAVING path (see {@code HavingSpecificationVisitor}).
     * <p>
     * The method is shared with {@code ENUM_LITERAL} ({@code #NAME#}); the {@code #} delimiter deliberately
     * falls through untouched, so enum values keep their current behaviour.
     * <p>
     * Backslash escaping is <em>not</em> handled here - it is out of scope.
     *
     * @param stringLiteral The STRING_LITERAL or ENUM_LITERAL terminal node
     * @return The literal value without delimiters, with a doubled delimiter un-escaped
     */
    static String getStringFromStringLiteral(TerminalNode stringLiteral) {
        String s = stringLiteral.getText();
        if (s.length() < 2) {
            return "";
        }
        String body = s.substring(1, s.length() - 1);
        char delimiter = s.charAt(0);
        if (delimiter == '"' || delimiter == '\'' || delimiter == '`') {
            String d = String.valueOf(delimiter);
            return body.replace(d + d, d);
        }

        return body;
    }

    static String getParamFromLiteral(TerminalNode literal) {
        String s = literal.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length());
        } else {
            s = "";
        }

        return s;
    }

    public static LocalDate getLocalDateFromDateLiteral(TerminalNode dateLiteral) {
        String s = dateLiteral.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length() - 1);
            return LocalDate.parse(s);
        }
        return null;
    }

    public static Instant getInstantFromDatetimeLiteral(TerminalNode datetimeLiteral) {
        String s = datetimeLiteral.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length() - 1);
            return Instant.parse(s);
        }
        return null;
    }

    static String getStringFromDatetimeLiteral(TerminalNode datetimeLiteral) {
        String s = datetimeLiteral.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length() - 1);
            return "'".concat(s).concat("'");
        }
        return null;
    }

    static String getStringFromDateLiteral(TerminalNode dateLiteral) {
        String s = dateLiteral.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length() - 1);
            return "'".concat(s).concat("'");
        }
        return null;
    }

    static boolean isFieldEnumType(Path<?> pathField) {
        return Enum.class.isAssignableFrom(pathField.getJavaType());
    }

    static boolean isFieldUuidType(Path<?> pathField) {
        return pathField.getJavaType().equals(java.util.UUID.class);
    }

    public static <E extends Enum<E>> E getEnum(String text, Class<E> klass) {
        return Enum.valueOf(klass, text);
    }

    public static String getFromClause(RsqlQuery query, String rootEntity, String rootEntityAlias) {
        String from = rootEntity + " " + rootEntityAlias;

        for (Map.Entry<String, RsqlJoin> entry : query.joins.entrySet()) {
            RsqlJoin join = entry.getValue();
            from += " " + join.joinType + " " + join.parentAlias + "." + join.attribute + " " + join.alias;
        }

        return from;
    }
}
