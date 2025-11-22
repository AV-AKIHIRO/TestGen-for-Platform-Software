package in.ac.iiitb.plproject.atc;

import in.ac.iiitb.plproject.parser.ast.*;
import in.ac.iiitb.plproject.ast.AstHelper;
import in.ac.iiitb.plproject.ast.Expr;
import java.util.*;
import in.ac.iiitb.plproject.atc.ir.*;
import in.ac.iiitb.plproject.symex.TypeMapper;

public class NewGenATC implements GenATC {

    @Override
    public AtcClass generateAtcFile(JmlSpecAst jmlSpecAst, TestStringAst testStringAst) {
        List<String> imports = new ArrayList<>();
        imports.add("org.junit.Test");
        imports.add("java.util.*");

        List<AtcTestMethod> actualTestMethods = new ArrayList<>();
        List<String> calls = testStringAst.getCalls();
        
        Map<String, JmlFunctionSpec> uniqueFunctionSpecs = new LinkedHashMap<>();
        Map<String, AtcTestMethod> generatedHelperMethods = new HashMap<>();

        for (String functionName : calls) {
            JmlFunctionSpec spec = jmlSpecAst.findSpecFor(functionName);
            if (spec != null) {
                uniqueFunctionSpecs.putIfAbsent(functionName, spec);
            }
        }
        
        for (Map.Entry<String, JmlFunctionSpec> entry : uniqueFunctionSpecs.entrySet()) {
            String funcName = entry.getKey();
            JmlFunctionSpec spec = entry.getValue();
            AtcTestMethod helperMethod = generateHelperFunction(spec);
            generatedHelperMethods.put(funcName, helperMethod);
            actualTestMethods.add(helperMethod);
        }

        List<AtcStatement> mainMethodStatements = new ArrayList<>();
        mainMethodStatements.add(new AtcVarDecl("GeneratedATCs", "instance", AstHelper.createObjectCreationExpr("GeneratedATCs", new ArrayList<>())));

        for (String functionName : calls) {
            AtcTestMethod helperMethod = generatedHelperMethods.get(functionName);
            if (helperMethod != null) {
                in.ac.iiitb.plproject.ast.MethodCallExpr callExpr =
                    AstHelper.createMethodCallExpr(AstHelper.createNameExpr("instance"), helperMethod.getMethodName(), new ArrayList<>());
                mainMethodStatements.add(new AtcMethodCallStmt(callExpr));
            }
        }

        String packageName = "in.ac.iiitb.plproject.atc.generated";
        String className = "GeneratedATCs";
        String runWithAnnotation = null;

        return new AtcClass(packageName, className, imports, actualTestMethods, mainMethodStatements, runWithAnnotation);
    }
  
