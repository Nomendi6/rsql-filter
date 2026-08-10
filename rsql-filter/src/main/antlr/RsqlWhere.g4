grammar RsqlWhere;

import RsqlCommonLexer;

@header {
package rsql.antlr.where;
}

/** The start rule; begin parsing here. */
where:   condition+ ;

/*
 * NOTE: this rule deliberately has NO error alternatives for a stray ')'.
 *
 * Up to 0.6.20 it carried two of them, both emitting "Missing opening parenthesis":
 *     | '(' condition ')' ')' { ... }
 *     | condition ')'         { ... }
 * They made every ')' ambiguous - it could either close a conditionParens or start the tail of
 * missingOpeningParenthesis - so the adaptive prediction explored 2^n paths on n parentheses, and
 * paid that price on VALID input: "((((...a==1...))))" with 26 levels (56 characters) took ~13 s.
 * They also rejected valid filters: a trailing newline after a grouping ')' made the inner
 * condition swallow the ')', leaving the outer '(' condition ')' unclosed - so "(a==1)\n" failed
 * with "Missing opening parenthesis" even though no parenthesis was missing.
 *
 * A stray ')' is now caught by RsqlWhereTreeParser.verifyWholeInputWasUsed instead, which reports
 * the position as well. Do not reintroduce them.
 */
condition:
        singleCondition # conditionSingle
    | '(' condition ')'        # conditionParens
    | condition (AND | SEMI) condition  # conditionAnd
    | condition (OR | COMMA) condition   # conditionOr
;

/*
 * NOTE: there used to be an `errorCondition` rule here, emitting "Missing closing parenthesis".
 * No rule ever invoked it, so that message was never produced - which is why the assertion for it in
 * CompilerWhereTextIT was commented out. Removed in 0.6.21.
 *
 * An unclosed parenthesis is reported by CustomErrorStrategy instead.
 */


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
            | operatorCLIKE
            | operatorCNLIKE
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
operatorCLIKE: '=^*' | '=' CLIKE '=';
operatorCNLIKE: '=!^*' | '!=^*' | '=' CNLIKE '=';
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
CNLIKE : C N L I K E;
CLIKE : C L I K E;
NIN : N I N;
IN: I N;
NBT : N B T;
BT: B T;

field:  ID(DOT_ID)*;


//addop : '+' | '-';
//mulop : '*' | '/' | '%' ;


