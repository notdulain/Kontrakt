# Paths
JFLEX = java -jar lib/jflex-full-1.9.1.jar
CUP = java -jar lib/java-cup-11b.jar
CUPRUNTIME = lib/java-cup-11b-runtime.jar
JUNIT_JAR = lib/junit-platform-console-standalone-1.10.1.jar

SRC = src
BIN = bin

SCANNER_SPEC = $(SRC)/Scanner.flex
PARSER_SPEC = $(SRC)/Parser.cup

# Targets
all: generate compile
	@echo "Build complete! Run 'make run' to test"

generate-scanner:
	@echo "🪚 Generating Scanner..."
	$(JFLEX) $(SCANNER_SPEC) -d $(SRC)

generate-parser:
	@echo "🔧 Generating Parser..."
	$(CUP) -destdir $(SRC) -parser parser $(PARSER_SPEC)

generate: generate-scanner generate-parser

compile:
	@echo "🔨 Compiling..."
	mkdir -p $(BIN)
	javac -cp "$(CUPRUNTIME):$(SRC)" -d $(BIN) $(SRC)/*.java

run:
	@echo "▶️  Running on examples/example_1.test..."
	java -cp "$(CUPRUNTIME):$(BIN)" Main examples/example_1.test

compile-tests:
	@echo "🔨 Compiling GeneratedTests.java..."
	javac -cp "$(JUNIT_JAR):." GeneratedTests.java

run-tests: compile-tests
	@echo "🧪 Running tests..."
	java -jar $(JUNIT_JAR) --class-path . --scan-class-path

test-full: run compile-tests run-tests
	@echo "✅ Full test pipeline complete!"

clean:
	rm -rf $(BIN)
	rm -f $(SRC)/KontraktScanner.java $(SRC)/parser.java $(SRC)/sym.java
	rm -f GeneratedTests.java

.PHONY: all generate compile run clean