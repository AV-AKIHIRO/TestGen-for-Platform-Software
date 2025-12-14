# PL Platform Testing - Comprehensive Project Report

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Implementation Details](#implementation-details)
4. [System Flow](#system-flow)
5. [Output Generation](#output-generation)
6. [Edge Cases and Limitations](#edge-cases-and-limitations)
7. [What's Left to Implement](#whats-left-to-implement)
8. [Usage Guide](#usage-guide)

---

## Project Overview

**PL Platform Testing** is a test generation framework that automatically generates test cases from JML (Java Modeling Language) specifications. The system converts JML pre/post-conditions into Abstract Test Cases (ATCs) that can be executed using symbolic execution tools like Java PathFinder (JPF).

### Key Features
- **JML Specification Parsing**: Parses JML annotations to extract pre-conditions and post-conditions
- **ATC Generation**: Converts specifications into an Intermediate Representation (IR) for test generation
- **Symbolic Execution Integration**: Transforms ATCs into JPF-compatible code for symbolic execution
- **Multiple Test Scenarios**: Supports primitive types, arrays, collections (Set, Map, List), and complex object interactions
- **Incremental Testing**: Provides example test cases for validation

### Technology Stack
- **Language**: Java 8
- **Build Tool**: Maven
- **Symbolic Execution**: Java PathFinder (JPF) with Symbolic PathFinder extension
- **Dependencies**: Custom AST classes for JML expression representation

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Input Layer                                │
├─────────────────────────────────────────────────────────────────┤
│  JML Specifications  │  Test Sequence Strings                  │
│  (Pre/Post Conditions)│  (Function Call Sequences)              │
└───────────────────────┴─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Parsing Layer                              │
├─────────────────────────────────────────────────────────────────┤
│  JmlSpecParser  →  JmlSpecAst                                   │
│  TestStringParser → TestStringAst                               │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                   ATC Generation Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  GenATC Interface                                               │
│  └── NewGenATC (Implementation)                                 │
│      ├── generateAtcFile()                                      │
│      └── generateHelperFunction()                               │
│                                                                 │
│  Output: AtcClass (IR Structure)                                │
│  ├── Package/Class metadata                                     │
│  ├── Test Methods (helper functions)                            │
│  └── Main method statements                                     │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│              Symbolic Execution Transformation Layer             │
├─────────────────────────────────────────────────────────────────┤
│  AtcIrToSymbolicIrTransformer                                   │
│  ├── transform() - Converts ATC IR to Symbolic IR               │
│  ├── transformSymbolicVarDecl() - Debug.makeSymbolic* calls     │
│  ├── transformAssumeStmt() - Debug.assume() calls               │
│  └── transformIfStmt() - Conditional statements                 │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Code Generation Layer                         │
├─────────────────────────────────────────────────────────────────┤
│  AtcIrCodeGenerator                                             │
│  ├── generateJavaFile() - Standard Java code                    │
│  └── generateSymbolicJavaFile() - JPF-compatible code           │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Symbolic Execution Wrapper                      │
├─────────────────────────────────────────────────────────────────┤
│  SpfWrapper                                                     │
│  ├── run() - Main entry point                                   │
│  ├── printBothVersions() - Debug output                         │
│  ├── saveOutputFiles() - File generation                        │
│  └── generateJpfFile() - JPF configuration files                │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Output Files                                 │
├─────────────────────────────────────────────────────────────────┤
│  GeneratedATCs.java - Test class with symbolic variables        │
│  GeneratedATCs_*.jpf - JPF configuration files                   │
│  Helper.java - Mock helper class for testing                    │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

#### 1. **Parser Layer** (`parser/`)
- **JmlSpecParser**: Parses JML annotations (currently placeholder)
- **PostconditionVisitor**: Visits and transforms post-condition expressions (currently placeholder)
- **AST Classes**: 
  - `JmlSpecAst`: Container for all JML function specifications
  - `JmlFunctionSpec`: Individual function specification with signature, pre-condition, post-condition
  - `FunctionSignature`: Function name, parameters, return type
  - `TestStringAst`: Test sequence representation

#### 2. **ATC Generation Layer** (`atc/`)
- **GenATC Interface**: Contract for ATC generation algorithms
- **NewGenATC**: Main implementation of the test generation algorithm
  - Generates helper methods for each function in test sequence
  - Handles parameter declarations (primitive, array, collection)
  - Manages pre-condition assumptions
  - Handles old state snapshots for post-conditions
  - Generates method calls and assertions

#### 3. **IR (Intermediate Representation)** (`atc/ir/`)
- **AtcClass**: Root node representing the entire test class
- **AtcTestMethod**: Individual test/helper method
- **AtcStatement**: Base class for all statements
  - `AtcSymbolicVarDecl`: Symbolic variable declaration
  - `AtcVarDecl`: Regular variable declaration
  - `AtcAssumeStmt`: Pre-condition assumption
  - `AtcAssertStmt`: Post-condition assertion
  - `AtcMethodCallStmt`: Method invocation
  - `AtcAssignStmt`: Variable assignment
  - `AtcIfStmt`: Conditional statement

#### 4. **AST Helper Layer** (`ast/`)
- **AstHelper**: Utility class for AST manipulation
  - Expression creation (binary, unary, method calls, etc.)
  - Post-condition transformation (handles `_post` suffix and prime operator `'`)
  - Variable snapshot collection
  - Java code generation from AST

#### 5. **Symbolic Execution Layer** (`symex/`)
- **AtcIrToSymbolicIrTransformer**: Transforms ATC IR to JPF-compatible IR
  - Converts `AtcSymbolicVarDecl` to `Debug.makeSymbolic*()` calls
  - Converts `AtcAssumeStmt` to `Debug.assume()` calls
  - Handles collection types with `Debug.makeSymbolicRef()`
- **SpfWrapper**: Wrapper for Java PathFinder integration
  - Generates JPF configuration files (`.jpf`)
  - Saves output files to `outputs/` directory
  - Provides debug output showing both original and transformed code
- **TypeMapper**: Maps collection types to their implementations

---

## Implementation Details

### 1. ATC Generation Algorithm (`NewGenATC`)

#### Main Flow (`generateAtcFile`)

```java
1. Extract unique function specifications from test sequence
2. For each unique function:
   a. Generate helper method (generateHelperFunction)
   b. Store in map for reuse
3. Create main method:
   a. Instantiate GeneratedATCs class
   b. Call helper methods in sequence from test string
4. Return AtcClass IR structure
```

#### Helper Function Generation (`generateHelperFunction`)

**Step 1: Parameter Declaration**
- Detect array parameters (if accessed as `x[0]` in pre/post conditions)
- For primitive types accessed as arrays: declare as `int[] x = new int[]{Symbolic.input("x")}`
- For regular primitives: declare as `AtcSymbolicVarDecl`
- For collections: declare as symbolic reference

**Step 2: Pre-condition Handling**
- **Primitive-only methods**: Add `assume(precondition)` first
- **Collection methods**: Add null checks first, then `assume(precondition)`

**Step 3: Old State Snapshot**
- Collect variables that appear in post-conditions (using `collectVarsToSnapshot`)
- For each variable:
  - If accessed as array: snapshot as `x_old = x[0]`
  - If regular primitive: snapshot as `x_old = x`
  - Collections are NOT snapshotted (passed by reference)

**Step 4: Null Checks (Collection Methods)**
- Generate: `if (data == null || result == null) { return; }`
- Add `assume(data != null)` and `assume(result != null)` after null check

**Step 5: Print Statements**
- Print test inputs for debugging
- For collections: print first collection parameter only
- For primitives: print first primitive parameter

**Step 6: Method Call Generation**
- **Post-state parameter detected** (parameter modified in-place):
  - If already array: pass directly
  - If primitive: wrap in array, call method, extract result
- **Regular call**: Pass all parameters as-is
- **Collection methods**: Wrap in `if (notNull)` block

**Step 7: Post-condition Assertion**
- Transform post-condition:
  - Replace `x_post` or `'(x)` with result variable or parameter name
  - Replace `x` (not in prime) with `x_old` if snapshot exists
- Generate `assert(transformedPostCondition)`

### 2. Post-Condition Transformation (`AstHelper.transformPostCondition`)

The transformation handles JML post-state notation:

**Notation Types:**
1. **Prime Operator**: `'(x)` or `'([x])` - represents post-state of `x`
2. **Post Suffix**: `x_post` - represents post-state of `x`
3. **Old State**: `x` (without prime) - represents pre-state, replaced with `x_old`

**Transformation Rules:**
- Inside prime operator: Map to result variable (if exists) or keep as parameter name
- Outside prime operator: Map to `_old` version if snapshot exists
- Method calls: Recursively transform scope and arguments
- Binary/Unary expressions: Transform left/right operands recursively

### 3. Array Parameter Detection

The system automatically detects when a primitive parameter is accessed as an array:

```java
// Example: Pre-condition contains "x[0] > 0"
// System detects: parameter "x" should be declared as int[] instead of int
```

**Detection Algorithm:**
1. Recursively traverse pre/post-condition expressions
2. Look for `NameExpr` with identifier matching pattern `paramName[index]`
3. Add parameter name to `arrayParams` set
4. During declaration: declare as array type if in set

### 4. Collection Type Handling

**Supported Collections:**
- `Set<T>`, `Map<K,V>`, `List<T>`, `Collection<T>`, `Queue<T>`, `Deque<T>`

**Special Handling:**
- Null checks added before method calls
- Symbolic references created using `Debug.makeSymbolicRef()`
- Default implementations: `HashSet`, `HashMap`, `ArrayList`, `LinkedList`

### 5. Symbolic IR Transformation (`AtcIrToSymbolicIrTransformer`)

**Transformations:**

| ATC IR Statement | JPF Symbolic Code |
|-----------------|-------------------|
| `AtcSymbolicVarDecl("int", "x")` | `int x = Debug.makeSymbolicInteger("x");` |
| `AtcSymbolicVarDecl("String", "s")` | `String s = Debug.makeSymbolicString("s");` |
| `AtcSymbolicVarDecl("Set<Integer>", "data")` | `Set<Integer> data = (Set<Integer>) Debug.makeSymbolicRef("data", new HashSet<>());` |
| `AtcAssumeStmt(condition)` | `Debug.assume(condition);` |

### 6. Code Generation (`AtcIrCodeGenerator`)

**Features:**
- Generates properly formatted Java code
- Handles indentation for nested structures (if statements)
- Extracts method calls from assertions (for complex post-conditions)
- Generates both standard Java and JPF-compatible code

**Method Call Extraction:**
- If assertion contains method calls (e.g., `Helper.update(...)`), extract to separate variable
- Example: `assert(Helper.update(result, data) != null)` becomes:
  ```java
  Map<?,?> expectedResult = Helper.update(result, data);
  assert(expectedResult != null);
  ```

---

## System Flow

### Complete Test Generation Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                         START                                        │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Input: JML Specifications + Test Sequence String                    │
│  Example:                                                            │
│    JML: @requires s != null; @ensures s != null;                     │
│    Test: ["appendExclamation", "appendExclamation"]                  │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Parse JML Specifications                                            │
│  └──> JmlSpecAst (contains JmlFunctionSpec objects)                  │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Parse Test Sequence                                                 │
│  └──> TestStringAst (list of function names)                         │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  NewGenATC.generateAtcFile()                                         │
│  ├── Extract unique function specs from test sequence                │
│  ├── For each function: generateHelperFunction()                     │
│  └── Create main method with function calls                          │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  generateHelperFunction() - For each function                        │
│  ├── Step 1: Declare parameters (detect arrays, collections)         │
│  ├── Step 2: Add pre-condition assume (with null checks if needed)   │
│  ├── Step 3: Snapshot old state (for post-condition variables)       │
│  ├── Step 4: Add null checks (for collection methods)                │
│  ├── Step 5: Add print statements (for debugging)                    │
│  ├── Step 6: Generate method call (with array wrapping if needed)    │
│  └── Step 7: Add post-condition assertion (with transformation)      │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Output: AtcClass IR Structure                                       │
│  ├── Package: "in.ac.iiitb.plproject.atc.generated"                  │
│  ├── Class: "GeneratedATCs"                                          │
│  ├── Test Methods: [appendExclamation_helper(), ...]                 │
│  └── Main Method: [instance creation, method calls]                  │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  SpfWrapper.run()                                                    │
│  ├── AtcIrToSymbolicIrTransformer.transform()                        │
│  │   └── Convert ATC IR to Symbolic IR                               │
│  ├── AtcIrCodeGenerator.generateSymbolicJavaFile()                   │
│  │   └── Generate JPF-compatible Java code                           │
│  └── saveOutputFiles()                                               │
│      ├── GeneratedATCs.java                                          │
│      ├── GeneratedATCs_main.jpf                                      │
│      ├── GeneratedATCs_*_helper.jpf (for each helper)                │
│      └── Helper.java (mock class)                                    │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Output Files Ready for JPF Execution                                │
│  └──> Can be run with: java -jar jpf-core.jar GeneratedATCs_main.jpf │
└──────────────────────────────────────────────────────────────────────┘
```

### Helper Function Generation Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│  Input: JmlFunctionSpec                                              │
│  ├── Signature: appendExclamation(s: String) -> void                 │
│  ├── Pre: s != null                                                  │
│  └── Post: s != null                                                 │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 1: Parameter Declaration                                       │
│  ├── Detect array access? (x[0] in pre/post)                         │
│  ├── If array: int[] x = new int[]{Symbolic.input("x")}              │
│  ├── If collection: Set<Integer> data = (Set<Integer>) ...           │
│  └── Else: int x = Symbolic.input("x")                               │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 2: Pre-condition                                               │
│  ├── Primitive-only? → assume(pre) first                             │
│  └── Has collections? → null checks first, then assume(pre)          │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 3: Old State Snapshot                                          │
│  ├── collectVarsToSnapshot(post) → {"x"}                             │
│  ├── For each variable:                                              │
│  │   ├── Array access? → x_old = x[0]                                │
│  │   └── Regular? → x_old = x                                        │
│  └── Collections: skip (passed by reference)                         │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 4: Null Checks (if collections present)                        │
│  └── if (data == null || result == null) { return; }                 │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 5: Print Statements                                            │
│  └── System.out.println("Test Input: s = " + s);                     │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 6: Method Call                                                 │
│  ├── Post-state param detected?                                      │
│  │   ├── Already array? → Helper.func(x)                             │
│  │   └── Primitive? → Wrap in array, call, extract                   │
│  ├── Has collections? → Wrap in if (notNull) block                   │
│  └── Regular? → Helper.func(params...)                               │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 7: Post-condition Assertion                                    │
│  ├── transformPostCondition(post, resultVar, oldStateMap)            │
│  │   ├── Replace x_post or '(x) with result/param                    │
│  │   └── Replace x (not prime) with x_old                            │
│  └── assert(transformedPost)                                         │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Output: AtcTestMethod                                               │
│  └── appendExclamation_helper() with all statements                  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Output Generation

### Generated Files Structure

```
outputs/
├── GeneratedATCs.java                    # Main test class
├── GeneratedATCs_main.jpf                # JPF config for main method
├── GeneratedATCs_appendExclamation_helper.jpf  # JPF config for helper
├── GeneratedATCs_increment_helper.jpf
├── GeneratedATCs_process_helper.jpf
└── in/ac/iiitb/plproject/atc/generated/
    └── Helper.java                       # Mock helper class
```

### Example Output: `GeneratedATCs.java`

```java
package in.ac.iiitb.plproject.atc.generated;

import java.util.*;
import gov.nasa.jpf.symbc.Debug;

public class GeneratedATCs {

    public void appendExclamation_helper() {
        String s = Debug.makeSymbolicString("s");
        Debug.assume((s != null));
        System.out.println(("Test Input: s = " + s));
        Helper.appendExclamation(s);
        assert(s != null);
    }

    public static void main(String[] args) {
        GeneratedATCs instance = new GeneratedATCs();
        instance.appendExclamation_helper();
        instance.appendExclamation_helper();
        instance.appendExclamation_helper();
    }
}
```

### Example Output: `GeneratedATCs_main.jpf`

```properties
# Target class
target = in.ac.iiitb.plproject.atc.generated.GeneratedATCs

# Set the classpath to point to your compiled classes
classpath = ${jpf-symbc}/build/examples

# Path to source code
sourcepath = ${jpf-symbc}/src/examples

# Constraint solver - required for symbolic execution
symbolic.dp = z3

# Symbolic string variables created via Debug.makeSymbolicString
symbolic.string_dp = true

# Add instruction factory for symbolic execution
vm.insn_factory.class = gov.nasa.jpf.symbc.SymbolicInstructionFactory

# Enable symbolic array handling (needed for array operations)
symbolic.arrays = true

# Integer ranges
symbolic.minint = -100
symbolic.maxint = 100
symbolic.undefined = -1000

# Search bounds to prevent infinite exploration
search.depth_limit = 200
search.time_limit = 60
search.multiple_errors = true
search.class = .search.heuristic.BFSHeuristic

# Show path conditions and symbolic execution results
jpf.report.console.finished = gov.nasa.jpf.symbc.SymbolicPathListener

# Enable output so System.out.println shows test input values
vm.output = true
```

### Output Characteristics

**For Simple Examples (Primitive Types):**
- Clean, straightforward code
- Direct symbolic variable declarations
- Simple pre/post-condition handling

**For Array Examples:**
- Automatic array wrapping for primitive parameters accessed as arrays
- Array element access in pre/post-conditions handled correctly

**For Collection Examples:**
- Null checks added automatically
- Symbolic references created with default implementations
- Conditional method calls (wrapped in null checks)

---

## Edge Cases and Limitations

### 1. **Function Return Values with Links (Not Implemented)**

**Issue**: The system does not yet support JML annotations where the return value has links (using `\return` annotation).

**Example Not Supported:**
```java
// @ensures \result > 0 && \result == x + 1;
public int increment(int x) { ... }
```

**Current Behavior**: The system focuses on in-place modifications and void functions. Return value handling needs to be implemented.

**Impact**: Functions that return values and have post-conditions on the return value cannot be tested.

---

### 2. **Complex Examples Support is Thin**

**Issue**: As noted in `note.md`, support for complex examples is very limited.

**Current Limitations:**
- Nested method calls in pre/post-conditions may not be handled correctly
- Complex object creation in specifications may fail
- Multiple levels of indirection in post-conditions may cause issues

**Example That May Fail:**
```java
// @ensures Helper.update(Helper.process(data), result) != null;
public void complexMethod(Set<Integer> data, Map<Integer, Integer> result) { ... }
```

**Impact**: The algorithm works for "normal base cases" but may fail on complex scenarios.

---

### 3. **Array Parameter Detection Limitations**

**Issue**: Array detection only works for simple patterns like `x[0]`.

**Limitations:**
- Does not handle `x[i]` where `i` is a variable
- Does not handle multi-dimensional arrays
- Does not handle array length checks (`x.length > 0`)

**Example That May Fail:**
```java
// @requires x.length > 0 && x[i] > 0;  // 'i' is a variable
public void method(int[] x, int i) { ... }
```

**Impact**: Some array-based specifications may not be handled correctly.

---

### 4. **Post-State Parameter Detection**

**Issue**: The system uses pattern matching and reflection to detect post-state parameters, which can be fragile.

**Detection Methods:**
1. String pattern matching: `'([x])`, `'(x)`, `x_post`
2. Recursive AST traversal with reflection

**Limitations:**
- May miss edge cases in post-condition expressions
- Reflection-based approach may fail if AST structure changes
- Complex nested post-state references may not be detected

**Example That May Fail:**
```java
// @ensures Helper.update('(result), data) != null;  // Nested post-state
public void method(Map<Integer, Integer> result, Set<Integer> data) { ... }
```

**Impact**: Some post-state parameters may not be detected, leading to incorrect test generation.

---

### 5. **Collection Type Limitations**

**Issue**: Collection handling has several limitations.

**Limitations:**
- Only supports standard Java collections (Set, Map, List, etc.)
- Does not handle custom collection types
- Generic type inference may fail for complex generics
- Does not handle nested generics (e.g., `Map<String, List<Integer>>`)

**Example That May Fail:**
```java
// @requires data instanceof CustomCollection;
public void method(CustomCollection<Integer> data) { ... }
```

**Impact**: Custom collection types or complex generic types may not be handled.

---

### 6. **Pre/Post-Condition Expression Complexity**

**Issue**: Complex expressions in pre/post-conditions may not be fully supported.

**Limitations:**
- Quantifiers (forall, exists) not supported
- Complex method chains may fail
- Type casting in conditions may not work
- Arithmetic operations in conditions may have issues

**Example That May Fail:**
```java
// @requires (\forall int i; 0 <= i && i < arr.length; arr[i] > 0);
public void method(int[] arr) { ... }
```

**Impact**: Complex JML specifications may not be parseable or transformable.

---

### 7. **Symbolic Execution Integration**

**Issue**: The `SpfWrapper.run()` method currently does not actually execute JPF.

**Current Implementation:**
```java
public List<ConcreteInput> run(AtcClass atcClass) {
    // ... generates files ...
    System.out.println("(SPF execution not yet implemented)");
    return new ArrayList<ConcreteInput>();
}
```

**Impact**: Test generation works, but concrete test inputs are not extracted from JPF execution.

---

### 8. **Error Handling**

**Issue**: Limited error handling throughout the system.

**Limitations:**
- No validation of JML specification syntax
- No error recovery if transformation fails
- Reflection-based code may throw exceptions silently
- No user-friendly error messages

**Impact**: Failures may be silent or produce cryptic error messages.

---

### 9. **Type System Limitations**

**Issue**: Type inference and mapping have limitations.

**Limitations:**
- Primitive type detection is hardcoded (int, double, String, boolean, etc.)
- Does not handle user-defined types well
- Type casting in expressions may not be preserved
- Generic type erasure may cause issues

**Example That May Fail:**
```java
// Custom type in specification
public void method(MyCustomType obj) { ... }
```

**Impact**: Non-standard types may not be handled correctly.

---

### 10. **Main Method Generation**

**Issue**: Main method simply calls helper methods in sequence without any orchestration.

**Limitations:**
- No state management between calls
- No handling of return values from helper methods
- No conditional execution based on previous results
- All calls are unconditional

**Impact**: Test sequences are linear and cannot express complex test scenarios.

---

## What's Left to Implement

### High Priority

#### 1. **Return Value Handling (`\return` annotation)**
- **Status**: Not implemented
- **Description**: Support JML post-conditions on function return values
- **Example**:
  ```java
  // @ensures \result > 0 && \result == x + 1;
  public int increment(int x) { ... }
  ```
- **Implementation Needed**:
  - Detect `\result` in post-conditions
  - Generate return value variable
  - Transform post-conditions to use return variable
  - Add assertions on return value

#### 2. **Actual JPF Execution Integration**
- **Status**: Placeholder implementation
- **Description**: Execute JPF and extract concrete test inputs
- **Implementation Needed**:
  - Integrate JPF execution in `SpfWrapper.run()`
  - Parse JPF output to extract path conditions
  - Use constraint solver to get concrete values
  - Return `List<ConcreteInput>` with actual test data

#### 3. **JML Parser Implementation**
- **Status**: Placeholder files exist
- **Description**: Parse actual JML annotations from Java source files
- **Implementation Needed**:
  - Implement `JmlSpecParser` to parse `@requires`, `@ensures` annotations
  - Handle JML expression syntax
  - Build AST from parsed JML

#### 4. **Complex Expression Support**
- **Status**: Basic support only
- **Description**: Handle nested method calls, quantifiers, complex object creation
- **Implementation Needed**:
  - Extend AST transformation for nested calls
  - Support JML quantifiers (forall, exists)
  - Handle complex object creation in specifications

### Medium Priority

#### 5. **Better Array Handling**
- **Status**: Basic support for `x[0]` pattern
- **Description**: Support variable indices, multi-dimensional arrays, length checks
- **Implementation Needed**:
  - Detect array access with variable indices
  - Handle multi-dimensional arrays
  - Support array length operations

#### 6. **Error Handling and Validation**
- **Status**: Limited error handling
- **Description**: Add comprehensive error handling and user-friendly messages
- **Implementation Needed**:
  - Validate JML specification syntax
  - Provide clear error messages
  - Add error recovery mechanisms
  - Logging and debugging support

#### 7. **Custom Type Support**
- **Status**: Limited to standard types
- **Description**: Support user-defined classes and custom collection types
- **Implementation Needed**:
  - Type registry for custom types
  - Symbolic variable generation for custom types
  - Handle custom type methods in specifications

#### 8. **State Management in Test Sequences**
- **Status**: Linear sequence only
- **Description**: Support stateful test sequences with conditional execution
- **Implementation Needed**:
  - Track state between test method calls
  - Support conditional execution
  - Handle return values in sequences

### Low Priority

#### 9. **Performance Optimization**
- **Status**: Not optimized
- **Description**: Optimize AST traversal and transformation
- **Implementation Needed**:
  - Cache transformation results
  - Optimize reflection usage
  - Reduce AST copying

#### 10. **Documentation and Examples**
- **Status**: Basic examples exist
- **Description**: Comprehensive documentation and more examples
- **Implementation Needed**:
  - API documentation
  - More complex example scenarios
  - Tutorial for users

#### 11. **Testing Infrastructure**
- **Status**: Basic test files exist
- **Description**: Comprehensive unit tests and integration tests
- **Implementation Needed**:
  - Unit tests for each component
  - Integration tests for end-to-end flow
  - Regression tests for edge cases

---

## Usage Guide

### Running the Project

#### 1. **Compile the Project**
```bash
cd pl-platform-testing
mvn compile
```

#### 2. **Run Incremental Test Example**
```bash
# Simple example
mvn exec:java -Dexec.mainClass="in.ac.iiitb.plproject.atc.IncrementalTestExample" -Dexec.args="simple"

# Complex example
mvn exec:java -Dexec.mainClass="in.ac.iiitb.plproject.atc.IncrementalTestExample" -Dexec.args="complex"

# All examples
mvn exec:java -Dexec.mainClass="in.ac.iiitb.plproject.atc.IncrementalTestExample" -Dexec.args="all"
```

#### 3. **Check Output Files**
After running, check the `outputs/` directory:
- `GeneratedATCs.java` - Generated test class
- `GeneratedATCs_*.jpf` - JPF configuration files
- `Helper.java` - Mock helper class

#### 4. **Run with JPF (Manual)**
```bash
# Compile generated files
javac -cp "jpf-core.jar:jpf-symbc.jar" outputs/GeneratedATCs.java

# Run JPF
java -jar jpf-core.jar outputs/GeneratedATCs_main.jpf
```

### Example: Creating a New Test Case

1. **Create JML Specification** (in `IncrementalTestExample.java`):
```java
private static JmlFunctionSpec createMockMyFunctionSpec() {
    Variable param = new Variable("x", "int");
    FunctionSignature signature = new FunctionSignature(
        "myFunction",
        Arrays.asList(param),
        "void"
    );
    
    Expr pre = createBinaryExpr(
        AstHelper.createNameExpr("x"),
        createIntegerLiteral(0),
        "GREATER_THAN"
    );
    
    Expr post = createBinaryExpr(
        AstHelper.createNameExpr("x"),
        createIntegerLiteral(0),
        "GREATER_THAN"
    );
    
    return new JmlFunctionSpec("myFunction", signature, pre, post);
}
```

2. **Create Test Sequence**:
```java
TestStringAst testStringAst = new TestStringAst(
    Arrays.asList("myFunction", "myFunction")
);
```

3. **Generate ATCs**:
```java
GenATC genAtc = new NewGenATC();
AtcClass atcClass = genAtc.generateAtcFile(jmlSpecAst, testStringAst);
SpfWrapper spfWrapper = new SpfWrapper();
spfWrapper.run(atcClass);
```

---

## Conclusion

The PL Platform Testing project provides a solid foundation for automatic test generation from JML specifications. The core architecture is well-designed with a clear separation of concerns:

- **Parsing Layer**: Ready for JML parser implementation
- **ATC Generation**: Fully functional for base cases
- **Symbolic Execution Integration**: Framework in place, needs actual JPF execution
- **Code Generation**: Complete and working

**Current State**: The system works well for simple to moderate complexity examples with primitive types, arrays, and basic collections. Complex examples and return value handling need additional work.

**Next Steps**: Focus on implementing return value handling, actual JPF execution integration, and JML parser to make the system production-ready.

---

## References

- **Java PathFinder (JPF)**: https://github.com/javapathfinder/jpf-core
- **Symbolic PathFinder**: https://github.com/SymbolicPathFinder/jpf-symbc
- **JML Specification**: http://www.eecs.ucf.edu/~leavens/JML/
- **Project Note**: See `note.md` for implementation notes

---

###  Authors

* **Areen Vaghasiya** 
* **Siddharth Palod**
