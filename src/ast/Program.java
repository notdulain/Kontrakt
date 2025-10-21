package ast;

import java.util.*;

public class Program {
    private Config config;
    private List<Variable> variables;
    private List<Test> tests;

    public Program() {
        this.config = null;
        this.variables = new ArrayList<>();
        this.tests = new ArrayList<>();
    }
    
    public Program(Config config, List<Variable> variables, List<Test> tests) {
        this.config = config;
        this.variables = variables != null ? variables : new ArrayList<>();
        this.tests = tests != null ? tests : new ArrayList<>();
    }

    public void setConfig(Config config) {
        this.config = config;
    }
    
    public void addVariable(Variable var) {
        this.variables.add(var);
    }
    
    public void addTest(Test test) {
        this.tests.add(test);
    }

    //getters
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

    @Override
    public String toString() {
        return "Program(config=" + (config != null ? "present" : "null") + 
               ", vars=" + variables.size() + 
               ", tests=" + tests.size() + ")";
    }
}
