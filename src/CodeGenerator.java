import ast.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CodeGenerator {
    private StringBuilder output = new StringBuilder();
    private Program program;

    //This map will be CRITICAL for Step 2
    private Map<String, String> variables = new HashMap<>();
    // Tracks whether we've declared the normalized body variable for the
    // current response in the current test method
    private boolean bodyNormDeclaredForCurrentResp = false;

    // Main entry point
    public String generate(Program program){
        this.program = program;

        // 1. Visit variables first to populate the map
        visit(program.getVariables());

        // 2. Build the class "shell"
        buildClassShell();

        // 3. Build the @Test methods
        for (Test test : program.getTests()) {
            visit(test);
        }

        // 4. Close the class
        output.append("}\n");

        return output.toString();
    }

    // --- Visitor Methods for each AST node ---

    private void visit(List<Variable> vars) {
        for (Variable var : vars) {
            // Store variables for later substitution
            variables.put(var.getName(), var.getValue());
        }
    }

    private void buildClassShell() {
        // Append all the imports (java.net.http.*, org.junit.jupiter.*, etc.)
        output.append("import org.junit.jupiter.api.*;\n");
        output.append("import static org.junit.jupiter.api.Assertions.*;\n");
        output.append("import java.net.http.*;\n");
        output.append("import java.net.*;\n");
        output.append("import java.time.Duration;\n");
        output.append("import java.nio.charset.StandardCharsets;\n");
        output.append("import java.util.*;\n\n");

        // Class definition
        output.append("public class GeneratedTests {\n");

        // Static fields from the spec
        output.append("  static String BASE = \"\";\n"); // Default
        output.append("  static Map<String,String> DEFAULT_HEADERS = new HashMap<>();\n");
        output.append("  static HttpClient client;\n\n");

        // @BeforeAll setup method
        output.append("  @BeforeAll\n");
        output.append("  static void setup() {\n");
        output.append("    client = HttpClient.newBuilder()\n");
        output.append("      .version(HttpClient.Version.HTTP_1_1)\n");
        output.append("      .connectTimeout(Duration.ofSeconds(5))\n");
        output.append("      .build();\n");

        // NOW, use your Config AST!
        if (program.getConfig() != null) {
            Config cfg = program.getConfig();
            if (cfg.getBaseUrl() != null) {
                // Escape the URL string for inclusion in Java code
                output.append("    BASE = \"" + escapeJava(cfg.getBaseUrl()) + "\";\n");
            }
            for (Map.Entry<String, String> header : cfg.getDefaultHeaders().entrySet()) {
                output.append("    DEFAULT_HEADERS.put(\"" + escapeJava(header.getKey()) + "\", \"" + escapeJava(header.getValue()) + "\");\n");
            }
        }

        output.append("  }\n\n");
    }

    //visitor method for Test
    private void visit(Test test) {
        // Generate a new @Test method
        output.append("  @Test\n");
        output.append("  void test_" + test.getName() + "() throws Exception {\n");
        bodyNormDeclaredForCurrentResp = false;

        // Visit all statements (requests and assertions) inside this test
        for (Statement stmt : test.getStatements()) {
            if (stmt instanceof Request) {
                visit((Request) stmt);
            } else if (stmt instanceof Assertion) {
                visit((Assertion) stmt);
            }
        }

        output.append("  }\n\n");
    }

    //visitor method for Request
    private void visit(Request req) {
        // This is where we translate our Request AST node into HttpClient code
        // *the most complex part

        // Handle variable substitution first!
        String path = substitute(req.getPath());

        // Note: The spec example shows BASE + path.
        // You must decide if the path is absolute or relative.
        // A simple check:
        String url;
        if (path.startsWith("/")) {
            url = "BASE + \"" + escapeJava(path) + "\"";
        } else {
            url = "\"" + escapeJava(path) + "\""; // Assumes it's a full URL
        }

        output.append("    System.out.println(\"--> " + req.getMethod() + " \" + " + url + ");\n");
        output.append("    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(" + url + "))\n");
        output.append("      .timeout(Duration.ofSeconds(10))\n");

        // Handle method
        switch (req.getMethod()) {
            case GET:
                output.append("      .GET();\n");
                break;
            case POST:
                String body = substitute(req.getBody());
                output.append("      .POST(HttpRequest.BodyPublishers.ofString(\"" + escapeJava(body) + "\"));\n");
                output.append("    System.out.println(\"    body=\" + \"" + escapeJava(body) + "\");\n");
                break;
            case PUT:
                String bodyPut = substitute(req.getBody());
                output.append("      .PUT(HttpRequest.BodyPublishers.ofString(\"" + escapeJava(bodyPut) + "\"));\n");
                output.append("    System.out.println(\"    body=\" + \"" + escapeJava(bodyPut) + "\");\n");
                break;
            case DELETE:
                output.append("      .DELETE();\n");
                break;
        }

        // Add default headers (required by spec)
        output.append("    for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());\n");
        output.append("    b.header(\"Accept\", \"application/json\");\n");

        // TODO: Add request-specific headers (if your AST supports them)

        // Send the request
        output.append("    HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));\n");
        output.append("    System.out.println(\"<-- status=\" + resp.statusCode());\n");
        output.append("    System.out.println(resp.body());\n\n");
        // New response means normalized body variable needs to be (re)declared
        bodyNormDeclaredForCurrentResp = false;
    }

    //visitor method for Assertion
    private void visit(Assertion ast) {
        // This is a direct mapping from your Assertion AST node to a JUnit assertion
        switch (ast.getType()) {
            case STATUS:
                output.append("    assertEquals(" + ast.getStatusCode() + ", resp.statusCode());\n");
                break;
            case BODY_CONTAINS:
                // Make body-contains assertions whitespace-insensitive.
                // Declare normalized body once per response, then reuse.
                if (!bodyNormDeclaredForCurrentResp) {
                    output.append("    String _bodyNoWs = resp.body().replace(\" \", \"\").replace(\"\\n\", \"\").replace(\"\\r\", \"\").replace(\"\\t\", \"\");\n");
                    bodyNormDeclaredForCurrentResp = true;
                }
                output.append("    assertTrue(_bodyNoWs.contains(\""
                              + escapeJava(ast.getExpectedValue()).replaceAll("\\s+", "") + "\"));\n");
                break;
            case HEADER_EQUALS:
                output.append("    assertEquals(\"" + escapeJava(ast.getExpectedValue()) + "\", resp.headers().firstValue(\"" + escapeJava(ast.getHeaderName()) + "\").orElse(\"\"));\n");
                break;
            case HEADER_CONTAINS:
                output.append("    assertTrue(resp.headers().firstValue(\"" + escapeJava(ast.getHeaderName()) + "\").orElse(\"\").contains(\"" + escapeJava(ast.getExpectedValue()) + "\"));\n");
                break;
        }
    }

    // --- Helper Methods ---

    /**
     * Variable substitution: replaces $var with its value
     */
    private String substitute(String input) {
        if (input == null) return null;

        String result = input;
        for (Map.Entry<String, String> var : variables.entrySet()) {
            result = result.replace("$" + var.getKey(), var.getValue());
        }
        return result;
    }

    /**
     * Escapes string for Java source code
     */
    private String escapeJava(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
}
