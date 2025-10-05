// Generated from RsqlHaving.g4 by ANTLR 4.13.2

package rsql.antlr.having;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class RsqlHavingParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, AND=8, OR=9, NULL=10, 
		TRUE=11, FALSE=12, SUM=13, AVG=14, COUNT=15, MIN=16, MAX=17, GRP=18, ALL=19, 
		DIST=20, STAR=21, GT=22, LT=23, GE=24, LE=25, LIKE=26, NLIKE=27, IN=28, 
		NIN=29, BT=30, NBT=31, DOT=32, LR_BRACKET=33, RR_BRACKET=34, COMMA=35, 
		SEMI=36, AT_SIGN=37, SINGLE_QUOTE_SYMB=38, DOUBLE_QUOTE_SYMB=39, REVERSE_QUOTE_SYMB=40, 
		PARAM_LITERAL=41, DATE_LITERAL=42, DATETIME_LITERAL=43, ENUM_LITERAL=44, 
		STRING_LITERAL=45, DECIMAL_LITERAL=46, REAL_LITERAL=47, DOT_ID=48, ID=49, 
		NEWLINE=50, WS=51;
	public static final int
		RULE_having = 0, RULE_havingCondition = 1, RULE_singleHavingCondition = 2, 
		RULE_literalList = 3, RULE_havingExpression = 4, RULE_functionCall = 5, 
		RULE_aggregateFunction = 6, RULE_functionArgs = 7, RULE_functionArg = 8, 
		RULE_literal = 9, RULE_operator = 10, RULE_operatorBasic = 11, RULE_operatorEQ = 12, 
		RULE_operatorNEQ = 13, RULE_operatorGT = 14, RULE_operatorLT = 15, RULE_operatorGE = 16, 
		RULE_operatorLE = 17, RULE_operatorLIKE = 18, RULE_operatorNLIKE = 19, 
		RULE_operatorIN = 20, RULE_operatorNIN = 21, RULE_operatorBT = 22, RULE_operatorNBT = 23, 
		RULE_field = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"having", "havingCondition", "singleHavingCondition", "literalList", 
			"havingExpression", "functionCall", "aggregateFunction", "functionArgs", 
			"functionArg", "literal", "operator", "operatorBasic", "operatorEQ", 
			"operatorNEQ", "operatorGT", "operatorLT", "operatorGE", "operatorLE", 
			"operatorLIKE", "operatorNLIKE", "operatorIN", "operatorNIN", "operatorBT", 
			"operatorNBT", "field"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'=='", "'=!'", "'!='", "'='", "'=*'", "'=!*'", "'!=*'", null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"'*'", null, null, null, null, null, null, null, null, null, null, "'.'", 
			"'('", "')'", "','", "';'", "'@'", "'''", "'\"'", "'`'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, "AND", "OR", "NULL", 
			"TRUE", "FALSE", "SUM", "AVG", "COUNT", "MIN", "MAX", "GRP", "ALL", "DIST", 
			"STAR", "GT", "LT", "GE", "LE", "LIKE", "NLIKE", "IN", "NIN", "BT", "NBT", 
			"DOT", "LR_BRACKET", "RR_BRACKET", "COMMA", "SEMI", "AT_SIGN", "SINGLE_QUOTE_SYMB", 
			"DOUBLE_QUOTE_SYMB", "REVERSE_QUOTE_SYMB", "PARAM_LITERAL", "DATE_LITERAL", 
			"DATETIME_LITERAL", "ENUM_LITERAL", "STRING_LITERAL", "DECIMAL_LITERAL", 
			"REAL_LITERAL", "DOT_ID", "ID", "NEWLINE", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "RsqlHaving.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RsqlHavingParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HavingContext extends ParserRuleContext {
		public List<HavingConditionContext> havingCondition() {
			return getRuleContexts(HavingConditionContext.class);
		}
		public HavingConditionContext havingCondition(int i) {
			return getRuleContext(HavingConditionContext.class,i);
		}
		public HavingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_having; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHaving(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHaving(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHaving(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingContext having() throws RecognitionException {
		HavingContext _localctx = new HavingContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_having);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(50);
				havingCondition(0);
				}
				}
				setState(53); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 562958543872000L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionContext extends ParserRuleContext {
		public HavingConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingCondition; }
	 
		public HavingConditionContext() { }
		public void copyFrom(HavingConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionSingleContext extends HavingConditionContext {
		public SingleHavingConditionContext singleHavingCondition() {
			return getRuleContext(SingleHavingConditionContext.class,0);
		}
		public HavingConditionSingleContext(HavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionSingle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionSingle(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionSingle(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionAndContext extends HavingConditionContext {
		public List<HavingConditionContext> havingCondition() {
			return getRuleContexts(HavingConditionContext.class);
		}
		public HavingConditionContext havingCondition(int i) {
			return getRuleContext(HavingConditionContext.class,i);
		}
		public TerminalNode AND() { return getToken(RsqlHavingParser.AND, 0); }
		public TerminalNode SEMI() { return getToken(RsqlHavingParser.SEMI, 0); }
		public HavingConditionAndContext(HavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionOrContext extends HavingConditionContext {
		public List<HavingConditionContext> havingCondition() {
			return getRuleContexts(HavingConditionContext.class);
		}
		public HavingConditionContext havingCondition(int i) {
			return getRuleContext(HavingConditionContext.class,i);
		}
		public TerminalNode OR() { return getToken(RsqlHavingParser.OR, 0); }
		public TerminalNode COMMA() { return getToken(RsqlHavingParser.COMMA, 0); }
		public HavingConditionOrContext(HavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionOr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionParensContext extends HavingConditionContext {
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public HavingConditionContext havingCondition() {
			return getRuleContext(HavingConditionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public HavingConditionParensContext(HavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionParens(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionParens(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionParens(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingConditionContext havingCondition() throws RecognitionException {
		return havingCondition(0);
	}

	private HavingConditionContext havingCondition(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		HavingConditionContext _localctx = new HavingConditionContext(_ctx, _parentState);
		HavingConditionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_havingCondition, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUM:
			case AVG:
			case COUNT:
			case MIN:
			case MAX:
			case GRP:
			case ID:
				{
				_localctx = new HavingConditionSingleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(56);
				singleHavingCondition();
				}
				break;
			case LR_BRACKET:
				{
				_localctx = new HavingConditionParensContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(57);
				match(LR_BRACKET);
				setState(58);
				havingCondition(0);
				setState(59);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(71);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(69);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						_localctx = new HavingConditionAndContext(new HavingConditionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_havingCondition);
						setState(63);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(64);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(65);
						havingCondition(3);
						}
						break;
					case 2:
						{
						_localctx = new HavingConditionOrContext(new HavingConditionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_havingCondition);
						setState(66);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(67);
						_la = _input.LA(1);
						if ( !(_la==OR || _la==COMMA) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(68);
						havingCondition(2);
						}
						break;
					}
					} 
				}
				setState(73);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SingleHavingConditionContext extends ParserRuleContext {
		public SingleHavingConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleHavingCondition; }
	 
		public SingleHavingConditionContext() { }
		public void copyFrom(SingleHavingConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionLiteralContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public HavingConditionLiteralContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionBetweenContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorBTContext operatorBT() {
			return getRuleContext(OperatorBTContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(RsqlHavingParser.COMMA, 0); }
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public HavingConditionBetweenContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionBetween(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionBetween(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionBetween(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionNullContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode NULL() { return getToken(RsqlHavingParser.NULL, 0); }
		public HavingConditionNullContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionNull(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionNull(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionNull(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionNotBetweenContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorNBTContext operatorNBT() {
			return getRuleContext(OperatorNBTContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(RsqlHavingParser.COMMA, 0); }
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public HavingConditionNotBetweenContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionNotBetween(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionNotBetween(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionNotBetween(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionTrueContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode TRUE() { return getToken(RsqlHavingParser.TRUE, 0); }
		public HavingConditionTrueContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionTrue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionTrue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionTrue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionComparisonContext extends SingleHavingConditionContext {
		public List<HavingExpressionContext> havingExpression() {
			return getRuleContexts(HavingExpressionContext.class);
		}
		public HavingExpressionContext havingExpression(int i) {
			return getRuleContext(HavingExpressionContext.class,i);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public HavingConditionComparisonContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionNotInContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorNINContext operatorNIN() {
			return getRuleContext(OperatorNINContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public LiteralListContext literalList() {
			return getRuleContext(LiteralListContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public HavingConditionNotInContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionNotIn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionNotIn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionNotIn(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionInContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorINContext operatorIN() {
			return getRuleContext(OperatorINContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public LiteralListContext literalList() {
			return getRuleContext(LiteralListContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public HavingConditionInContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionIn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionIn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionIn(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingConditionFalseContext extends SingleHavingConditionContext {
		public HavingExpressionContext havingExpression() {
			return getRuleContext(HavingExpressionContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode FALSE() { return getToken(RsqlHavingParser.FALSE, 0); }
		public HavingConditionFalseContext(SingleHavingConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingConditionFalse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingConditionFalse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingConditionFalse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleHavingConditionContext singleHavingCondition() throws RecognitionException {
		SingleHavingConditionContext _localctx = new SingleHavingConditionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_singleHavingCondition);
		try {
			setState(122);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new HavingConditionBetweenContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				havingExpression();
				setState(75);
				operatorBT();
				setState(76);
				match(LR_BRACKET);
				setState(77);
				literal();
				setState(78);
				match(COMMA);
				setState(79);
				literal();
				setState(80);
				match(RR_BRACKET);
				}
				break;
			case 2:
				_localctx = new HavingConditionNotBetweenContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(82);
				havingExpression();
				setState(83);
				operatorNBT();
				setState(84);
				match(LR_BRACKET);
				setState(85);
				literal();
				setState(86);
				match(COMMA);
				setState(87);
				literal();
				setState(88);
				match(RR_BRACKET);
				}
				break;
			case 3:
				_localctx = new HavingConditionInContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(90);
				havingExpression();
				setState(91);
				operatorIN();
				setState(92);
				match(LR_BRACKET);
				setState(93);
				literalList();
				setState(94);
				match(RR_BRACKET);
				}
				break;
			case 4:
				_localctx = new HavingConditionNotInContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(96);
				havingExpression();
				setState(97);
				operatorNIN();
				setState(98);
				match(LR_BRACKET);
				setState(99);
				literalList();
				setState(100);
				match(RR_BRACKET);
				}
				break;
			case 5:
				_localctx = new HavingConditionComparisonContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(102);
				havingExpression();
				setState(103);
				operator();
				setState(104);
				havingExpression();
				}
				break;
			case 6:
				_localctx = new HavingConditionLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(106);
				havingExpression();
				setState(107);
				operator();
				setState(108);
				literal();
				}
				break;
			case 7:
				_localctx = new HavingConditionNullContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(110);
				havingExpression();
				setState(111);
				operatorBasic();
				setState(112);
				match(NULL);
				}
				break;
			case 8:
				_localctx = new HavingConditionTrueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(114);
				havingExpression();
				setState(115);
				operatorBasic();
				setState(116);
				match(TRUE);
				}
				break;
			case 9:
				_localctx = new HavingConditionFalseContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(118);
				havingExpression();
				setState(119);
				operatorBasic();
				setState(120);
				match(FALSE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralListContext extends ParserRuleContext {
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RsqlHavingParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RsqlHavingParser.COMMA, i);
		}
		public LiteralListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterLiteralList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitLiteralList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitLiteralList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralListContext literalList() throws RecognitionException {
		LiteralListContext _localctx = new LiteralListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_literalList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			literal();
			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(125);
				match(COMMA);
				setState(126);
				literal();
				}
				}
				setState(131);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HavingExpressionContext extends ParserRuleContext {
		public HavingExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingExpression; }
	 
		public HavingExpressionContext() { }
		public void copyFrom(HavingExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingExprFunctionCallContext extends HavingExpressionContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public HavingExprFunctionCallContext(HavingExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingExprFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingExprFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingExprFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HavingExprFieldContext extends HavingExpressionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public HavingExprFieldContext(HavingExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterHavingExprField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitHavingExprField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitHavingExprField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingExpressionContext havingExpression() throws RecognitionException {
		HavingExpressionContext _localctx = new HavingExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_havingExpression);
		try {
			setState(134);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUM:
			case AVG:
			case COUNT:
			case MIN:
			case MAX:
			case GRP:
				_localctx = new HavingExprFunctionCallContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(132);
				functionCall();
				}
				break;
			case ID:
				_localctx = new HavingExprFieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
				field();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ParserRuleContext {
		public AggregateFunctionContext aggregateFunction() {
			return getRuleContext(AggregateFunctionContext.class,0);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			aggregateFunction();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AggregateFunctionContext extends ParserRuleContext {
		public AggregateFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregateFunction; }
	 
		public AggregateFunctionContext() { }
		public void copyFrom(AggregateFunctionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountAllContext extends AggregateFunctionContext {
		public Token starArg;
		public Token aggregator;
		public TerminalNode COUNT() { return getToken(RsqlHavingParser.COUNT, 0); }
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public FunctionArgContext functionArg() {
			return getRuleContext(FunctionArgContext.class,0);
		}
		public TerminalNode STAR() { return getToken(RsqlHavingParser.STAR, 0); }
		public TerminalNode ALL() { return getToken(RsqlHavingParser.ALL, 0); }
		public CountAllContext(AggregateFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterCountAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitCountAll(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitCountAll(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncCallContext extends AggregateFunctionContext {
		public Token aggregator;
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public FunctionArgContext functionArg() {
			return getRuleContext(FunctionArgContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public TerminalNode AVG() { return getToken(RsqlHavingParser.AVG, 0); }
		public TerminalNode MAX() { return getToken(RsqlHavingParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(RsqlHavingParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(RsqlHavingParser.SUM, 0); }
		public TerminalNode GRP() { return getToken(RsqlHavingParser.GRP, 0); }
		public TerminalNode ALL() { return getToken(RsqlHavingParser.ALL, 0); }
		public TerminalNode DIST() { return getToken(RsqlHavingParser.DIST, 0); }
		public FuncCallContext(AggregateFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountDistContext extends AggregateFunctionContext {
		public Token aggregator;
		public TerminalNode COUNT() { return getToken(RsqlHavingParser.COUNT, 0); }
		public TerminalNode LR_BRACKET() { return getToken(RsqlHavingParser.LR_BRACKET, 0); }
		public FunctionArgsContext functionArgs() {
			return getRuleContext(FunctionArgsContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlHavingParser.RR_BRACKET, 0); }
		public TerminalNode DIST() { return getToken(RsqlHavingParser.DIST, 0); }
		public CountDistContext(AggregateFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterCountDist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitCountDist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitCountDist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateFunctionContext aggregateFunction() throws RecognitionException {
		AggregateFunctionContext _localctx = new AggregateFunctionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_aggregateFunction);
		int _la;
		try {
			setState(162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				_localctx = new FuncCallContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(138);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 483328L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(139);
				match(LR_BRACKET);
				setState(141);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DIST) {
					{
					setState(140);
					((FuncCallContext)_localctx).aggregator = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==ALL || _la==DIST) ) {
						((FuncCallContext)_localctx).aggregator = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(143);
				functionArg();
				setState(144);
				match(RR_BRACKET);
				}
				break;
			case 2:
				_localctx = new CountAllContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				match(COUNT);
				setState(147);
				match(LR_BRACKET);
				setState(153);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case STAR:
					{
					setState(148);
					((CountAllContext)_localctx).starArg = match(STAR);
					}
					break;
				case SUM:
				case AVG:
				case COUNT:
				case MIN:
				case MAX:
				case GRP:
				case ALL:
				case ID:
					{
					setState(150);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ALL) {
						{
						setState(149);
						((CountAllContext)_localctx).aggregator = match(ALL);
						}
					}

					setState(152);
					functionArg();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(155);
				match(RR_BRACKET);
				}
				break;
			case 3:
				_localctx = new CountDistContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(156);
				match(COUNT);
				setState(157);
				match(LR_BRACKET);
				setState(158);
				((CountDistContext)_localctx).aggregator = match(DIST);
				setState(159);
				functionArgs();
				setState(160);
				match(RR_BRACKET);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionArgsContext extends ParserRuleContext {
		public List<FunctionArgContext> functionArg() {
			return getRuleContexts(FunctionArgContext.class);
		}
		public FunctionArgContext functionArg(int i) {
			return getRuleContext(FunctionArgContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RsqlHavingParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RsqlHavingParser.COMMA, i);
		}
		public FunctionArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterFunctionArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitFunctionArgs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitFunctionArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgsContext functionArgs() throws RecognitionException {
		FunctionArgsContext _localctx = new FunctionArgsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_functionArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			functionArg();
			setState(169);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(165);
				match(COMMA);
				setState(166);
				functionArg();
				}
				}
				setState(171);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionArgContext extends ParserRuleContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FunctionArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterFunctionArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitFunctionArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitFunctionArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgContext functionArg() throws RecognitionException {
		FunctionArgContext _localctx = new FunctionArgContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_functionArg);
		try {
			setState(174);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(172);
				field();
				}
				break;
			case SUM:
			case AVG:
			case COUNT:
			case MIN:
			case MAX:
			case GRP:
				enterOuterAlt(_localctx, 2);
				{
				setState(173);
				functionCall();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(RsqlHavingParser.STRING_LITERAL, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(RsqlHavingParser.DECIMAL_LITERAL, 0); }
		public TerminalNode REAL_LITERAL() { return getToken(RsqlHavingParser.REAL_LITERAL, 0); }
		public TerminalNode DATE_LITERAL() { return getToken(RsqlHavingParser.DATE_LITERAL, 0); }
		public TerminalNode DATETIME_LITERAL() { return getToken(RsqlHavingParser.DATETIME_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(RsqlHavingParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(RsqlHavingParser.FALSE, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 259484744161280L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorContext extends ParserRuleContext {
		public OperatorEQContext operatorEQ() {
			return getRuleContext(OperatorEQContext.class,0);
		}
		public OperatorNEQContext operatorNEQ() {
			return getRuleContext(OperatorNEQContext.class,0);
		}
		public OperatorLTContext operatorLT() {
			return getRuleContext(OperatorLTContext.class,0);
		}
		public OperatorGTContext operatorGT() {
			return getRuleContext(OperatorGTContext.class,0);
		}
		public OperatorLEContext operatorLE() {
			return getRuleContext(OperatorLEContext.class,0);
		}
		public OperatorGEContext operatorGE() {
			return getRuleContext(OperatorGEContext.class,0);
		}
		public OperatorLIKEContext operatorLIKE() {
			return getRuleContext(OperatorLIKEContext.class,0);
		}
		public OperatorNLIKEContext operatorNLIKE() {
			return getRuleContext(OperatorNLIKEContext.class,0);
		}
		public OperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorContext operator() throws RecognitionException {
		OperatorContext _localctx = new OperatorContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_operator);
		try {
			setState(186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(178);
				operatorEQ();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(179);
				operatorNEQ();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(180);
				operatorLT();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(181);
				operatorGT();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(182);
				operatorLE();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(183);
				operatorGE();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(184);
				operatorLIKE();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(185);
				operatorNLIKE();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorBasicContext extends ParserRuleContext {
		public OperatorEQContext operatorEQ() {
			return getRuleContext(OperatorEQContext.class,0);
		}
		public OperatorNEQContext operatorNEQ() {
			return getRuleContext(OperatorNEQContext.class,0);
		}
		public OperatorBasicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorBasic; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorBasic(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorBasic(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorBasic(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorBasicContext operatorBasic() throws RecognitionException {
		OperatorBasicContext _localctx = new OperatorBasicContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_operatorBasic);
		try {
			setState(190);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(188);
				operatorEQ();
				}
				break;
			case T__1:
			case T__2:
				enterOuterAlt(_localctx, 2);
				{
				setState(189);
				operatorNEQ();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorEQContext extends ParserRuleContext {
		public OperatorEQContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorEQ; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorEQ(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorEQ(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorEQ(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorEQContext operatorEQ() throws RecognitionException {
		OperatorEQContext _localctx = new OperatorEQContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_operatorEQ);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorNEQContext extends ParserRuleContext {
		public OperatorNEQContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNEQ; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorNEQ(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorNEQ(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorNEQ(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNEQContext operatorNEQ() throws RecognitionException {
		OperatorNEQContext _localctx = new OperatorNEQContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_operatorNEQ);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			_la = _input.LA(1);
			if ( !(_la==T__1 || _la==T__2) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorGTContext extends ParserRuleContext {
		public TerminalNode GT() { return getToken(RsqlHavingParser.GT, 0); }
		public OperatorGTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorGT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorGT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorGT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorGT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorGTContext operatorGT() throws RecognitionException {
		OperatorGTContext _localctx = new OperatorGTContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_operatorGT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			match(T__3);
			setState(197);
			match(GT);
			setState(198);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorLTContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(RsqlHavingParser.LT, 0); }
		public OperatorLTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorLT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorLT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorLT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorLT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorLTContext operatorLT() throws RecognitionException {
		OperatorLTContext _localctx = new OperatorLTContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_operatorLT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(T__3);
			setState(201);
			match(LT);
			setState(202);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorGEContext extends ParserRuleContext {
		public TerminalNode GE() { return getToken(RsqlHavingParser.GE, 0); }
		public OperatorGEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorGE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorGE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorGE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorGE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorGEContext operatorGE() throws RecognitionException {
		OperatorGEContext _localctx = new OperatorGEContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_operatorGE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			match(T__3);
			setState(205);
			match(GE);
			setState(206);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorLEContext extends ParserRuleContext {
		public TerminalNode LE() { return getToken(RsqlHavingParser.LE, 0); }
		public OperatorLEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorLE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorLE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorLE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorLE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorLEContext operatorLE() throws RecognitionException {
		OperatorLEContext _localctx = new OperatorLEContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_operatorLE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			match(T__3);
			setState(209);
			match(LE);
			setState(210);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorLIKEContext extends ParserRuleContext {
		public TerminalNode LIKE() { return getToken(RsqlHavingParser.LIKE, 0); }
		public OperatorLIKEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorLIKE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorLIKE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorLIKE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorLIKE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorLIKEContext operatorLIKE() throws RecognitionException {
		OperatorLIKEContext _localctx = new OperatorLIKEContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_operatorLIKE);
		try {
			setState(216);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(212);
				match(T__4);
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 2);
				{
				setState(213);
				match(T__3);
				setState(214);
				match(LIKE);
				setState(215);
				match(T__3);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorNLIKEContext extends ParserRuleContext {
		public TerminalNode NLIKE() { return getToken(RsqlHavingParser.NLIKE, 0); }
		public OperatorNLIKEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNLIKE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorNLIKE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorNLIKE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorNLIKE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNLIKEContext operatorNLIKE() throws RecognitionException {
		OperatorNLIKEContext _localctx = new OperatorNLIKEContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_operatorNLIKE);
		try {
			setState(223);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
				enterOuterAlt(_localctx, 1);
				{
				setState(218);
				match(T__5);
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 2);
				{
				setState(219);
				match(T__6);
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 3);
				{
				setState(220);
				match(T__3);
				setState(221);
				match(NLIKE);
				setState(222);
				match(T__3);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorINContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(RsqlHavingParser.IN, 0); }
		public OperatorINContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorIN; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorIN(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorIN(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorIN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorINContext operatorIN() throws RecognitionException {
		OperatorINContext _localctx = new OperatorINContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_operatorIN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(225);
			match(T__3);
			setState(226);
			match(IN);
			setState(227);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorNINContext extends ParserRuleContext {
		public TerminalNode NIN() { return getToken(RsqlHavingParser.NIN, 0); }
		public OperatorNINContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNIN; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorNIN(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorNIN(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorNIN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNINContext operatorNIN() throws RecognitionException {
		OperatorNINContext _localctx = new OperatorNINContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_operatorNIN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(T__3);
			setState(230);
			match(NIN);
			setState(231);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorBTContext extends ParserRuleContext {
		public TerminalNode BT() { return getToken(RsqlHavingParser.BT, 0); }
		public OperatorBTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorBT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorBT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorBT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorBT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorBTContext operatorBT() throws RecognitionException {
		OperatorBTContext _localctx = new OperatorBTContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_operatorBT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			match(T__3);
			setState(234);
			match(BT);
			setState(235);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OperatorNBTContext extends ParserRuleContext {
		public TerminalNode NBT() { return getToken(RsqlHavingParser.NBT, 0); }
		public OperatorNBTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNBT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterOperatorNBT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitOperatorNBT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitOperatorNBT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNBTContext operatorNBT() throws RecognitionException {
		OperatorNBTContext _localctx = new OperatorNBTContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_operatorNBT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237);
			match(T__3);
			setState(238);
			match(NBT);
			setState(239);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RsqlHavingParser.ID, 0); }
		public List<TerminalNode> DOT_ID() { return getTokens(RsqlHavingParser.DOT_ID); }
		public TerminalNode DOT_ID(int i) {
			return getToken(RsqlHavingParser.DOT_ID, i);
		}
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlHavingListener ) ((RsqlHavingListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlHavingVisitor ) return ((RsqlHavingVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_field);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(241);
			match(ID);
			setState(245);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(242);
					match(DOT_ID);
					}
					} 
				}
				setState(247);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return havingCondition_sempred((HavingConditionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean havingCondition_sempred(HavingConditionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u00013\u00f9\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0004\u00004\b\u0000\u000b\u0000\f\u00005\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001>\b"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0005\u0001F\b\u0001\n\u0001\f\u0001I\t\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002{\b"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u0080\b\u0003\n"+
		"\u0003\f\u0003\u0083\t\u0003\u0001\u0004\u0001\u0004\u0003\u0004\u0087"+
		"\b\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0003"+
		"\u0006\u008e\b\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u0097\b\u0006\u0001\u0006\u0003"+
		"\u0006\u009a\b\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u00a3\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0005\u0007\u00a8\b\u0007\n\u0007\f\u0007\u00ab\t\u0007"+
		"\u0001\b\u0001\b\u0003\b\u00af\b\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00bb\b\n\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u00bf\b\u000b\u0001\f\u0001\f\u0001\r\u0001\r"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u00d9\b\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00e0\b\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0005\u0018"+
		"\u00f4\b\u0018\n\u0018\f\u0018\u00f7\t\u0018\u0001\u0018\u0000\u0001\u0002"+
		"\u0019\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(*,.0\u0000\u0006\u0002\u0000\b\b$$\u0002\u0000"+
		"\t\t##\u0002\u0000\r\u000e\u0010\u0012\u0001\u0000\u0013\u0014\u0003\u0000"+
		"\u000b\f*+-/\u0001\u0000\u0002\u0003\u0100\u00003\u0001\u0000\u0000\u0000"+
		"\u0002=\u0001\u0000\u0000\u0000\u0004z\u0001\u0000\u0000\u0000\u0006|"+
		"\u0001\u0000\u0000\u0000\b\u0086\u0001\u0000\u0000\u0000\n\u0088\u0001"+
		"\u0000\u0000\u0000\f\u00a2\u0001\u0000\u0000\u0000\u000e\u00a4\u0001\u0000"+
		"\u0000\u0000\u0010\u00ae\u0001\u0000\u0000\u0000\u0012\u00b0\u0001\u0000"+
		"\u0000\u0000\u0014\u00ba\u0001\u0000\u0000\u0000\u0016\u00be\u0001\u0000"+
		"\u0000\u0000\u0018\u00c0\u0001\u0000\u0000\u0000\u001a\u00c2\u0001\u0000"+
		"\u0000\u0000\u001c\u00c4\u0001\u0000\u0000\u0000\u001e\u00c8\u0001\u0000"+
		"\u0000\u0000 \u00cc\u0001\u0000\u0000\u0000\"\u00d0\u0001\u0000\u0000"+
		"\u0000$\u00d8\u0001\u0000\u0000\u0000&\u00df\u0001\u0000\u0000\u0000("+
		"\u00e1\u0001\u0000\u0000\u0000*\u00e5\u0001\u0000\u0000\u0000,\u00e9\u0001"+
		"\u0000\u0000\u0000.\u00ed\u0001\u0000\u0000\u00000\u00f1\u0001\u0000\u0000"+
		"\u000024\u0003\u0002\u0001\u000032\u0001\u0000\u0000\u000045\u0001\u0000"+
		"\u0000\u000053\u0001\u0000\u0000\u000056\u0001\u0000\u0000\u00006\u0001"+
		"\u0001\u0000\u0000\u000078\u0006\u0001\uffff\uffff\u00008>\u0003\u0004"+
		"\u0002\u00009:\u0005!\u0000\u0000:;\u0003\u0002\u0001\u0000;<\u0005\""+
		"\u0000\u0000<>\u0001\u0000\u0000\u0000=7\u0001\u0000\u0000\u0000=9\u0001"+
		"\u0000\u0000\u0000>G\u0001\u0000\u0000\u0000?@\n\u0002\u0000\u0000@A\u0007"+
		"\u0000\u0000\u0000AF\u0003\u0002\u0001\u0003BC\n\u0001\u0000\u0000CD\u0007"+
		"\u0001\u0000\u0000DF\u0003\u0002\u0001\u0002E?\u0001\u0000\u0000\u0000"+
		"EB\u0001\u0000\u0000\u0000FI\u0001\u0000\u0000\u0000GE\u0001\u0000\u0000"+
		"\u0000GH\u0001\u0000\u0000\u0000H\u0003\u0001\u0000\u0000\u0000IG\u0001"+
		"\u0000\u0000\u0000JK\u0003\b\u0004\u0000KL\u0003,\u0016\u0000LM\u0005"+
		"!\u0000\u0000MN\u0003\u0012\t\u0000NO\u0005#\u0000\u0000OP\u0003\u0012"+
		"\t\u0000PQ\u0005\"\u0000\u0000Q{\u0001\u0000\u0000\u0000RS\u0003\b\u0004"+
		"\u0000ST\u0003.\u0017\u0000TU\u0005!\u0000\u0000UV\u0003\u0012\t\u0000"+
		"VW\u0005#\u0000\u0000WX\u0003\u0012\t\u0000XY\u0005\"\u0000\u0000Y{\u0001"+
		"\u0000\u0000\u0000Z[\u0003\b\u0004\u0000[\\\u0003(\u0014\u0000\\]\u0005"+
		"!\u0000\u0000]^\u0003\u0006\u0003\u0000^_\u0005\"\u0000\u0000_{\u0001"+
		"\u0000\u0000\u0000`a\u0003\b\u0004\u0000ab\u0003*\u0015\u0000bc\u0005"+
		"!\u0000\u0000cd\u0003\u0006\u0003\u0000de\u0005\"\u0000\u0000e{\u0001"+
		"\u0000\u0000\u0000fg\u0003\b\u0004\u0000gh\u0003\u0014\n\u0000hi\u0003"+
		"\b\u0004\u0000i{\u0001\u0000\u0000\u0000jk\u0003\b\u0004\u0000kl\u0003"+
		"\u0014\n\u0000lm\u0003\u0012\t\u0000m{\u0001\u0000\u0000\u0000no\u0003"+
		"\b\u0004\u0000op\u0003\u0016\u000b\u0000pq\u0005\n\u0000\u0000q{\u0001"+
		"\u0000\u0000\u0000rs\u0003\b\u0004\u0000st\u0003\u0016\u000b\u0000tu\u0005"+
		"\u000b\u0000\u0000u{\u0001\u0000\u0000\u0000vw\u0003\b\u0004\u0000wx\u0003"+
		"\u0016\u000b\u0000xy\u0005\f\u0000\u0000y{\u0001\u0000\u0000\u0000zJ\u0001"+
		"\u0000\u0000\u0000zR\u0001\u0000\u0000\u0000zZ\u0001\u0000\u0000\u0000"+
		"z`\u0001\u0000\u0000\u0000zf\u0001\u0000\u0000\u0000zj\u0001\u0000\u0000"+
		"\u0000zn\u0001\u0000\u0000\u0000zr\u0001\u0000\u0000\u0000zv\u0001\u0000"+
		"\u0000\u0000{\u0005\u0001\u0000\u0000\u0000|\u0081\u0003\u0012\t\u0000"+
		"}~\u0005#\u0000\u0000~\u0080\u0003\u0012\t\u0000\u007f}\u0001\u0000\u0000"+
		"\u0000\u0080\u0083\u0001\u0000\u0000\u0000\u0081\u007f\u0001\u0000\u0000"+
		"\u0000\u0081\u0082\u0001\u0000\u0000\u0000\u0082\u0007\u0001\u0000\u0000"+
		"\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0084\u0087\u0003\n\u0005\u0000"+
		"\u0085\u0087\u00030\u0018\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0086"+
		"\u0085\u0001\u0000\u0000\u0000\u0087\t\u0001\u0000\u0000\u0000\u0088\u0089"+
		"\u0003\f\u0006\u0000\u0089\u000b\u0001\u0000\u0000\u0000\u008a\u008b\u0007"+
		"\u0002\u0000\u0000\u008b\u008d\u0005!\u0000\u0000\u008c\u008e\u0007\u0003"+
		"\u0000\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000"+
		"\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u0090\u0003\u0010"+
		"\b\u0000\u0090\u0091\u0005\"\u0000\u0000\u0091\u00a3\u0001\u0000\u0000"+
		"\u0000\u0092\u0093\u0005\u000f\u0000\u0000\u0093\u0099\u0005!\u0000\u0000"+
		"\u0094\u009a\u0005\u0015\u0000\u0000\u0095\u0097\u0005\u0013\u0000\u0000"+
		"\u0096\u0095\u0001\u0000\u0000\u0000\u0096\u0097\u0001\u0000\u0000\u0000"+
		"\u0097\u0098\u0001\u0000\u0000\u0000\u0098\u009a\u0003\u0010\b\u0000\u0099"+
		"\u0094\u0001\u0000\u0000\u0000\u0099\u0096\u0001\u0000\u0000\u0000\u009a"+
		"\u009b\u0001\u0000\u0000\u0000\u009b\u00a3\u0005\"\u0000\u0000\u009c\u009d"+
		"\u0005\u000f\u0000\u0000\u009d\u009e\u0005!\u0000\u0000\u009e\u009f\u0005"+
		"\u0014\u0000\u0000\u009f\u00a0\u0003\u000e\u0007\u0000\u00a0\u00a1\u0005"+
		"\"\u0000\u0000\u00a1\u00a3\u0001\u0000\u0000\u0000\u00a2\u008a\u0001\u0000"+
		"\u0000\u0000\u00a2\u0092\u0001\u0000\u0000\u0000\u00a2\u009c\u0001\u0000"+
		"\u0000\u0000\u00a3\r\u0001\u0000\u0000\u0000\u00a4\u00a9\u0003\u0010\b"+
		"\u0000\u00a5\u00a6\u0005#\u0000\u0000\u00a6\u00a8\u0003\u0010\b\u0000"+
		"\u00a7\u00a5\u0001\u0000\u0000\u0000\u00a8\u00ab\u0001\u0000\u0000\u0000"+
		"\u00a9\u00a7\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000"+
		"\u00aa\u000f\u0001\u0000\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000"+
		"\u00ac\u00af\u00030\u0018\u0000\u00ad\u00af\u0003\n\u0005\u0000\u00ae"+
		"\u00ac\u0001\u0000\u0000\u0000\u00ae\u00ad\u0001\u0000\u0000\u0000\u00af"+
		"\u0011\u0001\u0000\u0000\u0000\u00b0\u00b1\u0007\u0004\u0000\u0000\u00b1"+
		"\u0013\u0001\u0000\u0000\u0000\u00b2\u00bb\u0003\u0018\f\u0000\u00b3\u00bb"+
		"\u0003\u001a\r\u0000\u00b4\u00bb\u0003\u001e\u000f\u0000\u00b5\u00bb\u0003"+
		"\u001c\u000e\u0000\u00b6\u00bb\u0003\"\u0011\u0000\u00b7\u00bb\u0003 "+
		"\u0010\u0000\u00b8\u00bb\u0003$\u0012\u0000\u00b9\u00bb\u0003&\u0013\u0000"+
		"\u00ba\u00b2\u0001\u0000\u0000\u0000\u00ba\u00b3\u0001\u0000\u0000\u0000"+
		"\u00ba\u00b4\u0001\u0000\u0000\u0000\u00ba\u00b5\u0001\u0000\u0000\u0000"+
		"\u00ba\u00b6\u0001\u0000\u0000\u0000\u00ba\u00b7\u0001\u0000\u0000\u0000"+
		"\u00ba\u00b8\u0001\u0000\u0000\u0000\u00ba\u00b9\u0001\u0000\u0000\u0000"+
		"\u00bb\u0015\u0001\u0000\u0000\u0000\u00bc\u00bf\u0003\u0018\f\u0000\u00bd"+
		"\u00bf\u0003\u001a\r\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00be\u00bd"+
		"\u0001\u0000\u0000\u0000\u00bf\u0017\u0001\u0000\u0000\u0000\u00c0\u00c1"+
		"\u0005\u0001\u0000\u0000\u00c1\u0019\u0001\u0000\u0000\u0000\u00c2\u00c3"+
		"\u0007\u0005\u0000\u0000\u00c3\u001b\u0001\u0000\u0000\u0000\u00c4\u00c5"+
		"\u0005\u0004\u0000\u0000\u00c5\u00c6\u0005\u0016\u0000\u0000\u00c6\u00c7"+
		"\u0005\u0004\u0000\u0000\u00c7\u001d\u0001\u0000\u0000\u0000\u00c8\u00c9"+
		"\u0005\u0004\u0000\u0000\u00c9\u00ca\u0005\u0017\u0000\u0000\u00ca\u00cb"+
		"\u0005\u0004\u0000\u0000\u00cb\u001f\u0001\u0000\u0000\u0000\u00cc\u00cd"+
		"\u0005\u0004\u0000\u0000\u00cd\u00ce\u0005\u0018\u0000\u0000\u00ce\u00cf"+
		"\u0005\u0004\u0000\u0000\u00cf!\u0001\u0000\u0000\u0000\u00d0\u00d1\u0005"+
		"\u0004\u0000\u0000\u00d1\u00d2\u0005\u0019\u0000\u0000\u00d2\u00d3\u0005"+
		"\u0004\u0000\u0000\u00d3#\u0001\u0000\u0000\u0000\u00d4\u00d9\u0005\u0005"+
		"\u0000\u0000\u00d5\u00d6\u0005\u0004\u0000\u0000\u00d6\u00d7\u0005\u001a"+
		"\u0000\u0000\u00d7\u00d9\u0005\u0004\u0000\u0000\u00d8\u00d4\u0001\u0000"+
		"\u0000\u0000\u00d8\u00d5\u0001\u0000\u0000\u0000\u00d9%\u0001\u0000\u0000"+
		"\u0000\u00da\u00e0\u0005\u0006\u0000\u0000\u00db\u00e0\u0005\u0007\u0000"+
		"\u0000\u00dc\u00dd\u0005\u0004\u0000\u0000\u00dd\u00de\u0005\u001b\u0000"+
		"\u0000\u00de\u00e0\u0005\u0004\u0000\u0000\u00df\u00da\u0001\u0000\u0000"+
		"\u0000\u00df\u00db\u0001\u0000\u0000\u0000\u00df\u00dc\u0001\u0000\u0000"+
		"\u0000\u00e0\'\u0001\u0000\u0000\u0000\u00e1\u00e2\u0005\u0004\u0000\u0000"+
		"\u00e2\u00e3\u0005\u001c\u0000\u0000\u00e3\u00e4\u0005\u0004\u0000\u0000"+
		"\u00e4)\u0001\u0000\u0000\u0000\u00e5\u00e6\u0005\u0004\u0000\u0000\u00e6"+
		"\u00e7\u0005\u001d\u0000\u0000\u00e7\u00e8\u0005\u0004\u0000\u0000\u00e8"+
		"+\u0001\u0000\u0000\u0000\u00e9\u00ea\u0005\u0004\u0000\u0000\u00ea\u00eb"+
		"\u0005\u001e\u0000\u0000\u00eb\u00ec\u0005\u0004\u0000\u0000\u00ec-\u0001"+
		"\u0000\u0000\u0000\u00ed\u00ee\u0005\u0004\u0000\u0000\u00ee\u00ef\u0005"+
		"\u001f\u0000\u0000\u00ef\u00f0\u0005\u0004\u0000\u0000\u00f0/\u0001\u0000"+
		"\u0000\u0000\u00f1\u00f5\u00051\u0000\u0000\u00f2\u00f4\u00050\u0000\u0000"+
		"\u00f3\u00f2\u0001\u0000\u0000\u0000\u00f4\u00f7\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000"+
		"\u00f61\u0001\u0000\u0000\u0000\u00f7\u00f5\u0001\u0000\u0000\u0000\u0012"+
		"5=EGz\u0081\u0086\u008d\u0096\u0099\u00a2\u00a9\u00ae\u00ba\u00be\u00d8"+
		"\u00df\u00f5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}