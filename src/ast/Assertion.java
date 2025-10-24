package ast;

public class Assertion implements Statement {
    public enum Type {
        STATUS,              //expect status=200
        STATUS_RANGE,        //expect status in 200..299
        HEADER_EQUALS,       //expect header "Content-Type" = "application/json"
        HEADER_CONTAINS,     //expect header "Content-Type" contains "application/json"
        BODY_CONTAINS        //expect body contains "text"
    }
    
    private Type type;
    private Integer statusCode;      //for STATUS
    private Integer minStatus; // for range lower bound
    private Integer maxStatus; // for range upper bound
    private String headerName;       //for HEADER_*
    private String expectedValue;    //for equals/contains
    
    //Constructor for STATUS assertion
    public static Assertion status(int code) {
        Assertion a = new Assertion();
        a.type = Type.STATUS;
        a.statusCode = code;
        return a;
    }

    public static Assertion statusRange(int min, int max) {
        Assertion a = new Assertion();
        a.type = Type.STATUS_RANGE;
        a.minStatus = min;
        a.maxStatus = max;
        return a;
    }
    
    //Constructor for HEADER_EQUALS
    public static Assertion headerEquals(String name, String value) {
        Assertion a = new Assertion();
        a.type = Type.HEADER_EQUALS;
        a.headerName = name;
        a.expectedValue = value;
        return a;
    }
    
    //Constructor for HEADER_CONTAINS
    public static Assertion headerContains(String name, String substring) {
        Assertion a = new Assertion();
        a.type = Type.HEADER_CONTAINS;
        a.headerName = name;
        a.expectedValue = substring;
        return a;
    }
    
    //Constructor for BODY_CONTAINS
    public static Assertion bodyContains(String substring) {
        Assertion a = new Assertion();
        a.type = Type.BODY_CONTAINS;
        a.expectedValue = substring;
        return a;
    }
    
    //Private constructor - use only the factory methods in here
    private Assertion() {}
    
    //Getters
    public Type getType() { return type; }
    public Integer getStatusCode() { return statusCode; }
    public Integer getMinStatus() { return minStatus; }
    public Integer getMaxStatus() { return maxStatus; }
    public String getHeaderName() { return headerName; }
    public String getExpectedValue() { return expectedValue; }
}