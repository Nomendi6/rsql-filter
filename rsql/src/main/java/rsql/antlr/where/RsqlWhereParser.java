// Generated from /home/vrba/v/rsql-filter/nomendi6/rsql-filter/rsql/src/main/antlr/RsqlWhere.g4 by ANTLR 4.13.1
package rsql.antlr.where;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class RsqlWhereParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, AND=7, OR=8, NULL=9, TRUE=10, 
		FALSE=11, GT=12, LT=13, GE=14, LE=15, NLIKE=16, LIKE=17, NIN=18, IN=19, 
		NBT=20, BT=21, DOT=22, LR_BRACKET=23, RR_BRACKET=24, COMMA=25, SEMI=26, 
		AT_SIGN=27, SINGLE_QUOTE_SYMB=28, DOUBLE_QUOTE_SYMB=29, REVERSE_QUOTE_SYMB=30, 
		PARAM_LITERAL=31, DATE_LITERAL=32, DATETIME_LITERAL=33, ENUM_LITERAL=34, 
		STRING_LITERAL=35, DECIMAL_LITERAL=36, REAL_LITERAL=37, DOT_ID=38, ID=39, 
		NEWLINE=40, WS=41;
	public static final int
		RULE_where = 0, RULE_condition = 1, RULE_errorCondition = 2, RULE_inList = 3, 
		RULE_inListElement = 4, RULE_singleCondition = 5, RULE_operator = 6, RULE_operatorBasic = 7, 
		RULE_operatorEQ = 8, RULE_operatorNEQ = 9, RULE_operatorGT = 10, RULE_operatorLT = 11, 
		RULE_operatorGE = 12, RULE_operatorLE = 13, RULE_operatorLIKE = 14, RULE_operatorNLIKE = 15, 
		RULE_operatorIN = 16, RULE_operatorNIN = 17, RULE_operatorNBT = 18, RULE_operatorBT = 19, 
		RULE_field = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"where", "condition", "errorCondition", "inList", "inListElement", "singleCondition", 
			"operator", "operatorBasic", "operatorEQ", "operatorNEQ", "operatorGT", 
			"operatorLT", "operatorGE", "operatorLE", "operatorLIKE", "operatorNLIKE", 
			"operatorIN", "operatorNIN", "operatorNBT", "operatorBT", "field"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'=='", "'=!'", "'!='", "'='", "'=*'", "'=!*'", null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"'.'", "'('", "')'", "','", "';'", "'@'", "'''", "'\"'", "'`'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, "AND", "OR", "NULL", "TRUE", 
			"FALSE", "GT", "LT", "GE", "LE", "NLIKE", "LIKE", "NIN", "IN", "NBT", 
			"BT", "DOT", "LR_BRACKET", "RR_BRACKET", "COMMA", "SEMI", "AT_SIGN", 
			"SINGLE_QUOTE_SYMB", "DOUBLE_QUOTE_SYMB", "REVERSE_QUOTE_SYMB", "PARAM_LITERAL", 
			"DATE_LITERAL", "DATETIME_LITERAL", "ENUM_LITERAL", "STRING_LITERAL", 
			"DECIMAL_LITERAL", "REAL_LITERAL", "DOT_ID", "ID", "NEWLINE", "WS"
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
	public String getGrammarFileName() { return "RsqlWhere.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RsqlWhereParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhereContext extends ParserRuleContext {
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public WhereContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterWhere(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitWhere(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitWhere(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereContext where() throws RecognitionException {
		WhereContext _localctx = new WhereContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_where);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(42);
				condition(0);
				}
				}
				setState(45); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LR_BRACKET || _la==ID );
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
	public static class ConditionContext extends ParserRuleContext {
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
	 
		public ConditionContext() { }
		public void copyFrom(ConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConditionSingleContext extends ConditionContext {
		public SingleConditionContext singleCondition() {
			return getRuleContext(SingleConditionContext.class,0);
		}
		public ConditionSingleContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterConditionSingle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitConditionSingle(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitConditionSingle(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MissingOpeningParenthesisContext extends ConditionContext {
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public List<TerminalNode> RR_BRACKET() { return getTokens(RsqlWhereParser.RR_BRACKET); }
		public TerminalNode RR_BRACKET(int i) {
			return getToken(RsqlWhereParser.RR_BRACKET, i);
		}
		public MissingOpeningParenthesisContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterMissingOpeningParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitMissingOpeningParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitMissingOpeningParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConditionAndContext extends ConditionContext {
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public TerminalNode AND() { return getToken(RsqlWhereParser.AND, 0); }
		public TerminalNode SEMI() { return getToken(RsqlWhereParser.SEMI, 0); }
		public ConditionAndContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterConditionAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitConditionAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitConditionAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConditionOrContext extends ConditionContext {
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public TerminalNode OR() { return getToken(RsqlWhereParser.OR, 0); }
		public TerminalNode COMMA() { return getToken(RsqlWhereParser.COMMA, 0); }
		public ConditionOrContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterConditionOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitConditionOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitConditionOr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConditionParensContext extends ConditionContext {
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlWhereParser.RR_BRACKET, 0); }
		public ConditionParensContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterConditionParens(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitConditionParens(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitConditionParens(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		return condition(0);
	}

	private ConditionContext condition(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ConditionContext _localctx = new ConditionContext(_ctx, _parentState);
		ConditionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_condition, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				_localctx = new ConditionSingleContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(48);
				singleCondition();
				}
				break;
			case 2:
				{
				_localctx = new ConditionParensContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(49);
				match(LR_BRACKET);
				setState(50);
				condition(0);
				setState(51);
				match(RR_BRACKET);
				}
				break;
			case 3:
				{
				_localctx = new MissingOpeningParenthesisContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(53);
				match(LR_BRACKET);
				setState(54);
				condition(0);
				setState(55);
				match(RR_BRACKET);
				setState(56);
				match(RR_BRACKET);
				 notifyErrorListeners("Missing opening parenthesis"); 
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(72);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(70);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						_localctx = new ConditionAndContext(new ConditionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(61);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(62);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(63);
						condition(5);
						}
						break;
					case 2:
						{
						_localctx = new ConditionOrContext(new ConditionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(64);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(65);
						_la = _input.LA(1);
						if ( !(_la==OR || _la==COMMA) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(66);
						condition(4);
						}
						break;
					case 3:
						{
						_localctx = new MissingOpeningParenthesisContext(new ConditionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(67);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(68);
						match(RR_BRACKET);
						 notifyErrorListeners("Missing opening parenthesis"); 
						}
						break;
					}
					} 
				}
				setState(74);
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
	public static class ErrorConditionContext extends ParserRuleContext {
		public ErrorConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCondition; }
	 
		public ErrorConditionContext() { }
		public void copyFrom(ErrorConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MissingClosingParenthesis2Context extends ErrorConditionContext {
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public MissingClosingParenthesis2Context(ErrorConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterMissingClosingParenthesis2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitMissingClosingParenthesis2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitMissingClosingParenthesis2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorConditionContext errorCondition() throws RecognitionException {
		ErrorConditionContext _localctx = new ErrorConditionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_errorCondition);
		try {
			_localctx = new MissingClosingParenthesis2Context(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(LR_BRACKET);
			setState(76);
			condition(0);
			 notifyErrorListeners("Missing closing parenthesis"); 
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
	public static class InListContext extends ParserRuleContext {
		public List<InListElementContext> inListElement() {
			return getRuleContexts(InListElementContext.class);
		}
		public InListElementContext inListElement(int i) {
			return getRuleContext(InListElementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RsqlWhereParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RsqlWhereParser.COMMA, i);
		}
		public InListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterInList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitInList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitInList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InListContext inList() throws RecognitionException {
		InListContext _localctx = new InListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_inList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(79);
			inListElement();
			}
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(80);
				match(COMMA);
				setState(81);
				inListElement();
				}
				}
				setState(86);
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
	public static class InListElementContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(RsqlWhereParser.STRING_LITERAL, 0); }
		public TerminalNode DATE_LITERAL() { return getToken(RsqlWhereParser.DATE_LITERAL, 0); }
		public TerminalNode DATETIME_LITERAL() { return getToken(RsqlWhereParser.DATETIME_LITERAL, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(RsqlWhereParser.DECIMAL_LITERAL, 0); }
		public TerminalNode REAL_LITERAL() { return getToken(RsqlWhereParser.REAL_LITERAL, 0); }
		public TerminalNode ENUM_LITERAL() { return getToken(RsqlWhereParser.ENUM_LITERAL, 0); }
		public TerminalNode PARAM_LITERAL() { return getToken(RsqlWhereParser.PARAM_LITERAL, 0); }
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public InListElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inListElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterInListElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitInListElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitInListElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InListElementContext inListElement() throws RecognitionException {
		InListElementContext _localctx = new InListElementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_inListElement);
		try {
			setState(95);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(87);
				match(STRING_LITERAL);
				}
				break;
			case DATE_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				match(DATE_LITERAL);
				}
				break;
			case DATETIME_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(89);
				match(DATETIME_LITERAL);
				}
				break;
			case DECIMAL_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(90);
				match(DECIMAL_LITERAL);
				}
				break;
			case REAL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(91);
				match(REAL_LITERAL);
				}
				break;
			case ENUM_LITERAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(92);
				match(ENUM_LITERAL);
				}
				break;
			case PARAM_LITERAL:
				enterOuterAlt(_localctx, 7);
				{
				setState(93);
				match(PARAM_LITERAL);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 8);
				{
				setState(94);
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
	public static class SingleConditionContext extends ParserRuleContext {
		public SingleConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleCondition; }
	 
		public SingleConditionContext() { }
		public void copyFrom(SingleConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionOtherFieldContext extends SingleConditionContext {
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public SingleConditionOtherFieldContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionOtherField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionOtherField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionOtherField(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionParamContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public TerminalNode PARAM_LITERAL() { return getToken(RsqlWhereParser.PARAM_LITERAL, 0); }
		public SingleConditionParamContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionParam(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionParam(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionDateContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public TerminalNode DATE_LITERAL() { return getToken(RsqlWhereParser.DATE_LITERAL, 0); }
		public SingleConditionDateContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionDate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionDate(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionNotInContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorNINContext operatorNIN() {
			return getRuleContext(OperatorNINContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public InListContext inList() {
			return getRuleContext(InListContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlWhereParser.RR_BRACKET, 0); }
		public SingleConditionNotInContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionNotIn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionNotIn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionNotIn(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionStringContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public TerminalNode STRING_LITERAL() { return getToken(RsqlWhereParser.STRING_LITERAL, 0); }
		public SingleConditionStringContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionString(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionDecimalContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public TerminalNode DECIMAL_LITERAL() { return getToken(RsqlWhereParser.DECIMAL_LITERAL, 0); }
		public SingleConditionDecimalContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionDecimal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionDecimal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionDecimal(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionTrueContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode TRUE() { return getToken(RsqlWhereParser.TRUE, 0); }
		public SingleConditionTrueContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionTrue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionTrue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionTrue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionBetweenContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorBTContext operatorBT() {
			return getRuleContext(OperatorBTContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public List<InListElementContext> inListElement() {
			return getRuleContexts(InListElementContext.class);
		}
		public InListElementContext inListElement(int i) {
			return getRuleContext(InListElementContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(RsqlWhereParser.COMMA, 0); }
		public TerminalNode RR_BRACKET() { return getToken(RsqlWhereParser.RR_BRACKET, 0); }
		public SingleConditionBetweenContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionBetween(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionBetween(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionBetween(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionNullContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode NULL() { return getToken(RsqlWhereParser.NULL, 0); }
		public SingleConditionNullContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionNull(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionNull(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionNull(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionEnumContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode ENUM_LITERAL() { return getToken(RsqlWhereParser.ENUM_LITERAL, 0); }
		public SingleConditionEnumContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionEnum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionEnum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionEnum(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionInContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorINContext operatorIN() {
			return getRuleContext(OperatorINContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public InListContext inList() {
			return getRuleContext(InListContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlWhereParser.RR_BRACKET, 0); }
		public SingleConditionInContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionIn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionIn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionIn(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionNotBetweenContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorNBTContext operatorNBT() {
			return getRuleContext(OperatorNBTContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(RsqlWhereParser.LR_BRACKET, 0); }
		public List<InListElementContext> inListElement() {
			return getRuleContexts(InListElementContext.class);
		}
		public InListElementContext inListElement(int i) {
			return getRuleContext(InListElementContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(RsqlWhereParser.COMMA, 0); }
		public TerminalNode RR_BRACKET() { return getToken(RsqlWhereParser.RR_BRACKET, 0); }
		public SingleConditionNotBetweenContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionNotBetween(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionNotBetween(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionNotBetween(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionDatetimeContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public TerminalNode DATETIME_LITERAL() { return getToken(RsqlWhereParser.DATETIME_LITERAL, 0); }
		public SingleConditionDatetimeContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionDatetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionDatetime(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionDatetime(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionFalseContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorBasicContext operatorBasic() {
			return getRuleContext(OperatorBasicContext.class,0);
		}
		public TerminalNode FALSE() { return getToken(RsqlWhereParser.FALSE, 0); }
		public SingleConditionFalseContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionFalse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionFalse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionFalse(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleConditionRealContext extends SingleConditionContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public OperatorContext operator() {
			return getRuleContext(OperatorContext.class,0);
		}
		public TerminalNode REAL_LITERAL() { return getToken(RsqlWhereParser.REAL_LITERAL, 0); }
		public SingleConditionRealContext(SingleConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterSingleConditionReal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitSingleConditionReal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitSingleConditionReal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleConditionContext singleCondition() throws RecognitionException {
		SingleConditionContext _localctx = new SingleConditionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_singleCondition);
		try {
			setState(169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				_localctx = new SingleConditionBetweenContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				field();
				setState(98);
				operatorBT();
				setState(99);
				match(LR_BRACKET);
				setState(100);
				inListElement();
				setState(101);
				match(COMMA);
				setState(102);
				inListElement();
				setState(103);
				match(RR_BRACKET);
				}
				break;
			case 2:
				_localctx = new SingleConditionNotBetweenContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(105);
				field();
				setState(106);
				operatorNBT();
				setState(107);
				match(LR_BRACKET);
				setState(108);
				inListElement();
				setState(109);
				match(COMMA);
				setState(110);
				inListElement();
				setState(111);
				match(RR_BRACKET);
				}
				break;
			case 3:
				_localctx = new SingleConditionInContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(113);
				field();
				setState(114);
				operatorIN();
				setState(115);
				match(LR_BRACKET);
				setState(116);
				inList();
				setState(117);
				match(RR_BRACKET);
				}
				break;
			case 4:
				_localctx = new SingleConditionNotInContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(119);
				field();
				setState(120);
				operatorNIN();
				setState(121);
				match(LR_BRACKET);
				setState(122);
				inList();
				setState(123);
				match(RR_BRACKET);
				}
				break;
			case 5:
				_localctx = new SingleConditionStringContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(125);
				field();
				setState(126);
				operator();
				setState(127);
				match(STRING_LITERAL);
				}
				break;
			case 6:
				_localctx = new SingleConditionDateContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(129);
				field();
				setState(130);
				operator();
				setState(131);
				match(DATE_LITERAL);
				}
				break;
			case 7:
				_localctx = new SingleConditionDatetimeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(133);
				field();
				setState(134);
				operator();
				setState(135);
				match(DATETIME_LITERAL);
				}
				break;
			case 8:
				_localctx = new SingleConditionDecimalContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(137);
				field();
				setState(138);
				operator();
				setState(139);
				match(DECIMAL_LITERAL);
				}
				break;
			case 9:
				_localctx = new SingleConditionRealContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(141);
				field();
				setState(142);
				operator();
				setState(143);
				match(REAL_LITERAL);
				}
				break;
			case 10:
				_localctx = new SingleConditionOtherFieldContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(145);
				field();
				setState(146);
				operator();
				setState(147);
				field();
				}
				break;
			case 11:
				_localctx = new SingleConditionParamContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(149);
				field();
				setState(150);
				operator();
				setState(151);
				match(PARAM_LITERAL);
				}
				break;
			case 12:
				_localctx = new SingleConditionEnumContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(153);
				field();
				setState(154);
				operatorBasic();
				setState(155);
				match(ENUM_LITERAL);
				}
				break;
			case 13:
				_localctx = new SingleConditionNullContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(157);
				field();
				setState(158);
				operatorBasic();
				setState(159);
				match(NULL);
				}
				break;
			case 14:
				_localctx = new SingleConditionTrueContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(161);
				field();
				setState(162);
				operatorBasic();
				setState(163);
				match(TRUE);
				}
				break;
			case 15:
				_localctx = new SingleConditionFalseContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(165);
				field();
				setState(166);
				operatorBasic();
				setState(167);
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
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorContext operator() throws RecognitionException {
		OperatorContext _localctx = new OperatorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_operator);
		try {
			setState(179);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(171);
				operatorEQ();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(172);
				operatorNEQ();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(173);
				operatorLT();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(174);
				operatorGT();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(175);
				operatorLE();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(176);
				operatorGE();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(177);
				operatorLIKE();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(178);
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
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorBasic(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorBasic(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorBasic(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorBasicContext operatorBasic() throws RecognitionException {
		OperatorBasicContext _localctx = new OperatorBasicContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_operatorBasic);
		try {
			setState(183);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(181);
				operatorEQ();
				}
				break;
			case T__1:
			case T__2:
				enterOuterAlt(_localctx, 2);
				{
				setState(182);
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
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorEQ(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorEQ(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorEQ(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorEQContext operatorEQ() throws RecognitionException {
		OperatorEQContext _localctx = new OperatorEQContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_operatorEQ);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
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
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorNEQ(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorNEQ(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorNEQ(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNEQContext operatorNEQ() throws RecognitionException {
		OperatorNEQContext _localctx = new OperatorNEQContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_operatorNEQ);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
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
		public TerminalNode GT() { return getToken(RsqlWhereParser.GT, 0); }
		public OperatorGTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorGT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorGT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorGT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorGT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorGTContext operatorGT() throws RecognitionException {
		OperatorGTContext _localctx = new OperatorGTContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_operatorGT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(T__3);
			setState(190);
			match(GT);
			setState(191);
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
		public TerminalNode LT() { return getToken(RsqlWhereParser.LT, 0); }
		public OperatorLTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorLT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorLT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorLT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorLT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorLTContext operatorLT() throws RecognitionException {
		OperatorLTContext _localctx = new OperatorLTContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_operatorLT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(T__3);
			setState(194);
			match(LT);
			setState(195);
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
		public TerminalNode GE() { return getToken(RsqlWhereParser.GE, 0); }
		public OperatorGEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorGE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorGE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorGE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorGE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorGEContext operatorGE() throws RecognitionException {
		OperatorGEContext _localctx = new OperatorGEContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_operatorGE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(T__3);
			setState(198);
			match(GE);
			setState(199);
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
		public TerminalNode LE() { return getToken(RsqlWhereParser.LE, 0); }
		public OperatorLEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorLE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorLE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorLE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorLE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorLEContext operatorLE() throws RecognitionException {
		OperatorLEContext _localctx = new OperatorLEContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_operatorLE);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			match(T__3);
			setState(202);
			match(LE);
			setState(203);
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
		public TerminalNode LIKE() { return getToken(RsqlWhereParser.LIKE, 0); }
		public OperatorLIKEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorLIKE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorLIKE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorLIKE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorLIKE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorLIKEContext operatorLIKE() throws RecognitionException {
		OperatorLIKEContext _localctx = new OperatorLIKEContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_operatorLIKE);
		try {
			setState(209);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(205);
				match(T__4);
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 2);
				{
				setState(206);
				match(T__3);
				setState(207);
				match(LIKE);
				setState(208);
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
		public TerminalNode NLIKE() { return getToken(RsqlWhereParser.NLIKE, 0); }
		public OperatorNLIKEContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNLIKE; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorNLIKE(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorNLIKE(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorNLIKE(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNLIKEContext operatorNLIKE() throws RecognitionException {
		OperatorNLIKEContext _localctx = new OperatorNLIKEContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_operatorNLIKE);
		try {
			setState(215);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
				enterOuterAlt(_localctx, 1);
				{
				setState(211);
				match(T__5);
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 2);
				{
				setState(212);
				match(T__3);
				setState(213);
				match(NLIKE);
				setState(214);
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
		public TerminalNode IN() { return getToken(RsqlWhereParser.IN, 0); }
		public OperatorINContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorIN; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorIN(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorIN(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorIN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorINContext operatorIN() throws RecognitionException {
		OperatorINContext _localctx = new OperatorINContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_operatorIN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			match(T__3);
			setState(218);
			match(IN);
			setState(219);
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
		public TerminalNode NIN() { return getToken(RsqlWhereParser.NIN, 0); }
		public OperatorNINContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNIN; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorNIN(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorNIN(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorNIN(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNINContext operatorNIN() throws RecognitionException {
		OperatorNINContext _localctx = new OperatorNINContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_operatorNIN);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			match(T__3);
			setState(222);
			match(NIN);
			setState(223);
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
		public TerminalNode NBT() { return getToken(RsqlWhereParser.NBT, 0); }
		public OperatorNBTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorNBT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorNBT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorNBT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorNBT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorNBTContext operatorNBT() throws RecognitionException {
		OperatorNBTContext _localctx = new OperatorNBTContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_operatorNBT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(225);
			match(T__3);
			setState(226);
			match(NBT);
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
	public static class OperatorBTContext extends ParserRuleContext {
		public TerminalNode BT() { return getToken(RsqlWhereParser.BT, 0); }
		public OperatorBTContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operatorBT; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterOperatorBT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitOperatorBT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitOperatorBT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperatorBTContext operatorBT() throws RecognitionException {
		OperatorBTContext _localctx = new OperatorBTContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_operatorBT);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(T__3);
			setState(230);
			match(BT);
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
	public static class FieldContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RsqlWhereParser.ID, 0); }
		public List<TerminalNode> DOT_ID() { return getTokens(RsqlWhereParser.DOT_ID); }
		public TerminalNode DOT_ID(int i) {
			return getToken(RsqlWhereParser.DOT_ID, i);
		}
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlWhereListener ) ((RsqlWhereListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlWhereVisitor ) return ((RsqlWhereVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_field);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			match(ID);
			setState(237);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(234);
					match(DOT_ID);
					}
					} 
				}
				setState(239);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
			return condition_sempred((ConditionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean condition_sempred(ConditionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001)\u00f1\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0001\u0000\u0004\u0000"+
		",\b\u0000\u000b\u0000\f\u0000-\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001<\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0005\u0001G\b\u0001\n\u0001\f\u0001J\t\u0001\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0005\u0003S\b\u0003\n\u0003\f\u0003V\t\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0003\u0004`\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u00aa\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0003\u0006\u00b4\b\u0006\u0001\u0007\u0001\u0007\u0003\u0007\u00b8"+
		"\b\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0003\u000e\u00d2\b\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0003\u000f\u00d8\b\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0005\u0014\u00ec\b\u0014\n"+
		"\u0014\f\u0014\u00ef\t\u0014\u0001\u0014\u0000\u0001\u0002\u0015\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(\u0000\u0003\u0002\u0000\u0007\u0007\u001a\u001a\u0002\u0000"+
		"\b\b\u0019\u0019\u0001\u0000\u0002\u0003\u0102\u0000+\u0001\u0000\u0000"+
		"\u0000\u0002;\u0001\u0000\u0000\u0000\u0004K\u0001\u0000\u0000\u0000\u0006"+
		"O\u0001\u0000\u0000\u0000\b_\u0001\u0000\u0000\u0000\n\u00a9\u0001\u0000"+
		"\u0000\u0000\f\u00b3\u0001\u0000\u0000\u0000\u000e\u00b7\u0001\u0000\u0000"+
		"\u0000\u0010\u00b9\u0001\u0000\u0000\u0000\u0012\u00bb\u0001\u0000\u0000"+
		"\u0000\u0014\u00bd\u0001\u0000\u0000\u0000\u0016\u00c1\u0001\u0000\u0000"+
		"\u0000\u0018\u00c5\u0001\u0000\u0000\u0000\u001a\u00c9\u0001\u0000\u0000"+
		"\u0000\u001c\u00d1\u0001\u0000\u0000\u0000\u001e\u00d7\u0001\u0000\u0000"+
		"\u0000 \u00d9\u0001\u0000\u0000\u0000\"\u00dd\u0001\u0000\u0000\u0000"+
		"$\u00e1\u0001\u0000\u0000\u0000&\u00e5\u0001\u0000\u0000\u0000(\u00e9"+
		"\u0001\u0000\u0000\u0000*,\u0003\u0002\u0001\u0000+*\u0001\u0000\u0000"+
		"\u0000,-\u0001\u0000\u0000\u0000-+\u0001\u0000\u0000\u0000-.\u0001\u0000"+
		"\u0000\u0000.\u0001\u0001\u0000\u0000\u0000/0\u0006\u0001\uffff\uffff"+
		"\u00000<\u0003\n\u0005\u000012\u0005\u0017\u0000\u000023\u0003\u0002\u0001"+
		"\u000034\u0005\u0018\u0000\u00004<\u0001\u0000\u0000\u000056\u0005\u0017"+
		"\u0000\u000067\u0003\u0002\u0001\u000078\u0005\u0018\u0000\u000089\u0005"+
		"\u0018\u0000\u00009:\u0006\u0001\uffff\uffff\u0000:<\u0001\u0000\u0000"+
		"\u0000;/\u0001\u0000\u0000\u0000;1\u0001\u0000\u0000\u0000;5\u0001\u0000"+
		"\u0000\u0000<H\u0001\u0000\u0000\u0000=>\n\u0004\u0000\u0000>?\u0007\u0000"+
		"\u0000\u0000?G\u0003\u0002\u0001\u0005@A\n\u0003\u0000\u0000AB\u0007\u0001"+
		"\u0000\u0000BG\u0003\u0002\u0001\u0004CD\n\u0001\u0000\u0000DE\u0005\u0018"+
		"\u0000\u0000EG\u0006\u0001\uffff\uffff\u0000F=\u0001\u0000\u0000\u0000"+
		"F@\u0001\u0000\u0000\u0000FC\u0001\u0000\u0000\u0000GJ\u0001\u0000\u0000"+
		"\u0000HF\u0001\u0000\u0000\u0000HI\u0001\u0000\u0000\u0000I\u0003\u0001"+
		"\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000KL\u0005\u0017\u0000\u0000"+
		"LM\u0003\u0002\u0001\u0000MN\u0006\u0002\uffff\uffff\u0000N\u0005\u0001"+
		"\u0000\u0000\u0000OT\u0003\b\u0004\u0000PQ\u0005\u0019\u0000\u0000QS\u0003"+
		"\b\u0004\u0000RP\u0001\u0000\u0000\u0000SV\u0001\u0000\u0000\u0000TR\u0001"+
		"\u0000\u0000\u0000TU\u0001\u0000\u0000\u0000U\u0007\u0001\u0000\u0000"+
		"\u0000VT\u0001\u0000\u0000\u0000W`\u0005#\u0000\u0000X`\u0005 \u0000\u0000"+
		"Y`\u0005!\u0000\u0000Z`\u0005$\u0000\u0000[`\u0005%\u0000\u0000\\`\u0005"+
		"\"\u0000\u0000]`\u0005\u001f\u0000\u0000^`\u0003(\u0014\u0000_W\u0001"+
		"\u0000\u0000\u0000_X\u0001\u0000\u0000\u0000_Y\u0001\u0000\u0000\u0000"+
		"_Z\u0001\u0000\u0000\u0000_[\u0001\u0000\u0000\u0000_\\\u0001\u0000\u0000"+
		"\u0000_]\u0001\u0000\u0000\u0000_^\u0001\u0000\u0000\u0000`\t\u0001\u0000"+
		"\u0000\u0000ab\u0003(\u0014\u0000bc\u0003&\u0013\u0000cd\u0005\u0017\u0000"+
		"\u0000de\u0003\b\u0004\u0000ef\u0005\u0019\u0000\u0000fg\u0003\b\u0004"+
		"\u0000gh\u0005\u0018\u0000\u0000h\u00aa\u0001\u0000\u0000\u0000ij\u0003"+
		"(\u0014\u0000jk\u0003$\u0012\u0000kl\u0005\u0017\u0000\u0000lm\u0003\b"+
		"\u0004\u0000mn\u0005\u0019\u0000\u0000no\u0003\b\u0004\u0000op\u0005\u0018"+
		"\u0000\u0000p\u00aa\u0001\u0000\u0000\u0000qr\u0003(\u0014\u0000rs\u0003"+
		" \u0010\u0000st\u0005\u0017\u0000\u0000tu\u0003\u0006\u0003\u0000uv\u0005"+
		"\u0018\u0000\u0000v\u00aa\u0001\u0000\u0000\u0000wx\u0003(\u0014\u0000"+
		"xy\u0003\"\u0011\u0000yz\u0005\u0017\u0000\u0000z{\u0003\u0006\u0003\u0000"+
		"{|\u0005\u0018\u0000\u0000|\u00aa\u0001\u0000\u0000\u0000}~\u0003(\u0014"+
		"\u0000~\u007f\u0003\f\u0006\u0000\u007f\u0080\u0005#\u0000\u0000\u0080"+
		"\u00aa\u0001\u0000\u0000\u0000\u0081\u0082\u0003(\u0014\u0000\u0082\u0083"+
		"\u0003\f\u0006\u0000\u0083\u0084\u0005 \u0000\u0000\u0084\u00aa\u0001"+
		"\u0000\u0000\u0000\u0085\u0086\u0003(\u0014\u0000\u0086\u0087\u0003\f"+
		"\u0006\u0000\u0087\u0088\u0005!\u0000\u0000\u0088\u00aa\u0001\u0000\u0000"+
		"\u0000\u0089\u008a\u0003(\u0014\u0000\u008a\u008b\u0003\f\u0006\u0000"+
		"\u008b\u008c\u0005$\u0000\u0000\u008c\u00aa\u0001\u0000\u0000\u0000\u008d"+
		"\u008e\u0003(\u0014\u0000\u008e\u008f\u0003\f\u0006\u0000\u008f\u0090"+
		"\u0005%\u0000\u0000\u0090\u00aa\u0001\u0000\u0000\u0000\u0091\u0092\u0003"+
		"(\u0014\u0000\u0092\u0093\u0003\f\u0006\u0000\u0093\u0094\u0003(\u0014"+
		"\u0000\u0094\u00aa\u0001\u0000\u0000\u0000\u0095\u0096\u0003(\u0014\u0000"+
		"\u0096\u0097\u0003\f\u0006\u0000\u0097\u0098\u0005\u001f\u0000\u0000\u0098"+
		"\u00aa\u0001\u0000\u0000\u0000\u0099\u009a\u0003(\u0014\u0000\u009a\u009b"+
		"\u0003\u000e\u0007\u0000\u009b\u009c\u0005\"\u0000\u0000\u009c\u00aa\u0001"+
		"\u0000\u0000\u0000\u009d\u009e\u0003(\u0014\u0000\u009e\u009f\u0003\u000e"+
		"\u0007\u0000\u009f\u00a0\u0005\t\u0000\u0000\u00a0\u00aa\u0001\u0000\u0000"+
		"\u0000\u00a1\u00a2\u0003(\u0014\u0000\u00a2\u00a3\u0003\u000e\u0007\u0000"+
		"\u00a3\u00a4\u0005\n\u0000\u0000\u00a4\u00aa\u0001\u0000\u0000\u0000\u00a5"+
		"\u00a6\u0003(\u0014\u0000\u00a6\u00a7\u0003\u000e\u0007\u0000\u00a7\u00a8"+
		"\u0005\u000b\u0000\u0000\u00a8\u00aa\u0001\u0000\u0000\u0000\u00a9a\u0001"+
		"\u0000\u0000\u0000\u00a9i\u0001\u0000\u0000\u0000\u00a9q\u0001\u0000\u0000"+
		"\u0000\u00a9w\u0001\u0000\u0000\u0000\u00a9}\u0001\u0000\u0000\u0000\u00a9"+
		"\u0081\u0001\u0000\u0000\u0000\u00a9\u0085\u0001\u0000\u0000\u0000\u00a9"+
		"\u0089\u0001\u0000\u0000\u0000\u00a9\u008d\u0001\u0000\u0000\u0000\u00a9"+
		"\u0091\u0001\u0000\u0000\u0000\u00a9\u0095\u0001\u0000\u0000\u0000\u00a9"+
		"\u0099\u0001\u0000\u0000\u0000\u00a9\u009d\u0001\u0000\u0000\u0000\u00a9"+
		"\u00a1\u0001\u0000\u0000\u0000\u00a9\u00a5\u0001\u0000\u0000\u0000\u00aa"+
		"\u000b\u0001\u0000\u0000\u0000\u00ab\u00b4\u0003\u0010\b\u0000\u00ac\u00b4"+
		"\u0003\u0012\t\u0000\u00ad\u00b4\u0003\u0016\u000b\u0000\u00ae\u00b4\u0003"+
		"\u0014\n\u0000\u00af\u00b4\u0003\u001a\r\u0000\u00b0\u00b4\u0003\u0018"+
		"\f\u0000\u00b1\u00b4\u0003\u001c\u000e\u0000\u00b2\u00b4\u0003\u001e\u000f"+
		"\u0000\u00b3\u00ab\u0001\u0000\u0000\u0000\u00b3\u00ac\u0001\u0000\u0000"+
		"\u0000\u00b3\u00ad\u0001\u0000\u0000\u0000\u00b3\u00ae\u0001\u0000\u0000"+
		"\u0000\u00b3\u00af\u0001\u0000\u0000\u0000\u00b3\u00b0\u0001\u0000\u0000"+
		"\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b2\u0001\u0000\u0000"+
		"\u0000\u00b4\r\u0001\u0000\u0000\u0000\u00b5\u00b8\u0003\u0010\b\u0000"+
		"\u00b6\u00b8\u0003\u0012\t\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b6\u0001\u0000\u0000\u0000\u00b8\u000f\u0001\u0000\u0000\u0000\u00b9"+
		"\u00ba\u0005\u0001\u0000\u0000\u00ba\u0011\u0001\u0000\u0000\u0000\u00bb"+
		"\u00bc\u0007\u0002\u0000\u0000\u00bc\u0013\u0001\u0000\u0000\u0000\u00bd"+
		"\u00be\u0005\u0004\u0000\u0000\u00be\u00bf\u0005\f\u0000\u0000\u00bf\u00c0"+
		"\u0005\u0004\u0000\u0000\u00c0\u0015\u0001\u0000\u0000\u0000\u00c1\u00c2"+
		"\u0005\u0004\u0000\u0000\u00c2\u00c3\u0005\r\u0000\u0000\u00c3\u00c4\u0005"+
		"\u0004\u0000\u0000\u00c4\u0017\u0001\u0000\u0000\u0000\u00c5\u00c6\u0005"+
		"\u0004\u0000\u0000\u00c6\u00c7\u0005\u000e\u0000\u0000\u00c7\u00c8\u0005"+
		"\u0004\u0000\u0000\u00c8\u0019\u0001\u0000\u0000\u0000\u00c9\u00ca\u0005"+
		"\u0004\u0000\u0000\u00ca\u00cb\u0005\u000f\u0000\u0000\u00cb\u00cc\u0005"+
		"\u0004\u0000\u0000\u00cc\u001b\u0001\u0000\u0000\u0000\u00cd\u00d2\u0005"+
		"\u0005\u0000\u0000\u00ce\u00cf\u0005\u0004\u0000\u0000\u00cf\u00d0\u0005"+
		"\u0011\u0000\u0000\u00d0\u00d2\u0005\u0004\u0000\u0000\u00d1\u00cd\u0001"+
		"\u0000\u0000\u0000\u00d1\u00ce\u0001\u0000\u0000\u0000\u00d2\u001d\u0001"+
		"\u0000\u0000\u0000\u00d3\u00d8\u0005\u0006\u0000\u0000\u00d4\u00d5\u0005"+
		"\u0004\u0000\u0000\u00d5\u00d6\u0005\u0010\u0000\u0000\u00d6\u00d8\u0005"+
		"\u0004\u0000\u0000\u00d7\u00d3\u0001\u0000\u0000\u0000\u00d7\u00d4\u0001"+
		"\u0000\u0000\u0000\u00d8\u001f\u0001\u0000\u0000\u0000\u00d9\u00da\u0005"+
		"\u0004\u0000\u0000\u00da\u00db\u0005\u0013\u0000\u0000\u00db\u00dc\u0005"+
		"\u0004\u0000\u0000\u00dc!\u0001\u0000\u0000\u0000\u00dd\u00de\u0005\u0004"+
		"\u0000\u0000\u00de\u00df\u0005\u0012\u0000\u0000\u00df\u00e0\u0005\u0004"+
		"\u0000\u0000\u00e0#\u0001\u0000\u0000\u0000\u00e1\u00e2\u0005\u0004\u0000"+
		"\u0000\u00e2\u00e3\u0005\u0014\u0000\u0000\u00e3\u00e4\u0005\u0004\u0000"+
		"\u0000\u00e4%\u0001\u0000\u0000\u0000\u00e5\u00e6\u0005\u0004\u0000\u0000"+
		"\u00e6\u00e7\u0005\u0015\u0000\u0000\u00e7\u00e8\u0005\u0004\u0000\u0000"+
		"\u00e8\'\u0001\u0000\u0000\u0000\u00e9\u00ed\u0005\'\u0000\u0000\u00ea"+
		"\u00ec\u0005&\u0000\u0000\u00eb\u00ea\u0001\u0000\u0000\u0000\u00ec\u00ef"+
		"\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001\u0000\u0000\u0000\u00ed\u00ee"+
		"\u0001\u0000\u0000\u0000\u00ee)\u0001\u0000\u0000\u0000\u00ef\u00ed\u0001"+
		"\u0000\u0000\u0000\f-;FHT_\u00a9\u00b3\u00b7\u00d1\u00d7\u00ed";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}