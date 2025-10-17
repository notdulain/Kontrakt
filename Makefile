# Paths
JFLEX = java -jar lib/jflex-full-1.9.1.jar
CUP = java -jar lib/java-cup-11b.jar
CUPRUNTIME = lib/java-cup-11b-runtime.jar
SRC = src
BIN = bin

# Targets
all: generate compile

generate:
	@echo "🔧 Generating scanner..."
	$(JFLEX) $(SRC)/lexer.flex -d $(SRC)
	@echo "🔧 Generating parser..."
	$(CUP) -destdir $(SRC) -parser parser $(SRC)/parser.cup

compile:
	@echo "🔨 Compiling..."
	mkdir -p $(BIN)
	javac -cp "$(CUPRUNTIME):$(SRC)" -d $(BIN) $(SRC)/*.java $(SRC)/ast/*.java

run:
	@echo "▶️  Running on examples/example.test..."
	java -cp "$(CUPRUNTIME):$(BIN)" Main examples/example.test

clean:
	rm -rf $(BIN)
	rm -f $(SRC)/Lexer.java $(SRC)/parser.java $(SRC)/sym.java
	rm -f GeneratedTests.java

.PHONY: all generate compile run clean