package ast;

import java.util.*;

public class Config {
    private String baseUrl;
    private Map<String, String> defaultHeaders;
    
    public Config() {
        this.defaultHeaders = new HashMap<>();
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public void addHeader(String key, String value) {
        this.defaultHeaders.put(key, value);
    }
    
    public String getBaseUrl() { return baseUrl; }
    public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
}