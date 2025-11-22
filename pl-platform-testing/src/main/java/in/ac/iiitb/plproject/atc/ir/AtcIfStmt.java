package in.ac.iiitb.plproject.atc.ir;

import in.ac.iiitb.plproject.ast.Expr;
import java.util.List;

/**
 * Represents an if statement with optional return.
 */
public class AtcIfStmt extends AtcStatement {
    private Expr condition;
    private List<AtcStatement> thenStatements;
    private boolean hasReturn;
    
    public AtcIfStmt(Expr condition, List<AtcStatement> thenStatements, boolean hasReturn) {
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.hasReturn = hasReturn;
    }
    
    public Expr getCondition() {
        return condition;
    }
    
    public List<AtcStatement> getThenStatements() {
        return thenStatements;
    }
    
    public boolean hasReturn() {
        return hasReturn;
    }
}

