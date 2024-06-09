package rsql.converter;

import jakarta.persistence.criteria.Predicate;
import org.hibernate.metamodel.model.domain.internal.AnyDiscriminatorSqmPath;
import org.hibernate.metamodel.model.domain.internal.EntityDiscriminatorSqmPath;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.cte.SqmCteContainer;
import org.hibernate.query.sqm.tree.cte.SqmCteStatement;
import org.hibernate.query.sqm.tree.delete.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.domain.*;
import org.hibernate.query.sqm.tree.expression.*;
import org.hibernate.query.sqm.tree.from.*;
import org.hibernate.query.sqm.tree.insert.SqmConflictClause;
import org.hibernate.query.sqm.tree.insert.SqmInsertSelectStatement;
import org.hibernate.query.sqm.tree.insert.SqmInsertValuesStatement;
import org.hibernate.query.sqm.tree.insert.SqmValues;
import org.hibernate.query.sqm.tree.predicate.*;
import org.hibernate.query.sqm.tree.select.*;
import org.hibernate.query.sqm.tree.update.SqmAssignment;
import org.hibernate.query.sqm.tree.update.SqmSetClause;
import org.hibernate.query.sqm.tree.update.SqmUpdateStatement;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the SemanticQueryWalker interface and provides methods to convert
 * SqmSelectStatement and Predicate objects to their string representations.
 */
public class PredicateToText implements SemanticQueryWalker<Object> {

    /**
     * Converts a SqmSelectStatement object to its string representation.
     *
     * @param sqmSelectStatement The SqmSelectStatement object to be converted.
     * @return The string representation of the SqmSelectStatement object.
     */
    public static String convert(SqmSelectStatement<?> sqmSelectStatement) {
        PredicateToText predicateToText = new PredicateToText();
        sqmSelectStatement.accept(predicateToText);
        return predicateToText.toString();
    }

    /**
     * Converts a Predicate object to its string representation.
     *
     * @param predicate The Predicate object to be converted.
     * @return The string representation of the Predicate object.
     */
    public static String convert (Predicate predicate) {
        SqmPredicate sqmPredicate = (SqmPredicate) predicate;
        PredicateToText predicateToText = new PredicateToText();
        Object predicateText = sqmPredicate.accept(predicateToText);
        return (String) predicateText;
    }

    @Override
    public Object visitUpdateStatement(SqmUpdateStatement<?> statement) {
        return null;
    }

    @Override
    public Object visitSetClause(SqmSetClause setClause) {
        return null;
    }

    @Override
    public Object visitAssignment(SqmAssignment<?> assignment) {
        return null;
    }

    @Override
    public Object visitInsertSelectStatement(SqmInsertSelectStatement<?> statement) {
        return null;
    }

    @Override
    public Object visitInsertValuesStatement(SqmInsertValuesStatement<?> statement) {
        return null;
    }

    @Override
    public Object visitConflictClause(SqmConflictClause<?> sqmConflictClause) {
        return null;
    }

    @Override
    public Object visitDeleteStatement(SqmDeleteStatement<?> statement) {
        return null;
    }

    @Override
    public Object visitSelectStatement(SqmSelectStatement<?> statement) {
        return null;
    }

    @Override
    public Object visitCteStatement(SqmCteStatement<?> sqmCteStatement) {
        return null;
    }

    @Override
    public Object visitCteContainer(SqmCteContainer consumer) {
        return null;
    }

    @Override
    public Object visitFromClause(SqmFromClause fromClause) {
        return null;
    }

    @Override
    public Object visitRootPath(SqmRoot<?> sqmRoot) {
        return null;
    }

    @Override
    public Object visitRootDerived(SqmDerivedRoot<?> sqmRoot) {
        return null;
    }

    @Override
    public Object visitRootCte(SqmCteRoot<?> sqmRoot) {
        return null;
    }

    @Override
    public Object visitCrossJoin(SqmCrossJoin<?> joinedFromElement) {
        return null;
    }

    @Override
    public Object visitPluralPartJoin(SqmPluralPartJoin<?, ?> joinedFromElement) {
        return null;
    }

