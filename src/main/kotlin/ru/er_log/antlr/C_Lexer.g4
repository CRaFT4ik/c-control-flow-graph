lexer grammar C_Lexer;

fragment SP             : [ \t]+;
fragment WP             : [ \t\r\n]+;
fragment NewLines       : [\r\n]+;
fragment NOTNewLine     : ~[\r\n];
fragment ANY            : .*?;

SingleLineComment   : [/]{2} NOTNewLine -> skip;
MultiLineComment    : '/*' ANY '*/' -> skip;

fragment BRACKET_O      : WP* '(' WP*;
fragment BRACKET_C      : WP* ')' WP*;
BRACKETS                : BRACKET_O ANY BRACKET_C;

BRACE_O                 : WP* '{' WP*;
BRACE_C                 : WP* '}' WP*;

//SOMETHING               : WP* ~[ ]+ WP*;
WORD                    : WP* [a-zA-Z_][a-zA-Z0-9]* WP*;

//VAR                 : WORD WP WORD ANY ';';
//FUN                 : WORD WP WORD WP* '(' ANY ')' WP* '{' ANY '}';

OTHER : .{1} -> skip;//[; \t\r\n] -> skip;
//fragment NAME    : Word;

