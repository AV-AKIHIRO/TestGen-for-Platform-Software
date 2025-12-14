package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.*;
import in.ac.iiitb.plproject.ast.AstHelper;
import in.ac.iiitb.plproject.ast.Expr;
import in.ac.iiitb.plproject.atc.ir.AtcClass;
import in.ac.iiitb.plproject.atc.ir.AtcTestMethod;
import in.ac.iiitb.plproject.atc.ir.AtcStatement;
import in.ac.iiitb.plproject.atc.ir.AtcSymbolicVarDecl;
import in.ac.iiitb.plproject.atc.ir.AtcVarDecl;
import in.ac.iiitb.plproject.atc.ir.AtcAssumeStmt;
import in.ac.iiitb.plproject.atc.ir.AtcMethodCallStmt;
import in.ac.iiitb.plproject.atc.ir.AtcAssertStmt;
import in.ac.iiitb.plproject.symex.SpfWrapper;

import java.util.*;
import java.util.Arrays;

/**
 * Incremental Test Example for NewGenATC Algorithm
 * 
 * Use this class to test your algorithm implementation incrementally.
 * This creates mock JML specs and test strings to test the GenATC implementation.
 */
public class IncrementalTestExample {

    public static void main(String[] args) {
        System.out.println("=== NewGenATC Algorithm - Incremental Testing ===\n");

        // Run different test cases
        if (args.length > 0) {
            String testCase = args[0];
            switch (testCase.toLowerCase()) {
                case "simple":
                    testSimpleExample();
                    break;
                case "complex":
                    testComplexExample();
                    break;
                case "all":
                    testSimpleExample();
                    testComplexExample();
                    break;
                default:
                    System.out.println("Unknown test case: " + testCase);
                    System.out.println("Available: simple, complex, all");
            }
        } else {
            // Default: run simple example
            testSimpleExample();
        }
    }

