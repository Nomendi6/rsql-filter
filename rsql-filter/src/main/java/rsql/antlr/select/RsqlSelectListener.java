// Generated from RsqlSelect.g4 by ANTLR 4.13.2

package rsql.antlr.select;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RsqlSelectParser}.
 */
public interface RsqlSelectListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(RsqlSelectParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(RsqlSelectParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#selectElements}.
	 * @param ctx the parse tree
	 */
	void enterSelectElements(RsqlSelectParser.SelectElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#selectElements}.
	 * @param ctx the parse tree
	 */
	void exitSelectElements(RsqlSelectParser.SelectElementsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code seAll}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSeAll(RsqlSelectParser.SeAllContext ctx);
	/**
	 * Exit a parse tree produced by the {@code seAll}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSeAll(RsqlSelectParser.SeAllContext ctx);
	/**
	 * Enter a parse tree produced by the {@code seField}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSeField(RsqlSelectParser.SeFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code seField}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSeField(RsqlSelectParser.SeFieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code seFuncCall}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code seFuncCall}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(RsqlSelectParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(RsqlSelectParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterFuncCall(RsqlSelectParser.FuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitFuncCall(RsqlSelectParser.FuncCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countAll}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterCountAll(RsqlSelectParser.CountAllContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countAll}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitCountAll(RsqlSelectParser.CountAllContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countDist}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterCountDist(RsqlSelectParser.CountDistContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countDist}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitCountDist(RsqlSelectParser.CountDistContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArgs(RsqlSelectParser.FunctionArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArgs(RsqlSelectParser.FunctionArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArg(RsqlSelectParser.FunctionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArg(RsqlSelectParser.FunctionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#simpleField}.
	 * @param ctx the parse tree
	 */
	void enterSimpleField(RsqlSelectParser.SimpleFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#simpleField}.
	 * @param ctx the parse tree
	 */
	void exitSimpleField(RsqlSelectParser.SimpleFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RsqlSelectParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(RsqlSelectParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RsqlSelectParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(RsqlSelectParser.FieldContext ctx);
}