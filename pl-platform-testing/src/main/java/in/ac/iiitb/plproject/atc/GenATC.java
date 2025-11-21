package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.JmlSpecAst;
import in.ac.iiitb.plproject.parser.ast.TestStringAst;
import in.ac.iiitb.plproject.atc.ir.AtcClass;
import in.ac.iiitb.plproject.atc.ir.AtcIrCodeGenerator;

public interface GenATC {

    /**
     * Generates a complete, runnable Java test file (the ATC)
     * from JML specs and a test string.
clear     * @param jmlSpecAst The full set of parsed JML specs (e.g., STACK specs).
     * @param testStringAst The sequence of operations to test (e.g., PUSH_OK, POP_OK).
     * @return An AtcClass IR object containing the generated test code structure.
     */
    AtcClass generateAtcFile(JmlSpecAst jmlSpecAst, TestStringAst testStringAst);

    /**
     * Pretty-prints the IR representation to a Java code string.
     * This is a utility method for converting the IR output to a readable string format.
     *
     * @param atcClass The AtcClass IR object to convert to string.
     * @return A formatted Java code string representation of the IR.
     */
    default String prettyPrint(AtcClass atcClass) {
        AtcIrCodeGenerator codeGenerator = new AtcIrCodeGenerator();
        return codeGenerator.generateJavaFile(atcClass);
    }
}

