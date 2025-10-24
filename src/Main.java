import java.io.*;
import java_cup.runtime.*;
import ast.*;

public class Main {
    public static void main(String[] args) {
        //System.out.println("🚀 Starting Kontrakt++ Parser Test\n");
        
        try {
            String filename = args.length > 0 ? args[0] : "examples/example.test";
            
            FileReader fileReader = new FileReader(filename);
            KontraktScanner scanner = new KontraktScanner(fileReader);
            parser parser = new parser(scanner);

            Symbol result = parser.parse();
            Program program = parser.getProgram();
            
            System.out.println("\n✅ Parsing completed successfully!");

            //code generation
            CodeGenerator generator = new CodeGenerator();
            String javaCode = generator.generate(program);

            // Write to a new file - GeneratedTests.java
            try (FileWriter writer = new FileWriter("GeneratedTests.java")) {
                writer.write(javaCode);
            }

            System.out.println("✅ Generated GeneratedTests.java");
            
        } catch (FileNotFoundException e) {
            System.err.println("❌ File not found: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            // Custom parser error messages are already printed via report_error().
            // Avoid adding extra noise here.
            System.exit(1);
        }
    }
}
