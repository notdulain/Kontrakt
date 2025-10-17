package ast;

import java.util.*;

public class Program {
    private Config config;
    private List<Variable> variables;
    private List<Test> tests;
    
    public Program(Config config, List<Variable> variables, List<Test> tests) {
        this.config = config;
        this.variables = variables != null ? variables : new ArrayList<>();
        this.tests = tests != null ? tests : new ArrayList<>();
    }
    
    public Config getConfig() { return config; }
    public List<Variable> getVariables() { return variables; }
    public List<Test> getTests() { return tests; }
    
    //Helper: Find variable value by name
    public String getVariableValue(String name) {
        for (Variable v : variables) {
            if (v.getName().equals(name)) {
                return v.getValue();
            }
        }
        return null;
    }
}