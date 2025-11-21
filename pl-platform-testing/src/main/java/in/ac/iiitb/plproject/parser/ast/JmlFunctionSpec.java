package in.ac.iiitb.plproject.parser.ast;

import java.util.List;
import java.util.ArrayList;
import in.ac.iiitb.plproject.ast.Expr;

/**
 * Represents a JML function specification with preconditions, postconditions, and function signature.
 * 
 * JML can have multiple requires and ensures clauses. They are combined with AND.
 * The precondition and postcondition fields store the combined expression (AST Expr).
 */
public class JmlFunctionSpec {
    private String name;
    private FunctionSignature signature;
    private Expr precondition;  // Combined requires clauses (AST Expr) - multiple clauses combined with AND
    private Expr postcondition; // Combined ensures clauses (AST Expr) - multiple clauses combined with AND
    private List<Expr> requiresClauses; // Individual requires clauses (for reference)
    private List<Expr> ensuresClauses;  // Individual ensures clauses (for reference)

    /**
     * Constructor that takes individual requires and ensures clauses.
     * They will be combined with AND operations.
     */
    public JmlFunctionSpec(String name, FunctionSignature signature, List<Expr> requiresClauses, List<Expr> ensuresClauses) {
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
    public JmlFunctionSpec(String name, FunctionSignature signature, Expr precondition, Expr postcondition) {
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
    private Expr combineWithAnd(List<Expr> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        }
        // Combine all expressions with AND: expr1 && expr2 && expr3 ...
        Expr result = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            result = (Expr) in.ac.iiitb.plproject.ast.AstHelper.createBinaryExpr(result, expressions.get(i), "AND");
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
    public Expr getPrecondition() {
        return precondition;
    }

    /**
     * Get the combined postcondition (all ensures clauses combined with AND).
     */
    public Expr getPostcondition() {
        return postcondition;
    }

    /**
     * Get individual requires clauses.
     */
    public List<Expr> getRequiresClauses() {
        return new ArrayList<>(requiresClauses);
    }

    /**
     * Get individual ensures clauses.
     */
    public List<Expr> getEnsuresClauses() {
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