    @Override
    public Object visitQualifiedEntityJoin(SqmEntityJoin<?> joinedFromElement) {
        return null;
    }

    @Override
    public Object visitQualifiedAttributeJoin(SqmAttributeJoin<?, ?> joinedFromElement) {
        return null;
    }

    @Override
    public Object visitQualifiedDerivedJoin(SqmDerivedJoin<?> joinedFromElement) {
        return null;
    }

    @Override
    public Object visitQualifiedCteJoin(SqmCteJoin<?> joinedFromElement) {
        return null;
    }

    @Override
    public Object visitBasicValuedPath(SqmBasicValuedSimplePath<?> path) {
        return null;
    }

    @Override
    public Object visitEmbeddableValuedPath(SqmEmbeddedValuedSimplePath<?> path) {
        return null;
    }

    @Override
    public Object visitAnyValuedValuedPath(SqmAnyValuedSimplePath<?> path) {
        return null;
    }

    @Override
    public Object visitNonAggregatedCompositeValuedPath(NonAggregatedCompositeSimplePath<?> path) {
        return null;
    }

    @Override
    public Object visitEntityValuedPath(SqmEntityValuedSimplePath<?> path) {
        return null;
    }

    @Override
    public Object visitPluralValuedPath(SqmPluralValuedSimplePath<?> path) {
        return null;
    }

    @Override
    public Object visitFkExpression(SqmFkExpression<?> fkExpression) {
        return null;
    }

    @Override
    public Object visitDiscriminatorPath(EntityDiscriminatorSqmPath sqmPath) {
        return null;
    }

    @Override
    public Object visitIndexedPluralAccessPath(SqmIndexedCollectionAccessPath<?> path) {
        return null;
    }

    @Override
    public Object visitElementAggregateFunction(SqmElementAggregateFunction<?> path) {
        return null;
    }

    @Override
    public Object visitIndexAggregateFunction(SqmIndexAggregateFunction<?> path) {
        return null;
    }

    @Override
    public Object visitTreatedPath(SqmTreatedPath<?, ?> sqmTreatedPath) {
        return null;
    }

    @Override
    public Object visitCorrelation(SqmCorrelation<?, ?> correlation) {
        return null;
    }

    @Override
    public Object visitQueryGroup(SqmQueryGroup<?> queryGroup) {
        return null;
    }

    @Override
    public Object visitQuerySpec(SqmQuerySpec<?> querySpec) {
        return null;
    }

    @Override
    public Object visitSelectClause(SqmSelectClause selectClause) {
        return null;
    }

    @Override
    public Object visitSelection(SqmSelection<?> selection) {
        return null;
    }

    @Override
    public Object visitValues(SqmValues values) {
        return null;
    }

    @Override
    public Object visitGroupByClause(List<SqmExpression<?>> groupByClauseExpressions) {
        return null;
    }

    @Override
    public Object visitHavingClause(SqmPredicate clause) {
        return null;
    }

    @Override
    public Object visitDynamicInstantiation(SqmDynamicInstantiation<?> sqmDynamicInstantiation) {
        return null;
    }

    @Override
    public Object visitJpaCompoundSelection(SqmJpaCompoundSelection<?> selection) {
        return null;
    }

    @Override
    public Object visitLiteral(SqmLiteral<?> literal) {
        return null;
    }

    @Override
    public Object visitEnumLiteral(SqmEnumLiteral<?> sqmEnumLiteral) {
        return null;
    }

    @Override
    public Object visitFieldLiteral(SqmFieldLiteral<?> sqmFieldLiteral) {
        return null;
    }

    @Override
    public <N extends Number> Object visitHqlNumericLiteral(SqmHqlNumericLiteral<N> sqmHqlNumericLiteral) {
        return null;
    }

    @Override
    public Object visitTuple(SqmTuple<?> sqmTuple) {
        return null;
    }

    @Override
    public Object visitCollation(SqmCollation sqmCollate) {
        return null;
    }

    @Override
    public Object visitBinaryArithmeticExpression(SqmBinaryArithmetic<?> expression) {
        return null;
    }

