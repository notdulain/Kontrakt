%%
%class KontraktScanner
%cup

TEST = "test"

GET = "GET"
POST = "POST"
PUT = "PUT"
DELETE = "DELETE"

EXPECT = "expect"

NUMBER = [0-9]+
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*
STRING = \"[^\"]*\"
%%

{TEST} {return new Token(TokenType.TEST, yytext());}

{GET} {return new Token(TokenType.GET, yytext());}
{POST} {return new Token(TokenType.POST, yytext());}
{PUT} {return new Token(TokenType.PUT, yytext());}
{DELETE} {return new Token(TokenType.DELETE, yytext());}

{EXPECT} {return new Token(TokenType.EXPECT, yytext());}

{NUMBER} {return new Token(TokenType.NUMBER, yytext());}
{IDENTIFIER} {return new Token(TokenType.IDENTIFIER, yytext());}
{STRING} {return new Token(TokenType.STRING, yytext().substring(1, yytext().length()-1));}

[ \t\n] { /* ignore whitespace */ }