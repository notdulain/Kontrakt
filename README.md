# 🚀 Kontrakt++: Testing APIs shouldn't feel like doing rocket physics

> Kontrakt++ is a DSL that turns your HTTP API tests from Δv = v_e · ln(m0/mf) into actual working Java code, no PhD required 👍🏻

## what sorcery is this?

Kontrakt++ is a **Domain-Specific Language** (a fancy term for "mini-language that does one thing well") that lets you write API tests in a human-friendly format and then magically transforms them into **real JUnit tests** that actually run against your backend.

### The Magic:
.test files (you write) → AST voodoo → GeneratedTests.java (JUnit) → ✅/❌ results


## 📁 Project Structure: Where Things Live
```
kontrakt-plus-plus/
├── src/                # The brains of the operation
│ ├── Scanner.flex          # Tokenizer rules (JFlex)
│ ├── Parser.cup            # Grammar rules (CUP)
│ ├── Main.java             # The main event
│ ├── CodeGenerator.java    # I mean, the name 🤷🏻‍♂️
│ └── ast/               # Abstract Syntax Tree classes
├── examples/            # Test files you can actually read
│ ├── example.test          # Basic example
│ ├── example_1.test        # More complex stuff
│ └── example_2.test        # Even more complex stuff
├── backend/              # SpringBoot backend to test against
├── lib/                  # JAR files (the dependencies)
└── Makefile   # this is one of the coolest things ever
```


## 🛠️ Setup: getting your ducks in a row

### Step 1: Clone
```bash
git clone https://github.com/notdulain/Kontrakt.git
cd Kontrakt
```

### Step 2: Check Your JARs 🫙
make sure these files exist in lib/ (because magic needs tools):

```
jflex-full-1.9.1.jar
java-cup-11b.jar
java-cup-11b-runtime.jar
junit-platform-console-standalone-1.10.1.jar
```
if they're missing, well... good luck with compiling 💀

## ⚡ The Fun Part: Making Things Happen

### Phase 1: Compiling the Parser & Scanner
```bash
make generate
```
what this does: runs JFlex and CUP to create Java code that understands your DSL. Think of it as teaching Java to speak the "test-language".

### Phase 2: Compile everything
```bash
make compile
```
translation: "please turn all this java code into something the computer can actually run (class files)"

### Phase 3: Parse your .test file
```bash
make run
```
What happens: Takes example_1.test (for now) and creates GeneratedTests.java. This is where the real magic happens!

[Screenshot: Show the input .test file and the output GeneratedTests.java side by side]

## 🎭 Running the backend: The test subject

### Option A: CLI (for terminal nerds)
```bash
cd backend
mvn clean package
java -jar target/testlang-demo-0.0.1-SNAPSHOT.jar
```

### Option B: IntelliJ (jetbrains didn't develop the best IDE for java only for us to use the terminal 🤌🏻)
- open the backend folder as a project in IntelliJ
- build the maven project (troubleshooting this is a whole other paradigm of programming imo)
- find `src/main/java/whatever/KontraktBackendApplication.java`
- look for the cute little green play button ▶️ at the top
- um, click it
- wait for "Started Application" in the console

[Screenshot: IntelliJ with the running Spring Boot app]

just to verify, open http://localhost:8080 in your browser. if you see something other than an error, you're golden!

## 🧪 The Grand Finale: Running Tests

### Step 1: Compile the GeneratedTests.java
```bash
make compile-tests
```
this compiles GeneratedTests.java with JUnit in the classpath.

### Step 2: Run the Tests
```bash
make run-tests
```
the moment of truth: this runs your generated JUnit tests against the running backend.

[Screenshot: Green passing tests in the terminal]

## 🎪 Example Test Cases That Actually Work
Here's what you can test right now (assuming backend is running):

### 🎯 Test 1: Login
```testlang
test Login {
  POST "/api/login" {
    body = "{ \"username\": \"$username\", \"password\": \"$password\" }";
  };
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "\"success\": true";
  expect body contains "\"token\":";
  expect body contains "\"message\": \"Login successful\"";
}
```

### 🎯 Test 2: Get User Info
```testlang
test GetUser {
  GET "/api/users/$user_id";
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "\"id\": 42";
  expect body contains "\"username\": \"user42\"";
  expect body contains "\"email\": \"user42@example.com\"";
  expect body contains "\"role\": \"USER\"";
}
```

### 🎯 Test 3: Update User
```testlang
test UpdateUser {
  PUT "/api/users/$user_id" {
    body = "{ \"role\": \"ADMIN\" }";
  };
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "\"id\": 42";
  expect body contains "\"updated\": true";
  expect body contains "\"role\": \"ADMIN\"";
  expect body contains "\"message\": \"User updated successfully\"";
}
```

### 🎯 Test 4: Delete User
```testlang
test DeleteUser {
  DELETE "/api/users/$delete_id";
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "\"id\": 99";
  expect body contains "\"deleted\": true";
  expect body contains "\"message\": \"User deleted successfully\"";
}
```

### 🎯 Test 5: Get Non-Existent User (Error case - only works for 9999)
```testlang
test GetNonExistentUser {
  GET "/api/users/9999";
  expect status = 404;
  expect body contains "\"error\"";
  expect body contains "\"User not found\"";
}
```

## 🚨 common "oh cRap" moments & fixes
❌ "Parser error on line X"
Probably: you forgot a semicolon. yes, again 🤦🏻‍♂️ (or forgot your own language's syntax)

❌ "Connection refused to localhost:8080"
Solution: Your backend isn't running. See the "Running the Backend" section above.

❌ "GeneratedTests.java doesn't reflect my changes"
Fix: Run make run again. The generator isn't psychic (yet).

❌ "Tests fail but curl works"
Likely cause: Your test expectations don't match what the backend actually returns. Check those JSON strings!

## 🎊 You Did It!
If you see green checkmarks and passing tests, congratulations! You've successfully:

🏗️ Built a language parser from scratch

🔄 Transformed DSL into executable Java code

🚀 Tested a real HTTP API

😎 Looked cool doing it

now go forth and test all the APIs! or take a well-deserved coffee break, I won't judge 🙂‍↔️