    /**
     * Simple test case: appendExclamation function - simple example without arrays
     */
    private static void testSimpleExample() {
        System.out.println("--- Test Case 1: Simple AppendExclamation ---");
        
        try {
            // Create a mock JML function spec
            // In real implementation, this would come from the JML parser
            JmlFunctionSpec spec = createMockAppendExclamationSpec();
            
            List<JmlFunctionSpec> specs = Arrays.asList(spec);
            JmlSpecAst jmlSpecAst = new JmlSpecAst(specs);
            
            // Create test string: test appendExclamation function
            TestStringAst testStringAst = new TestStringAst(
                Arrays.asList("appendExclamation", "appendExclamation", "appendExclamation")
            );
            
            // Print the JML Spec AST for debugging
            System.out.println("Input JML Spec AST:");
            printJmlSpecAst(jmlSpecAst);
            System.out.println("Complete JML Spec AST:");
            System.out.println(jmlSpecAst);
            System.out.println();
            
            System.out.println("Test String AST:");
            System.out.println("  Calls: " + testStringAst.getCalls());
            System.out.println();
            
            GenATC genAtc = new NewGenATC();
            AtcClass atcClass = genAtc.generateAtcFile(jmlSpecAst, testStringAst);
            
            // Print the generated IR structure for verification
            System.out.println("Generated ATC IR Structure:");
            printAtcClassIr(atcClass);
            System.out.println("Complete ATC IR Structure:");
            System.out.println(atcClass);
            System.out.println();
            
            // Use SpfWrapper to transform and save files (prints both simple and JPF versions, and saves to outputs/)
            // Now using IR-based approach directly - no string conversion needed
            SpfWrapper spfWrapper = new SpfWrapper();
            spfWrapper.run(atcClass);
            
        } catch (Exception e) {
            System.err.println("Error in simple example: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Complex test case: process function with object creation
     */
    private static void testComplexExample() {
        System.out.println("--- Test Case 2: Complex Example ---");
        
        try {
            // Create a mock JML function spec for process
            JmlFunctionSpec spec = createMockProcessSpec();
            
            List<JmlFunctionSpec> specs = Arrays.asList(spec);
            JmlSpecAst jmlSpecAst = new JmlSpecAst(specs);
            
            // Create test string
            TestStringAst testStringAst = new TestStringAst(
                Arrays.asList("process")
            );
            
            // Print the JML Spec AST for debugging
            System.out.println("Input JML Spec AST:");
            printJmlSpecAst(jmlSpecAst);
            System.out.println("Complete JML Spec AST:");
            System.out.println(jmlSpecAst);
            System.out.println();
            
            System.out.println("Test String AST:");
            System.out.println("  Calls: " + testStringAst.getCalls());
            System.out.println();
            
            GenATC genAtc = new NewGenATC();
            AtcClass atcClass = genAtc.generateAtcFile(jmlSpecAst, testStringAst);
            
            // Print the generated IR structure for verification
            System.out.println("Generated ATC IR Structure:");
            printAtcClassIr(atcClass);
            System.out.println("Complete ATC IR Structure:");
            System.out.println(atcClass);
            System.out.println();
            
            // Use SpfWrapper to transform and save files (prints both simple and JPF versions, and saves to outputs/)
            // Now using IR-based approach directly - no string conversion needed
            SpfWrapper spfWrapper = new SpfWrapper();
            spfWrapper.run(atcClass);
            
        } catch (Exception e) {
            System.err.println("Error in complex example: " + e.getMessage());
            e.printStackTrace();
        }
    }



    /**
     * Helper method to create a mock JML spec for appendExclamation function.
     * Simple example: appends "!" to a string (no arrays, just a simple String parameter).
     * In real implementation, this would come from the JML parser.
     */
    private static JmlFunctionSpec createMockAppendExclamationSpec() {
        // Create function signature: appendExclamation(s: String) -> void
        Variable param = new Variable("s", "String");
        FunctionSignature signature = new FunctionSignature(
            "appendExclamation",
            Arrays.asList(param),
            "void"
        );
        
        // Pre-condition: s != null (string must not be null)
        Expr pre = createBinaryExpr(
            AstHelper.createNameExpr("s"),
            AstHelper.createNameExpr("null"),
            "NOT_EQUALS"
        );
        
        // Post-condition: s != null (string is still not null after modification)
        Expr post = createBinaryExpr(
            AstHelper.createNameExpr("s"),
            AstHelper.createNameExpr("null"),
            "NOT_EQUALS"
        );
        
        return new JmlFunctionSpec("appendExclamation", signature, pre, post);
    }

    /**
     * Helper method to create a mock JML spec for increment function.
     * In real implementation, this would come from the JML parser.
     */
    private static JmlFunctionSpec createMockIncrementSpec() {
        // Create function signature: increment(x: int) -> void
        Variable param = new Variable("x", "int");
        FunctionSignature signature = new FunctionSignature(
            "increment",
            Arrays.asList(param),
            "void"
        );
        
        // Pre-condition: x[0] > 0
        // Using AstHelper to create expressions (since Expr classes are package-private)
        Expr pre = createBinaryExpr(
            AstHelper.createNameExpr("x[0]"),
            createIntegerLiteral(0),
            "GREATER_THAN"
        );
        
        // Post-condition: x[0] > '([x[0]]) (new value > old value, using prime operator notation)
        List<Expr> primeArgs = new ArrayList<>();
        primeArgs.add(AstHelper.createNameExpr("x[0]"));
        // Convert List<Expr> to List<Object> for the helper method
        List<Object> primeArgsObj = new ArrayList<>(primeArgs);
        Expr post = createBinaryExpr(
            AstHelper.createNameExpr("x[0]"),
            createMethodCall(null, "'", primeArgsObj),
            "GREATER_THAN"
        );
        
        return new JmlFunctionSpec("increment", signature, pre, post);
    }

    /**
     * Helper method to create a mock JML spec for process function.
     */
    private static JmlFunctionSpec createMockProcessSpec() {
        // Create function signature: process(data: Set<Integer>, result: Map<Integer, Integer>) -> void
        Variable param1 = new Variable("data", "Set<Integer>");
        Variable param2 = new Variable("result", "Map<Integer, Integer>");
        FunctionSignature signature = new FunctionSignature(
            "process",
            Arrays.asList(param1, param2),
            "void"
        );
        
        // Pre-condition: new Set([1,2,3]).contains(2)
        // Using helper methods to create expressions
        List<Expr> setArgs = Arrays.asList(
            createIntegerLiteral(1),
            createIntegerLiteral(2),
            createIntegerLiteral(3)
        );
        List<Expr> containsArgs = Arrays.asList(createIntegerLiteral(2));
        // Convert List<Expr> to List<Object> for the helper method
        List<Object> containsArgsObj = new ArrayList<>(containsArgs);
        Expr pre = createMethodCall(
            createObjectCreation("Set", setArgs),
            "contains",
            containsArgsObj
        );
        
        // Post-condition: '(result) != null (simplified - just check result is not null after process)
        List<Expr> primeArgs = new ArrayList<>();
        primeArgs.add(AstHelper.createNameExpr("result"));
        // Convert List<Expr> to List<Object> for the helper method
        List<Object> primeArgsObj = new ArrayList<>(primeArgs);
        Expr post = createBinaryExpr(
            createMethodCall(null, "'", primeArgsObj),
            AstHelper.createNameExpr("null"),
            "NOT_EQUALS"
        );
        
        return new JmlFunctionSpec("process", signature, pre, post);
    }
    
    // ===================================
    // Helper methods to create AST expressions using AstHelper
    // ===================================
    
    /**
     * Helper to create BinaryExpr using AstHelper.
     */
    private static Expr createBinaryExpr(Expr left, Expr right, String operator) {
        return AstHelper.createBinaryExpr(left, right, operator);
    }
    
    /**
     * Helper to create MethodCallExpr using AstHelper.
     */
    private static Expr createMethodCall(Expr scope, String methodName, List<Object> args) {
        // Convert List<Object> to List<Expr> for AstHelper
        List<Expr> argsExpr = new ArrayList<>();
        for (Object arg : args) {
            argsExpr.add((Expr) arg);
        }
        return AstHelper.createMethodCallExpr(scope, methodName, argsExpr);
    }
    
    /**
     * Helper to create ObjectCreationExpr using AstHelper.
     */
    private static Expr createObjectCreation(String typeName, List<Expr> args) {
        return AstHelper.createObjectCreationExpr(typeName, args);
    }
    
    /**
     * Helper to create IntegerLiteralExpr using AstHelper.
     */
    private static Expr createIntegerLiteral(int value) {
        return (Expr) AstHelper.createIntegerLiteralExpr(value);
    }
    
    // ===================================
    // Debug printing methods
    // ===================================
    
    /**
     * Print the ATC IR structure in a readable format for verification.
     */
    private static void printAtcClassIr(AtcClass atcClass) {
        System.out.println("  Package: " + atcClass.getPackageName());
        System.out.println("  Class Name: " + atcClass.getClassName());
        
        if (atcClass.getRunWithAnnotationClass() != null && !atcClass.getRunWithAnnotationClass().isEmpty()) {
            System.out.println("  @RunWith: " + atcClass.getRunWithAnnotationClass());
        }
        
        System.out.println("  Imports (" + atcClass.getImports().size() + "):");
        for (String imp : atcClass.getImports()) {
            System.out.println("    - " + imp);
        }
        
        System.out.println("  Test Methods (" + atcClass.getTestMethods().size() + "):");
        for (int i = 0; i < atcClass.getTestMethods().size(); i++) {
            printAtcTestMethod(atcClass.getTestMethods().get(i), i + 1);
        }
    }
    
    /**
     * Print a single test method IR structure.
     */
    private static void printAtcTestMethod(AtcTestMethod method, int index) {
        System.out.println("    Method " + index + ":");
        System.out.println("      Name: " + method.getMethodName());
        System.out.println("      @Test: " + method.isTestAnnotated());
        System.out.println("      Statements (" + method.getStatements().size() + "):");
        
        for (int i = 0; i < method.getStatements().size(); i++) {
            AtcStatement stmt = method.getStatements().get(i);
            System.out.print("        " + (i + 1) + ". ");
            printAtcStatement(stmt);
        }
    }
    
    /**
     * Print a single statement IR structure.
     * Uses AstHelper.exprToJavaCode() to show Java/JML-equivalent syntax.
     */
    private static void printAtcStatement(AtcStatement stmt) {
        if (stmt instanceof AtcSymbolicVarDecl) {
            AtcSymbolicVarDecl varDecl = (AtcSymbolicVarDecl) stmt;
            System.out.println("AtcSymbolicVarDecl: " + varDecl.getTypeName() + " " + varDecl.getVarName());
        } else if (stmt instanceof AtcVarDecl) {
            AtcVarDecl varDecl = (AtcVarDecl) stmt;
            System.out.print("AtcVarDecl: " + varDecl.getTypeName() + " " + varDecl.getVarName() + " = ");
            System.out.println(AstHelper.exprToJavaCode(varDecl.getInitExpr()));
        } else if (stmt instanceof AtcAssumeStmt) {
            AtcAssumeStmt assume = (AtcAssumeStmt) stmt;
            System.out.println("AtcAssumeStmt: " + AstHelper.exprToJavaCode(assume.getCondition()));
        } else if (stmt instanceof AtcMethodCallStmt) {
            AtcMethodCallStmt call = (AtcMethodCallStmt) stmt;
            System.out.println("AtcMethodCallStmt: " + AstHelper.exprToJavaCode(call.getCallExpr()));
        } else if (stmt instanceof AtcAssertStmt) {
            AtcAssertStmt assertStmt = (AtcAssertStmt) stmt;
            System.out.println("AtcAssertStmt: " + AstHelper.exprToJavaCode(assertStmt.getCondition()));
        } else {
            System.out.println("Unknown AtcStatement type: " + stmt.getClass().getSimpleName());
        }
    }
    
    /**
     * Print the JML Spec AST in a readable format for debugging.
     */
    private static void printJmlSpecAst(JmlSpecAst jmlSpecAst) {
        // Use reflection to access the specs list since it's private
        try {
            java.lang.reflect.Field specsField = JmlSpecAst.class.getDeclaredField("specs");
            specsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<JmlFunctionSpec> specs = (List<JmlFunctionSpec>) specsField.get(jmlSpecAst);
            
            System.out.println("  Number of specs: " + specs.size());
            System.out.println();
            
            for (int i = 0; i < specs.size(); i++) {
                JmlFunctionSpec spec = specs.get(i);
                printJmlFunctionSpec(spec, i + 1);
            }
        } catch (Exception e) {
            System.err.println("  Error printing JmlSpecAst: " + e.getMessage());
            System.out.println("  " + jmlSpecAst.toString());
        }
    }
    
    /**
     * Print a single JML Function Spec in a readable format.
     */
    private static void printJmlFunctionSpec(JmlFunctionSpec spec, int index) {
        System.out.println("  Spec " + index + ":");
        System.out.println("    Name: " + spec.getName());
        
        FunctionSignature sig = spec.getSignature();
        if (sig != null) {
            System.out.println("    Signature:");
            System.out.println("      Function: " + sig.getName() + "(" + formatParameters(sig.getParameters()) + ")");
            System.out.println("      Return Type: " + sig.getReturnTypeName());
        }
        
        Object pre = spec.getPrecondition();
        if (pre != null) {
            System.out.println("    Pre-condition: " + pre.toString());
        } else {
            System.out.println("    Pre-condition: (none)");
        }
        
        Object post = spec.getPostcondition();
        if (post != null) {
            System.out.println("    Post-condition: " + post.toString());
        } else {
            System.out.println("    Post-condition: (none)");
        }
        System.out.println();
    }
    
    /**
     * Format parameters list as a string.
     */
    private static String formatParameters(List<Variable> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            Variable v = params.get(i);
            sb.append(v.getName()).append(": ").append(v.getTypeName());
        }
        return sb.toString();
    }
}
