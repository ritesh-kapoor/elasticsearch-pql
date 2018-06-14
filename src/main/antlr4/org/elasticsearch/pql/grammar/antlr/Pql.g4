grammar Pql;
/*
    References : https://github.com/antlr/grammars-v4/blob/master/java/JavaParser.g4
*/
@lexer::header {
import java.util.*;
}
@lexer::members{
    public static Map<Integer,String> TOKEN_MAPPING = new HashMap(){{
        put(T_SEARCH,"search");
        put(T_SOURCE,"source");
        put(STRING_LITERAL,"string literal");
    }};
}



sections
    : sourceStatement
      (T_PIPE searchStatement)*
      (T_PIPE sortStatement)?
      (T_PIPE limitStatement)?
      (T_PIPE evalStatement )*
      (T_PIPE statsStatement (T_PIPE bucketStatement )? )?
      EOF
    ;

sourceStatement
    :
    T_SOURCE stringLiteral
    ;

// For query only
searchStatements
    :
    searchStatement (T_PIPE searchStatement)*
    ;

searchStatement
    :
    T_SEARCH booleanExpression
    ;

sortStatement
    :
    T_SORT sortField ( T_COMMA sortField )*
    ;

sortField
    :
        IDENTIFIER (T_ASC| T_DESC)?
    ;

limitStatement
    :
    T_LIMIT integerLiteral T_COMMA integerLiteral  (T_ASC| T_DESC)? ( T_COMMA IDENTIFIER (T_ASC| T_DESC)? )*
    ;



statsStatement
    :
        op=T_STATS statsFunctions (T_BY statsGroupingColumns)?
    |   op=T_DATEHISTOGRAM statsParameters (T_DO statsFunctions)?  (T_BY statsGroupingColumns)?
    |   op=T_HISTOGRAM statsParameters (T_DO statsFunctions)?  (T_BY statsGroupingColumns)?
    ;

evalStatement
    :
    T_EVAL (IDENTIFIER T_EQ stringLiteral) (T_COMMA IDENTIFIER T_EQ stringLiteral)*
    ;

bucketStatement
    :
    T_BUCKET (op=T_STATS statsFunctions)?
    ((T_SELECT booleanExpression)? (T_PIPE T_SELECT booleanExpression)*)
    ;

statsGroupingColumns
    :
    (IDENTIFIER) (T_COMMA IDENTIFIER)*
    ;

statsParameters
    :
    (IDENTIFIER T_EQ literal) (T_COMMA IDENTIFIER T_EQ literal)*
    ;

statsFunctions
    :
    (statsFunction) (T_COMMA statsFunction)*
    ;
statsFunction
    :
    (columnName=IDENTIFIER T_EQ methodName=IDENTIFIER T_LP fieldName=IDENTIFIER T_RP)
    ;

booleanExpression
    : primaryBooleanExpression
    | op=(T_NOT|T_EXCL) booleanExpression
    | op=T_REGEX identifier=IDENTIFIER T_EQ literal
    | op=T_WILDCARD identifier=IDENTIFIER T_EQ literal
    | identifier=IDENTIFIER op=(T_LTE | T_GTE | T_LT | T_GT) literal
    | identifier=IDENTIFIER op=(T_EQ | T_EQ2 | T_EQ3 | T_NEQ) literal
    | booleanExpression op=(T_AMP2 | T_AND) booleanExpression
    | booleanExpression op=(T_PIPE2 | T_OR) booleanExpression
    ;

primaryBooleanExpression
    : T_LP booleanExpression T_RP
    ;

literal
    : integerLiteral
    | floatLiteral
    | stringLiteral
    | BOOL_LITERAL
    | NULL_LITERAL
    ;

integerLiteral
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | OCT_LITERAL
    | BINARY_LITERAL
    ;

floatLiteral
    : FLOAT_LITERAL
    | HEX_FLOAT_LITERAL
    ;

stringLiteral
    : CHAR_LITERAL
    | STRING_LITERAL
    ;

// keywords
T_SEARCH:           S E A R C H;
T_EVAL:             E V A L;
T_SOURCE:           S O U R C E;
T_REGEX:            R E G E X;
T_WILDCARD:         W I L D C A R D;
T_DATEHISTOGRAM:    D A T E H I S T O G R A M;
T_HISTOGRAM:        H I S T O G R A M;
T_STATS:            S T A T S;
T_BUCKET:           B U C K E T;
T_SELECT:           S E L E C T;
T_BY:               B Y;
T_DO:               D O;
T_NOT:              N O T;
T_AND:              A N D;
T_OR:               O R;
T_SORT:             S O R T;
T_ASC:              A S C;
T_DESC:             D E S C;
T_LIMIT:            L I M I T;

// operators
T_PIPE:         '|';
T_PIPE2:        '||';
T_LP:           '(';
T_RP:           ')';
T_AMP2:         '&&';
T_EQ:           '=';
T_EQ2:          '==';
T_EQ3:          '===';
T_NEQ:          '!=';
T_LT:           '<';
T_LTE:          '<=';
T_GT:           '>';
T_GTE:          '>=';
T_EXCL:         '!';
T_COMMA:        ',';


// Identifiers
IDENTIFIER:         Letter LetterOrDigit*;

// Whitespace and comments
WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);


// Literals

DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
HEX_LITERAL:        '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?;
OCT_LITERAL:        '0' '_'* [0-7] ([0-7_]* [0-7])? [lL]?;
BINARY_LITERAL:     '0' [bB] [01] ([01_]* [01])? [lL]?;

FLOAT_LITERAL:      (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]?
             |       Digits (ExponentPart [fFdD]? | [fFdD])
             ;

HEX_FLOAT_LITERAL:  '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [fFdD]?;

BOOL_LITERAL:       'true'
            |       'false'
            ;

CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence)* '\'';
STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';

NULL_LITERAL:       'null';




// Fragment rules

fragment ExponentPart
    : [eE] [+-]? Digits
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;
fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;
fragment HexDigit
    : [0-9a-fA-F]
    ;
fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;
fragment LetterOrDigit
    : Letter
    | [0-9]
    ;
fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

fragment A : ('a'|'A') ;
fragment B : ('b'|'B') ;
fragment C : ('c'|'C') ;
fragment D : ('d'|'D') ;
fragment E : ('e'|'E') ;
fragment F : ('f'|'F') ;
fragment G : ('g'|'G') ;
fragment H : ('h'|'H') ;
fragment I : ('i'|'I') ;
fragment J : ('j'|'J') ;
fragment K : ('k'|'K') ;
fragment L : ('l'|'L') ;
fragment M : ('m'|'M') ;
fragment N : ('n'|'N') ;
fragment O : ('o'|'O') ;
fragment P : ('p'|'P') ;
fragment Q : ('q'|'Q') ;
fragment R : ('r'|'R') ;
fragment S : ('s'|'S') ;
fragment T : ('t'|'T') ;
fragment U : ('u'|'U') ;
fragment V : ('v'|'V') ;
fragment W : ('w'|'W') ;
fragment X : ('x'|'X') ;
fragment Y : ('y'|'Y') ;
fragment Z : ('z'|'Z') ;




expression
    : primary
    | methodCall
    | prefix=('+'|'-'|'++'|'--') expression
    | prefix='!' expression
    | expression bop=('*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=('==' | '!=') expression
    | expression bop=('&&' | T_AND) expression
    | expression bop=('||' | T_OR) expression
    ;

expressionList
    : expression (',' expression)*
    ;

methodCall
    : IDENTIFIER '(' expressionList? ')'
    ;

primary
    : '(' expression ')'
    | literal
    ;
