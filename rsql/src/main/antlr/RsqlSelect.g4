grammar RsqlSelect;

import RsqlCommonLexer;

/** The start rule; begin parsing here. */
select:   selectElements+ ;

selectElements
    : (star='*' | selectElement) (',' selectElement)*
;

selectElement
    : field '.' '*'    # seAll  // all fields from an entity
    | field (':' simpleField)?  # seField   // as
    | functionCall (':' simpleField)? # seFuncCall
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

simpleField: ID;
field:  ID(DOT_ID)*
;
