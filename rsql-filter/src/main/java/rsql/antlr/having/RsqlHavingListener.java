// Generated from RsqlHaving.g4 by ANTLR 4.13.2

package rsql.antlr.having;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RsqlHavingParser}.
 */
public interface RsqlHavingListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#having}.
	 * @param ctx the parse tree
	 */
	void enterHaving(RsqlHavingParser.HavingContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#having}.
	 * @param ctx the parse tree
	 */
	void exitHaving(RsqlHavingParser.HavingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionSingle}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionSingle(RsqlHavingParser.HavingConditionSingleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionSingle}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionSingle(RsqlHavingParser.HavingConditionSingleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionAnd}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionAnd(RsqlHavingParser.HavingConditionAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionAnd}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionAnd(RsqlHavingParser.HavingConditionAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionOr}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionOr(RsqlHavingParser.HavingConditionOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionOr}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionOr(RsqlHavingParser.HavingConditionOrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionParens}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionParens(RsqlHavingParser.HavingConditionParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionParens}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionParens(RsqlHavingParser.HavingConditionParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionBetween}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionBetween(RsqlHavingParser.HavingConditionBetweenContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionBetween}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionBetween(RsqlHavingParser.HavingConditionBetweenContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionNotBetween}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionNotBetween(RsqlHavingParser.HavingConditionNotBetweenContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionNotBetween}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionNotBetween(RsqlHavingParser.HavingConditionNotBetweenContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionIn}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionIn(RsqlHavingParser.HavingConditionInContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionIn}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionIn(RsqlHavingParser.HavingConditionInContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionNotIn}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionNotIn(RsqlHavingParser.HavingConditionNotInContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionNotIn}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionNotIn(RsqlHavingParser.HavingConditionNotInContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionComparison}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionComparison(RsqlHavingParser.HavingConditionComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionComparison}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionComparison(RsqlHavingParser.HavingConditionComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionLiteral}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionLiteral(RsqlHavingParser.HavingConditionLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionLiteral}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionLiteral(RsqlHavingParser.HavingConditionLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionNull}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionNull(RsqlHavingParser.HavingConditionNullContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionNull}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionNull(RsqlHavingParser.HavingConditionNullContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionTrue}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionTrue(RsqlHavingParser.HavingConditionTrueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionTrue}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionTrue(RsqlHavingParser.HavingConditionTrueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingConditionFalse}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void enterHavingConditionFalse(RsqlHavingParser.HavingConditionFalseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingConditionFalse}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 */
	void exitHavingConditionFalse(RsqlHavingParser.HavingConditionFalseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#literalList}.
	 * @param ctx the parse tree
	 */
	void enterLiteralList(RsqlHavingParser.LiteralListContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#literalList}.
	 * @param ctx the parse tree
	 */
	void exitLiteralList(RsqlHavingParser.LiteralListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingExprFunctionCall}
	 * labeled alternative in {@link RsqlHavingParser#havingExpression}.
	 * @param ctx the parse tree
	 */
	void enterHavingExprFunctionCall(RsqlHavingParser.HavingExprFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingExprFunctionCall}
	 * labeled alternative in {@link RsqlHavingParser#havingExpression}.
	 * @param ctx the parse tree
	 */
	void exitHavingExprFunctionCall(RsqlHavingParser.HavingExprFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code havingExprField}
	 * labeled alternative in {@link RsqlHavingParser#havingExpression}.
	 * @param ctx the parse tree
	 */
	void enterHavingExprField(RsqlHavingParser.HavingExprFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code havingExprField}
	 * labeled alternative in {@link RsqlHavingParser#havingExpression}.
	 * @param ctx the parse tree
	 */
	void exitHavingExprField(RsqlHavingParser.HavingExprFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(RsqlHavingParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(RsqlHavingParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterFuncCall(RsqlHavingParser.FuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitFuncCall(RsqlHavingParser.FuncCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countAll}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterCountAll(RsqlHavingParser.CountAllContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countAll}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitCountAll(RsqlHavingParser.CountAllContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countDist}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterCountDist(RsqlHavingParser.CountDistContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countDist}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitCountDist(RsqlHavingParser.CountDistContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArgs(RsqlHavingParser.FunctionArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArgs(RsqlHavingParser.FunctionArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArg(RsqlHavingParser.FunctionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArg(RsqlHavingParser.FunctionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(RsqlHavingParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(RsqlHavingParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(RsqlHavingParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(RsqlHavingParser.OperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorBasic}.
	 * @param ctx the parse tree
	 */
	void enterOperatorBasic(RsqlHavingParser.OperatorBasicContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorBasic}.
	 * @param ctx the parse tree
	 */
	void exitOperatorBasic(RsqlHavingParser.OperatorBasicContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorEQ}.
	 * @param ctx the parse tree
	 */
	void enterOperatorEQ(RsqlHavingParser.OperatorEQContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorEQ}.
	 * @param ctx the parse tree
	 */
	void exitOperatorEQ(RsqlHavingParser.OperatorEQContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorNEQ}.
	 * @param ctx the parse tree
	 */
	void enterOperatorNEQ(RsqlHavingParser.OperatorNEQContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorNEQ}.
	 * @param ctx the parse tree
	 */
	void exitOperatorNEQ(RsqlHavingParser.OperatorNEQContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorGT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorGT(RsqlHavingParser.OperatorGTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorGT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorGT(RsqlHavingParser.OperatorGTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorLT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorLT(RsqlHavingParser.OperatorLTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorLT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorLT(RsqlHavingParser.OperatorLTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorGE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorGE(RsqlHavingParser.OperatorGEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorGE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorGE(RsqlHavingParser.OperatorGEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorLE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorLE(RsqlHavingParser.OperatorLEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorLE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorLE(RsqlHavingParser.OperatorLEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorLIKE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorLIKE(RsqlHavingParser.OperatorLIKEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorLIKE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorLIKE(RsqlHavingParser.OperatorLIKEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorNLIKE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorNLIKE(RsqlHavingParser.OperatorNLIKEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorNLIKE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorNLIKE(RsqlHavingParser.OperatorNLIKEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorIN}.
	 * @param ctx the parse tree
	 */
	void enterOperatorIN(RsqlHavingParser.OperatorINContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorIN}.
	 * @param ctx the parse tree
	 */
	void exitOperatorIN(RsqlHavingParser.OperatorINContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorNIN}.
	 * @param ctx the parse tree
	 */
	void enterOperatorNIN(RsqlHavingParser.OperatorNINContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorNIN}.
	 * @param ctx the parse tree
	 */
	void exitOperatorNIN(RsqlHavingParser.OperatorNINContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorBT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorBT(RsqlHavingParser.OperatorBTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorBT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorBT(RsqlHavingParser.OperatorBTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#operatorNBT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorNBT(RsqlHavingParser.OperatorNBTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#operatorNBT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorNBT(RsqlHavingParser.OperatorNBTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlHavingParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(RsqlHavingParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlHavingParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(RsqlHavingParser.FieldContext ctx);
}