    private AtcTestMethod generateHelperFunction(JmlFunctionSpec spec) {
        List<AtcStatement> statements = new ArrayList<>();
        FunctionSignature signature = spec.getSignature();
        
        List<Variable> params = signature.getParameters();
        List<String> paramNames = new ArrayList<>();
        for (Variable param : params) {
            String name = param.getName();
            String type = param.getTypeName();
            paramNames.add(name);
            statements.add(new AtcSymbolicVarDecl(type, name));
        }
        
        Expr pre = spec.getPrecondition();
        if (pre != null) {
            statements.add(new AtcAssumeStmt(pre));
        } else {
            statements.add(new AtcAssumeStmt(AstHelper.createBooleanLiteralExpr(true)));
        }
        
        Expr post = spec.getPostcondition();
        Set<String> varsToSnapshot = collectVarsToSnapshot(post);
        Map<String, String> oldStateMap = new HashMap<>();
        
        for (String varName : varsToSnapshot) {
            String oldVarName = varName + "_old";
            oldStateMap.put(varName, oldVarName);
            
            String varType = "Object";
            for (Variable p : params) {
                if (p.getName().equals(varName)) {
                    varType = p.getTypeName();
                    break;
                }
            }
            
            String baseType = varType;
            if (varType.contains("<")) {
                baseType = varType.substring(0, varType.indexOf("<"));
            }
            
            if (baseType.equals("Set")) {
                List<Object> constructorArgs = new ArrayList<>();
                constructorArgs.add(AstHelper.createNameExpr(varName));
                String genericType = TypeMapper.getGenericType(varType);
                statements.add(new AtcVarDecl(genericType, oldVarName, 
                    AstHelper.createObjectCreationExpr("HashSet", constructorArgs)));
            } else if (baseType.equals("Map")) {
                List<Object> constructorArgs = new ArrayList<>();
                constructorArgs.add(AstHelper.createNameExpr(varName));
                String genericType = TypeMapper.getGenericType(varType);
                statements.add(new AtcVarDecl(genericType, oldVarName, 
                    AstHelper.createObjectCreationExpr("HashMap", constructorArgs)));
            } else {
                statements.add(new AtcVarDecl(varType, oldVarName, AstHelper.createNameExpr(varName)));
            }
        }
        
        for (String pName : paramNames) {
            String paramType = getParamType(pName, params);
            if (isPrimitiveType(paramType) || paramType.equals("String")) {
                Object printlnArg = AstHelper.createBinaryExpr(
                    AstHelper.createStringLiteralExpr("Test Input: " + pName + " = "),
                    AstHelper.createNameExpr(pName),
                    "PLUS"
                );
                in.ac.iiitb.plproject.ast.MethodCallExpr printlnCall = AstHelper.createMethodCallExpr(
                    AstHelper.createNameExpr("System.out"), "println", 
                    Arrays.asList(printlnArg)
                );
                statements.add(new AtcMethodCallStmt(printlnCall));
                break;
            }
        }
        
        String functionName = signature.getName();
        String resultVarName = null;
        String postStateParam = null;
        if (post != null) {
            List<String> paramNameList = new ArrayList<>();
            for (Variable p : params) {
                paramNameList.add(p.getName());
            }
            postStateParam = AstHelper.findPostStateParameter(post, paramNameList);
            
            if (postStateParam == null) {
                postStateParam = findPostStateParameter(post, params);
            }
        }
        
        if (postStateParam != null && isPrimitiveType(getParamType(postStateParam, params))) {
            String paramType = getParamType(postStateParam, params);
            String refVarName = postStateParam + "Ref";
            
            List<Object> arrayInitArgs = new ArrayList<>();
            arrayInitArgs.add(AstHelper.createNameExpr(postStateParam));
            Expr arrayInit = (Expr) AstHelper.createObjectCreationExpr(paramType + "[]", arrayInitArgs);
            statements.add(new AtcVarDecl(paramType + "[]", refVarName, arrayInit));
            
            List<Object> callArgs = new ArrayList<>();
            callArgs.add(AstHelper.createNameExpr(refVarName));
            in.ac.iiitb.plproject.ast.MethodCallExpr callExpr = AstHelper.createMethodCallExpr(
                AstHelper.createNameExpr("Helper"), functionName, callArgs);
            statements.add(new AtcMethodCallStmt(callExpr));
            
            Expr arrayAccessExpr = AstHelper.createNameExpr(refVarName + "[0]");
            statements.add(new AtcAssignStmt(postStateParam, arrayAccessExpr));
            
            resultVarName = postStateParam;
        } else {
            List<Object> callArgs = new ArrayList<>();
            for (String pName : paramNames) {
                callArgs.add(AstHelper.createNameExpr(pName));
            }
            in.ac.iiitb.plproject.ast.MethodCallExpr callExpr = AstHelper.createMethodCallExpr(
                AstHelper.createNameExpr("Helper"), functionName, callArgs);
            statements.add(new AtcMethodCallStmt(callExpr));
        }
        
        if (post != null) {
            Expr transformedPost = transformPostCondition(post, resultVarName, oldStateMap, params);
            statements.add(new AtcAssertStmt(transformedPost));
        }
        
        String helperMethodName = spec.getName() + "_helper";
        return new AtcTestMethod(helperMethodName, statements);
    }
    
    private Set<String> collectVarsToSnapshot(Expr expr) {
        return AstHelper.collectVarsToSnapshot(expr);
    }
    private Expr transformPostCondition(Expr expr, String resultVarName, Map<String, String> oldStateMap, List<Variable> params) {
        return (Expr) AstHelper.transformPostCondition(expr, resultVarName, oldStateMap, params);
    }
    