    @Override
    public Object visitSubQueryExpression(SqmSubQuery<?> expression) {
        return null;
    }

    @Override
    public Object visitModifiedSubQueryExpression(SqmModifiedSubQueryExpression<?> expression) {
        return null;
    }

    @Override
    public Object visitSimpleCaseExpression(SqmCaseSimple<?, ?> expression) {
        return null;
    }

    @Override
    public Object visitSearchedCaseExpression(SqmCaseSearched<?> expression) {
        return null;
    }

    @Override
    public Object visitAny(SqmAny<?> sqmAny) {
        return null;
    }

    @Override
    public Object visitEvery(SqmEvery<?> sqmEvery) {
        return null;
    }

    @Override
    public Object visitSummarization(SqmSummarization<?> sqmSummarization) {
        return null;
    }

    @Override
    public Object visitPositionalParameterExpression(SqmPositionalParameter<?> expression) {
        return null;
    }

    @Override
    public Object visitNamedParameterExpression(SqmNamedParameter<?> expression) {
        return null;
    }

    @Override
    public Object visitJpaCriteriaParameter(JpaCriteriaParameter<?> expression) {
        return null;
    }

    @Override
    public Object visitEntityTypeLiteralExpression(SqmLiteralEntityType<?> expression) {
        return null;
    }

    @Override
    public Object visitAnyDiscriminatorTypeExpression(AnyDiscriminatorSqmPath<?> expression) {
        return null;
    }

    @Override
    public Object visitAnyDiscriminatorTypeValueExpression(SqmAnyDiscriminatorValue<?> expression) {
        return null;
    }

    @Override
    public Object visitParameterizedEntityTypeExpression(SqmParameterizedEntityType<?> expression) {
        return null;
    }

    @Override
    public Object visitUnaryOperationExpression(SqmUnaryOperation<?> expression) {
        return null;
    }

    @Override
    public Object visitFunction(SqmFunction<?> tSqmFunction) {
        return null;
    }

    @Override
    public Object visitExtractUnit(SqmExtractUnit<?> extractUnit) {
        return null;
    }

    @Override
    public Object visitFormat(SqmFormat sqmFormat) {
        return null;
    }

    @Override
    public Object visitCastTarget(SqmCastTarget<?> sqmCastTarget) {
        return null;
    }

    @Override
    public Object visitTrimSpecification(SqmTrimSpecification trimSpecification) {
        return null;
    }

    @Override
    public Object visitDistinct(SqmDistinct<?> distinct) {
        return null;
    }

    @Override
    public Object visitStar(SqmStar sqmStar) {
        return null;
    }

    @Override
    public Object visitOver(SqmOver<?> over) {
        return null;
    }

    @Override
    public Object visitWindow(SqmWindow widow) {
        return null;
    }

    @Override
    public Object visitOverflow(SqmOverflow<?> sqmOverflow) {
        return null;
    }

    @Override
    public Object visitCoalesce(SqmCoalesce<?> sqmCoalesce) {
        return null;
    }

    @Override
    public Object visitToDuration(SqmToDuration<?> toDuration) {
        return null;
    }

    @Override
    public Object visitByUnit(SqmByUnit sqmByUnit) {
        return null;
    }

    @Override
    public Object visitDurationUnit(SqmDurationUnit<?> durationUnit) {
        return null;
    }

