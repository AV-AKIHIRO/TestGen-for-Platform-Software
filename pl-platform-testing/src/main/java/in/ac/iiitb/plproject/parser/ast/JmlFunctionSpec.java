package in.ac.iiitb.plproject.parser.ast;

// Note: Expr is package-private in ast package
// We'll use Object for now and cast when needed, or create a wrapper
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a JML function specification with preconditions, postconditions, and function signature.
 * 
 * JML can have multiple requires and ensures clauses. They are combined with AND.
 * The precondition and postcondition fields store the combined expression (AST Expr as Object).
 */
public class JmlFunctionSpec {
    private String name;
    private FunctionSignature signature;
    private Object precondition;  // Combined requires clauses (AST Expr as Object) - multiple clauses combined with AND
    private Object postcondition; // Combined ensures clauses (AST Expr as Object) - multiple clauses combined with AND
    private List<Object> requiresClauses; // Individual requires clauses (for reference)
    private List<Object> ensuresClauses;  // Individual ensures clauses (for reference)

    /**
     * Constructor that takes individual requires and ensures clauses.
     * They will be combined with AND operations.
     */
    public JmlFunctionSpec(String name, FunctionSignature signature, List<Object> requiresClauses, List<Object> ensuresClauses) {
        this.name = name;
        this.signature = signature;
        this.requiresClauses = requiresClauses != null ? new ArrayList<>(requiresClauses) : new ArrayList<>();
        this.ensuresClauses = ensuresClauses != null ? new ArrayList<>(ensuresClauses) : new ArrayList<>();
        // Combine requires clauses with AND
        this.precondition = combineWithAnd(this.requiresClauses);
        // Combine ensures clauses with AND
        this.postcondition = combineWithAnd(this.ensuresClauses);
    }

    /**
     * Constructor for backward compatibility - takes single precondition and postcondition.
     * These should be the combined expressions (multiple clauses already combined with AND).
     */
    public JmlFunctionSpec(String name, FunctionSignature signature, Object precondition, Object postcondition) {
        this.name = name;
        this.signature = signature;
        this.precondition = precondition;
        this.postcondition = postcondition;
        this.requiresClauses = new ArrayList<>();
        this.ensuresClauses = new ArrayList<>();
        if (precondition != null) {
            this.requiresClauses.add(precondition);
        }
        if (postcondition != null) {
            this.ensuresClauses.add(postcondition);
        }
    }

    /**
     * Helper method to combine multiple expressions with AND.
     * Uses AstHelper to create BinaryExpr with AND operator.
     */
    private Object combineWithAnd(List<Object> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        }
        // Combine all expressions with AND: expr1 && expr2 && expr3 ...
        Object result = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            result = in.ac.iiitb.plproject.ast.AstHelper.createBinaryExpr(result, expressions.get(i), "AND");
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public FunctionSignature getSignature() {
        return signature;
    }

    /**
     * Get the combined precondition (all requires clauses combined with AND).
     */
    @SuppressWarnings("unchecked")
    public <T> T getPrecondition() {
        return (T) precondition;
    }

    /**
     * Get the combined postcondition (all ensures clauses combined with AND).
     */
    @SuppressWarnings("unchecked")
    public <T> T getPostcondition() {
        return (T) postcondition;
    }

    /**
     * Get individual requires clauses.
     */
    public List<Object> getRequiresClauses() {
        return new ArrayList<>(requiresClauses);
    }

    /**
     * Get individual ensures clauses.
     */
    public List<Object> getEnsuresClauses() {
        return new ArrayList<>(ensuresClauses);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JmlFunctionSpec(");
        sb.append("Name: ").append(name);
        if (signature != null) {
            sb.append(", Signature: ").append(signature);
        }
        if (precondition != null) {
            sb.append(", Pre: ").append(precondition);
        } else {
            sb.append(", Pre: (none)");
        }
        if (postcondition != null) {
            sb.append(", Post: ").append(postcondition);
        } else {
            sb.append(", Post: (none)");
        }
        sb.append(")");
        return sb.toString();
    }
}
