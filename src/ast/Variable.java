package ast;

public class Variable {
    private String name;
    private String value;  // Could be a string or number as a string
    
    public Variable(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() { return name; }
    public String getValue() { return value; }
}