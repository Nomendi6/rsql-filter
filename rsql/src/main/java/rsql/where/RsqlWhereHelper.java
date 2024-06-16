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
     * Extract the field name (or the field path) from the FieldContext
     *
     * @param ctx The field context
     * @return Field name or field path (tbl1.tbl2.field2)
     */
    static String getFieldName(RsqlWhereParser.FieldContext ctx) {
        StringBuilder field = new StringBuilder(ctx.ID().getText());
        for (int i = 0; i < ctx.DOT_ID().size(); i++) {
            field.append(ctx.DOT_ID(i).getText());
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

    static String getStringFromStringLiteral(TerminalNode stringLiteral) {
        String s = stringLiteral.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length() - 1);
        } else {
            s = "";
        }

        return s;
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

    static LocalDate getLocalDateFromDateLiteral(TerminalNode dateLiteral) {
        String s = dateLiteral.getText();
        if (s.length() > 1) {
            s = s.substring(1, s.length() - 1);
            return LocalDate.parse(s);
        }
        return null;
    }

    static Instant getInstantFromDatetimeLiteral(TerminalNode datetimeLiteral) {
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
