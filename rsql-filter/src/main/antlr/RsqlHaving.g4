grammar RsqlHaving;

import RsqlCommonLexer;

@header {
package rsql.antlr.having;
}

/**
 * Entry point: HAVING clause can contain one or more conditions
 * Example: "totalPrice=gt=1000"
 * Example: "totalPrice=gt=1000;productCount=ge=3"
 */
having: havingCondition+;

/**
 * Recursive rule for logical conditions with precedence:
 * 1. Parentheses have highest precedence
 * 2. AND (;) has higher precedence than OR (,)
 * 3. Left associativity
 */
havingCondition:
        singleHavingCondition                               # havingConditionSingle
    |   '(' havingCondition ')'                            # havingConditionParens
    |   havingCondition (AND | SEMI) havingCondition       # havingConditionAnd
    |   havingCondition (OR | COMMA) havingCondition       # havingConditionOr
    ;

/**
 * Single HAVING condition - different types of comparisons
 */
singleHavingCondition:
        // BETWEEN: avgPrice BETWEEN 50 AND 150
        havingExpression operatorBT LR_BRACKET literal COMMA literal RR_BRACKET
        # havingConditionBetween

        // NOT BETWEEN: count(id) NOT BETWEEN 1 AND 5
    |   havingExpression operatorNBT LR_BRACKET literal COMMA literal RR_BRACKET
        # havingConditionNotBetween

        // IN: count(id) IN (1, 2, 3, 5)
    |   havingExpression operatorIN LR_BRACKET literalList RR_BRACKET
        # havingConditionIn

        // NOT IN: sum(price) NOT IN (100, 200, 300)
    |   havingExpression operatorNIN LR_BRACKET literalList RR_BRACKET
        # havingConditionNotIn

        // Comparison of two expressions: SUM(debit) > SUM(credit)
    |   havingExpression operator havingExpression
        # havingConditionComparison

        // Comparison with literal: totalPrice > 1000
    |   havingExpression operator literal
        # havingConditionLiteral

        // NULL comparison: count(description) == NULL
    |   havingExpression operatorBasic NULL
        # havingConditionNull

        // Boolean comparison: someFlag == TRUE
    |   havingExpression operatorBasic TRUE
        # havingConditionTrue

    |   havingExpression operatorBasic FALSE
        # havingConditionFalse
    ;

/**
 * List of literal values for IN/NOT IN operators
 */
literalList:
        literal (COMMA literal)*
    ;

/**
 * HAVING expression can be:
 * 1. Aggregate function: SUM(price), COUNT(*), AVG(quantity)
 * 2. Field (alias or GROUP BY field): totalPrice, productType.name
 */
havingExpression:
        functionCall                                       # havingExprFunctionCall
    |   field                                              # havingExprField
    ;

/**
 * Function call - wrapper for aggregate functions
 * NOTE: Uses the same structure as RsqlSelect.g4 for consistency
 */
functionCall:
        aggregateFunction
    ;

/**
 * Aggregate functions that can be used in HAVING clause
 * NOTE: Identical syntax as in RsqlSelect.g4
 */
aggregateFunction:
        (AVG | MAX | MIN | SUM | GRP) LR_BRACKET aggregator=(ALL | DIST)? functionArg RR_BRACKET   # funcCall
    |   COUNT LR_BRACKET (starArg=STAR | aggregator=ALL? functionArg) RR_BRACKET                   # countAll
    |   COUNT LR_BRACKET aggregator=DIST functionArgs RR_BRACKET                                   # countDist
    ;

/**
 * Function arguments - list of arguments for COUNT(DIST field1, field2, ...)
 */
functionArgs:
        functionArg (COMMA functionArg)*
    ;

/**
 * Function argument - can be a field or nested function call
 * NOTE: Nested function calls are not currently supported, but syntax allows future extension
 */
functionArg:
        field
    |   functionCall
    ;

/**
 * Literal values that can be used in comparison
 */
literal:
        STRING_LITERAL      // 'Electronics', "Product A"
    |   DECIMAL_LITERAL     // 100, 1000
    |   REAL_LITERAL        // 50.5, 99.99
    |   DATE_LITERAL        // #2024-01-01#
    |   DATETIME_LITERAL    // #2024-01-01T10:30:00#
    |   TRUE                // TRUE
    |   FALSE               // FALSE
    ;

// ==================== KEYWORDS ====================

AND: A N D;
OR: O R;
NULL: N U L L;
TRUE: T R U E;
FALSE: F A L S E;
SUM: S U M;
AVG: A V G;
COUNT: C O U N T;
MIN: M I N;
MAX: M A X;
GRP: G R P;
ALL: A L L;
DIST: D I S T;  // Short version of DISTINCT (compatible with SELECT)
STAR: '*';      // For COUNT(*)

// ==================== OPERATORS ====================

operator:
        operatorEQ
    |   operatorNEQ
    |   operatorLT
    |   operatorGT
    |   operatorLE
    |   operatorGE
    |   operatorLIKE
    |   operatorNLIKE
    ;

operatorBasic:
        operatorEQ
    |   operatorNEQ
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
operatorBT: '=' BT '=';
operatorNBT: '=' NBT '=';

GT: G T;
LT: L T;
GE: G E;
LE: L E;
LIKE: L I K E;
NLIKE: N L I K E;
IN: I N;
NIN: N I N;
BT: B T;
NBT: N B T;

// ==================== FIELD PATH ====================

/**
 * Field path - can be:
 * 1. Alias from SELECT: "totalPrice", "type"
 * 2. GROUP BY field: "productType.name"
 * 3. Field inside aggregate function: SUM("price"), COUNT("id")
 */
field: ID(DOT_ID)*;
