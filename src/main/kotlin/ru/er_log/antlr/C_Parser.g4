parser grammar C_Parser;

options { tokenVocab = C_Lexer; }

primaryExpression
    : file EOF;

/* File document. */
file
    : stuff function*
    ;

/* All other things. */
stuff
    : WORD*
    ;

function
    : WORD WORD BRACKETS BRACE_O body BRACE_C
    ;

body
    : body_elem*
    ;

body_elem
    : variable
    | function_call
    ;

variable
    : WORD WORD
    ;

/* All between '{' and '}'. */
function_call
    : WORD BRACKETS
    ;

