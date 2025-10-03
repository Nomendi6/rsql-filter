grammar RsqlSelect;

@header {
package rsql.antlr.select;
}

/** The start rule; begin parsing here. */
select:   selectElements+ ;

selectElements
    : (star='*' | selectElement) (',' selectElement)*
;

selectElement
    : field '.' '*'    # seAll  // all fields from an entity
    | field (COLON simpleField)?  # seField   // as
    | functionCall (COLON simpleField)? # seFuncCall
    ;

COLON: ':';

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

WS: [ \t\r\n]+ -> skip;
