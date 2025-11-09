package in.ac.iiitb.plproject.ast;

import java.util.*;

/**
 * Helper class for working with AST expressions.
 * This class is in the ast package so it can access package-private classes.
 * Other packages can use this class to work with AST expressions.
 */
public class AstHelper {
    
    /**
     * Extract variable name from a NameExpr.
     */
    public static String getNameFromExpr(Object expr) {
        if (expr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) expr;
            if (nameExpr.name instanceof SimpleName) {
                return ((SimpleName) nameExpr.name).identifier;
            }
        }
        return null;
    }
    
    /**
     * Create a NameExpr from a string name.
     */
    public static NameExpr createNameExpr(String name) {
        return new NameExpr(new SimpleName(name));
    }
    
    /**
     * Transform post-condition expressions.
     * Handles post-state variables (x_post -> x, x -> x_old).
     */
    public static Object transformPostCondition(Object expr, Set<String> postStateVars) {
        // TODO: Implement - transform post-state expressions
        // Similar to removethedashexpr from Version1
        if (expr instanceof Expr) {
            Expr e = (Expr) expr;
            // Implement transformation logic here
            return e;
        }
        return expr;
    }
    
    /**
     * Collect variables that appear in post-state.
     */
    public static Set<String> collectPostStateVariables(Object expr) {
        // TODO: Implement - collect variables that need old state saved
        // Similar to addthedashexpr from Version1
        Set<String> result = new HashSet<>();
        if (expr instanceof Expr) {
            Expr e = (Expr) expr;
            // Implement collection logic here
        }
        return result;
    }
    
    /**
     * Convert an AST expression to Java code string.
     */
    public static String exprToJavaCode(Object expr) {
        // TODO: Implement - convert AST Expr to Java code string
        if (expr instanceof Expr) {
            Expr e = (Expr) expr;
            // Implement conversion logic here
            return e.toString();
        }
        return expr.toString();
    }
    
    // ===================================
    // Factory methods for creating expressions
    // ===================================
    
    /**
     * Create a BinaryExpr.
     */
    public static BinaryExpr createBinaryExpr(Object left, Object right, String operatorName) {
        BinaryExpr.Operator op = BinaryExpr.Operator.valueOf(operatorName);
        return new BinaryExpr((Expr) left, (Expr) right, op);
    }
    
    /**
     * Create a MethodCallExpr.
     */
    public static MethodCallExpr createMethodCallExpr(Object scope, String methodName, List<Object> args) {
        List<Expr> exprArgs = new ArrayList<>();
        for (Object arg : args) {
            exprArgs.add((Expr) arg);
        }
        return new MethodCallExpr(
            scope != null ? (Expr) scope : null,
            new SimpleName(methodName),
            exprArgs
        );
    }
    
    /**
     * Create an ObjectCreationExpr.
     */
    public static ObjectCreationExpr createObjectCreationExpr(String typeName, List<Object> args) {
        List<Expr> exprArgs = new ArrayList<>();
        for (Object arg : args) {
            exprArgs.add((Expr) arg);
        }
        return new ObjectCreationExpr(
            new ClassOrInterfaceType(new SimpleName(typeName)),
            exprArgs
        );
    }
    
    /**
     * Create an IntegerLiteralExpr.
     */
    public static IntegerLiteralExpr createIntegerLiteralExpr(int value) {
        return new IntegerLiteralExpr(value);
    }

    /**
     * Create a UnaryExpr.
     */
    public static UnaryExpr createUnaryExpr(Object expr, String operatorName) {
        UnaryExpr.Operator op = UnaryExpr.Operator.valueOf(operatorName);
        return new UnaryExpr((Expr) expr, op);
    }

    /**
     * Combine multiple expressions with AND operator.
     * Useful for combining multiple requires/ensures clauses.
     */
    public static Object combineExpressionsWithAnd(List<Object> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        }
        // Combine all expressions with AND: expr1 && expr2 && expr3 ...
        Object result = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            result = createBinaryExpr(result, expressions.get(i), "AND");
        }
        return result;
    }

    /**
     * Combine two expressions with AND operator.
     */
    public static Object combineWithAnd(Object left, Object right) {
        if (left == null) return right;
        if (right == null) return left;
        return createBinaryExpr(left, right, "AND");
    }
}

