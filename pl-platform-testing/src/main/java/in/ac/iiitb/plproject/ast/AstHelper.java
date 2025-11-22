package in.ac.iiitb.plproject.ast;

import java.util.*;
import in.ac.iiitb.plproject.parser.ast.Variable;

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
     * Also handles prime operator notation ('(x) -> x or result).
     */
    public static Object transformPostCondition(Object expr, String resultVarName, Map<String, String> oldStateMap, List<Variable> params) {
        if (expr == null) {
            return null;
        }
        if (!(expr instanceof Expr)) {
            return expr; // Return as is if not an AST expression
        }
        return transformPostConditionRecursive((Expr) expr, resultVarName, oldStateMap, params, false);
    }

    private static Expr transformPostConditionRecursive(Expr expr, String resultVarName, Map<String, String> oldStateMap, List<Variable> params, boolean insidePrime) {
        if (expr == null) {
            return null;
        }

        if (expr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) expr;
            String name = ((SimpleName) nameExpr.name).identifier;

            // Case 1: We're inside a prime operator (insidePrime = true)
            // The variable represents post-state
            if (insidePrime) {
                // If there's a return value, post-state variables map to resultVarName
                // Otherwise, they represent in-place modification (keep the name as-is)
                if (resultVarName != null) {
                    // Check if this variable is a parameter (might be modified in-place or returned)
                    // For simplicity, if resultVarName exists, map to result
                    // This matches the behavior for _post suffix
                    return createNameExpr(resultVarName);
                } else {
                    // Void function: variable represents in-place modification, keep name as-is
                    return nameExpr;
                }
            }
            
            // Case 2: Variable needs to be replaced with its old state (e.g., x -> old_x)
            // Only if we're NOT inside a prime operator
            if (oldStateMap.containsKey(name)) {
                return createNameExpr(oldStateMap.get(name));
            }
            
            // Case 3: Post-state variable (e.g., x_post)
            if (name.endsWith("_post")) {
                String baseName = name.substring(0, name.length() - "_post".length());
                // If there's a return value, x_post maps to resultVarName
                if (resultVarName != null) {
                    // Simplified: assume if resultVarName exists, post-state vars map to it
                    return createNameExpr(resultVarName);
                }
                // If void function, x_post maps to x (in-place modification)
                else {
                    return createNameExpr(baseName);
                }
            }
            
            // Case 4: Other NameExpr - return as is
            return nameExpr;
        } else if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
            
            // Handle prime operator: '(x) -> extract x and transform it
            if (methodCallExpr.name.identifier.equals("'")) {
                // Remove the prime operator wrapper and transform the inner expression
                // The inner expression represents post-state, so we pass insidePrime=true
                if (!methodCallExpr.args.isEmpty()) {
                    return transformPostConditionRecursive(methodCallExpr.args.get(0), resultVarName, oldStateMap, params, true);
                } else {
                    return null; // Invalid prime operator call with no arguments
                }
            } else {
                // Regular method call - transform scope and arguments
                Expr scope = transformPostConditionRecursive(methodCallExpr.scope, resultVarName, oldStateMap, params, false);
                List<Object> args = new ArrayList<>();
                for (Expr arg : methodCallExpr.args) {
                    args.add(transformPostConditionRecursive(arg, resultVarName, oldStateMap, params, false));
                }
                
                // If scope is null and method name is a known helper function (like "update"), prefix with Helper.
                if (scope == null && isKnownHelperFunction(methodCallExpr.name.identifier)) {
                    scope = createNameExpr("Helper");
                }
                
                return createMethodCallExpr(scope, methodCallExpr.name.identifier, args);
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binExpr = (BinaryExpr) expr;
            Expr left = transformPostConditionRecursive(binExpr.left, resultVarName, oldStateMap, params, false);
            Expr right = transformPostConditionRecursive(binExpr.right, resultVarName, oldStateMap, params, false);
            return createBinaryExpr(left, right, binExpr.op.toString());
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) expr;
            Expr innerExpr = transformPostConditionRecursive(unaryExpr.expr, resultVarName, oldStateMap, params, false);
            return createUnaryExpr(innerExpr, unaryExpr.op.toString());
        } else if (expr instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) expr;
            Expr scope = transformPostConditionRecursive(fieldAccessExpr.scope, resultVarName, oldStateMap, params, false);
            return new FieldAccessExpr(scope, fieldAccessExpr.field);
        } else if (expr instanceof SetExpr) {
            SetExpr setExpr = (SetExpr) expr;
            List<Expr> elements = new ArrayList<>();
            for (Expr element : setExpr.elements) {
                elements.add(transformPostConditionRecursive(element, resultVarName, oldStateMap, params, false));
            }
            return new SetExpr(elements);
        } else if (expr instanceof MapExpr) {
            MapExpr mapExpr = (MapExpr) expr;
            List<Pair<NameExpr, Expr>> entries = new ArrayList<>();
            for (Pair<NameExpr, Expr> entry : mapExpr.entries) {
                // Key is NameExpr, Value is Expr
                NameExpr key = (NameExpr) transformPostConditionRecursive(entry.key, resultVarName, oldStateMap, params, false);
                Expr value = transformPostConditionRecursive(entry.value, resultVarName, oldStateMap, params, false);
                entries.add(new Pair<>(key, value));
            }
            return new MapExpr(entries);
        } else if (expr instanceof TupleExpr) {
            TupleExpr tupleExpr = (TupleExpr) expr;
            List<Expr> elements = new ArrayList<>();
            for (Expr element : tupleExpr.elements) {
                elements.add(transformPostConditionRecursive(element, resultVarName, oldStateMap, params, false));
            }
            return new TupleExpr(elements);
        } else if (expr instanceof ObjectCreationExpr) {
            ObjectCreationExpr objCreationExpr = (ObjectCreationExpr) expr;
            List<Object> args = new ArrayList<>();
            for (Expr arg : objCreationExpr.args) {
                args.add(transformPostConditionRecursive(arg, resultVarName, oldStateMap, params, false));
            }
            return createObjectCreationExpr(((ClassOrInterfaceType) objCreationExpr.type).name.identifier, args);
        }
        // For literals (IntegerLiteralExpr, DoubleLiteralExpr, StringLiteralExpr, BooleanLiteralExpr) and ThisExpr, return as is.
        return expr;
    }
    
    /**
     * Collect variables that appear in post-state.
     * This method collects variables that need old state saved.
     * Handles both '_post' suffix notation and prime operator ('(x)) notation.
     */
    public static Set<String> collectPostStateVariables(Object expr) {
        // This is now implemented via collectVarsToSnapshot
        return collectVarsToSnapshot(expr);
    }

    /**
     * Collect variables that need to be snapshotted (saved as old state).
     * Handles both '_post' suffix notation and prime operator ('(x)) notation.
     * For post-condition "x_post > x" or "'(x) > x", this returns {"x"}.
     */
    public static Set<String> collectVarsToSnapshot(Object expr) {
        Set<String> result = new HashSet<>();
        if (!(expr instanceof Expr)) {
            return result;
        }
        collectVarsToSnapshotRecursive((Expr) expr, result);
        return result;
    }
    
    /**
     * Finds if a parameter is referenced in post-state (with prime notation or _post suffix).
     * Returns the first parameter name found that appears in post-state, or null if none found.
     * This is used to determine if a method's return value should be assigned back to a parameter.
     * 
     * @param post The postcondition expression
     * @param paramNames List of parameter names to check
     * @return Parameter name if referenced in post-state, null otherwise
     */
    public static String findPostStateParameter(Object post, List<String> paramNames) {
        if (post == null || !(post instanceof Expr)) {
            return null;
        }
        return findPostStateParameterRecursive((Expr) post, paramNames);
    }
    
    /**
     * Recursive helper to find parameter referenced in post-state.
     * Has direct access to AST classes since it's in the same package.
     */
    private static String findPostStateParameterRecursive(Expr expr, List<String> paramNames) {
        if (expr == null) {
            return null;
        }
        
        if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
            
            // Check for prime operator: '(x)
            if (methodCallExpr.name.identifier.equals("'") && !methodCallExpr.args.isEmpty()) {
                // Prime operator found - extract variable name from first argument
                Expr arg = methodCallExpr.args.get(0);
                String varName = getNameFromExpr(arg);
                if (varName != null && paramNames.contains(varName)) {
                    return varName;
                }
            }
            
            // Recursively check arguments
            for (Expr arg : methodCallExpr.args) {
                String result = findPostStateParameterRecursive(arg, paramNames);
                if (result != null) {
                    return result;
                }
            }
            
            // Check scope if present
            if (methodCallExpr.scope != null) {
                return findPostStateParameterRecursive(methodCallExpr.scope, paramNames);
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binExpr = (BinaryExpr) expr;
            String result = findPostStateParameterRecursive(binExpr.left, paramNames);
            if (result != null) {
                return result;
            }
            return findPostStateParameterRecursive(binExpr.right, paramNames);
        } else if (expr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) expr;
            String name = ((SimpleName) nameExpr.name).identifier;
            
            // Check for _post suffix
            if (name.endsWith("_post")) {
                String baseName = name.substring(0, name.length() - "_post".length());
                if (paramNames.contains(baseName)) {
                    return baseName;
                }
            }
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) expr;
            return findPostStateParameterRecursive(unaryExpr.expr, paramNames);
        }
        
        return null;
    }

    private static void collectVarsToSnapshotRecursive(Expr expr, Set<String> result) {
        if (expr == null) {
            return;
        }

        if (expr instanceof NameExpr) {
            String name = ((SimpleName) ((NameExpr) expr).name).identifier;
            if (!name.endsWith("_post")) {
                result.add(name);
            }
        } else if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
            
            // Handle prime operator: '(x) -> extract variable name from argument
            if (methodCallExpr.name.identifier.equals("'")) {
                // Collect the variable name from the first argument of the prime operator
                if (!methodCallExpr.args.isEmpty()) {
                    Expr arg0 = methodCallExpr.args.get(0);
                    String varName = getNameFromExpr(arg0);
                    if (varName != null) {
                        result.add(varName);
                    }
                }
                // Don't recurse into prime operator arguments - we've already handled it
                return;
            } else {
                // Regular method call - recurse into scope and arguments
                if (methodCallExpr.scope != null) {
                    collectVarsToSnapshotRecursive(methodCallExpr.scope, result);
                }
                for (Expr arg : methodCallExpr.args) {
                    collectVarsToSnapshotRecursive(arg, result);
                }
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binExpr = (BinaryExpr) expr;
            collectVarsToSnapshotRecursive(binExpr.left, result);
            collectVarsToSnapshotRecursive(binExpr.right, result);
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) expr;
            collectVarsToSnapshotRecursive(unaryExpr.expr, result);
        } else if (expr instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) expr;
            collectVarsToSnapshotRecursive(fieldAccessExpr.scope, result);
            // The field itself is a SimpleName, not an Expr, so no recursion on it directly.
        } else if (expr instanceof SetExpr) {
            SetExpr setExpr = (SetExpr) expr;
            for (Expr element : setExpr.elements) {
                collectVarsToSnapshotRecursive(element, result);
            }
        } else if (expr instanceof MapExpr) {
            MapExpr mapExpr = (MapExpr) expr;
            for (Pair<NameExpr, Expr> entry : mapExpr.entries) {
                collectVarsToSnapshotRecursive(entry.key, result);
                collectVarsToSnapshotRecursive(entry.value, result);
            }
        } else if (expr instanceof TupleExpr) {
            TupleExpr tupleExpr = (TupleExpr) expr;
            for (Expr element : tupleExpr.elements) {
                collectVarsToSnapshotRecursive(element, result);
            }
        }
        // Literals, ThisExpr, ObjectCreationExpr do not contain variables to snapshot directly
    }
    
    /**
     * Convert an AST expression to Java code string.
     */
    public static String exprToJavaCode(Object expr) {
        if (expr == null) {
            return "true"; // Represent null/empty expression as true in Java (e.g., for missing preconditions)
        }
        if (!(expr instanceof Expr)) {
            return expr.toString(); // Fallback for non-Expr objects (e.g., JmlFunctionSpec itself)
        }

        Expr e = (Expr) expr;

        if (e instanceof BinaryExpr) {
            BinaryExpr binExpr = (BinaryExpr) e;
            String left = exprToJavaCode(binExpr.left);
            String right = exprToJavaCode(binExpr.right);
            String operator = "";
            BinaryExpr.Operator op = binExpr.op;
            if (op == BinaryExpr.Operator.AND) {
                operator = " && ";
                // Strip outer parentheses from left and right if they exist, then combine
                String leftClean = stripOuterParens(left);
                String rightClean = stripOuterParens(right);
                return leftClean + operator + rightClean;
            } else if (op == BinaryExpr.Operator.OR) {
                operator = " || ";
                // Strip outer parentheses from left and right if they exist, then combine
                String leftClean = stripOuterParens(left);
                String rightClean = stripOuterParens(right);
                return leftClean + operator + rightClean;
            } else if (op == BinaryExpr.Operator.EQUALS) {
                // For EQUALS, use .equals() for object comparisons, == for primitives
                // Special case: if comparing with null, use simple == null
                if (left.equals("null") || right.equals("null")) {
                    String varName = left.equals("null") ? right : left;
                    return "(" + varName + " == null)";
                } else if (isObjectExpression(binExpr.left) || isObjectExpression(binExpr.right)) {
                    // Check if one side is a method call (which returns a new object)
                    // For Map comparisons, we need to use .equals() directly, not Objects.equals()
                    boolean leftIsMethodCall = binExpr.left instanceof MethodCallExpr;
                    boolean rightIsMethodCall = binExpr.right instanceof MethodCallExpr;
                    boolean mightBeMapComparison = isLikelyMapExpression(binExpr.left) || isLikelyMapExpression(binExpr.right);
                    
                    // If one side is a method call or this looks like a Map comparison,
                    // generate code that uses .equals() with explicit null checks
                    // This ensures Map.equals() is used for content comparison, not reference equality
                    if (leftIsMethodCall || rightIsMethodCall || mightBeMapComparison) {
                        // Generate: (left != null && right != null && left.equals(right))
                        // Note: This assumes the method call is on the right side
                        // For method calls on left, we'd need to handle it differently
                        if (rightIsMethodCall) {
                            // Method call on right: store result and compare
                            // We can't easily store it here, so generate direct comparison with null checks
                            return "(" + left + " != null && " + right + " != null && " + left + ".equals(" + right + "))";
                        } else if (leftIsMethodCall) {
                            // Method call on left: compare with right
                            return "(" + left + " != null && " + right + " != null && " + left + ".equals(" + right + "))";
                        } else {
                            // Map comparison: use .equals() with null checks
                            return "(" + left + " != null && " + right + " != null && " + left + ".equals(" + right + "))";
                        }
                    } else {
                        // Use .equals() for object comparisons with null safety
                        // Generate: (left != null && left.equals(right)) || (left == null && right == null)
                        // Or simpler: java.util.Objects.equals(left, right)
                        return "(java.util.Objects.equals(" + left + ", " + right + "))";
                    }
                } else {
                    operator = " == ";
                }
            } else if (op == BinaryExpr.Operator.NOT_EQUALS) {
                // For NOT_EQUALS, use !.equals() for object comparisons, != for primitives
                // Special case: if comparing with null, use simple != null
                if (left.equals("null") || right.equals("null")) {
                    String varName = left.equals("null") ? right : left;
                    return "(" + varName + " != null)";
                } else if (isObjectExpression(binExpr.left) || isObjectExpression(binExpr.right)) {
                    return "(!java.util.Objects.equals(" + left + ", " + right + "))";
                } else {
                    operator = " != ";
                }
            } else if (op == BinaryExpr.Operator.LESS_THAN) {
                operator = " < ";
            } else if (op == BinaryExpr.Operator.LESS_THAN_OR_EQUAL) {
                operator = " <= ";
            } else if (op == BinaryExpr.Operator.GREATER_THAN) {
                operator = " > ";
            } else if (op == BinaryExpr.Operator.GREATER_THAN_OR_EQUAL) {
                operator = " >= ";
            } else if (op == BinaryExpr.Operator.PLUS) {
                operator = " + ";
                // Keep parentheses for string concatenation (used in println)
                // This matches the correct version which has parentheses
            } else if (op == BinaryExpr.Operator.MINUS) {
                operator = " - ";
            } else if (op == BinaryExpr.Operator.MULTIPLY) {
                operator = " * ";
            } else if (op == BinaryExpr.Operator.DIVIDE) {
                operator = " / ";
            } else {
                operator = " " + binExpr.op.toString() + " "; // Fallback
            }
            return "(" + left + operator + right + ")";
        } else if (e instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) e;
            return ((SimpleName) nameExpr.name).identifier;
        } else if (e instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) e;
            StringBuilder sb = new StringBuilder();
            if (methodCallExpr.scope != null) {
                sb.append(exprToJavaCode(methodCallExpr.scope)).append(".");
            }
            sb.append(methodCallExpr.name.identifier).append("(");
            for (int i = 0; i < methodCallExpr.args.size(); i++) {
                sb.append(exprToJavaCode(methodCallExpr.args.get(i)));
                if (i < methodCallExpr.args.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            return sb.toString();
        } else if (e instanceof IntegerLiteralExpr) {
            return String.valueOf(((IntegerLiteralExpr) e).value);
        } else if (e instanceof DoubleLiteralExpr) {
            return String.valueOf(((DoubleLiteralExpr) e).value);
        } else if (e instanceof StringLiteralExpr) {
            return "\"" + ((StringLiteralExpr) e).value + "\"";
        } else if (e instanceof BooleanLiteralExpr) { // Handle BooleanLiteralExpr
            return String.valueOf(((BooleanLiteralExpr) e).value);
        } else if (e instanceof UnaryExpr) {
            UnaryExpr unaryExpr = (UnaryExpr) e;
            String expression = exprToJavaCode(unaryExpr.expr);
            String operator = "";
            UnaryExpr.Operator op = unaryExpr.op;
            if (op == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
                operator = "!";
            } else if (op == UnaryExpr.Operator.MINUS) {
                operator = "-";
            } else if (op == UnaryExpr.Operator.PLUS) {
                operator = "+";
            } else {
                operator = unaryExpr.op.toString(); // Fallback
            }
            return operator + expression;
        } else if (e instanceof ObjectCreationExpr) {
            ObjectCreationExpr objCreationExpr = (ObjectCreationExpr) e;
            String typeName = ((ClassOrInterfaceType) objCreationExpr.type).name.identifier;
            
            // Special handling for array types: "int[]" -> "new int[]{x}"
            if (typeName.endsWith("[]")) {
                String baseType = typeName.substring(0, typeName.length() - 2);
                StringBuilder sb = new StringBuilder("new ");
                sb.append(baseType).append("[]{");
                for (int i = 0; i < objCreationExpr.args.size(); i++) {
                    sb.append(exprToJavaCode(objCreationExpr.args.get(i)));
                    if (i < objCreationExpr.args.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("}");
                return sb.toString();
            }
            
            // Special handling for Set - cannot instantiate interface directly
            if (typeName.equals("Set")) {
                // Generate: new HashSet<>(Arrays.asList(...))
                StringBuilder sb = new StringBuilder("new HashSet<>(Arrays.asList(");
                for (int i = 0; i < objCreationExpr.args.size(); i++) {
                    sb.append(exprToJavaCode(objCreationExpr.args.get(i)));
                    if (i < objCreationExpr.args.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("))");
                return sb.toString();
            }
            
            // For other types, generate normally
            StringBuilder sb = new StringBuilder("new ");
            sb.append(typeName).append("(");
            for (int i = 0; i < objCreationExpr.args.size(); i++) {
                sb.append(exprToJavaCode(objCreationExpr.args.get(i)));
                if (i < objCreationExpr.args.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            return sb.toString();
        } else if (e instanceof ThisExpr) {
            return "this";
        } else if (e instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) e;
            return exprToJavaCode(fieldAccessExpr.scope) + "." + fieldAccessExpr.field.identifier;
        } else if (e instanceof SetExpr) {
            SetExpr setExpr = (SetExpr) e;
            StringBuilder sb = new StringBuilder("new HashSet<>(Arrays.asList(");
            for (int i = 0; i < setExpr.elements.size(); i++) {
                sb.append(exprToJavaCode(setExpr.elements.get(i)));
                if (i < setExpr.elements.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("))");
            return sb.toString();
        } else if (e instanceof MapExpr) {
            MapExpr mapExpr = (MapExpr) e;
            StringBuilder sb = new StringBuilder("new HashMap<>() {{");
            for (Pair<NameExpr, Expr> entry : mapExpr.entries) {
                sb.append(" put(").append(exprToJavaCode(entry.key)).append(", ").append(exprToJavaCode(entry.value)).append(");");
            }
            sb.append("}}");
            return sb.toString();
        } else if (e instanceof TupleExpr) {
            TupleExpr tupleExpr = (TupleExpr) e;
            StringBuilder sb = new StringBuilder("new Object[] {");
            for (int i = 0; i < tupleExpr.elements.size(); i++) {
                sb.append(exprToJavaCode(tupleExpr.elements.get(i)));
                if (i < tupleExpr.elements.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            return sb.toString();
        }
        // Fallback for unhandled Expr types
        return e.toString();
    }
    
    /**
     * Determines if an expression represents an object (vs a primitive).
     * This is used to decide whether to use .equals() or == for comparisons.
     * 
     * @param expr The expression to check
     * @return true if the expression likely represents an object, false if it's a primitive
     */
    private static boolean isObjectExpression(Expr expr) {
        if (expr == null) {
            return false;
        }
        
        // Method calls always return objects (or could return objects)
        if (expr instanceof MethodCallExpr) {
            return true;
        }
        
        // NameExpr (variables) - assume they're objects unless we know otherwise
        // This is a heuristic: variables like "result", "data" are objects
        if (expr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) expr;
            String name = ((SimpleName) nameExpr.name).identifier;
            // Primitives are usually single letters or common names
            // Objects are usually longer names or collection types
            // This is a heuristic - could be improved with type information
            return !isPrimitiveVariableName(name);
        }
        
        // Object creation expressions are objects
        if (expr instanceof ObjectCreationExpr) {
            return true;
        }
        
        // SetExpr, MapExpr, TupleExpr are objects
        if (expr instanceof SetExpr || expr instanceof MapExpr || expr instanceof TupleExpr) {
            return true;
        }
        
        // Binary expressions with arithmetic operations are usually primitives
        // But if they involve method calls or object variables, they could be objects
        if (expr instanceof BinaryExpr) {
            BinaryExpr binExpr = (BinaryExpr) expr;
            // If either side is an object, the result might be an object
            return isObjectExpression(binExpr.left) || isObjectExpression(binExpr.right);
        }
        
        // Literals (IntegerLiteralExpr, etc.) are primitives
        // Field access could be either, but default to object for safety
        return true; // Default to object for safety (use .equals())
    }
    
    /**
     * Checks if a variable name is likely a primitive variable.
     * This is a heuristic - ideally we'd have type information.
     */
    private static boolean isPrimitiveVariableName(String name) {
        // Common primitive variable names are short or follow patterns
        if (name.length() <= 2) {
            return true; // x, y, i, j, etc.
        }
        // Variables ending in common primitive suffixes
        if (name.endsWith("_old") || name.endsWith("_size") || name.endsWith("_count") || 
            name.endsWith("_val") || name.endsWith("_num")) {
            return true;
        }
        // Common primitive names
        return name.equals("x") || name.equals("y") || name.equals("z") ||
               name.equals("i") || name.equals("j") || name.equals("k") ||
               name.equals("count") || name.equals("size") || name.equals("value");
    }
    
    /**
     * Checks if an expression is likely a Map type.
     * This is a heuristic based on variable names and method calls.
     * 
     * @param expr The expression to check
     * @return true if the expression likely represents a Map
     */
    private static boolean isLikelyMapExpression(Expr expr) {
        if (expr == null) {
            return false;
        }
        
        // MapExpr is definitely a Map
        if (expr instanceof MapExpr) {
            return true;
        }
        
        // Check variable names that are commonly Maps
        if (expr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) expr;
            String name = ((SimpleName) nameExpr.name).identifier;
            // Common Map variable names
            return name.equals("result") || name.equals("map") || name.equals("data") ||
                   name.startsWith("result") || name.startsWith("map") ||
                   name.endsWith("Map") || name.endsWith("Result");
        }
        
        // Method calls that might return Maps (e.g., Helper.update(...))
        if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) expr;
            String methodName = methodCall.name.identifier;
            // Common Map-returning method names
            return methodName.equals("update") || methodName.equals("getMap") ||
                   methodName.equals("createMap") || methodName.endsWith("Map");
        }
        
        return false;
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
     * Checks if a method name is a known helper function that should be prefixed with Helper.
     */
    private static boolean isKnownHelperFunction(String methodName) {
        // List of known helper functions that should be called as Helper.methodName()
        return methodName.equals("update") || 
               methodName.equals("increment") || 
               methodName.equals("process");
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
     * Create a StringLiteralExpr.
     */
    public static StringLiteralExpr createStringLiteralExpr(String value) {
        return new StringLiteralExpr(value);
    }

    /**
     * Create a UnaryExpr.
     */
    public static UnaryExpr createUnaryExpr(Object expr, String operatorName) {
        UnaryExpr.Operator op = UnaryExpr.Operator.valueOf(operatorName);
        return new UnaryExpr((Expr) expr, op);
    }

    /**
     * Create a BooleanLiteralExpr.
     */
    public static BooleanLiteralExpr createBooleanLiteralExpr(boolean value) {
        return new BooleanLiteralExpr(value);
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
    
    /**
     * Strip outer parentheses from an expression string if they exist.
     * Used to clean up expressions before combining with AND/OR.
     */
    private static String stripOuterParens(String expr) {
        if (expr == null || expr.length() < 2) {
            return expr;
        }
        if (expr.startsWith("(") && expr.endsWith(")")) {
            // Check if it's a balanced pair of outer parentheses
            int depth = 0;
            for (int i = 0; i < expr.length(); i++) {
                if (expr.charAt(i) == '(') {
                    depth++;
                } else if (expr.charAt(i) == ')') {
                    depth--;
                    if (depth == 0 && i < expr.length() - 1) {
                        // Not a balanced outer pair
                        return expr;
                    }
                }
            }
            if (depth == 0) {
                return expr.substring(1, expr.length() - 1);
            }
        }
        return expr;
    }
}

