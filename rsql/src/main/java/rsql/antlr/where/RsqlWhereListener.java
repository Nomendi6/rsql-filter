// Generated from java-escape by ANTLR 4.11.1
package rsql.antlr.where;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RsqlWhereParser}.
 */
public interface RsqlWhereListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#where}.
	 * @param ctx the parse tree
	 */
	void enterWhere(RsqlWhereParser.WhereContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#where}.
	 * @param ctx the parse tree
	 */
	void exitWhere(RsqlWhereParser.WhereContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conditionSingle}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterConditionSingle(RsqlWhereParser.ConditionSingleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conditionSingle}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitConditionSingle(RsqlWhereParser.ConditionSingleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code missingOpeningParenthesis}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterMissingOpeningParenthesis(RsqlWhereParser.MissingOpeningParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code missingOpeningParenthesis}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitMissingOpeningParenthesis(RsqlWhereParser.MissingOpeningParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conditionAnd}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterConditionAnd(RsqlWhereParser.ConditionAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conditionAnd}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitConditionAnd(RsqlWhereParser.ConditionAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conditionOr}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterConditionOr(RsqlWhereParser.ConditionOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conditionOr}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitConditionOr(RsqlWhereParser.ConditionOrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code conditionParens}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterConditionParens(RsqlWhereParser.ConditionParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code conditionParens}
	 * labeled alternative in {@link RsqlWhereParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitConditionParens(RsqlWhereParser.ConditionParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code missingClosingParenthesis}
	 * labeled alternative in {@link RsqlWhereParser#errorCondition}.
	 * @param ctx the parse tree
	 */
	void enterMissingClosingParenthesis(RsqlWhereParser.MissingClosingParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code missingClosingParenthesis}
	 * labeled alternative in {@link RsqlWhereParser#errorCondition}.
	 * @param ctx the parse tree
	 */
	void exitMissingClosingParenthesis(RsqlWhereParser.MissingClosingParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#inList}.
	 * @param ctx the parse tree
	 */
	void enterInList(RsqlWhereParser.InListContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#inList}.
	 * @param ctx the parse tree
	 */
	void exitInList(RsqlWhereParser.InListContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#inListElement}.
	 * @param ctx the parse tree
	 */
	void enterInListElement(RsqlWhereParser.InListElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#inListElement}.
	 * @param ctx the parse tree
	 */
	void exitInListElement(RsqlWhereParser.InListElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionBetween}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionBetween}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionIn}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionIn(RsqlWhereParser.SingleConditionInContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionIn}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionIn(RsqlWhereParser.SingleConditionInContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionNotIn}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionNotIn(RsqlWhereParser.SingleConditionNotInContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionNotIn}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionNotIn(RsqlWhereParser.SingleConditionNotInContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionString}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionString(RsqlWhereParser.SingleConditionStringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionString}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionString(RsqlWhereParser.SingleConditionStringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionDate}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionDate(RsqlWhereParser.SingleConditionDateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionDate}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionDate(RsqlWhereParser.SingleConditionDateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionDatetime}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionDatetime(RsqlWhereParser.SingleConditionDatetimeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionDatetime}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionDatetime(RsqlWhereParser.SingleConditionDatetimeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionDecimal}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionDecimal(RsqlWhereParser.SingleConditionDecimalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionDecimal}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionDecimal(RsqlWhereParser.SingleConditionDecimalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionReal}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionReal(RsqlWhereParser.SingleConditionRealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionReal}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionReal(RsqlWhereParser.SingleConditionRealContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionOtherField}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionOtherField(RsqlWhereParser.SingleConditionOtherFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionOtherField}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionOtherField(RsqlWhereParser.SingleConditionOtherFieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionParam}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionParam(RsqlWhereParser.SingleConditionParamContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionParam}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionParam(RsqlWhereParser.SingleConditionParamContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionEnum}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionEnum(RsqlWhereParser.SingleConditionEnumContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionEnum}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionEnum(RsqlWhereParser.SingleConditionEnumContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionNull}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionNull(RsqlWhereParser.SingleConditionNullContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionNull}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionNull(RsqlWhereParser.SingleConditionNullContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionTrue}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionTrue(RsqlWhereParser.SingleConditionTrueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionTrue}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionTrue(RsqlWhereParser.SingleConditionTrueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleConditionFalse}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSingleConditionFalse(RsqlWhereParser.SingleConditionFalseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleConditionFalse}
	 * labeled alternative in {@link RsqlWhereParser#singleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSingleConditionFalse(RsqlWhereParser.SingleConditionFalseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(RsqlWhereParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(RsqlWhereParser.OperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorBasic}.
	 * @param ctx the parse tree
	 */
	void enterOperatorBasic(RsqlWhereParser.OperatorBasicContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorBasic}.
	 * @param ctx the parse tree
	 */
	void exitOperatorBasic(RsqlWhereParser.OperatorBasicContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorEQ}.
	 * @param ctx the parse tree
	 */
	void enterOperatorEQ(RsqlWhereParser.OperatorEQContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorEQ}.
	 * @param ctx the parse tree
	 */
	void exitOperatorEQ(RsqlWhereParser.OperatorEQContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorNEQ}.
	 * @param ctx the parse tree
	 */
	void enterOperatorNEQ(RsqlWhereParser.OperatorNEQContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorNEQ}.
	 * @param ctx the parse tree
	 */
	void exitOperatorNEQ(RsqlWhereParser.OperatorNEQContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorGT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorGT(RsqlWhereParser.OperatorGTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorGT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorGT(RsqlWhereParser.OperatorGTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorLT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorLT(RsqlWhereParser.OperatorLTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorLT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorLT(RsqlWhereParser.OperatorLTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorGE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorGE(RsqlWhereParser.OperatorGEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorGE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorGE(RsqlWhereParser.OperatorGEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorLE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorLE(RsqlWhereParser.OperatorLEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorLE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorLE(RsqlWhereParser.OperatorLEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorLIKE}.
	 * @param ctx the parse tree
	 */
	void enterOperatorLIKE(RsqlWhereParser.OperatorLIKEContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorLIKE}.
	 * @param ctx the parse tree
	 */
	void exitOperatorLIKE(RsqlWhereParser.OperatorLIKEContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorIN}.
	 * @param ctx the parse tree
	 */
	void enterOperatorIN(RsqlWhereParser.OperatorINContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorIN}.
	 * @param ctx the parse tree
	 */
	void exitOperatorIN(RsqlWhereParser.OperatorINContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorNIN}.
	 * @param ctx the parse tree
	 */
	void enterOperatorNIN(RsqlWhereParser.OperatorNINContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorNIN}.
	 * @param ctx the parse tree
	 */
	void exitOperatorNIN(RsqlWhereParser.OperatorNINContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#operatorBT}.
	 * @param ctx the parse tree
	 */
	void enterOperatorBT(RsqlWhereParser.OperatorBTContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#operatorBT}.
	 * @param ctx the parse tree
	 */
	void exitOperatorBT(RsqlWhereParser.OperatorBTContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlWhereParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(RsqlWhereParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlWhereParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(RsqlWhereParser.FieldContext ctx);
}