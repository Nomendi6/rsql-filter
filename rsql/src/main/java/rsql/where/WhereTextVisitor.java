package rsql.where;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.hibernate.spi.NavigablePath;
import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.antlr.where.RsqlWhereParser;
// import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.exceptions.SyntaxErrorException;

// import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static rsql.where.RsqlWhereHelper.*;

@SuppressWarnings("unchecked")
@Service
public class WhereTextVisitor<T> extends RsqlWhereBaseVisitor<RsqlQuery> {

    private static final String ALIAS_FOR_STARTROOT = "a0";
    private final Logger log = LoggerFactory.getLogger(WhereSpecificationVisitor.class);

    protected RsqlContext<T> rsqlContext;
    protected int paramCounter = 0;
    protected int aliasCounter = 0;
    protected Map<String, RsqlJoin> joins = new LinkedHashMap<>();

    public WhereTextVisitor() {}

    private void createParamList(
            RsqlWhereParser.InListContext inListContext,
            Path<?> pathField,
            String paramListName,
            boolean isEnum,
            RsqlQuery query
    ) {
        List<Object> paramList = new ArrayList<>();

        for (int i = 0; i < inListContext.inListElement().size(); i++) {
            Object element = getInListElement(inListContext.inListElement(i));
            if (element != null) {
                if (isEnum && element.getClass().equals(String.class)) {
                    Path<Enum> enumField = (Path<Enum>) pathField;
                    element = RsqlWhereHelper.getEnum((String) element, enumField.getJavaType());
                }
                paramList.add(element);
            }
        }

        query.paramLists.add(new RsqlQueryParamList(paramListName, paramList));
    }

    private String getFieldFromPath(Path<?> path) {
        String fieldName;

        String alias = getAliasForPath(path);
        if (path instanceof SqmBasicValuedSimplePath) {
            NavigablePath navigablePath = ((SqmBasicValuedSimplePath<?>) path).getNavigablePath();
            fieldName = getFullPath(navigablePath);

        } else {
            throw new IllegalArgumentException("Path is not an instance of SqmBasicValuedSimplePath");
        }
        if (!alias.isEmpty()) {
            return alias + "." + fieldName;
        } else {
            return fieldName;
        }
    }

    private String getAliasForPath(Path<?> path) {
        String alias = path.getAlias();
        if (alias == null) {
            alias = rsqlContext.root.getAlias();
        }
        alias = alias == null ? "" : alias;
        return alias;
    }


    private Object getInListElement(RsqlWhereParser.InListElementContext ctx) {
        if (ctx.field() != null) {
            return getPropertyPath(getFieldName(ctx.field()), rsqlContext.root);
        }
        return getInListLiteral(ctx);
    }

    private Path<?> getPropertyPath(String fieldName, Path<?> startRoot) {
        Path<?> propertyPath = getPropertyPathRecursive(fieldName, startRoot);
        addMissingJoins(propertyPath);
        return propertyPath;
    }

    private void addMissingJoins(Path<?> propertyPath) {
        NavigablePath navigablePath = ((SqmBasicValuedSimplePath<?>) propertyPath).getNavigablePath();
        if (navigablePath.getParent() != null) {
            NavigablePath parent = navigablePath.getParent();
            if (parent.getParent() != null) { // if not root then add a new join if necessary
                String parentPath = getFullPath(parent);

                if (!joins.containsKey(parentPath)) {
                    RsqlJoin join = new RsqlJoin();
                    join.root = propertyPath.getParentPath();
                    join.path = parentPath;
                    join.fullPath = parentPath;

                    joins.put(parentPath, join);
                }

            }
        }
//        String pathString = getFullPath(navigablePath);
    }

    private String getFullPath(NavigablePath navigablePath) {
        String rootPath = getRootPath(navigablePath);
        int rootPathLength = rootPath.length();
        String fullPath = navigablePath.getIdentifierForTableGroup().toString().substring(rootPathLength+1);

        return fullPath;
    }

    private String getRootPath(NavigablePath navigablePath) {
        if (navigablePath.getParent() == null) {
            return navigablePath.getIdentifierForTableGroup().toString();
        } else {
            return getRootPath(navigablePath.getParent());
        }
    }


