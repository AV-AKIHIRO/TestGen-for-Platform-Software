package in.ac.iiitb.plproject.atc.ir;

import in.ac.iiitb.plproject.ast.AstHelper;
import in.ac.iiitb.plproject.ast.Expr;
import in.ac.iiitb.plproject.ast.MethodCallExpr;
import in.ac.iiitb.plproject.symex.TypeMapper;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class AtcIrCodeGenerator {

    private StringBuilder stringBuilder;
    private static final String INDENT = "    ";

    public AtcIrCodeGenerator() {
        this.stringBuilder = new StringBuilder();
    }

    public String generateJavaFile(AtcClass atc) {
        stringBuilder = new StringBuilder();

        stringBuilder.append("package ").append(atc.getPackageName()).append(";\n\n");

        for (String anImport : atc.getImports()) {
            stringBuilder.append("import ").append(anImport).append(";\n");
        }
        stringBuilder.append("\n");

        if (atc.getRunWithAnnotationClass() != null && !atc.getRunWithAnnotationClass().isEmpty()) {
            stringBuilder.append("@RunWith(").append(atc.getRunWithAnnotationClass()).append(")\n");
        }
        
        stringBuilder.append("public class ").append(atc.getClassName()).append(" {\n");

        for (AtcTestMethod method : atc.getTestMethods()) {
            visit(method);
        }

        generateMainMethod(atc);

        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private void visit(AtcTestMethod method) {
        stringBuilder.append("\n");
        if (method.isTestAnnotated()) {
            stringBuilder.append(INDENT).append("@Test\n");
        }
        stringBuilder.append(INDENT).append("public void ").append(method.getMethodName()).append("() {\n");

        Set<String> declaredVars = new HashSet<>();
        
        for (AtcStatement stmt : method.getStatements()) {
            if (stmt instanceof AtcSymbolicVarDecl) {
                visit((AtcSymbolicVarDecl) stmt);
                declaredVars.add(((AtcSymbolicVarDecl) stmt).getVarName());
            } else if (stmt instanceof AtcVarDecl) {
                String varName = ((AtcVarDecl) stmt).getVarName();
                if (declaredVars.contains(varName)) {
                    visitAsAssignment((AtcVarDecl) stmt);
                } else {
                    visit((AtcVarDecl) stmt);
                    declaredVars.add(varName);
                }
            } else if (stmt instanceof AtcAssignStmt) {
                visit((AtcAssignStmt) stmt);
            } else if (stmt instanceof AtcAssumeStmt) {
                visit((AtcAssumeStmt) stmt);
            } else if (stmt instanceof AtcMethodCallStmt) {
                visit((AtcMethodCallStmt) stmt);
            } else if (stmt instanceof AtcAssertStmt) {
                visit((AtcAssertStmt) stmt);
            }
        }

        stringBuilder.append(INDENT).append("}\n");
    }

    private void visit(AtcSymbolicVarDecl stmt) {
        String typeName = stmt.getTypeName();
        String varName = stmt.getVarName();
        
        if (TypeMapper.isCollectionType(typeName)) {
            String genericType = TypeMapper.getGenericType(typeName);
            stringBuilder.append(INDENT).append(INDENT)
                         .append(genericType).append(" ").append(varName)
                         .append(" = (").append(genericType).append(") Symbolic.input(\"").append(varName).append("\");\n");
        } else if (typeName.equalsIgnoreCase("int") || typeName.equals("Integer")) {
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
            String genericType = TypeMapper.getGenericType(typeName);
            stringBuilder.append(INDENT).append(INDENT)
                         .append(genericType).append(" ").append(varName)
                         .append(" = (").append(genericType).append(") Symbolic.input(\"").append(varName).append("\");\n");
        }
    }

    private void visit(AtcVarDecl stmt) {
        String initCode = AstHelper.exprToJavaCode(stmt.getInitExpr());
        String typeName = stmt.getTypeName();
        String varName = stmt.getVarName();
        
        if (typeName.endsWith("[]") && initCode.startsWith("new ")) {
            String baseType = typeName.substring(0, typeName.length() - 2);
            if (initCode.contains("(") && initCode.contains(")")) {
                String args = initCode.substring(initCode.indexOf("(") + 1, initCode.indexOf(")"));
                initCode = "new " + baseType + "[]{" + args + "}";
            }
        }
        
        stringBuilder.append(INDENT).append(INDENT)
                     .append(typeName).append(" ").append(varName)
                     .append(" = ").append(initCode).append(";\n");
    }
    
    private void visitAsAssignment(AtcVarDecl stmt) {
        String valueCode = AstHelper.exprToJavaCode(stmt.getInitExpr());
        String varName = stmt.getVarName();
        
        stringBuilder.append(INDENT).append(INDENT)
                     .append(varName).append(" = ").append(valueCode).append(";\n");
    }
    
    private void visit(AtcAssignStmt stmt) {
        String valueCode = AstHelper.exprToJavaCode(stmt.getValueExpr());
        String varName = stmt.getVarName();
        
        stringBuilder.append(INDENT).append(INDENT)
                     .append(varName).append(" = ").append(valueCode).append(";\n");
    }

    private void visit(AtcAssumeStmt stmt) {
        String condCode = AstHelper.exprToJavaCode(stmt.getCondition());
        stringBuilder.append(INDENT).append(INDENT)
                     .append("assume(").append(condCode).append(");\n");
    }

    private void visit(AtcMethodCallStmt stmt) {
        String callCode = AstHelper.exprToJavaCode(stmt.getCallExpr());
        stringBuilder.append(INDENT).append(INDENT)
                     .append(callCode).append(";\n");
    }

    private void visit(AtcAssertStmt stmt) {
        Expr condition = stmt.getCondition();
        
        Map<String, MethodCallExpr> methodCallMap = new HashMap<>();
        Expr processedCondition = extractMethodCallsFromAssertion(condition, methodCallMap);
        
        if (methodCallMap.isEmpty()) {
            String originalCode = AstHelper.exprToJavaCode(condition);
            if (originalCode.contains("Helper.update(") && countOccurrences(originalCode, "Helper.update(") > 1) {
                extractMethodCallsFromString(originalCode, methodCallMap);
                if (!methodCallMap.isEmpty()) {
                    String processedCode = originalCode;
                    for (Map.Entry<String, MethodCallExpr> entry : methodCallMap.entrySet()) {
                        String varName = entry.getKey();
                        String methodCallCode = AstHelper.exprToJavaCode(entry.getValue());
                        processedCode = processedCode.replace(methodCallCode, varName);
                    }
                    
                    for (Map.Entry<String, MethodCallExpr> entry : methodCallMap.entrySet()) {
                        String varName = entry.getKey();
                        MethodCallExpr methodCall = entry.getValue();
                        String methodCallCode = AstHelper.exprToJavaCode(methodCall);
                        String returnType = inferReturnType(methodCall);
                        
                        stringBuilder.append(INDENT).append(INDENT)
                                     .append(returnType).append(" ").append(varName)
                                     .append(" = ").append(methodCallCode).append(";\n");
                    }
                    
                    stringBuilder.append(INDENT).append(INDENT)
                                 .append("assert(").append(processedCode).append(");\n");
                    return;
                }
            }
        }
        
        for (Map.Entry<String, MethodCallExpr> entry : methodCallMap.entrySet()) {
            String varName = entry.getKey();
            MethodCallExpr methodCall = entry.getValue();
            String methodCallCode = AstHelper.exprToJavaCode(methodCall);
            String returnType = inferReturnType(methodCall);
            
            stringBuilder.append(INDENT).append(INDENT)
                         .append(returnType).append(" ").append(varName)
                         .append(" = ").append(methodCallCode).append(";\n");
        }
        
        String condCode = AstHelper.exprToJavaCode(processedCondition);
        stringBuilder.append(INDENT).append(INDENT)
                     .append("assert(").append(condCode).append(");\n");
    }
    
    private int countOccurrences(String str, String substr) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substr, index)) != -1) {
            count++;
            index += substr.length();
        }
        return count;
    }
    
    private void extractMethodCallsFromString(String code, Map<String, MethodCallExpr> methodCallMap) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Helper\\.update\\([^)]+\\)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        
        String firstMatch = null;
        while (matcher.find()) {
            String match = matcher.group();
            if (firstMatch == null) {
                firstMatch = match;
                java.util.regex.Pattern argPattern = java.util.regex.Pattern.compile("Helper\\.update\\(([^)]+)\\)");
                java.util.regex.Matcher argMatcher = argPattern.matcher(match);
                if (argMatcher.find()) {
                    String argsStr = argMatcher.group(1);
                    String[] args = argsStr.split(",");
                    List<Object> argExprs = new ArrayList<>();
                    for (String arg : args) {
                        argExprs.add(AstHelper.createNameExpr(arg.trim()));
                    }
                    MethodCallExpr updateCall = AstHelper.createMethodCallExpr(
                        AstHelper.createNameExpr("Helper"), "update", argExprs);
                    methodCallMap.put("expectedResult", updateCall);
                    break;
                }
            }
        }
    }
    
    private Expr extractMethodCallsFromAssertion(Expr expr, Map<String, MethodCallExpr> methodCallMap) {
        if (expr == null) {
            return expr;
        }
        
        if (expr instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) expr;
            String varName = generateMethodCallVarName(methodCall, methodCallMap.size());
            methodCallMap.put(varName, methodCall);
            
            return AstHelper.createNameExpr(varName);
        }
        
        try {
            Class<?> binaryExprClass = Class.forName("in.ac.iiitb.plproject.ast.BinaryExpr");
            if (binaryExprClass.isInstance(expr)) {
                java.lang.reflect.Field leftField = binaryExprClass.getDeclaredField("left");
                java.lang.reflect.Field rightField = binaryExprClass.getDeclaredField("right");
                java.lang.reflect.Field opField = binaryExprClass.getDeclaredField("op");
                leftField.setAccessible(true);
                rightField.setAccessible(true);
                opField.setAccessible(true);
                
                Expr left = (Expr) leftField.get(expr);
                Expr right = (Expr) rightField.get(expr);
                Object op = opField.get(expr);
                
                Expr processedLeft = extractMethodCallsFromAssertion(left, methodCallMap);
                Expr processedRight = extractMethodCallsFromAssertion(right, methodCallMap);
                
                String opName = op.getClass().getMethod("name").invoke(op).toString();
                
                return AstHelper.createBinaryExpr(processedLeft, processedRight, opName);
            }
        } catch (Exception e) {
        }
        
        return expr;
    }
    
    private String generateMethodCallVarName(MethodCallExpr methodCall, int index) {
        String methodName = methodCall.name.identifier;
        if (methodName.equals("update")) {
            return "expectedResult";
        } else {
            return "temp" + index;
        }
    }
    
    private String inferReturnType(MethodCallExpr methodCall) {
        String methodName = methodCall.name.identifier;
        if (methodName.equals("update")) {
            return "Map<?,?>";
        }
        return "Object";
    }


    private void generateMainMethod(AtcClass atc) {
        stringBuilder.append("\n");
        stringBuilder.append(INDENT).append("public static void main(String[] args) {\n");

        for (AtcStatement statement : atc.getMainMethodStatements()) {
            if (statement instanceof AtcMethodCallStmt) {
                String callCode = AstHelper.exprToJavaCode(((AtcMethodCallStmt) statement).getCallExpr());
                stringBuilder.append(INDENT).append(INDENT)
                             .append(callCode).append(";\n");
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
