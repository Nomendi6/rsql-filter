package rsql.where;

import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.antlr.where.RsqlWhereParser;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import rsql.exceptions.SyntaxErrorException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
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

    private String currentAlias() {
        return "a" + aliasCounter;
    }

    private static String getFieldFromPath(Path<?> path) {
        return path.getAlias() + "." + ((SingularAttributePath<?>) path).getAttribute().getName();
    }

    private Object getInListElement(RsqlWhereParser.InListElementContext ctx) {
        if (ctx.field() != null) {
            return getPropertyPath(getFieldName(ctx.field()), rsqlContext.root);
        }
        return getInListLiteral(ctx);
    }

    private Path<?> getPropertyPath(String fieldName, Path<?> startRoot) {
        String[] graph = fieldName.split("\\.");

        if (graph.length == 1) {
            Path<?> result = startRoot.get(fieldName);
            result.alias(ALIAS_FOR_STARTROOT);
            return result;
        } else {
            //            Path<?> path = getPropertyPathRecursive(fieldName, startRoot);
            //            String attribute = ((SingularAttributePath<?>) path).getAttribute().getName();
            //            return query.alias + "." + attribute;
            return getPropertyPathRecursive(fieldName, startRoot);
        }
    }

    private Path<?> getPropertyPathRecursive(String fieldName, Path<?> startRoot) {
        String[] graph = fieldName.split("\\.");
        String path = "";
        String alias = currentAlias();
        int startingProperty = 0;

        Path<?> root = startRoot;
        // try to find the existing path
        for (int i = graph.length - 1; i >= 0; i--) {
            path = createPathFromGraph(graph, i);
            if (this.joins.containsKey(path)) {
                RsqlJoin join = this.joins.get(path);
                root = join.root;
                alias = join.alias;
                startingProperty = i + 1;
                break;
            }
        }
        // try to build a property path
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> classMetadata = metamodel.managedType(startRoot.getJavaType());

        // go from the last found path and complete paths
        for (int i = startingProperty; i < graph.length; i++) {
            String property = graph[i];

            if (!hasPropertyName(property, classMetadata)) {
                throw new SyntaxErrorException(
                        "Unknown property: " + property + " from entity " + classMetadata.getJavaType().getName()
                );
            }

            if (isAssociationType(property, classMetadata)) {
                path = createPathFromGraph(graph, i);

                Class<?> associationType = findPropertyType(property, classMetadata);
                String previousClass = classMetadata.getJavaType().getName();
                classMetadata = metamodel.managedType(associationType);

                log.trace("Create a join between {} and {}.", previousClass, classMetadata.getJavaType().getName());
                root = ((From) root).join(property);
                this.joins.put(path, new RsqlJoin(path, property, root.getModel().toString(), nextAlias(), alias, root, "INNER JOIN"));
                alias = currentAlias();
                root.alias(alias);
            } else {
                log.trace("Create property path for type {} property {}.", classMetadata.getJavaType().getName(), property);
                root = root.get(property);
                root.alias(alias);
                if (isEmbeddedType(property, classMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
            }
        }

        return root;
    }

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