    @Override
    public Object visitWhereClause(SqmWhereClause whereClause) {
        if (whereClause == null || whereClause.getPredicate() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("WHERE ");

        SqmPredicate predicate = whereClause.getPredicate();
        String predicateString = (String) predicate.accept(this); // Delegate to another visit method for the predicate

        sb.append(predicateString);

        return sb.toString();
    }

    @Override
    public Object visitGroupedPredicate(SqmGroupedPredicate predicate) {
        return "GroupedPredicate";
    }

    @Override
    public Object visitJunctionPredicate(SqmJunctionPredicate predicate) {
        String operator = predicate.getOperator() == Predicate.BooleanOperator.AND ? " and " : " or ";
        List<String> expressions = new ArrayList<>();
        for ( SqmPredicate subPredicate : predicate.getPredicates() ) {
            String expression = (String) subPredicate.accept( this );
            expressions.add( "(" + expression +")");
        }
        return String.join(operator, expressions);
    }

    @Override
    public Object visitComparisonPredicate(SqmComparisonPredicate predicate) {

        String leftHandSide = getExpressionValue(predicate.getLeftHandExpression());
        String rightHandSide = getExpressionValue(predicate.getRightHandExpression());

        String operator = convertOperatorName(predicate.getSqmOperator().name());

        return leftHandSide + " " + operator + " " + rightHandSide;
    }

    private String getFieldWithPath(String wholePath) {
        // find "domain." in the wholePath and get the string right after it
        String leftHandSide = wholePath.substring(wholePath.indexOf("domain.") + 7);
        // find the first "." in the leftHandSide and get the string after it
        leftHandSide = leftHandSide.substring(leftHandSide.indexOf(".") + 1);
        return leftHandSide;
    }

    private String convertOperatorName(String name) {
        // Convert operator name to SQL equivalent
        // for example: convert "EQUAL" to "="
        switch (name) {
            case "EQUAL":
                return "=";
            case "NOT_EQUAL":
                return "!=";
            case "LESS_THAN":
                return "<";
            case "LESS_THAN_OR_EQUAL":
                return "<=";
            case "GREATER_THAN":
                return ">";
            case "GREATER_THAN_OR_EQUAL":
                return ">=";
            case "LIKE":
                return "LIKE";
            case "NOT_LIKE":
                return "NOT LIKE";
            case "BETWEEN":
                return "BETWEEN";
            case "NOT_BETWEEN":
                return "NOT BETWEEN";
            case "IN":
                return "IN";
            case "NOT_IN":
                return "NOT IN";
            case "IS_NULL":
                return "IS NULL";
            case "IS_NOT_NULL":
                return "IS NOT NULL";
            case "MEMBER_OF":
                return "MEMBER OF";
            case "NOT_MEMBER_OF":
                return "NOT MEMBER OF";
            case "EXISTS":
                return "EXISTS";
            case "NOT_EXISTS":
                return "NOT EXISTS";
            case "IS_EMPTY":
                return "IS EMPTY";
            case "IS_NOT_EMPTY":
                return "IS NOT EMPTY";
            case "IS_MEMBER":
                return "IS MEMBER";
            case "IS_NOT_MEMBER":
                return "IS NOT MEMBER";
            case "IS_TRUE":
                return "IS TRUE";
            case "IS_NOT_TRUE":
                return "IS NOT TRUE";
            case "IS_FALSE":
                return "IS FALSE";
            case "IS_NOT_FALSE":
                return "IS NOT FALSE";
            case "CONJUNCTION":
                return "AND";
            case "DISJUNCTION":
                return "OR";
            case "NEGATION":
                return "NOT";
            default:
                throw new AssertionError("Unexpected operator name: " + name);
        }

    }

    private String getExpressionValue(SqmExpression<?> expression) {
        if (expression instanceof SqmParameter) {
            SqmParameter<?> rightParameter = (SqmParameter<?>) expression;
            if (rightParameter instanceof ValueBindJpaCriteriaParameter<?>) {
                Object value = ((ValueBindJpaCriteriaParameter) rightParameter).getValue();
                return convertValueToString(value);
            }
            return ":" + rightParameter.getName();
        } else if (expression instanceof SqmLiteral) {
            SqmLiteral<?> rightLiteral = (SqmLiteral<?>) expression;
            Object value = rightLiteral.getLiteralValue();
            return convertValueToString(value);
        } else if (expression instanceof SqmBasicValuedSimplePath<?>) {
            SqmBasicValuedSimplePath<?> rightPath = (SqmBasicValuedSimplePath<?>) expression;
            String wholePath = rightPath.getNavigablePath().getIdentifierForTableGroup();
            return getFieldWithPath(wholePath);

        } else if (expression instanceof SelfRenderingSqmFunction<?>) {
            SelfRenderingSqmFunction<?> functionDef = (SelfRenderingSqmFunction<?>) expression;
            String functionName = functionDef.getFunctionName();
            List<String> arguments = new ArrayList<>();
            for (SqmTypedNode<?> argument : functionDef.getArguments()) {
                // convert the SqmNodeType to String
                String argumentValue = getExpressionValue((SqmExpression<?>) argument);
                arguments.add(argumentValue);
            }
            return functionName + "(" + String.join(", ", arguments) + ")";
        }
        throw new AssertionError("Unexpected expression type: " + expression.getClass());
    }

    private String convertValueToString(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        if (value instanceof Instant) {
            return "TIMESTAMP('" + value.toString() + "')";
        }
        if (value instanceof LocalDate) {
            return "DATE('" + value.toString() + "')";
        }
        if (value instanceof Enum) {
            return "'" + value.toString() + "'";
        }
        return value.toString();
    }

    @Override
    public Object visitIsEmptyPredicate(SqmEmptinessPredicate predicate) {
        return null;
    }

    @Override
    public Object visitIsNullPredicate(SqmNullnessPredicate predicate) {
        String leftHandSide = getExpressionValue(predicate.getExpression());

        String operator = predicate.isNegated() ? " is not null" : " is null";
        return leftHandSide + operator;
    }

    @Override
    public Object visitIsTruePredicate(SqmTruthnessPredicate sqmTruthnessPredicate) {
        return null;
    }

    @Override
    public Object visitBetweenPredicate(SqmBetweenPredicate predicate) {
        String leftExpression = getExpressionValue(predicate.getExpression());

        String lowerExpression = getExpressionValue(predicate.getLowerBound());
        String upperExpression = getExpressionValue(predicate.getUpperBound());

        String operator = predicate.isNegated() ? " not between " : " between ";

        return leftExpression + operator + lowerExpression + " and " + upperExpression;
    }

    @Override
    public Object visitLikePredicate(SqmLikePredicate predicate) {
        String leftExpression = getExpressionValue(predicate.getMatchExpression());
        String operation = predicate.isNegated() ? " not like " : " like ";
        String pattern = getExpressionValue(predicate.getPattern());

        return leftExpression + operation + pattern;
    }

    @Override
    public Object visitMemberOfPredicate(SqmMemberOfPredicate predicate) {
        return null;
    }

    @Override
    public Object visitNegatedPredicate(SqmNegatedPredicate predicate) {
        return null;
    }

    @Override
    public Object visitInListPredicate(SqmInListPredicate<?> predicate) {
        String expression = getExpressionValue(predicate.getTestExpression());
        List<String> listExpressions = new ArrayList<>();
        for (SqmExpression<?> listExpression : predicate.getListExpressions()) {
            listExpressions.add(getExpressionValue(listExpression));
        }
        String listExpression = String.join(", ", listExpressions);
        String operator = predicate.isNegated() ? " not in " : " in ";
        return expression + operator + "(" + listExpression + ")";
    }

    @Override
    public Object visitInSubQueryPredicate(SqmInSubQueryPredicate<?> predicate) {
        return null;
    }

    @Override
    public Object visitBooleanExpressionPredicate(SqmBooleanExpressionPredicate predicate) {
        return null;
    }

    @Override
    public Object visitExistsPredicate(SqmExistsPredicate sqmExistsPredicate) {
        return null;
    }

    @Override
    public Object visitOrderByClause(SqmOrderByClause orderByClause) {
        return null;
    }

    @Override
    public Object visitSortSpecification(SqmSortSpecification sortSpecification) {
        return null;
    }

    @Override
    public Object visitOffsetExpression(SqmExpression<?> expression) {
        return null;
    }

    @Override
    public Object visitFetchExpression(SqmExpression<?> expression) {
        return null;
    }

    @Override
    public Object visitPluralAttributeSizeFunction(SqmCollectionSize function) {
        return null;
    }

    @Override
    public Object visitMapEntryFunction(SqmMapEntryReference<?, ?> function) {
        return null;
    }

    @Override
    public Object visitFullyQualifiedClass(Class<?> namedClass) {
        return null;
    }
}
