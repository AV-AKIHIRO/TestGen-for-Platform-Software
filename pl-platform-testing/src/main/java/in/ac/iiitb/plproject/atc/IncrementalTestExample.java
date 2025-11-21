package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.*;
import in.ac.iiitb.plproject.ast.AstHelper;
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
            AtcClass atcClass = genAtc.generateAtcFile(jmlSpecAst, testStringAst);
            
            // Print the generated IR structure for verification
            System.out.println("Generated ATC IR Structure:");
            printAtcClassIr(atcClass);
            System.out.println("Complete ATC IR Structure:");
            System.out.println(atcClass);
            System.out.println();
            
            // Convert IR to Java code string for display using prettyPrint
            String javaCode = genAtc.prettyPrint(atcClass);
            
            // Demonstrate SpfWrapper transformation (prints both simple and JPF versions)
            SpfWrapper spfWrapper = new SpfWrapper();
            spfWrapper.printBothVersions(javaCode);
            
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
            
            // Convert IR to Java code string for display using prettyPrint
            String javaCode = genAtc.prettyPrint(atcClass);
            
            // Demonstrate SpfWrapper transformation (prints both simple and JPF versions)
            SpfWrapper spfWrapper = new SpfWrapper();
            spfWrapper.printBothVersions(javaCode);
            
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
        
        // Cast Object to Expr for constructor
        return new JmlFunctionSpec("increment", signature, 
            (in.ac.iiitb.plproject.ast.Expr)pre, 
            (in.ac.iiitb.plproject.ast.Expr)post);
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
        List<Object> updateArgs = new ArrayList<>();
        updateArgs.add(AstHelper.createNameExpr("result"));
        updateArgs.add(AstHelper.createNameExpr("data"));
        Object post = createBinaryExpr(
            AstHelper.createNameExpr("result_post"),
            createMethodCall(null, "update", updateArgs),
            "EQUALS"
        );
        
        // Cast Object to Expr for constructor
        return new JmlFunctionSpec("process", signature, 
            (in.ac.iiitb.plproject.ast.Expr)pre, 
            (in.ac.iiitb.plproject.ast.Expr)post);
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
