# Paths
JFLEX = java -jar lib/jflex-full-1.9.1.jar
CUP = java -jar lib/java-cup-11b.jar
CUPRUNTIME = lib/java-cup-11b-runtime.jar

SRC = src
BIN = bin

SCANNER_SPEC = $(SRC)/Scanner.flex
PARSER_SPEC = $(SRC)/Parser.cup

# Targets
all: generate compile

generate-scanner:
	@echo "`🪚 Generating Scanner..."
	$(JFLEX) $(SCANNER_SPEC) -d $(SRC)

generate-parser:
	@echo "🔧 Generating Parser..."
	$(CUP) -destdir $(SRC) -parser parser $(PARSER_SPEC)

generate: generate-scanner generate-parser

compile:
	@echo "🔨 Compiling..."
	mkdir -p $(BIN)
	javac -cp "$(CUPRUNTIME):$(SRC)" -d $(BIN) $(SRC)/*.java $(SRC)/ast/*.java

run:
	@echo "▶️  Running on examples/example.test..."
	java -cp "$(CUPRUNTIME):$(BIN)" Main examples/example.test

clean:
	rm -rf $(BIN)
	rm -f $(SRC)/KontraktScanner.java $(SRC)/parser.java $(SRC)/sym.java
	rm -f GeneratedTests.java

.PHONY: all generate compile run clean