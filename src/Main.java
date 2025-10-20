import java.io.*;
import java_cup.runtime.*;

public class Main {
    public static void main(String[] args) {
        //System.out.println("🚀 Starting Kontrakt++ Parser Test\n");
        
        try {
            String filename = args.length > 0 ? args[0] : "examples/example.test";
            //System.out.println("📖 Reading: " + filename + "\n");
            
            FileReader fileReader = new FileReader(filename);
            
            //using KontraktScanner
            KontraktScanner scanner = new KontraktScanner(fileReader);
            //System.out.println("✅ Scanner created");
            
            parser parser = new parser(scanner);
            //System.out.println("✅ Parser created\n");
            
            //System.out.println("⚙️  Parsing...\n");
            Symbol result = parser.parse();
            
            System.out.println("\n✅ Parsing completed successfully!");
            
        } catch (FileNotFoundException e) {
            System.err.println("❌ File not found: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}