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
        
        // Store precondition and postcondition
        Expr pre = spec.getPrecondition();
        Expr post = spec.getPostcondition();
        
        // Check if we have collection parameters
        List<String> collectionParams = new ArrayList<>();
        for (String pName : paramNames) {
            String paramType = getParamType(pName, params);
            String baseType = paramType;
            if (paramType.contains("<")) {
                baseType = paramType.substring(0, paramType.indexOf("<"));
            }
            if (TypeMapper.isCollectionType(baseType)) {
                collectionParams.add(pName);
            }
        }
        
        // For primitive-only methods: add precondition first, then snapshot old state
        // For collection methods: add null checks first, then precondition
        if (collectionParams.isEmpty() && pre != null) {
            statements.add(new AtcAssumeStmt(pre));
        }
        
        Set<String> varsToSnapshot = collectVarsToSnapshot(post);
        // Filter out "null" and other literals that shouldn't be snapshotted
        varsToSnapshot.remove("null");
        Map<String, String> oldStateMap = new HashMap<>();
        
        // Only snapshot primitive types that need old state
        // For collections, we don't need to snapshot them as _old since they're passed by reference
        for (String varName : varsToSnapshot) {
            String varType = "Object";
            for (Variable p : params) {
                if (p.getName().equals(varName)) {
                    varType = p.getTypeName();
                    break;
                }
            }
            
            // Only snapshot primitive types
            if (isPrimitiveType(varType)) {
                String oldVarName = varName + "_old";
                oldStateMap.put(varName, oldVarName);
                statements.add(new AtcVarDecl(varType, oldVarName, AstHelper.createNameExpr(varName)));
            }
        }
        
        // Add null check return statement if we have collection params
        // Create: if (data == null || result == null) { return; }
        if (!collectionParams.isEmpty()) {
            List<Object> nullCheckConditions = new ArrayList<>();
            for (String pName : collectionParams) {
                nullCheckConditions.add(AstHelper.createBinaryExpr(
                    AstHelper.createNameExpr(pName),
                    AstHelper.createNameExpr("null"),
                    "EQUALS"
                ));
            }
            Expr nullCheckCondition = (Expr) nullCheckConditions.get(0);
            for (int i = 1; i < nullCheckConditions.size(); i++) {
                nullCheckCondition = AstHelper.createBinaryExpr(
                    nullCheckCondition,
                    (Expr) nullCheckConditions.get(i),
                    "OR"
                );
            }
            // Add if statement with return
            statements.add(new AtcIfStmt(nullCheckCondition, new ArrayList<>(), true));
            
            // Add assumes for collection parameters (after null check)
            for (String pName : collectionParams) {
                Expr notNullCondition = AstHelper.createBinaryExpr(
                    AstHelper.createNameExpr(pName),
                    AstHelper.createNameExpr("null"),
                    "NOT_EQUALS"
                );
                statements.add(new AtcAssumeStmt(notNullCondition));
            }
        }
        
        // Add precondition assume after null checks (if it exists and not already added)
        // Only add if we have collection params (primitive-only methods already have it)
        if (!collectionParams.isEmpty() && pre != null) {
            statements.add(new AtcAssumeStmt(pre));
        }
        
        // Add print statements for collection parameters (only first input collection, not output ones)
        boolean printedCollection = false;
        for (String pName : paramNames) {
            String paramType = getParamType(pName, params);
            String baseType = paramType;
            if (paramType.contains("<")) {
                baseType = paramType.substring(0, paramType.indexOf("<"));
            }
            if (TypeMapper.isCollectionType(baseType) && !printedCollection) {
                // Print actual variable value, not string literal
                // Only print the first collection parameter (typically the input, not output)
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
                printedCollection = true;
            } else if (isPrimitiveType(paramType) || paramType.equals("String")) {
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
            
            // Wrap method call and assertion in if statement for collection parameters
            if (!collectionParams.isEmpty()) {
                List<Object> notNullConditions = new ArrayList<>();
                for (String pName : collectionParams) {
                    notNullConditions.add(AstHelper.createBinaryExpr(
                        AstHelper.createNameExpr(pName),
                        AstHelper.createNameExpr("null"),
                        "NOT_EQUALS"
                    ));
                }
                Expr notNullCondition = (Expr) notNullConditions.get(0);
                for (int i = 1; i < notNullConditions.size(); i++) {
                    notNullCondition = AstHelper.createBinaryExpr(
                        notNullCondition,
                        (Expr) notNullConditions.get(i),
                        "AND"
                    );
                }
                
                List<AtcStatement> ifBody = new ArrayList<>();
                ifBody.add(new AtcMethodCallStmt(callExpr));
                
                statements.add(new AtcIfStmt(notNullCondition, ifBody, false));
                
                // Add print and assertion outside if block for collection params
                Object printlnArg = AstHelper.createStringLiteralExpr("Test Input: Helper." + functionName + " completed");
                in.ac.iiitb.plproject.ast.MethodCallExpr printlnCall = AstHelper.createMethodCallExpr(
                    AstHelper.createNameExpr("System.out"), "println", 
                    Arrays.asList(printlnArg)
                );
                statements.add(new AtcMethodCallStmt(printlnCall));
                
                if (post != null) {
                    Expr transformedPost = transformPostCondition(post, resultVarName, oldStateMap, params);
                    statements.add(new AtcAssertStmt(transformedPost));
                }
            } else {
                statements.add(new AtcMethodCallStmt(callExpr));
                if (post != null) {
                    Expr transformedPost = transformPostCondition(post, resultVarName, oldStateMap, params);
                    statements.add(new AtcAssertStmt(transformedPost));
                }
            }
        }
        
        // Add assertion for primitive postStateParam case
        if (postStateParam != null && isPrimitiveType(getParamType(postStateParam, params)) && post != null) {
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

