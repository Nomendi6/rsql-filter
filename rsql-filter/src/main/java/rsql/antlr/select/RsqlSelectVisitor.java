// Generated from RsqlSelect.g4 by ANTLR 4.13.2
package rsql.antlr.select;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RsqlSelectParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RsqlSelectVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(RsqlSelectParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#selectElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectElements(RsqlSelectParser.SelectElementsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code seAll}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeAll(RsqlSelectParser.SeAllContext ctx);
	/**
	 * Visit a parse tree produced by the {@code seField}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeField(RsqlSelectParser.SeFieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code seFuncCall}
	 * labeled alternative in {@link RsqlSelectParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(RsqlSelectParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCall(RsqlSelectParser.FuncCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countAll}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountAll(RsqlSelectParser.CountAllContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countDist}
	 * labeled alternative in {@link RsqlSelectParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountDist(RsqlSelectParser.CountDistContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#functionArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgs(RsqlSelectParser.FunctionArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#functionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArg(RsqlSelectParser.FunctionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#simpleField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleField(RsqlSelectParser.SimpleFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RsqlSelectParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(RsqlSelectParser.FieldContext ctx);
}