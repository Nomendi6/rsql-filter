package rsql.where;

import org.antlr.v4.runtime.tree.TerminalNode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import rsql.antlr.where.RsqlWhereParser;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.internal.ToOneAttributeMapping;
import org.hibernate.persister.entity.EntityPersister;

import java.lang.annotation.Annotation;
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

    /**
     * Whether the identifier of a to-one association can be read from the querying table's own foreign key
     * column, without joining the target.
     *
     * <p>Only the provider can answer this. The association shape you can see from the JPA metamodel is not
     * enough, because several mappings that look like an ordinary {@code ManyToOne} still force a join:</p>
     * <ul>
     *   <li>the inverse side of a {@code OneToOne} ({@code mappedBy}) - its foreign key is on the other
     *       table;</li>
     *   <li>{@code @NotFound}, where the foreign key may name a row that does not exist, so the target has
     *       to be looked up to find out;</li>
     *   <li>{@code @SoftDelete} on the target, whose condition lives on the join;</li>
     *   <li>a foreign key referencing a column other than the target's primary key, where the value stored
     *       locally is not the identifier at all.</li>
     * </ul>
     *
     * <p>Guessing wrong is not merely a missed optimisation. When Hibernate cannot elide the join it still
     * honours {@code get()}, but as an <em>implicit</em> join - and an implicit join is an INNER join. The
     * pre-existing LEFT JOIN would silently become one, dropping every row whose foreign key is null. So the
     * question is put to Hibernate itself: {@code isFkOptimizationAllowed()} is the very predicate it uses
     * to decide, and {@code isTargetKeyPropertyName} confirms that this particular attribute is one it can
     * satisfy from the foreign key.</p>
     *
     * <p>Anything unexpected - a type that is not an entity, a provider that is not Hibernate, an attribute
     * the mapping metamodel does not report - answers false and the caller builds the join it always did.</p>
     *
     * @param entityManager Entity manager whose provider decides.
     * @param ownerType     Type declaring the association.
     * @param property      The association attribute.
     * @param idName        Identifier attribute of the target, as named in the selector.
     * @return true only when the provider will resolve the identifier from the foreign key column.
     */
    public static boolean canReadIdFromForeignKey(EntityManager entityManager, Class<?> ownerType, String property, String idName) {
        try {
            SessionFactoryImplementor sessionFactory = entityManager
                    .getEntityManagerFactory()
                    .unwrap(SessionFactoryImplementor.class);
            EntityPersister persister = sessionFactory.getMappingMetamodel().getEntityDescriptor(ownerType);
            return persister.findAttributeMapping(property) instanceof ToOneAttributeMapping toOne
                    && toOne.isFkOptimizationAllowed()
                    && toOne.getTargetKeyPropertyNames().contains(idName);
        } catch (RuntimeException notAvailable) {
            return false;
        }
    }

    /**
     * Whether joining this entity would restrict rows in a way its foreign key column cannot.
     *
     * <p>A join to an entity is not always just a lookup by identifier. Three kinds of restriction ride on
     * it, and none of them can ride on a foreign key column:</p>
     * <ul>
     *   <li>a permanent one - {@code @SQLRestriction}, the {@code @Where} it replaced, soft delete - which
     *       Hibernate reports as a where-restriction on the entity;</li>
     *   <li>an enabled {@code @Filter} that the entity declares with {@code applyToLoadByKey = true}, the
     *       idiomatic way to express tenant or row-level scoping. Hibernate renders every enabled filter onto
     *       the join once the entity declares one such filter;</li>
     *   <li>being one subtype of an inheritance hierarchy, where the join carries the predicate that selects
     *       the subtype - a discriminator for {@code SINGLE_TABLE}, a further join for {@code JOINED}. The
     *       foreign key column is constrained only to the hierarchy's shared table, so it can hold the
     *       identifier of a row of a different subtype.</li>
     * </ul>
     *
     * <p>In each case reading the identifier off the foreign key would match rows the join excludes, which is
     * a change of result rather than of plan. The entity is asked rather than its annotations read, so that
     * inherited and XML-declared mappings count too, and so that this says what Hibernate actually resolved.
     * Anything unexpected answers true: keeping a join costs a join, dropping a restriction costs
     * correctness.</p>
     *
     * @param entityManager Entity manager whose provider knows the mapping.
     * @param entityType    The association target.
     * @return true when the join carries a restriction the foreign key column does not
     */
    public static boolean joinCarriesRestrictions(EntityManager entityManager, Class<?> entityType) {
        try {
            EntityPersister persister = entityManager
                    .getEntityManagerFactory()
                    .unwrap(SessionFactoryImplementor.class)
                    .getMappingMetamodel()
                    .getEntityDescriptor(entityType);

            return persister.hasWhereRestrictions()
                    || hasLoadByKeyFilter(persister)
                    || !persister.getEntityName().equals(persister.getRootEntityName());
        } catch (RuntimeException notAvailable) {
            return true;
        }
    }

    /**
     * Whether the entity declares a {@code @Filter} with {@code applyToLoadByKey = true}.
     *
     * <p>Called reflectively because the method arrived after the 6.x line: there, filters are not applied to
     * a to-one join at all, so its absence means there is no such restriction to lose.</p>
     */
    private static boolean hasLoadByKeyFilter(EntityPersister persister) {
        java.lang.reflect.Method method = LOAD_BY_KEY_FILTER_CHECK;
        if (method == null) return false;
        try {
            return Boolean.TRUE.equals(method.invoke(persister));
        } catch (ReflectiveOperationException | RuntimeException notAvailable) {
            return true;
        }
    }

    private static final java.lang.reflect.Method LOAD_BY_KEY_FILTER_CHECK = findLoadByKeyFilterCheck();

    private static java.lang.reflect.Method findLoadByKeyFilterCheck() {
        try {
            return EntityPersister.class.getMethod("hasFilterForLoadByKey");
        } catch (NoSuchMethodException notOnThisLine) {
            return null;
        }
    }

    /**
     * Reach the identifier of a to-one association through its foreign key column, if that is possible here.
     *
     * <p>A dotted selector ending in the identifier of a to-one association - {@code customer.id} - names a
     * value the querying table already stores. Navigating to it with {@code get()} lets the provider read it
     * from that column; {@code join()} is a request for a real join, and the provider has to honour it for a
     * column the query already has.</p>
     *
     * <p>Call this from inside the branch that would otherwise create the join, once per segment, and build
     * the join only when it answers null. Every path resolver in the library shares this one decision, so
     * WHERE, SELECT and GROUP BY cannot disagree about whether a given selector needs a join - which is what
     * makes it safe to apply on the aggregate paths, where a SELECT and a GROUP BY that resolved the same
     * field differently would be a broken query rather than a slow one.</p>
     *
     * @param graph         The selector split on dots.
     * @param index         Index of the segment being resolved; the shortcut only applies to the
     *                      second-to-last one, so that a longer path still builds the joins it needs.
     * @param root          Path resolved so far, which the returned path is built on.
     * @param classMetadata Metamodel of the type declaring {@code graph[index]}.
     * @param metamodel     The metamodel, for resolving the association target.
     * @param rsqlContext   Context holding the per-query settings.
     * @return the path to the identifier, or null when the join has to be built after all
     */
    public static Path<?> foreignKeyIdShortcut(
            String[] graph,
            int index,
            Path<?> root,
            ManagedType<?> classMetadata,
            Metamodel metamodel,
            RsqlContext<?> rsqlContext
    ) {
        if (index != graph.length - 2) return null;

        String property = graph[index];
        if (!rsqlContext.isForeignKeyIdShortcutEnabledFor(joinSegments(graph, index + 1))) return null;

        Class<?> targetType = findPropertyType(property, classMetadata);
        String idName = findSingleBasicIdName(metamodel.managedType(targetType));
        if (idName == null || !graph[index + 1].equals(idName)) return null;

        // A restricted target keeps its join: the restriction is a condition on that join, and dropping it
        // would change which rows match, not just how they are reached.
        if (joinCarriesRestrictions(rsqlContext.entityManager, targetType)) return null;

        if (!canReadIdFromForeignKey(rsqlContext.entityManager, classMetadata.getJavaType(), property, idName)) {
            return null;
        }

        return root.get(property).get(idName);
    }

    /**
     * Whether a selector ends in the identifier of the association its prefix names.
     *
     * <p>Every resolver starts by looking the selector's prefix up in the join cache and, on a hit, returns
     * the last segment straight off that join. For an identifier that has to be skipped, or the answer would
     * depend on whether some earlier clause happened to join the association first: the SELECT would read the
     * foreign key column and the GROUP BY, resolved after the join existed, would read the joined one. Same
     * value, but a query that says two different things about one field.</p>
     *
     * <p>Answering true here sends the selector through the full walk instead, where
     * {@link #foreignKeyIdShortcut} decides once and the same way regardless of what is already joined. When
     * the shortcut then declines, the walk finds the very same cached join, so nothing is built twice.</p>
     *
     * @param graph                The selector split on dots.
     * @param cachedTargetMetadata Metamodel of the type the cached prefix resolves to, or null if unknown.
     * @return true when the last segment is that type's single basic identifier
     */
    public static boolean endsInToOneIdentifier(String[] graph, ManagedType<?> cachedTargetMetadata) {
        if (cachedTargetMetadata == null || graph.length < 2) return false;
        String idName = findSingleBasicIdName(cachedTargetMetadata);
        return idName != null && graph[graph.length - 1].equals(idName);
    }

    /** The first {@code length} segments of a selector, joined back into a dotted path. */
    private static String joinSegments(String[] graph, int length) {
        StringBuilder path = new StringBuilder(graph[0]);
        for (int i = 1; i < length; i++) {
            path.append('.').append(graph[i]);
        }
        return path.toString();
    }

    /**
     * Name of the identifier attribute of a type, but only when that identifier is a single basic
     * attribute.
     *
     * <p>Composite identifiers return null. An {@code @IdClass} has no single id attribute at all, and an
     * {@code @EmbeddedId} maps to several columns; in both cases the identifier is not a single column
     * the caller could read off a foreign key.</p>
     *
     * @param classMetadata Metamodel of the type whose identifier is wanted.
     * @return The identifier attribute name, or null when there is no single basic identifier.
     */
    public static String findSingleBasicIdName(ManagedType<?> classMetadata) {
        if (!(classMetadata instanceof IdentifiableType<?> identifiableType)) return null;
        if (!identifiableType.hasSingleIdAttribute()) return null;
        for (SingularAttribute<?, ?> attribute : classMetadata.getSingularAttributes()) {
            if (attribute.isId()) {
                return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC
                        ? attribute.getName()
                        : null;
            }
        }
        return null;
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
            // Left unconverted: only the caller knows the type of the attribute it is compared with.
            return getDatetimeLiteral(ctx.DATETIME_LITERAL());
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

    /**
     * The instant a datetime literal names.
     *
     * @deprecated An instant is only the right value for an attribute that holds a moment. Use
     *             {@link #getDatetimeLiteral} and {@link DatetimeLiteral#as}, which give the value in the type
     *             of the attribute it is compared with.
     * @throws rsql.exceptions.SyntaxErrorException when the literal was written without a zone
     */
    @Deprecated
    public static Instant getInstantFromDatetimeLiteral(TerminalNode datetimeLiteral) {
        DatetimeLiteral literal = getDatetimeLiteral(datetimeLiteral);
        return literal == null ? null : literal.toInstant();
    }

    /**
     * A datetime literal, not yet converted to any Java type - see {@link DatetimeLiteral} for why.
     */
    public static DatetimeLiteral getDatetimeLiteral(TerminalNode datetimeLiteral) {
        String s = datetimeLiteral.getText();
        if (s.length() > 1) {
            return DatetimeLiteral.parse(s.substring(1, s.length() - 1));
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
