package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.JmlFunctionSpec;
import in.ac.iiitb.plproject.parser.ast.JmlSpecAst;
import in.ac.iiitb.plproject.parser.ast.TestStringAst;
import in.ac.iiitb.plproject.ast.AstHelper;
import in.ac.iiitb.plproject.ast.Expr;
import in.ac.iiitb.plproject.atc.ir.AtcClass;
import in.ac.iiitb.plproject.atc.ir.AtcIrCodeGenerator;
import in.ac.iiitb.plproject.atc.ir.AtcTestMethod;
import in.ac.iiitb.plproject.atc.ir.AtcStatement;
// Note: org.junit.Test and gov.nasa.jpf.symbc.Debug are external dependencies
// They are only used in generated code strings, not in this compilation unit
// import org.junit.Test;
// import gov.nasa.jpf.symbc.Debug;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
// Skeleton for LibTestGenATC (based on img2.jpeg)
public class LibTestGenATC implements GenATC {

    @Override
    public AtcClass generateAtcFile(JmlSpecAst jmlSpecAst, TestStringAst testStringAst) {
        
        // Convert to IR-based approach - for now, generate string and convert to IR
        // This is a compatibility layer - ideally this should be rewritten to generate IR directly
        StringBuilder atcFileContent = new StringBuilder();
        
        // Add necessary imports for SPF and the code-under-test
        atcFileContent.append("import gov.nasa.jpf.symbc.Debug;\n");
        atcFileContent.append("import your.project.Stack;\n"); // Example
        atcFileContent.append("\n");
        
        atcFileContent.append("public class GeneratedATCs {\n\n");

        // 1. Iterate through the calls in the "Test String"
        // e.g., testStringAst.getCalls() -> ["PUSH_OK", "POP_OK"]
        for (String functionName : testStringAst.getCalls()) {
            
            // 2. Find the corresponding JML spec for this function
            JmlFunctionSpec spec = jmlSpecAst.findSpecFor(functionName); // e.g., find "PUSH_OK"
            
            if (spec != null) {
                // 3. Generate a self-contained test function for this spec
                String testFunction = generateTestFunction(spec);
                atcFileContent.append(testFunction);
                atcFileContent.append("\n");
            }
        }
        
        atcFileContent.append("}\n");
        
        // Convert string to IR - this is a temporary compatibility solution
        // In a proper implementation, LibTestGenATC should generate IR directly
        String javaCode = atcFileContent.toString();
        AtcIrCodeGenerator codeGenerator = new AtcIrCodeGenerator();
        // Note: This is a workaround - we can't easily parse Java code back to IR
        // For now, return a minimal AtcClass structure
        // TODO: Refactor LibTestGenATC to generate IR directly
        List<String> imports = new ArrayList<>();
        imports.add("gov.nasa.jpf.symbc.Debug");
        imports.add("your.project.Stack");
        return new AtcClass("", "GeneratedATCs", imports, new ArrayList<AtcTestMethod>(), new ArrayList<AtcStatement>(), null);
    }

    /**
     * This is the heart of the "function-based ATC generation."
     * It translates one JML spec into one Java test method.
     */
    private String generateTestFunction(JmlFunctionSpec spec) {
        
        // Example: spec is for PUSH_OK from img3.jpeg
        // Call: push(x: int, s: Stack) -> s': Stack
        // Pre: true
        // Post: s'.size = s.size+1 ^ s'.top = x
        
        StringBuilder func = new StringBuilder();
        func.append("    @Test\n"); // Assuming a @Test annotation
        func.append("    public void test_").append(spec.getName()).append("() {\n\n");

        // Step A: Type Inference & Symbolic Variable Creation (Action Point 1)
        // This comes from the 'Call' signature
        func.append("        // 1. Create symbolic inputs\n");
        func.append("        int x = Debug.makeSymbolicInt(\"x\");\n");
        func.append("        Stack s = new Stack(); // Or symbolic stack if needed\n");
        
        // Step B: Translate Preconditions (@requires) to 'assume'
        // This aligns with Phase 4 
        func.append("\n        // 2. Set JML preconditions\n");
        // Note: getPrecondition() returns Expr, convert to string representation using AstHelper
        Expr preConditionExpr = spec.getPrecondition();
        String preCondition = (preConditionExpr != null) ? AstHelper.exprToJavaCode(preConditionExpr) : "true";
        func.append("        Debug.assume(").append(preCondition).append(");\n");
        
        // Step C: Snapshot "Old" State (Handling Primed Vars)
        // This is the S_old logic from doubts.pdf [cite: 523]
        func.append("\n        // 3. Snapshot 'old' state for postconditions\n");
        func.append("        int old_s_size = s.getSize();\n");
        
        // Step D: Generate the Actual Method Call
        func.append("\n        // 4. Make the actual call\n");
        func.append("        Stack s_prime = s.push(x, s);\n");

        // Step E: Translate Postconditions (@ensures) to 'assert'
        // This is the core verification
        func.append("\n        // 5. Assert JML postconditions\n");
        
        // We parse the postcondition: s'.size = s.size+1 ^ s'.top = x
        // And replace primed/old vars
        
        // Postcondition 1: s'.size = s.size+1
        String post1 = "s_prime.getSize() == old_s_size + 1";
        
        // Postcondition 2: s'.top = x
        String post2 = "s_prime.getTop() == x";

        func.append("        assert(").append(post1).append(");\n");
        func.append("        assert(").append(post2).append(");\n");

        func.append("    }\n");
        return func.toString();
    }
}