    /**
     * Creates a path from a given graph array and an index.
     *
     * @param graph The string graph array containing the nodes.
     * @param index The index up to which the path needs to be created.
     * @return The path created from the graph array up to the given index.
     */
    public static String createPathFromGraph(String[] graph, int index) {
        StringBuilder path = new StringBuilder(graph[0]);
        if (index >= 1) {
            for (int i = 1; i <= index; i++) {
                path.append(".").append(graph[i]);
            }
        }
        return path.toString();
    }

    /**
     * Generates the current alias based on the alias counter.
     *
     * @return The generated current alias.
     */
    private String currentAlias() {
        return "a" + aliasCounter;
    }


    public Path<?> getPropertyPathRecursive(String fieldName, Path<?> startRoot) {
        String[] parts = fieldName.split("\\.");
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> currentType = metamodel.managedType(startRoot.getJavaType());
        Path<?> currentPath = startRoot;

        for (String part : parts) {
            // Provjera da li postoji svojstvo
            if (!hasPropertyName(part, currentType)) {
                throw new SyntaxErrorException(
                        "Unknown property: " + part + " from entity " + currentType.getJavaType().getName()
                );
            }

            // Obrada asocijacije ili ugrađenog tipa
            if (isAssociationType(part, currentType)) {
                currentPath = handleAssociation(part, currentPath, metamodel);
                currentType = metamodel.managedType(currentPath.getJavaType());
            } else {
                currentPath = currentPath.get(part);
                if (isEmbeddedType(part, currentType)) {
                    currentType = metamodel.managedType(currentPath.getJavaType());
                }
            }
        }

        return currentPath;
    }

    private Path<?> handleAssociation(String associationName, Path<?> currentPath, Metamodel metamodel) {
        From<?, ?> from = (From<?, ?>) currentPath;
        Join<?, ?> join = from.join(associationName, JoinType.LEFT); // Može biti i INNER JOIN, ovisno o potrebi
        // join.alias(currentAlias());
        return join;
    }

/*

    private Path<?> getPropertyPathRecursiveOld(String fieldName, Path<?> startRoot) {
        String[] graph = fieldName.split("\\.");

        RsqlProperyPath rsqlProperyPath = initializeRsqlPropertyPath(startRoot, graph);
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> managedType = metamodel.managedType(startRoot.getJavaType());

        Path<?> currentPath = rsqlProperyPath.getJoin().root;

        // go from the last found path and complete remaining paths
        for (int i = rsqlProperyPath.getPropertyIndex(); i < graph.length; i++) {
            String property = graph[i];

            validateProperty(property, managedType);

            if (isAssociationType(property, managedType)) {
                String path = createPathFromGraph(graph, i);

                Class<?> associationType = findPropertyType(property, managedType);
                 String previousClass = managedType.getJavaType().getName();
                managedType = metamodel.managedType(associationType);

                 log.trace("Create a join between {} and {}.", previousClass, managedType.getJavaType().getName());
                currentPath = ((From) currentPath).join(property);
                this.joins.put(path, new RsqlJoin(path, property, currentPath.getModel().toString(), nextAlias(), rsqlProperyPath.getJoin().alias, currentPath, "INNER JOIN"));
                String alias = currentAlias();
                currentPath.alias(alias);
            } else {
                log.trace("Create property path for type {} property {}.", managedType.getJavaType().getName(), property);
                currentPath = currentPath.get(property);
                currentPath.alias(rsqlProperyPath.getJoin().alias);
                if (isEmbeddedType(property, managedType)) {
                    Class<?> embeddedType = findPropertyType(property, managedType);
                    managedType = metamodel.managedType(embeddedType);
                }
            }
        }

        return currentPath;
    }

    private static void validateProperty(String property, ManagedType<?> managedType) {
        if (!hasPropertyName(property, managedType)) {
            throw new SyntaxErrorException(
                    "Unknown property: " + property + " from entity " + managedType.getJavaType().getName()
            );
        }
    }

    private Path<?> handleAssociationType(String[] graph, int index, String property, ManagedType<?> managedType,
                                          Metamodel metamodel, Path<?> currentPath) {
        String path = createPathFromGraph(graph, index);
        Class<?> associationType = findPropertyType(property, managedType);
        String previousClass = managedType.getJavaType().getName();
        managedType = metamodel.managedType(associationType);

        log.trace("Create a join between {} and {}.", previousClass, managedType.getJavaType().getName());
        currentPath = ((From) currentPath).join(property);
        this.joins.put(path, new RsqlJoin(path, property, currentPath.getModel().toString(), nextAlias(),
                rsqlProperyPath.getJoin().alias, currentPath, "INNER JOIN"));
        String alias = currentAlias();
        currentPath.alias(alias);
        return currentPath;
    }

    private Path<?> handleSimpleType(String property, ManagedType<?> managedType, Path<?> currentPath) {
        log.trace("Create property path for type {} property {}.", managedType.getJavaType().getName(), property);
        currentPath = currentPath.get(property);
        currentPath.alias(rsqlProperyPath.getJoin().alias);
        if (isEmbeddedType(property, managedType)) {
            Class<?> embeddedType = findPropertyType(property, managedType);
            managedType = metamodel.managedType(embeddedType);
        }
        return currentPath;
    }

    private Path<?> processProperty(String[] graph, int index, String property, ManagedType<?> managedType,
                                    Metamodel metamodel, Path<?> currentPath) {
        if (isAssociationType(property, managedType)) {
            return handleAssociationType(graph, index, property, managedType, metamodel, currentPath);
        } else {
            return handleSimpleType(property, managedType, currentPath);
        }
    }
    private RsqlProperyPath initializeRsqlPropertyPath(Path<?> startRoot, String[] graph) {

        for (int i = graph.length - 1; i >= 0; i--) {
            String path = createPathFromGraph(graph, i);
            if (this.joins.containsKey(path)) {
                return new RsqlProperyPath(this.joins.get(path), i+1);

            }
        }
        return new RsqlProperyPath(startRoot, currentAlias(), 0);
    }
*/

