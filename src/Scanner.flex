import java_cup.runtime.*;

%%

//JFlex Directives
%class KontraktScanner
%unicode
%cup
%line
%column

%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

//Macros
NUMBER = [0-9]+
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*
STRING = \"([^\"\n\r\\]|\\\"|\\\\)*\"

%%

//Lexical rules
<YYINITIAL> {
    "config" {return symbol(sym.CONFIG);}
    "base_url" {return symbol(sym.BASE_URL);}
    "test" {return symbol(sym.TEST);}
    "expect" {return symbol(sym.EXPECT);}
    "status" {return symbol(sym.STATUS);}
    "in" { return symbol(sym.IN); }
    ".." { return symbol(sym.RANGE); }
    "body" {return symbol(sym.BODY);}
    "header" {return symbol(sym.HEADER);}
    "contains" {return symbol(sym.CONTAINS);}
    "let" {return symbol(sym.LET);}
    
    "=" {return symbol(sym.EQUALS);}
    "{" {return symbol(sym.LBRACE);}
    "}" {return symbol(sym.RBRACE);}
    ";" {return symbol(sym.SEMICOLON);}

    "GET" {return symbol(sym.GET);}
    "POST" {return symbol(sym.POST);}
    "PUT" {return symbol(sym.PUT);}
    "DELETE" {return symbol(sym.DELETE);}

    {NUMBER} {return symbol(sym.NUMBER, yytext());}
    {IDENTIFIER} {return symbol(sym.IDENTIFIER, yytext());}
    {STRING} {
                // Remove quotes AND unescape
                String val = yytext().substring(1, yytext().length()-1);
                
                // Unescape common sequences
                val = val.replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t");
                
                return symbol(sym.STRING, val);
              }

    [ \t\n] { /* ignore whitespace */ }
    "//" [^\n]* { /* ignore comments */ }
}