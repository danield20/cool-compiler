lexer grammar CoolLexer;

tokens { ERROR } 

@header{
    package cool.lexer;
}

@members {
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

//key words, now case sensitive

CLASS : C L A S S;
ELSE : E L S E;
FI : F I;
IF : I F;
IN : I N;
INHERITS : I N H E R I T S;
ISVOID : I S V O I D;
LET : L E T;
LOOP : L O O P;
POOL : P O O L;
THEN : T H E N;
WHILE : W H I L E;
CASE : C A S E;
ESAC : E S A C;
NEW : N E W;
OF: O F;
NOT: N O T;

//key words, first letter has to be lowercase

fragment TRUE : 't' R U E;
fragment FALSE : 'f' A L S E;

BOOL : TRUE | FALSE;

//types

fragment SELF_TYPE : 'SELF_TYPE';
TYPE : 'Int' | 'Bool' | 'String' | SELF_TYPE | 'IO' | 'Object' | [A-Z] INSIDEID;

//identificator

fragment LETTER : [a-zA-Z];
fragment DIGIT : [0-9];
fragment DIGITS : DIGIT+;
fragment EXPONENT : 'e' ('+' | '-')? DIGITS;
fragment INSIDEID : (LETTER | '_' | DIGIT)*;

ID : [a-z] INSIDEID;
INT : DIGIT+;
FLOAT : (DIGITS ('.' DIGITS?)? | '.' DIGITS) EXPONENT?;

fragment ESCAPED_NEWLINE : '\\' NEW_LINE;
STRING : '"' (ESCAPED_NEWLINE | .)*? (  '"'
                                        | EOF { raiseError("EOF in string constant"); }
                                        | NEW_LINE { raiseError("Unterminated string constant"); })
    {
        String tokenText = getText();

        if (tokenText.indexOf('\0') != -1) {
            raiseError("String contains null character");
            return;
        }

        if (tokenText.length() >= 1024) {
            raiseError("String constant too long");
            return;
        }

        setText(getText().replace("\\n", "\n"));
        setText(getText().replace("\\t", "\t"));
        setText(getText().replace("\\f", "\f"));
        setText(getText().replace("\\b", "\b"));
        setText(getText().replaceAll("\\\\([\\s\\S])", "$1"));
        setText(getText().replace("\"", ""));
    }
    ;

fragment NEW_LINE : '\r'? '\n';

LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

BLOCK_COMMENT
    : '(*'
      (BLOCK_COMMENT | .)*?
      '*)' -> skip
     ;

UNFINISHED_BLOCK_COMM
    : '(*'
      (BLOCK_COMMENT | .)*?
      (EOF { raiseError("EOF in comment"); } | '*)')
     ;

END_COMM : '*)' { raiseError("Unmatched *)"); };

ASSIGN : '<-';

RARROW : '=>';

POINT : '.';

NEG : '~';

AT : '@';

COLON : ':';

SEMI : ';';

COMMA : ',';

LPAREN : '(';

RPAREN : ')';

LBRACE : '{';

RBRACE : '}';

PLUS : '+';

MINUS : '-';

MULT : '*';

DIV : '/';

EQUAL : '=';

LT : '<';

LE : '<=';

WS
    :   [ \n\f\r\t]+ -> skip
    ;

INVALID : . {raiseError("Invalid character: " + getText());};
