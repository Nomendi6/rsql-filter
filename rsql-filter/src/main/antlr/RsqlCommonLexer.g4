lexer grammar RsqlCommonLexer;

// NOTE: the package is set with the -package argument of antlr4-maven-plugin, NOT with @header.
// An @header here would be inherited by the grammars that import this lexer (RsqlWhere, RsqlHaving),
// producing a second package declaration in their generated sources.

DOT:                                 '.';
LR_BRACKET:                          '(';
RR_BRACKET:                          ')';
COMMA:                               ',';
SEMI:                                ';';
AT_SIGN:                             '@';
SINGLE_QUOTE_SYMB:                   '\'';
DOUBLE_QUOTE_SYMB:                   '"';
REVERSE_QUOTE_SYMB:                  '`';
//PLUS_SIGN:                           '+';
//MINUS_SIGN:                          '-';
//STAR_SIGN:                           '*';
//SLASH_SIGN:                          '/';


PARAM_LITERAL: ':' ID_LITERAL;

DATE_LITERAL: '#' DEC_DIGIT+ '-' DEC_DIGIT+ '-' DEC_DIGIT+ '#'
            | '#' DEC_DIGIT+ '#'
;
// The zone is optional: a literal without one names calendar fields, for LocalDateTime and LocalDate
// attributes; compared with an attribute that holds a moment it is rejected when the filter is compiled.
DATETIME_LITERAL:
    '#' DEC_DIGIT+ '-' DEC_DIGIT+ '-' DEC_DIGIT+ 'T' DEC_DIGIT+ ':' DEC_DIGIT+ ':' DEC_DIGIT+ ZONE_SUFFIX? '#'
    | '#' DEC_DIGIT+ 'T' DEC_DIGIT+ ':' DEC_DIGIT+ ':' DEC_DIGIT+ ZONE_SUFFIX? '#'
    | '#' DEC_DIGIT+ '-' DEC_DIGIT+ '-' DEC_DIGIT+ 'T' DEC_DIGIT+ ':' DEC_DIGIT+ ':' DEC_DIGIT+ '.' DEC_DIGIT+ ZONE_SUFFIX? '#'
    | '#' DEC_DIGIT+ 'T' DEC_DIGIT+ ':' DEC_DIGIT+ ':' DEC_DIGIT+ '.' DEC_DIGIT+ ZONE_SUFFIX? '#'

    ;

fragment ZONE_SUFFIX: 'Z' | ('+'|'-') DEC_DIGIT+ ':' DEC_DIGIT+;

ENUM_LITERAL:
    '#' ID_LITERAL '#'
;


STRING_LITERAL:  DQUOTA_STRING | SQUOTA_STRING | BQUOTA_STRING;
//UNSIGNED_DECIMAL_LITERAL: DEC_DIGIT+;
DECIMAL_LITERAL: ('-')? DEC_DIGIT+;
//DECIMAL_LITERAL: ('-')? UNSIGNED_DECIMAL_LITERAL;
REAL_LITERAL:    ('-')? (DEC_DIGIT+)? '.' DEC_DIGIT+
                 | ('-')? DEC_DIGIT+ '.' EXPONENT_NUM_PART
                 | ('-')? (DEC_DIGIT+)? '.' (DEC_DIGIT+ EXPONENT_NUM_PART)
                 | ('-')? DEC_DIGIT+ EXPONENT_NUM_PART;
//UNSIGNED_REAL_LITERAL:    (DEC_DIGIT+)? '.' DEC_DIGIT+
//                 | DEC_DIGIT+ '.' EXPONENT_NUM_PART
//                 | (DEC_DIGIT+)? '.' (DEC_DIGIT+ EXPONENT_NUM_PART)
//                 | DEC_DIGIT+ EXPONENT_NUM_PART;
//REAL_LITERAL:    ('-')? UNSIGNED_REAL_LITERAL;


DOT_ID: '.' ID_LITERAL;
ID: ID_LITERAL;

fragment A : [aA]; // match either an 'a' or 'A'
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

fragment ID_LITERAL  :   [a-zA-Z_$][0-9a-zA-Z_$]* ;      // match identifiers <label id="code.tour.expr.3"/>
fragment EXPONENT_NUM_PART:          'E' [-+]? DEC_DIGIT+;
fragment DEC_DIGIT :   [0-9] ;
// The only escape mechanism is doubling the delimiter. A backslash is an ordinary character:
// it does not protect the next one, so a value may end with a backslash. Encoding is therefore total -
// any value can be written as delimiter + value.replace(delimiter, delimiter+delimiter) + delimiter.
fragment DQUOTA_STRING: '"' ( '""' | ~'"' )* '"';
fragment SQUOTA_STRING: '\'' ( '\'\'' | ~'\'' )* '\'';
fragment BQUOTA_STRING: '`' ( '``' | ~'`' )* '`';
NEWLINE:'\r'? '\n' ;     // return newlines to parser (is end-statement signal)
WS  :   [ \t]+ -> skip ; // toss out whitespace
