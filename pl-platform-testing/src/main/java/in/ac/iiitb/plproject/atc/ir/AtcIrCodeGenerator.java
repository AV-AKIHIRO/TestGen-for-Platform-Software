package in.ac.iiitb.plproject.atc.ir;

import in.ac.iiitb.plproject.ast.AstHelper;

/**
 * A visitor class that walks the AtcClass IR tree and generates the final Java code string.
 */
public class AtcIrCodeGenerator {

    private StringBuilder stringBuilder;
    private static final String INDENT = "    "; // 4 spaces for indentation

    public AtcIrCodeGenerator() {
        this.stringBuilder = new StringBuilder();
    }

    public String generateJavaFile(AtcClass atc) {
        stringBuilder = new StringBuilder(); // Reset for each generation

        // Package declaration
        stringBuilder.append("package ").append(atc.getPackageName()).append(";\n\n");

        // Imports
        for (String anImport : atc.getImports()) {
            stringBuilder.append("import ").append(anImport).append(";\n");
        }
        stringBuilder.append("\n");

        // Class annotations (e.g., @RunWith)
        if (atc.getRunWithAnnotationClass() != null && !atc.getRunWithAnnotationClass().isEmpty()) {
            stringBuilder.append("@RunWith(").append(atc.getRunWithAnnotationClass()).append(")\n");
        }
        
        // Class header
        stringBuilder.append("public class ").append(atc.getClassName()).append(" {\n");

        // Test methods
        for (AtcTestMethod method : atc.getTestMethods()) {
            visit(method);
        }

        // Generate the main method
        generateMainMethod(atc);

        // Class closing brace
        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private void visit(AtcTestMethod method) {
        stringBuilder.append("\n");
        // Method annotations (e.g., @Test)
        if (method.isTestAnnotated()) {
            stringBuilder.append(INDENT).append("@Test\n");
        }
        // Method header
        stringBuilder.append(INDENT).append("public void ").append(method.getMethodName()).append("() {\n");

        // Statements in method body
        for (AtcStatement stmt : method.getStatements()) {
            // Polymorphic call to visit specific statement types
            if (stmt instanceof AtcSymbolicVarDecl) {
                visit((AtcSymbolicVarDecl) stmt);
            } else if (stmt instanceof AtcVarDecl) {
                visit((AtcVarDecl) stmt);
            } else if (stmt instanceof AtcAssumeStmt) {
                visit((AtcAssumeStmt) stmt);
            } else if (stmt instanceof AtcMethodCallStmt) {
                visit((AtcMethodCallStmt) stmt);
            } else if (stmt instanceof AtcAssertStmt) {
                visit((AtcAssertStmt) stmt);
            }
            // Add more else if for other AtcStatement types if needed
        }

        // Method closing brace
        stringBuilder.append(INDENT).append("}\n");
    }

    private void visit(AtcSymbolicVarDecl stmt) {
        String typeName = stmt.getTypeName();
        String varName = stmt.getVarName();
        
        // Generate simple Java code - JPF transformation will be done by SpfWrapper
        // Use Symbolic.input() as a placeholder that SpfWrapper will replace
        if (typeName.equalsIgnoreCase("int") || typeName.equals("Integer")) {
            stringBuilder.append(INDENT).append(INDENT)
                         .append("int ").append(varName)
                         .append(" = Symbolic.input(\"").append(varName).append("\");\n");
        } else if (typeName.equalsIgnoreCase("double") || typeName.equals("Double")) {
            stringBuilder.append(INDENT).append(INDENT)
                         .append("double ").append(varName)
                         .append(" = Symbolic.input(\"").append(varName).append("\");\n");
        } else if (typeName.equalsIgnoreCase("String")) {
            stringBuilder.append(INDENT).append(INDENT)
                         .append("String ").append(varName)
                         .append(" = Symbolic.input(\"").append(varName).append("\");\n");
        } else if (typeName.equalsIgnoreCase("boolean") || typeName.equals("Boolean")) {
            stringBuilder.append(INDENT).append(INDENT)
                         .append("boolean ").append(varName)
                         .append(" = Symbolic.input(\"").append(varName).append("\");\n");
        } else {
            // For objects (Set, Map, custom classes), use Symbolic.input with cast
            stringBuilder.append(INDENT).append(INDENT)
                         .append(typeName).append(" ").append(varName)
                         .append(" = (").append(typeName).append(") Symbolic.input(\"").append(varName).append("\");\n");
        }
    }

    private void visit(AtcVarDecl stmt) {
        String initCode = AstHelper.exprToJavaCode(stmt.getInitExpr());
        stringBuilder.append(INDENT).append(INDENT)
                     .append(stmt.getTypeName()).append(" ").append(stmt.getVarName())
                     .append(" = ").append(initCode).append(";\n");
    }

    private void visit(AtcAssumeStmt stmt) {
        String condCode = AstHelper.exprToJavaCode(stmt.getCondition());
        // Generate simple assume - SpfWrapper will convert to Debug.assume()
        stringBuilder.append(INDENT).append(INDENT)
                     .append("assume(").append(condCode).append(");\n");
    }

    private void visit(AtcMethodCallStmt stmt) {
        String callCode = AstHelper.exprToJavaCode(stmt.getCallExpr());
        stringBuilder.append(INDENT).append(INDENT)
                     .append(callCode).append(";\n");
    }

    private void visit(AtcAssertStmt stmt) {
        String condCode = AstHelper.exprToJavaCode(stmt.getCondition());
        stringBuilder.append(INDENT).append(INDENT)
                     .append("assert(").append(condCode).append(");\n");
    }


    private void generateMainMethod(AtcClass atc) {
        stringBuilder.append("\n");
        stringBuilder.append(INDENT).append("public static void main(String[] args) {\n");

        // The instantiation of 'instance' is now handled as part of mainMethodStatements
        // from NewGenATC.java. Removing this line to prevent duplication.
        // stringBuilder.append(INDENT).append(INDENT)
        //              .append(atc.getClassName()).append(" instance = new ").append(atc.getClassName()).append("();\n");

        // Use the pre-constructed main method statements
        for (AtcStatement statement : atc.getMainMethodStatements()) {
            // Assuming AtcMethodCallStmt is the only type of statement here for simplicity
            // A more robust solution might use a visitor pattern for different statement types
            if (statement instanceof AtcMethodCallStmt) {
                String callCode = AstHelper.exprToJavaCode(((AtcMethodCallStmt) statement).getCallExpr());
                stringBuilder.append(INDENT).append(INDENT)
                             .append(callCode).append(";\n"); // MethodCallExpr already includes 'instance.' and method name
            } else if (statement instanceof AtcVarDecl) {
                String initCode = AstHelper.exprToJavaCode(((AtcVarDecl) statement).getInitExpr());
                stringBuilder.append(INDENT).append(INDENT)
                             .append(((AtcVarDecl) statement).getTypeName()).append(" ")
                             .append(((AtcVarDecl) statement).getVarName()).append(" = ")
                             .append(initCode).append(";\n");
            }
        }
        stringBuilder.append(INDENT).append("}\n");
    }
}
