parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

//@header{
//    package cool.parser;
//}

program
    : (classG SEMI)+
    ;

classG
    : CLASS className = TYPE (INHERITS inheritType = TYPE)? LBRACE (feature SEMI)* RBRACE
    ;

feature
    : ID LPAREN (formal (COMMA formal)*)? RPAREN COLON TYPE LBRACE body=expr RBRACE     #funcFeature
    | ID COLON TYPE (ASSIGN value=expr)?                                                #varFeature
    ;

formal
    : ID COLON TYPE
    ;

case_rule
    : ID COLON TYPE RARROW expr SEMI
    ;

let_decl
    : ID COLON TYPE (ASSIGN expr)?
    ;

expr
    : caller=expr (AT TYPE)? POINT ID LPAREN (expr (COMMA expr)*)? RPAREN               # complexcall
    | ID LPAREN (expr (COMMA expr)*)? RPAREN                                            # call
    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI	                        # if
    | WHILE cond=expr LOOP block=expr POOL                                              # while
    | LBRACE (expr SEMI)+ RBRACE                                                        # block
    | LET let_decl (COMMA let_decl)* IN expr                                            # let
    | CASE value_case=expr OF case_rule+ ESAC                                           # case
    | NEW TYPE                                                                          # instantiate
    | ISVOID value=expr                                                                 # isvoid
    | LPAREN value=expr RPAREN                                                          # parenthesized
    | NEG value=expr                                                                    # neg
    | left=expr (MULT | DIV) right=expr                                                 # mulDiv
    | left=expr (PLUS | MINUS) right=expr                                               # addSub
    | left=expr (LT | LE | EQUAL) right=expr                                            # comparison
    | NOT value=expr                                                                    # not
    | ID ASSIGN value=expr                                                              # assignment
    | ID                                                                                # id
    | INT                                                                               # int
    | STRING                                                                            # string
    | BOOL                                                                              # bool
    ;