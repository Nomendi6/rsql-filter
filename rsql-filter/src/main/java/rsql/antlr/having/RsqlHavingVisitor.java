// Generated from RsqlHaving.g4 by ANTLR 4.13.2

package rsql.antlr.having;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RsqlHavingParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RsqlHavingVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#having}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving(RsqlHavingParser.HavingContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionSingle}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionSingle(RsqlHavingParser.HavingConditionSingleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionAnd}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionAnd(RsqlHavingParser.HavingConditionAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionOr}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionOr(RsqlHavingParser.HavingConditionOrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionParens}
	 * labeled alternative in {@link RsqlHavingParser#havingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionParens(RsqlHavingParser.HavingConditionParensContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionBetween}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionBetween(RsqlHavingParser.HavingConditionBetweenContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionNotBetween}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionNotBetween(RsqlHavingParser.HavingConditionNotBetweenContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionIn}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionIn(RsqlHavingParser.HavingConditionInContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionNotIn}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionNotIn(RsqlHavingParser.HavingConditionNotInContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionComparison}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionComparison(RsqlHavingParser.HavingConditionComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionLiteral}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionLiteral(RsqlHavingParser.HavingConditionLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionNull}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionNull(RsqlHavingParser.HavingConditionNullContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionTrue}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionTrue(RsqlHavingParser.HavingConditionTrueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingConditionFalse}
	 * labeled alternative in {@link RsqlHavingParser#singleHavingCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingConditionFalse(RsqlHavingParser.HavingConditionFalseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#literalList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralList(RsqlHavingParser.LiteralListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingExprFunctionCall}
	 * labeled alternative in {@link RsqlHavingParser#havingExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingExprFunctionCall(RsqlHavingParser.HavingExprFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code havingExprField}
	 * labeled alternative in {@link RsqlHavingParser#havingExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingExprField(RsqlHavingParser.HavingExprFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(RsqlHavingParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCall(RsqlHavingParser.FuncCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countAll}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountAll(RsqlHavingParser.CountAllContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countDist}
	 * labeled alternative in {@link RsqlHavingParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountDist(RsqlHavingParser.CountDistContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#functionArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgs(RsqlHavingParser.FunctionArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#functionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArg(RsqlHavingParser.FunctionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(RsqlHavingParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator(RsqlHavingParser.OperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorBasic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorBasic(RsqlHavingParser.OperatorBasicContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorEQ}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorEQ(RsqlHavingParser.OperatorEQContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorNEQ}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorNEQ(RsqlHavingParser.OperatorNEQContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorGT}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorGT(RsqlHavingParser.OperatorGTContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorLT}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorLT(RsqlHavingParser.OperatorLTContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorGE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorGE(RsqlHavingParser.OperatorGEContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorLE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorLE(RsqlHavingParser.OperatorLEContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorLIKE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorLIKE(RsqlHavingParser.OperatorLIKEContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorNLIKE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorNLIKE(RsqlHavingParser.OperatorNLIKEContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorIN}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorIN(RsqlHavingParser.OperatorINContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorNIN}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorNIN(RsqlHavingParser.OperatorNINContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorBT}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorBT(RsqlHavingParser.OperatorBTContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#operatorNBT}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorNBT(RsqlHavingParser.OperatorNBTContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlHavingParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(RsqlHavingParser.FieldContext ctx);
}