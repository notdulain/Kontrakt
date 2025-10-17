package ast;

import java.util.*;

public class Test {
    private String name;
    private List<Statement> statements;
    
    public Test(String name, List<Statement> statements) {
        this.name = name;
        this.statements = statements;
    }
    
    public String getName() { return name; }
    public List<Statement> getStatements() { return statements; }
}