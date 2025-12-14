package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.JmlSpecAst;
import in.ac.iiitb.plproject.parser.ast.TestStringAst;
import in.ac.iiitb.plproject.atc.ir.AtcClass;
import in.ac.iiitb.plproject.atc.ir.AtcIrCodeGenerator;

public interface GenATC {
    AtcClass generateAtcFile(JmlSpecAst jmlSpecAst, TestStringAst testStringAst);

    default String prettyPrint(AtcClass atcClass) {
        AtcIrCodeGenerator codeGenerator = new AtcIrCodeGenerator();
        return codeGenerator.generateJavaFile(atcClass);
    }
}

