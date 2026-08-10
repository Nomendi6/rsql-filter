grammar RsqlSelect;

@header {
package rsql.antlr.select;
}

/*
 * The start rule.
 *
 * NOTE: no '+' here, deliberately. Up to 0.6.20 this read `selectElements+`, which let a second group of
 * elements begin at any position. That had two effects, both bad:
 *   - "code name" parsed as though the comma were there, because the visitors iterate every selectElements
 *     and accumulate;
 *   - since selectElements may start with '*', and '*' is also the multiplication operator, the parser had
 *     to decide at every '*' whether the current expression continued or a new group began. That decision
 *     needs lookahead over the whole expression, so "a+b*c" repeated 200 times took about 14 seconds.
 * A missing separator is now caught by SelectTreeParser.verifyWholeInputWasUsed.
 */
select:   selectElements ;

selectElements
    : (star='*' | selectElement) (',' selectElement)*
;

selectElement
    : field '.' '*'                     # seAll        // all fields from an entity
    | expression (COLON simpleField)?   # seExpression // arithmetic expression with optional alias (MUST be before seField/seFuncCall)
    | field (COLON simpleField)?        # seField      // simple field with optional alias (backward compatibility)
    | functionCall (COLON simpleField)? # seFuncCall   // function with optional alias (backward compatibility)
    ;

COLON: ':';

// Expression rules for arithmetic operations
// Precedence: multiplication/division > addition/subtraction
expression
    : '(' expression ')'                          # parenExpression
    | expression op=('*' | '/') expression        # mulDivExpression
    | expression op=('+' | '-') expression        # addSubExpression
    | functionCall                                # funcExpression
    | field                                       # fieldExpression
    | NUMBER                                      # numberExpression
    ;

functionCall
    : aggregateFunction
;

aggregateFunction
    : (AVG | MAX | MIN | SUM | GRP)  '(' aggregator=(ALL | DIST)? functionArg ')' # funcCall
        | COUNT '(' (starArg='*' | aggregator=ALL? functionArg) ')' # countAll
        | COUNT '(' aggregator=DIST functionArgs ')'                # countDist
;

functionArgs
    : (
        functionArg
      )
         (
            ',' functionArg
         )*
;

functionArg
    : field
    | functionCall
;

AVG: A V G;
MAX: M A X;
MIN: M I N;
SUM: S U M;
ALL: A L L;
DIST: D I S T;
COUNT: C O U N T;
GRP: G R P;

simpleField: ID | AVG | MAX | MIN | SUM | ALL | DIST | COUNT | GRP;
field:  ID(DOT_ID)*
;

// Lexer rules
DOT_ID: '.' ID_LITERAL;
ID: ID_LITERAL;

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

fragment ID_LITERAL: [a-zA-Z_$][0-9a-zA-Z_$]*;

NUMBER: [0-9]+ ('.' [0-9]+)?;

WS: [ \t\r\n]+ -> skip;
