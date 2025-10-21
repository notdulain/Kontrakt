package ast;

import java.util.*;

public class Request implements Statement {
    private HttpMethod method;
    private String path;
    private Map<String, String> headers;
    private String body;
    
    public Request(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
        this.body = null;
    }
    
    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
    
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Request(" + method + " " + path + 
               (body != null ? " with body" : "") + ")";
    }
}
