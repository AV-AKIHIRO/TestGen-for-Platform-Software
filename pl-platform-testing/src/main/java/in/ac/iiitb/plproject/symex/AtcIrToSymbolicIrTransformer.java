package in.ac.iiitb.plproject.symex;

import in.ac.iiitb.plproject.atc.ir.*;
import in.ac.iiitb.plproject.ast.AstHelper;
import in.ac.iiitb.plproject.ast.Expr;
import in.ac.iiitb.plproject.ast.MethodCallExpr;
import in.ac.iiitb.plproject.symex.TypeMapper;
import java.util.List;
import java.util.ArrayList;

public class AtcIrToSymbolicIrTransformer {
    
    private static final String DEBUG_IMPORT = "gov.nasa.jpf.symbc.Debug";
    
    public AtcClass transform(AtcClass atcClass) {
        List<String> imports = new ArrayList<>(atcClass.getImports());
        
        boolean hasDebugImport = false;
        for (String imp : imports) {
            if (imp.equals(DEBUG_IMPORT) || imp.endsWith(".Debug")) {
                hasDebugImport = true;
                break;
            }
        }
        
        if (!hasDebugImport) {
            imports.add(DEBUG_IMPORT);
        }
        
        List<AtcTestMethod> transformedMethods = new ArrayList<>();
        for (AtcTestMethod method : atcClass.getTestMethods()) {
            transformedMethods.add(transformMethod(method));
        }
        
        List<AtcStatement> transformedMainStatements = new ArrayList<>();
        for (AtcStatement stmt : atcClass.getMainMethodStatements()) {
            transformedMainStatements.add(transformStatement(stmt));
        }
        
        return new AtcClass(
            atcClass.getPackageName(),
            atcClass.getClassName(),
            imports,
            transformedMethods,
            transformedMainStatements,
            atcClass.getRunWithAnnotationClass()
        );
    }
    
    private AtcTestMethod transformMethod(AtcTestMethod method) {
        List<AtcStatement> transformedStatements = new ArrayList<>();
        for (AtcStatement stmt : method.getStatements()) {
            transformedStatements.add(transformStatement(stmt));
        }
        return new AtcTestMethod(method.getMethodName(), transformedStatements, 
                                method.isStatic(), method.isMain());
    }
    
    private AtcStatement transformStatement(AtcStatement stmt) {
        if (stmt instanceof AtcSymbolicVarDecl) {
            return transformSymbolicVarDecl((AtcSymbolicVarDecl) stmt);
        } else if (stmt instanceof AtcAssumeStmt) {
            return transformAssumeStmt((AtcAssumeStmt) stmt);
        } else if (stmt instanceof AtcIfStmt) {
            return transformIfStmt((AtcIfStmt) stmt);
        }
        return stmt;
    }
    
    private AtcStatement transformSymbolicVarDecl(AtcSymbolicVarDecl stmt) {
        String typeName = stmt.getTypeName();
        String varName = stmt.getVarName();
        
        if (TypeMapper.isCollectionType(typeName)) {
            String genericType = TypeMapper.getGenericType(typeName);
            Expr debugCall = createDebugMakeSymbolicRefCall(typeName, varName);
            return new AtcVarDecl(genericType, varName, debugCall);
        }
        
        Expr debugCall = createDebugMakeSymbolicCall(typeName, varName);
        return new AtcVarDecl(typeName, varName, debugCall);
    }
    
    private Expr createDebugMakeSymbolicCall(String typeName, String varName) {
        List<Expr> args = new ArrayList<>();
        args.add(AstHelper.createStringLiteralExpr(varName));
        
        if (typeName.equalsIgnoreCase("int") || typeName.equals("Integer")) {
            return AstHelper.createMethodCallExpr(
                AstHelper.createNameExpr("Debug"), "makeSymbolicInteger", args);
        } else if (typeName.equalsIgnoreCase("double") || typeName.equals("Double")) {
            return AstHelper.createMethodCallExpr(
                AstHelper.createNameExpr("Debug"), "makeSymbolicDouble", args);
        } else if (typeName.equalsIgnoreCase("String")) {
            return AstHelper.createMethodCallExpr(
                AstHelper.createNameExpr("Debug"), "makeSymbolicString", args);
        } else if (typeName.equalsIgnoreCase("boolean") || typeName.equals("Boolean")) {
            Expr intCall = AstHelper.createMethodCallExpr(
                AstHelper.createNameExpr("Debug"), "makeSymbolicInteger", args);
            Expr zero = AstHelper.createIntegerLiteralExpr(0);
            return AstHelper.createBinaryExpr(intCall, zero, "NOT_EQUALS");
        } else {
            return createDebugMakeSymbolicRefCall(typeName, varName);
        }
    }
    
    private Expr createDebugMakeSymbolicRefCall(String typeName, String varName) {
        List<Expr> args = new ArrayList<>();
        args.add(AstHelper.createStringLiteralExpr(varName));
        
        String baseType = typeName.split("[<>]")[0].trim();
        if (TypeMapper.isCollectionType(baseType)) {
            Expr defaultValue = createCollectionInitExpr(baseType);
            args.add(defaultValue);
        } else {
            args.add(AstHelper.createNameExpr("null"));
        }
        
        return AstHelper.createMethodCallExpr(
            AstHelper.createNameExpr("Debug"), "makeSymbolicRef", args);
    }
    
    private Expr createCollectionInitExpr(String baseType) {
        String className = getCollectionClassName(baseType);
        List<Expr> emptyArgs = new ArrayList<>();
        return AstHelper.createObjectCreationExpr(className, emptyArgs);
    }
    
    private String getCollectionClassName(String baseType) {
        switch (baseType) {
            case "Set":
            case "java.util.Set":
                return "java.util.HashSet";
            case "Map":
            case "java.util.Map":
                return "java.util.HashMap";
            case "List":
            case "java.util.List":
                return "java.util.ArrayList";
            case "Collection":
            case "java.util.Collection":
                return "java.util.ArrayList";
            case "Queue":
            case "java.util.Queue":
                return "java.util.LinkedList";
            case "Deque":
            case "java.util.Deque":
                return "java.util.LinkedList";
            default:
                return "java.util.HashSet";
        }
    }
    
    private AtcStatement transformAssumeStmt(AtcAssumeStmt stmt) {
        Expr condition = stmt.getCondition();
        List<Expr> args = new ArrayList<>();
        args.add(condition);
        
        MethodCallExpr debugAssumeCall = AstHelper.createMethodCallExpr(
            AstHelper.createNameExpr("Debug"), "assume", args);
        
        return new AtcMethodCallStmt(debugAssumeCall);
    }
    
    private AtcStatement transformIfStmt(AtcIfStmt stmt) {
        List<AtcStatement> transformedThenStmts = new ArrayList<>();
        for (AtcStatement thenStmt : stmt.getThenStatements()) {
            transformedThenStmts.add(transformStatement(thenStmt));
        }
        return new AtcIfStmt(stmt.getCondition(), transformedThenStmts, stmt.hasReturn());
    }
}