    private RsqlQuery getQueryForInCondition(String fieldName, RsqlWhereParser.InListContext inListContext, String operator) {
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String p1 = nextParam();
        boolean isEnum = RsqlWhereHelper.isFieldEnumType(pathField);
        createParamList(inListContext, pathField, p1, isEnum, query);

        query.where = fieldPath + " " + operator + " (:" + p1 + ")";
        return query;
    }

    private String nextAlias() {
        this.aliasCounter++;
        return "a" + aliasCounter;
    }

    private String nextParam() {
        this.paramCounter++;
        return "p" + paramCounter;
    }

    public void setSpecificationContext(RsqlContext<T> rsqlContext) {
        this.rsqlContext = rsqlContext;
    }

    @Override
    public RsqlQuery visitConditionAnd(RsqlWhereParser.ConditionAndContext ctx) {
        RsqlQuery left = super.visit(ctx.condition(0));
        RsqlQuery right = super.visit(ctx.condition(1));
        return left.and(right);
    }

    @Override
    public RsqlQuery visitConditionOr(RsqlWhereParser.ConditionOrContext ctx) {
        RsqlQuery left = super.visit(ctx.condition(0));
        RsqlQuery right = super.visit(ctx.condition(1));
        return left.or(right);
    }

    @Override
    public RsqlQuery visitConditionParens(RsqlWhereParser.ConditionParensContext ctx) {
        RsqlQuery query = super.visit(ctx.condition());
        return query.addParens();
    }


