import ast.*;
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
    
}
