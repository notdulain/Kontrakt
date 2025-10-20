import java_cup.runtime.*;

//helper methods
%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

%%

//JFlex Directives
%class KontraktScanner
%unicode
%cup
%line
%column

//Macros
NUMBER = [0-9]+
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*
STRING = \"(\\.|[^"\\])*\"
%%

//Lexical rules
<YYINITIAL> {
    "config" {return symbol(sym.CONFIG);}
    "test" {return symbol(sym.TEST);}
    "expect" {return symbol(sym.EXPECT);}

    "GET" {return symbol(sym.GET);}
    "POST" {return symbol(sym.POST);}
    "PUT" {return symbol(sym.PUT);}
    "DELETE" {return symbol(sym.DELETE);}

    {NUMBER} {return symbol(sym.NUMBER, yytext());}
    {IDENTIFIER} {return symbol(sym.IDENTIFIER, yytext());}
    {STRING} {
                // This is how you pass the *value* (without quotes)
                String val = yytext().substring(1, yytext().length()-1);
                return symbol(sym.STRING, val);
            }

    [ \t\n] { /* ignore whitespace */ }
    "//" [^\n]* { /* ignore comments */ }
}