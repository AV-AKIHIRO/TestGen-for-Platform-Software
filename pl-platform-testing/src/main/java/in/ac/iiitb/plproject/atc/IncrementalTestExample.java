package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.*;
import in.ac.iiitb.plproject.ast.AstHelper;

import java.util.*;

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
     * Simple test case: increment function
     */
    private static void testSimpleExample() {
        System.out.println("--- Test Case 1: Simple Increment ---");
        
        try {
            // Create a mock JML function spec
            // In real implementation, this would come from the JML parser
            JmlFunctionSpec spec = createMockIncrementSpec();
            
            List<JmlFunctionSpec> specs = Arrays.asList(spec);
            JmlSpecAst jmlSpecAst = new JmlSpecAst(specs);
            
            // Create test string: test increment function
            TestStringAst testStringAst = new TestStringAst(
                Arrays.asList("increment", "increment", "increment")
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
            JavaFile result = genAtc.generateAtcFile(jmlSpecAst, testStringAst);
            
            System.out.println("Generated Java Test Code:");
            System.out.println(result.getContent());
            System.out.println();
            
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
            JavaFile result = genAtc.generateAtcFile(jmlSpecAst, testStringAst);
            
            System.out.println("Generated Java Test Code:");
            System.out.println(result.getContent());
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error in complex example: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // Pre-condition: x > 0
        // Using AstHelper to create expressions (since Expr classes are package-private)
        Object pre = createBinaryExpr(
            AstHelper.createNameExpr("x"),
            createIntegerLiteral(0),
            "GREATER_THAN"
        );
        
        // Post-condition: x_post > x (x_post represents x')
        Object post = createBinaryExpr(
            AstHelper.createNameExpr("x_post"),
            AstHelper.createNameExpr("x"),
            "GREATER_THAN"
        );
        
        return new JmlFunctionSpec("increment", signature, pre, post);
    }

    /**
     * Helper method to create a mock JML spec for process function.
     */
    private static JmlFunctionSpec createMockProcessSpec() {
        // Create function signature: process(data: Set, result: Map) -> void
        Variable param1 = new Variable("data", "Set");
        Variable param2 = new Variable("result", "Map");
        FunctionSignature signature = new FunctionSignature(
            "process",
            Arrays.asList(param1, param2),
            "void"
        );
        
        // Pre-condition: new Set([1,2,3]).contains(2)
        // Using helper methods to create expressions
        Object pre = createMethodCall(
            createObjectCreation("Set", Arrays.asList(
                createIntegerLiteral(1),
                createIntegerLiteral(2),
                createIntegerLiteral(3)
            )),
            "contains",
            Arrays.asList(createIntegerLiteral(2))
        );
        
        // Post-condition: result_post == update(result, data)
        Object post = createBinaryExpr(
            AstHelper.createNameExpr("result_post"),
            createMethodCall(
                null,
                "update",
                Arrays.asList(
                    AstHelper.createNameExpr("result"),
                    AstHelper.createNameExpr("data")
                )
            ),
            "EQUALS"
        );
        
        return new JmlFunctionSpec("process", signature, pre, post);
    }
    
    // ===================================
    // Helper methods to create AST expressions using AstHelper
    // ===================================
    
    /**
     * Helper to create BinaryExpr using AstHelper.
     */
    private static Object createBinaryExpr(Object left, Object right, String operator) {
        return AstHelper.createBinaryExpr(left, right, operator);
    }
    
    /**
     * Helper to create MethodCallExpr using AstHelper.
     */
    private static Object createMethodCall(Object scope, String methodName, List<Object> args) {
        return AstHelper.createMethodCallExpr(scope, methodName, args);
    }
    
    /**
     * Helper to create ObjectCreationExpr using AstHelper.
     */
    private static Object createObjectCreation(String typeName, List<Object> args) {
        return AstHelper.createObjectCreationExpr(typeName, args);
    }
    
    /**
     * Helper to create IntegerLiteralExpr using AstHelper.
     */
    private static Object createIntegerLiteral(int value) {
        return AstHelper.createIntegerLiteralExpr(value);
    }
    
    // ===================================
    // Debug printing methods
    // ===================================
    
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
