grammar RsqlWhere;

import RsqlCommonLexer;

/** The start rule; begin parsing here. */
where:   condition+ ;

condition:
        singleCondition # conditionSingle
    | '(' condition ')'        # conditionParens
    | condition (AND | SEMI) condition  # conditionAnd
    | condition (OR | COMMA) condition   # conditionOr
    | '(' condition ')' ')' { notifyErrorListeners("Missing opening parenthesis"); } # missingOpeningParenthesis
    | condition ')' { notifyErrorListeners("Missing opening parenthesis"); }        # missingOpeningParenthesis
;

errorCondition:
      '(' condition         { notifyErrorListeners("Missing closing parenthesis"); }       # missingClosingParenthesis2
;


inList
    : (inListElement) (',' inListElement)*
;


inListElement
    : STRING_LITERAL
    | DATE_LITERAL
    | DATETIME_LITERAL
    | DECIMAL_LITERAL
    | REAL_LITERAL
    | ENUM_LITERAL
    | PARAM_LITERAL
    | field
;



singleCondition    :
        field operatorBT LR_BRACKET inListElement COMMA inListElement RR_BRACKET # singleConditionBetween
    |   field operatorNBT LR_BRACKET inListElement COMMA inListElement RR_BRACKET # singleConditionNotBetween
    |   field operatorIN LR_BRACKET inList RR_BRACKET  # singleConditionIn
    |   field operatorNIN LR_BRACKET inList RR_BRACKET  # singleConditionNotIn
    |   field operator STRING_LITERAL  # singleConditionString
    |   field operator DATE_LITERAL    # singleConditionDate
    |   field operator DATETIME_LITERAL    # singleConditionDatetime
    |   field operator DECIMAL_LITERAL # singleConditionDecimal
    |   field operator REAL_LITERAL    # singleConditionReal
    |   field operator field # singleConditionOtherField
    |   field operator PARAM_LITERAL    # singleConditionParam
    |   field operatorBasic ENUM_LITERAL    # singleConditionEnum
    |   field operatorBasic NULL    # singleConditionNull
    |   field operatorBasic TRUE    # singleConditionTrue
    |   field operatorBasic FALSE    # singleConditionFalse
//    |   field operator algebraicExpression # singleConditionAlgebraicExpression
    ;

/*
algebraicExpression
    : '-' algebraicExpression # algebraicExpressionNegative
    | '(' algebraicExpression ')' # algebraicExpressionParens
    | '(' algebraicExpression ')' ')' { notifyErrorListeners("Missing opening parenthesis"); } # algebraicExpressionMissingParens
    | '(' algebraicExpression  { notifyErrorListeners("Missing closing parenthesis"); } # algebraicExpressionMissingParens
    | algebraicExpression mulop algebraicExpression # algebraicExpressionAdd
    | algebraicExpression addop algebraicExpression # algebraicExpressionAdd
    | DECIMAL_LITERAL # algebraicExpressionDecimal
    | REAL_LITERAL # algebraicExpressionReal
    | PARAM_LITERAL # algebraicExpressionParam
    | field # algebraicExpressionField
;
*/


AND: A N D ;
OR: O R ;
NULL: N U L L;
TRUE: T R U E;
FALSE: F A L S E;

operator
        :     operatorEQ
            | operatorNEQ
            | operatorLT
            | operatorGT
            | operatorLE
            | operatorGE
            | operatorLIKE
            | operatorNLIKE
            ;

operatorBasic
        :  operatorEQ
        | operatorNEQ
;

operatorEQ: '==';
operatorNEQ: '=!' | '!=';
operatorGT: '=' GT '=';
operatorLT: '=' LT '=';
operatorGE: '=' GE '=';
operatorLE: '=' LE '=';
operatorLIKE: '=*' | '=' LIKE '=';
operatorNLIKE: '=!*' | '!=*' | '=' NLIKE '=';
operatorIN: '=' IN '=';
operatorNIN: '=' NIN '=';
operatorNBT: '=' NBT '=';
operatorBT: '=' BT '=';

GT: G T;
LT: L T;
GE: G E;
LE: L E;
NLIKE : N L I K E;
LIKE : L I K E;
NIN : N I N;
IN: I N;
NBT : N B T;
BT: B T;

field:  ID(DOT_ID)*;


//addop : '+' | '-';
//mulop : '*' | '/' | '%' ;