    @Override
    public RsqlQuery visitSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx) {
        String fieldName = getFieldName(ctx.field());

        RsqlQuery query = new RsqlQuery();
        String p1 = nextParam();
        String p2 = nextParam();
        String fieldPath = getFieldFromPath(getPropertyPath(fieldName, rsqlContext.root));

        if (ctx.inListElement(0).STRING_LITERAL() != null && ctx.inListElement(1).STRING_LITERAL() != null) {
            query.where = fieldPath + " between :" + p1 + " and :" + p2;
            String from = getStringFromStringLiteral(ctx.inListElement(0).STRING_LITERAL());
            String to = getStringFromStringLiteral(ctx.inListElement(1).STRING_LITERAL());
            query.params.add(new RsqlQueryParam(p1, from));
            query.params.add(new RsqlQueryParam(p2, to));
            return query;
        } else if (ctx.inListElement(0).DECIMAL_LITERAL() != null && ctx.inListElement(1).DECIMAL_LITERAL() != null) {
            query.where = fieldPath + " between :" + p1 + " and :" + p2;
            Long from = Long.valueOf(ctx.inListElement(0).DECIMAL_LITERAL().getText());
            Long to = Long.valueOf(ctx.inListElement(1).DECIMAL_LITERAL().getText());
            query.params.add(new RsqlQueryParam(p1, from));
            query.params.add(new RsqlQueryParam(p2, to));
            return query;
        } else if (ctx.inListElement(0).REAL_LITERAL() != null && ctx.inListElement(1).REAL_LITERAL() != null) {
            query.where = fieldPath + " between :" + p1 + " and :" + p2;
            BigDecimal from = new BigDecimal(ctx.inListElement(0).REAL_LITERAL().getText());
            BigDecimal to = new BigDecimal(ctx.inListElement(1).REAL_LITERAL().getText());
            query.params.add(new RsqlQueryParam(p1, from));
            query.params.add(new RsqlQueryParam(p2, to));
            return query;
        } else if (ctx.inListElement(0).DATE_LITERAL() != null && ctx.inListElement(1).DATE_LITERAL() != null) {
            query.where = fieldPath + " between :" + p1 + " and :" + p2;
            LocalDate from = RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.inListElement(0).DATE_LITERAL());
            LocalDate to = RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.inListElement(1).DATE_LITERAL());
            query.params.add(new RsqlQueryParam(p1, from));
            query.params.add(new RsqlQueryParam(p2, to));
            return query;
        } else if (ctx.inListElement(0).DATETIME_LITERAL() != null && ctx.inListElement(1).DATETIME_LITERAL() != null) {
            query.where = fieldPath + " between :" + p1 + " and :" + p2;
            Instant from = RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.inListElement(0).DATETIME_LITERAL());
            Instant to = RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.inListElement(1).DATETIME_LITERAL());
            query.params.add(new RsqlQueryParam(p1, from));
            query.params.add(new RsqlQueryParam(p2, to));
            return query;
        } else if (ctx.inListElement(0).PARAM_LITERAL() != null && ctx.inListElement(1).PARAM_LITERAL() != null) {
            String fromParam = ctx.inListElement(0).PARAM_LITERAL().getText();
            String toParam = ctx.inListElement(1).PARAM_LITERAL().getText();
            query.where = fieldPath + " between " + fromParam + " and " + toParam;
            return query;
        } else if (ctx.inListElement(0).field() != null && ctx.inListElement(1).field() != null) {
            String fromField = getFieldName(ctx.inListElement(0).field());
            String toField = getFieldName(ctx.inListElement(1).field());
            query.where = fieldPath + " between " + fromField + " and " + toField;
            return query;
        }

        throw new SyntaxErrorException(
            "Invalid BETWEEN clause: " + fieldName + " between " + ctx.inListElement(0).getText() + " and " + ctx.inListElement(1).getText()
        );
    }

    @Override
    public RsqlQuery visitSingleConditionDate(RsqlWhereParser.SingleConditionDateContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String p1 = nextParam();
        LocalDate value = RsqlWhereHelper.getLocalDateFromDateLiteral((ctx.DATE_LITERAL()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=:" + p1;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">:" + p1;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=:" + p1;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<:" + p1;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=:" + p1;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=:" + p1;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        query.params.add(new RsqlQueryParam(p1, value));
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionDatetime(RsqlWhereParser.SingleConditionDatetimeContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String p1 = nextParam();
        Instant value = RsqlWhereHelper.getInstantFromDatetimeLiteral((ctx.DATETIME_LITERAL()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=:" + p1;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">:" + p1;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=:" + p1;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<:" + p1;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=:" + p1;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=:" + p1;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        query.params.add(new RsqlQueryParam(p1, value));
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionDecimal(RsqlWhereParser.SingleConditionDecimalContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String p1 = nextParam();
        Long value = Long.valueOf((ctx.DECIMAL_LITERAL().getText()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        String param = ":" + p1;
        if (pathField.getJavaType().getName() != "java.lang.Long") {
            // workaround if the field is a foreign key - then setting a parameter value directly on the field is throwing an error
            param = value.toString() + "L";
        } else {
            query.params.add(new RsqlQueryParam(p1, value));
        }
        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=" + param;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">" + param;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=" + param;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<" + param;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=" + param;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=" + param;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }

        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionEnum(RsqlWhereParser.SingleConditionEnumContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<Enum> pathField = (Path<Enum>) getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String value = getStringFromStringLiteral((ctx.ENUM_LITERAL()));
        String p1 = nextParam();
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=:" + p1;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=:" + p1;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        Object enumValue = RsqlWhereHelper.getEnum(value, pathField.getJavaType());
        query.params.add(new RsqlQueryParam(p1, enumValue));
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionFalse(RsqlWhereParser.SingleConditionFalseContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=false";
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=false";
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        return query;

    }

    @Override
    public RsqlQuery visitSingleConditionIn(RsqlWhereParser.SingleConditionInContext ctx) {
        String fieldName = getFieldName(ctx.field());
        return getQueryForInCondition(fieldName, ctx.inList(), "in");
    }

    @Override
    public RsqlQuery visitSingleConditionNotIn(RsqlWhereParser.SingleConditionNotInContext ctx) {
        String fieldName = getFieldName(ctx.field());
        return getQueryForInCondition(fieldName, ctx.inList(), "not in");
    }

    @Override
    public RsqlQuery visitSingleConditionNull(RsqlWhereParser.SingleConditionNullContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + " is null";
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + " is not null";
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionOtherField(RsqlWhereParser.SingleConditionOtherFieldContext ctx) {
        String fieldName = getFieldName(ctx.field(0));
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String value = getFieldName(ctx.field(1));
        Path<?> pathValue = getPropertyPath(value, rsqlContext.root);
        String valuePath = getFieldFromPath(pathValue);
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=" + valuePath;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">" + valuePath;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=" + valuePath;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<" + valuePath;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=" + valuePath;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=" + valuePath;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionParam(RsqlWhereParser.SingleConditionParamContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String value = getParamFromLiteral(ctx.PARAM_LITERAL());
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=:" + value;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">:" + value;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=:" + value;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<:" + value;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=:" + value;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=:" + value;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionReal(RsqlWhereParser.SingleConditionRealContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String p1 = nextParam();
        BigDecimal value = new BigDecimal((ctx.REAL_LITERAL().getText()));
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=:" + p1;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">:" + p1;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=:" + p1;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<:" + p1;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=:" + p1;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=:" + p1;
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        query.params.add(new RsqlQueryParam(p1, value));
        return query;
    }

    @Override
    public RsqlQuery visitSingleConditionString(RsqlWhereParser.SingleConditionStringContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        String p1 = nextParam();
        String value = getStringFromStringLiteral(ctx.STRING_LITERAL());
        RsqlWhereParser.OperatorContext operator = ctx.operator();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=:" + p1;
        } else if (operator.operatorGT() != null) {
            query.where = fieldPath + ">:" + p1;
        } else if (operator.operatorGE() != null) {
            query.where = fieldPath + ">=:" + p1;
        } else if (operator.operatorLT() != null) {
            query.where = fieldPath + "<:" + p1;
        } else if (operator.operatorLE() != null) {
            query.where = fieldPath + "<=:" + p1;
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=:" + p1;
        } else if (operator.operatorLIKE() != null) {
            value = value.replace('*', '%').toLowerCase(Locale.ROOT);
            query.where = "lower(".concat(fieldPath).concat(") like :").concat(p1);
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }

        query.params.add(new RsqlQueryParam(p1, value));
        return query;
    }


    @Override
    public RsqlQuery visitSingleConditionTrue(RsqlWhereParser.SingleConditionTrueContext ctx) {
        String fieldName = getFieldName(ctx.field());
        RsqlQuery query = new RsqlQuery();
        Path<?> pathField = getPropertyPath(fieldName, rsqlContext.root);
        String fieldPath = getFieldFromPath(pathField);
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null) {
            query.where = fieldPath + "=true";
        } else if (operator.operatorNEQ() != null) {
            query.where = fieldPath + "!=true";
        } else {
            throw new SyntaxErrorException("Unknown operator: " + operator.getText());
        }
        return query;
    }

    @Override
    public RsqlQuery visitWhere(RsqlWhereParser.WhereContext ctx) {
        RsqlQuery query = super.visitWhere(ctx);
        query.addJoins(this.joins);

        return query;
    }



}
