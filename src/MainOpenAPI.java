import java.io.*;
import java_cup.runtime.*;
import ast.*;

public class MainOpenAPI {
  public static void main(String[] args) throws Exception {

    String filename = args.length > 0 ? args[0] : "examples/example_1.test";

    try (FileReader r = new FileReader(filename)) {
      KontraktScanner scanner = new KontraktScanner(r);
      parser p = new parser(scanner);

      p.parse(); // builds Program inside parser
      
      Program program = p.getProgram();

      OpenApiGenerator gen = new OpenApiGenerator();
      String yaml = gen.generate(program);

      try (FileWriter w = new FileWriter("openapi.yaml")) {
        w.write(yaml);
      }
      System.out.println("✅ Wrote openapi.yaml");
    }
  }
}
