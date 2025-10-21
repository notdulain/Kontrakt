import ast.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CodeGenerator {
    private StringBuilder output = new StringBuilder();
    private Program program;

    //This map will be CRITICAL for Step 2
    private Map<String, String> variables = new HashMap<>();

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
        output.append("    client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();\n");

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
    
}
