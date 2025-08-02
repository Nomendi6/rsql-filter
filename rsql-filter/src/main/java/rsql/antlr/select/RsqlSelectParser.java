// Generated from /home/vrba/v/rsql-filter/nomendi6/rsql-filter-mvn/rsql/src/main/antlr/RsqlSelect.g4 by ANTLR 4.13.2
package rsql.antlr.select;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class RsqlSelectParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, AVG=3, MAX=4, MIN=5, SUM=6, ALL=7, DIST=8, COUNT=9, GRP=10, 
		DOT=11, LR_BRACKET=12, RR_BRACKET=13, COMMA=14, SEMI=15, AT_SIGN=16, SINGLE_QUOTE_SYMB=17, 
		DOUBLE_QUOTE_SYMB=18, REVERSE_QUOTE_SYMB=19, PARAM_LITERAL=20, DATE_LITERAL=21, 
		DATETIME_LITERAL=22, ENUM_LITERAL=23, STRING_LITERAL=24, DECIMAL_LITERAL=25, 
		REAL_LITERAL=26, DOT_ID=27, ID=28, NEWLINE=29, WS=30;
	public static final int
		RULE_select = 0, RULE_selectElements = 1, RULE_selectElement = 2, RULE_functionCall = 3, 
		RULE_aggregateFunction = 4, RULE_functionArgs = 5, RULE_functionArg = 6, 
		RULE_simpleField = 7, RULE_field = 8;
	private static String[] makeRuleNames() {
		return new String[] {
			"select", "selectElements", "selectElement", "functionCall", "aggregateFunction", 
			"functionArgs", "functionArg", "simpleField", "field"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'*'", "':'", null, null, null, null, null, null, null, null, "'.'", 
			"'('", "')'", "','", "';'", "'@'", "'''", "'\"'", "'`'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "AVG", "MAX", "MIN", "SUM", "ALL", "DIST", "COUNT", 
			"GRP", "DOT", "LR_BRACKET", "RR_BRACKET", "COMMA", "SEMI", "AT_SIGN", 
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
	public String getGrammarFileName() { return "RsqlSelect.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RsqlSelectParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectContext extends ParserRuleContext {
		public List<SelectElementsContext> selectElements() {
			return getRuleContexts(SelectElementsContext.class);
		}
		public SelectElementsContext selectElements(int i) {
			return getRuleContext(SelectElementsContext.class,i);
		}
		public SelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterSelect(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitSelect(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitSelect(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectContext select() throws RecognitionException {
		SelectContext _localctx = new SelectContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_select);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(18);
				selectElements();
				}
				}
				setState(21); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 268437114L) != 0) );
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
	public static class SelectElementsContext extends ParserRuleContext {
		public Token star;
		public List<SelectElementContext> selectElement() {
			return getRuleContexts(SelectElementContext.class);
		}
		public SelectElementContext selectElement(int i) {
			return getRuleContext(SelectElementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RsqlSelectParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RsqlSelectParser.COMMA, i);
		}
		public SelectElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectElements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterSelectElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitSelectElements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitSelectElements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectElementsContext selectElements() throws RecognitionException {
		SelectElementsContext _localctx = new SelectElementsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_selectElements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				{
				setState(23);
				((SelectElementsContext)_localctx).star = match(T__0);
				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case GRP:
			case ID:
				{
				setState(24);
				selectElement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(31);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(27);
				match(COMMA);
				setState(28);
				selectElement();
				}
				}
				setState(33);
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
	public static class SelectElementContext extends ParserRuleContext {
		public SelectElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectElement; }
	 
		public SelectElementContext() { }
		public void copyFrom(SelectElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SeFuncCallContext extends SelectElementContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public SimpleFieldContext simpleField() {
			return getRuleContext(SimpleFieldContext.class,0);
		}
		public SeFuncCallContext(SelectElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterSeFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitSeFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitSeFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SeFieldContext extends SelectElementContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public SimpleFieldContext simpleField() {
			return getRuleContext(SimpleFieldContext.class,0);
		}
		public SeFieldContext(SelectElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterSeField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitSeField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitSeField(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SeAllContext extends SelectElementContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public TerminalNode DOT() { return getToken(RsqlSelectParser.DOT, 0); }
		public SeAllContext(SelectElementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterSeAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitSeAll(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitSeAll(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectElementContext selectElement() throws RecognitionException {
		SelectElementContext _localctx = new SelectElementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_selectElement);
		int _la;
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				_localctx = new SeAllContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(34);
				field();
				setState(35);
				match(DOT);
				setState(36);
				match(T__0);
				}
				break;
			case 2:
				_localctx = new SeFieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(38);
				field();
				setState(41);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(39);
					match(T__1);
					setState(40);
					simpleField();
					}
				}

				}
				break;
			case 3:
				_localctx = new SeFuncCallContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(43);
				functionCall();
				setState(46);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(44);
					match(T__1);
					setState(45);
					simpleField();
					}
				}

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
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_functionCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
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
		public TerminalNode COUNT() { return getToken(RsqlSelectParser.COUNT, 0); }
		public TerminalNode LR_BRACKET() { return getToken(RsqlSelectParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(RsqlSelectParser.RR_BRACKET, 0); }
		public FunctionArgContext functionArg() {
			return getRuleContext(FunctionArgContext.class,0);
		}
		public TerminalNode ALL() { return getToken(RsqlSelectParser.ALL, 0); }
		public CountAllContext(AggregateFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterCountAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitCountAll(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitCountAll(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncCallContext extends AggregateFunctionContext {
		public Token aggregator;
		public TerminalNode LR_BRACKET() { return getToken(RsqlSelectParser.LR_BRACKET, 0); }
		public FunctionArgContext functionArg() {
			return getRuleContext(FunctionArgContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlSelectParser.RR_BRACKET, 0); }
		public TerminalNode AVG() { return getToken(RsqlSelectParser.AVG, 0); }
		public TerminalNode MAX() { return getToken(RsqlSelectParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(RsqlSelectParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(RsqlSelectParser.SUM, 0); }
		public TerminalNode GRP() { return getToken(RsqlSelectParser.GRP, 0); }
		public TerminalNode ALL() { return getToken(RsqlSelectParser.ALL, 0); }
		public TerminalNode DIST() { return getToken(RsqlSelectParser.DIST, 0); }
		public FuncCallContext(AggregateFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountDistContext extends AggregateFunctionContext {
		public Token aggregator;
		public TerminalNode COUNT() { return getToken(RsqlSelectParser.COUNT, 0); }
		public TerminalNode LR_BRACKET() { return getToken(RsqlSelectParser.LR_BRACKET, 0); }
		public FunctionArgsContext functionArgs() {
			return getRuleContext(FunctionArgsContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(RsqlSelectParser.RR_BRACKET, 0); }
		public TerminalNode DIST() { return getToken(RsqlSelectParser.DIST, 0); }
		public CountDistContext(AggregateFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterCountDist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitCountDist(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitCountDist(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateFunctionContext aggregateFunction() throws RecognitionException {
		AggregateFunctionContext _localctx = new AggregateFunctionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_aggregateFunction);
		int _la;
		try {
			setState(76);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				_localctx = new FuncCallContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(52);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1144L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(53);
				match(LR_BRACKET);
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DIST) {
					{
					setState(54);
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

				setState(57);
				functionArg();
				setState(58);
				match(RR_BRACKET);
				}
				break;
			case 2:
				_localctx = new CountAllContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(60);
				match(COUNT);
				setState(61);
				match(LR_BRACKET);
				setState(67);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__0:
					{
					setState(62);
					((CountAllContext)_localctx).starArg = match(T__0);
					}
					break;
				case AVG:
				case MAX:
				case MIN:
				case SUM:
				case ALL:
				case COUNT:
				case GRP:
				case ID:
					{
					setState(64);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==ALL) {
						{
						setState(63);
						((CountAllContext)_localctx).aggregator = match(ALL);
						}
					}

					setState(66);
					functionArg();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(69);
				match(RR_BRACKET);
				}
				break;
			case 3:
				_localctx = new CountDistContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(70);
				match(COUNT);
				setState(71);
				match(LR_BRACKET);
				setState(72);
				((CountDistContext)_localctx).aggregator = match(DIST);
				setState(73);
				functionArgs();
				setState(74);
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
		public List<TerminalNode> COMMA() { return getTokens(RsqlSelectParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RsqlSelectParser.COMMA, i);
		}
		public FunctionArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterFunctionArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitFunctionArgs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitFunctionArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgsContext functionArgs() throws RecognitionException {
		FunctionArgsContext _localctx = new FunctionArgsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(78);
			functionArg();
			}
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(79);
				match(COMMA);
				setState(80);
				functionArg();
				}
				}
				setState(85);
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
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterFunctionArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitFunctionArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitFunctionArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgContext functionArg() throws RecognitionException {
		FunctionArgContext _localctx = new FunctionArgContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_functionArg);
		try {
			setState(88);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				field();
				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case GRP:
				enterOuterAlt(_localctx, 2);
				{
				setState(87);
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
	public static class SimpleFieldContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(RsqlSelectParser.ID, 0); }
		public SimpleFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterSimpleField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitSimpleField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitSimpleField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleFieldContext simpleField() throws RecognitionException {
		SimpleFieldContext _localctx = new SimpleFieldContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_simpleField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			match(ID);
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
		public TerminalNode ID() { return getToken(RsqlSelectParser.ID, 0); }
		public List<TerminalNode> DOT_ID() { return getTokens(RsqlSelectParser.DOT_ID); }
		public TerminalNode DOT_ID(int i) {
			return getToken(RsqlSelectParser.DOT_ID, i);
		}
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RsqlSelectListener ) ((RsqlSelectListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RsqlSelectVisitor ) return ((RsqlSelectVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_field);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			match(ID);
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT_ID) {
				{
				{
				setState(93);
				match(DOT_ID);
				}
				}
				setState(98);
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

	public static final String _serializedATN =
		"\u0004\u0001\u001ed\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0001\u0000\u0004\u0000\u0014\b\u0000\u000b\u0000\f\u0000\u0015"+
		"\u0001\u0001\u0001\u0001\u0003\u0001\u001a\b\u0001\u0001\u0001\u0001\u0001"+
		"\u0005\u0001\u001e\b\u0001\n\u0001\f\u0001!\t\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003"+
		"\u0002*\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002/\b\u0002"+
		"\u0003\u00021\b\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0003\u00048\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004A\b\u0004"+
		"\u0001\u0004\u0003\u0004D\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004M\b\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005R\b\u0005\n\u0005\f\u0005"+
		"U\t\u0005\u0001\u0006\u0001\u0006\u0003\u0006Y\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0005\b_\b\b\n\b\f\bb\t\b\u0001\b\u0000\u0000\t"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0000\u0002\u0002\u0000\u0003"+
		"\u0006\n\n\u0001\u0000\u0007\bi\u0000\u0013\u0001\u0000\u0000\u0000\u0002"+
		"\u0019\u0001\u0000\u0000\u0000\u00040\u0001\u0000\u0000\u0000\u00062\u0001"+
		"\u0000\u0000\u0000\bL\u0001\u0000\u0000\u0000\nN\u0001\u0000\u0000\u0000"+
		"\fX\u0001\u0000\u0000\u0000\u000eZ\u0001\u0000\u0000\u0000\u0010\\\u0001"+
		"\u0000\u0000\u0000\u0012\u0014\u0003\u0002\u0001\u0000\u0013\u0012\u0001"+
		"\u0000\u0000\u0000\u0014\u0015\u0001\u0000\u0000\u0000\u0015\u0013\u0001"+
		"\u0000\u0000\u0000\u0015\u0016\u0001\u0000\u0000\u0000\u0016\u0001\u0001"+
		"\u0000\u0000\u0000\u0017\u001a\u0005\u0001\u0000\u0000\u0018\u001a\u0003"+
		"\u0004\u0002\u0000\u0019\u0017\u0001\u0000\u0000\u0000\u0019\u0018\u0001"+
		"\u0000\u0000\u0000\u001a\u001f\u0001\u0000\u0000\u0000\u001b\u001c\u0005"+
		"\u000e\u0000\u0000\u001c\u001e\u0003\u0004\u0002\u0000\u001d\u001b\u0001"+
		"\u0000\u0000\u0000\u001e!\u0001\u0000\u0000\u0000\u001f\u001d\u0001\u0000"+
		"\u0000\u0000\u001f \u0001\u0000\u0000\u0000 \u0003\u0001\u0000\u0000\u0000"+
		"!\u001f\u0001\u0000\u0000\u0000\"#\u0003\u0010\b\u0000#$\u0005\u000b\u0000"+
		"\u0000$%\u0005\u0001\u0000\u0000%1\u0001\u0000\u0000\u0000&)\u0003\u0010"+
		"\b\u0000\'(\u0005\u0002\u0000\u0000(*\u0003\u000e\u0007\u0000)\'\u0001"+
		"\u0000\u0000\u0000)*\u0001\u0000\u0000\u0000*1\u0001\u0000\u0000\u0000"+
		"+.\u0003\u0006\u0003\u0000,-\u0005\u0002\u0000\u0000-/\u0003\u000e\u0007"+
		"\u0000.,\u0001\u0000\u0000\u0000./\u0001\u0000\u0000\u0000/1\u0001\u0000"+
		"\u0000\u00000\"\u0001\u0000\u0000\u00000&\u0001\u0000\u0000\u00000+\u0001"+
		"\u0000\u0000\u00001\u0005\u0001\u0000\u0000\u000023\u0003\b\u0004\u0000"+
		"3\u0007\u0001\u0000\u0000\u000045\u0007\u0000\u0000\u000057\u0005\f\u0000"+
		"\u000068\u0007\u0001\u0000\u000076\u0001\u0000\u0000\u000078\u0001\u0000"+
		"\u0000\u000089\u0001\u0000\u0000\u00009:\u0003\f\u0006\u0000:;\u0005\r"+
		"\u0000\u0000;M\u0001\u0000\u0000\u0000<=\u0005\t\u0000\u0000=C\u0005\f"+
		"\u0000\u0000>D\u0005\u0001\u0000\u0000?A\u0005\u0007\u0000\u0000@?\u0001"+
		"\u0000\u0000\u0000@A\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000"+
		"BD\u0003\f\u0006\u0000C>\u0001\u0000\u0000\u0000C@\u0001\u0000\u0000\u0000"+
		"DE\u0001\u0000\u0000\u0000EM\u0005\r\u0000\u0000FG\u0005\t\u0000\u0000"+
		"GH\u0005\f\u0000\u0000HI\u0005\b\u0000\u0000IJ\u0003\n\u0005\u0000JK\u0005"+
		"\r\u0000\u0000KM\u0001\u0000\u0000\u0000L4\u0001\u0000\u0000\u0000L<\u0001"+
		"\u0000\u0000\u0000LF\u0001\u0000\u0000\u0000M\t\u0001\u0000\u0000\u0000"+
		"NS\u0003\f\u0006\u0000OP\u0005\u000e\u0000\u0000PR\u0003\f\u0006\u0000"+
		"QO\u0001\u0000\u0000\u0000RU\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000"+
		"\u0000ST\u0001\u0000\u0000\u0000T\u000b\u0001\u0000\u0000\u0000US\u0001"+
		"\u0000\u0000\u0000VY\u0003\u0010\b\u0000WY\u0003\u0006\u0003\u0000XV\u0001"+
		"\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000Y\r\u0001\u0000\u0000\u0000"+
		"Z[\u0005\u001c\u0000\u0000[\u000f\u0001\u0000\u0000\u0000\\`\u0005\u001c"+
		"\u0000\u0000]_\u0005\u001b\u0000\u0000^]\u0001\u0000\u0000\u0000_b\u0001"+
		"\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000"+
		"a\u0011\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000\r\u0015\u0019"+
		"\u001f).07@CLSX`";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}