    private String findPostStateParameter(Expr post, List<Variable> params) {
        if (post == null) {
            return null;
        }
        
        String postStr = AstHelper.exprToJavaCode(post);
        
        for (Variable param : params) {
            String paramName = param.getName();
            String primePattern1 = "'([" + paramName + "])";
            String primePattern2 = "'(" + paramName + ")";
            String primePattern3 = "'(["+ paramName + "])";
            
            if (postStr.contains(primePattern1) || 
                postStr.contains(primePattern2) ||
                postStr.contains(primePattern3) ||
                postStr.matches(".*'\\s*\\(\\s*\\[\\s*" + paramName + "\\s*\\]\\s*\\).*") ||
                postStr.matches(".*'\\s*\\(\\s*" + paramName + "\\s*\\).*")) {
                return paramName;
            }
            if (postStr.matches(".*\\b" + paramName + "_post\\b.*")) {
                return paramName;
            }
        }
        
        return findPostStateParameterRecursive(post, params);
    }
    
    private String getParamType(String paramName, List<Variable> params) {
        for (Variable p : params) {
            if (p.getName().equals(paramName)) {
                return p.getTypeName();
            }
        }
        return "int";
    }
    
    private String findPostStateParameterRecursive(Expr expr, List<Variable> params) {
        if (expr == null) {
            return null;
        }
        
        String className = expr.getClass().getSimpleName();
        
        if (className.equals("MethodCallExpr")) {
            try {
                java.lang.reflect.Field nameField = expr.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                Object nameObj = nameField.get(expr);
                
                java.lang.reflect.Field identifierField = nameObj.getClass().getDeclaredField("identifier");
                identifierField.setAccessible(true);
                String identifier = (String) identifierField.get(nameObj);
                
                java.lang.reflect.Field argsField = expr.getClass().getDeclaredField("args");
                argsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Expr> args = (List<Expr>) argsField.get(expr);
                
                if ("'".equals(identifier) && !args.isEmpty()) {
                    Expr arg = args.get(0);
                    String varName = AstHelper.getNameFromExpr(arg);
                    if (varName != null) {
                        for (Variable param : params) {
                            if (param.getName().equals(varName)) {
                                return varName;
                            }
                        }
                    }
                }
                
                for (Expr arg : args) {
                    String result = findPostStateParameterRecursive(arg, params);
                    if (result != null) {
                        return result;
                    }
                }
            } catch (Exception e) {
                return null;
            }
        } else if (className.equals("BinaryExpr")) {
            try {
                java.lang.reflect.Field leftField = expr.getClass().getDeclaredField("left");
                leftField.setAccessible(true);
                Expr left = (Expr) leftField.get(expr);
                
                java.lang.reflect.Field rightField = expr.getClass().getDeclaredField("right");
                rightField.setAccessible(true);
                Expr right = (Expr) rightField.get(expr);
                
                String result = findPostStateParameterRecursive(left, params);
                if (result != null) {
                    return result;
                }
                return findPostStateParameterRecursive(right, params);
            } catch (Exception e) {
                return null;
            }
        } else if (className.equals("NameExpr")) {
            try {
                java.lang.reflect.Field nameField = expr.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                Object nameObj = nameField.get(expr);
                
                java.lang.reflect.Field identifierField = nameObj.getClass().getDeclaredField("identifier");
                identifierField.setAccessible(true);
                String identifier = (String) identifierField.get(nameObj);
                
                if (identifier != null && identifier.endsWith("_post")) {
                    String baseName = identifier.substring(0, identifier.length() - "_post".length());
                    for (Variable param : params) {
                        if (param.getName().equals(baseName)) {
                            return baseName;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    private boolean isPrimitiveType(String typeName) {
        return typeName.equals("int") || typeName.equals("Integer") ||
               typeName.equals("double") || typeName.equals("Double") ||
               typeName.equals("float") || typeName.equals("Float") ||
               typeName.equals("long") || typeName.equals("Long") ||
               typeName.equals("short") || typeName.equals("Short") ||
               typeName.equals("byte") || typeName.equals("Byte") ||
               typeName.equals("boolean") || typeName.equals("Boolean") ||
               typeName.equals("char") || typeName.equals("Character");
    }
}

