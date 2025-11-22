package in.ac.iiitb.plproject.symex;

import in.ac.iiitb.plproject.atc.JavaFile;
import in.ac.iiitb.plproject.atc.ConcreteInput;
import in.ac.iiitb.plproject.atc.ir.AtcClass;
import in.ac.iiitb.plproject.atc.ir.AtcTestMethod;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class SpfWrapper {
    
    public String transformToJpfCode(String simpleJavaCode) {
        StringBuilder jpfCode = new StringBuilder();
        String[] lines = simpleJavaCode.split("\n");
        boolean importsAdded = false;
        
        for (String line : lines) {
            if (line.trim().startsWith("package ") && !importsAdded) {
                jpfCode.append(line).append("\n\n");
                jpfCode.append("import gov.nasa.jpf.symbc.Debug;\n");
                importsAdded = true;
                continue;
            }
            
            if (line.trim().startsWith("import gov.nasa.jpf.symbc.Debug")) {
                continue;
            }
            
            if (line.contains("Symbolic.input(")) {
                line = transformSymbolicInput(line);
            }
            
            if (line.contains("assume(") && !line.contains("Debug.assume(")) {
                line = line.replace("assume(", "Debug.assume(");
            }
            
            jpfCode.append(line).append("\n");
        }
        
        return jpfCode.toString();
    }
    
    private String transformSymbolicInput(String line) {
        Pattern pattern1 = Pattern.compile("(\\s+)(int|double|String|boolean|Integer|Double|Boolean)\\s+(\\w+)\\s*=\\s*Symbolic\\.input\\(\"([^\"]+)\"\\);");
        Matcher matcher1 = pattern1.matcher(line);
        if (matcher1.find()) {
            String indent = matcher1.group(1);
            String type = matcher1.group(2);
            String varName = matcher1.group(3);
            String varNameInQuotes = matcher1.group(4);
            
            String debugCall = getDebugMakeSymbolicCall(type, varNameInQuotes);
            return indent + type + " " + varName + " = " + debugCall + ";";
        }
        
        Pattern pattern2 = Pattern.compile("(\\s+)([\\w.<?>,]+)\\s+(\\w+)\\s*=\\s*\\(([\\w.<?>,]+)\\)\\s*Symbolic\\.input\\(\"([^\"]+)\"\\);");
        Matcher matcher2 = pattern2.matcher(line);
        if (matcher2.find()) {
            String indent = matcher2.group(1);
            String type = matcher2.group(2);
            String varName = matcher2.group(3);
            String castType = matcher2.group(4);
            String varNameInQuotes = matcher2.group(5);
            
            String baseType = castType.split("[<>]")[0].trim();
            
            if (TypeMapper.isCollectionType(baseType)) {
                String genericType = TypeMapper.getGenericType(castType);
                String initCode = TypeMapper.getCollectionInitCode(castType, varNameInQuotes);
                return indent + genericType + " " + varName + " = " + initCode + ";";
            } else {
                String debugCall = TypeMapper.getMakeSymbolicRefCall(baseType, varNameInQuotes, false);
                return indent + type + " " + varName + " = " + debugCall + ";";
            }
        }
        
        if (line.contains("Symbolic.input(")) {
            Pattern fallbackPattern = Pattern.compile("(\\s+)([\\w.<?>,]+)\\s+(\\w+)\\s*=\\s*.*Symbolic\\.input\\(\"([^\"]+)\"\\);");
            Matcher fallbackMatcher = fallbackPattern.matcher(line);
            if (fallbackMatcher.find()) {
                String type = fallbackMatcher.group(2);
                String baseType = type.split("[<>]")[0].trim();
                if (TypeMapper.isCollectionType(baseType)) {
                    String genericType = TypeMapper.getGenericType(type);
                    String varName = fallbackMatcher.group(3);
                    String varNameInQuotes = fallbackMatcher.group(4);
                    String initCode = TypeMapper.getCollectionInitCode(type, varNameInQuotes);
                    return fallbackMatcher.group(1) + genericType + " " + varName + " = " + initCode + ";";
                }
            }
            return line.replace("Symbolic.input(\"", "Debug.makeSymbolicRef(\"").replace("\")", "\", null)");
        }
        
        return line;
    }
    
    private String getDebugMakeSymbolicCall(String type, String varName) {
        if (type.equalsIgnoreCase("int") || type.equals("Integer")) {
            return "Debug.makeSymbolicInteger(\"" + varName + "\")";
        } else if (type.equalsIgnoreCase("double") || type.equals("Double")) {
            return "Debug.makeSymbolicDouble(\"" + varName + "\")";
        } else if (type.equalsIgnoreCase("String")) {
            return "Debug.makeSymbolicString(\"" + varName + "\")";
        } else if (type.equalsIgnoreCase("boolean") || type.equals("Boolean")) {
            return "Debug.makeSymbolicInteger(\"" + varName + "\") != 0";
        } else if (TypeMapper.isCollectionType(type)) {
            return TypeMapper.getCollectionInitCode(type, varName);
        } else {
            return TypeMapper.getMakeSymbolicRefCall(type, varName, false);
        }
    }
    
    private String[] extractClassInfo(String javaCode) {
        String packageName = null;
        String className = null;
        
        String[] lines = javaCode.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("package ")) {
                packageName = line.substring(8, line.length() - 1).trim();
            } else if (line.startsWith("public class ")) {
                className = line.substring(13).split("\\s|\\{")[0].trim();
                break;
            }
        }
        
        if (packageName != null && className != null) {
            return new String[]{packageName, className};
        }
        return null;
    }
    
    private List<String> extractTestMethods(String javaCode) {
        List<String> methods = new ArrayList<>();
        Pattern methodPattern = Pattern.compile("public\\s+void\\s+(\\w+_helper)\\s*\\(\\s*\\)");
        Matcher matcher = methodPattern.matcher(javaCode);
        
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        
        return methods;
    }
    
    private String extractMethodSignature(String javaCode, String methodName) {
        Pattern methodPattern = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:static)?\\s*(?:void|\\w+)\\s+" + 
            Pattern.quote(methodName) + 
            "\\s*\\(([^)]*)\\)"
        );
        Matcher matcher = methodPattern.matcher(javaCode);
        
        if (matcher.find()) {
            String params = matcher.group(1).trim();
            if (params.isEmpty()) {
                return methodName + "()";
            }
            
            List<String> paramParts = new ArrayList<>();
            int depth = 0;
            StringBuilder currentParam = new StringBuilder();
            
            for (char c : params.toCharArray()) {
                if (c == '<') depth++;
                else if (c == '>') depth--;
                else if (c == ',' && depth == 0) {
                    paramParts.add(currentParam.toString().trim());
                    currentParam = new StringBuilder();
                    continue;
                }
                currentParam.append(c);
            }
            if (currentParam.length() > 0) {
                paramParts.add(currentParam.toString().trim());
            }
            
            StringBuilder signature = new StringBuilder(methodName + "(");
            
            for (int i = 0; i < paramParts.size(); i++) {
                String param = paramParts.get(i);
                if (param.isEmpty()) continue;
                
                param = param.replaceAll("^@\\w+\\s*", "");
                param = param.replaceAll("^final\\s+", "");
                param = param.replaceAll("\\s+[a-zA-Z_][a-zA-Z0-9_]*$", "");
                param = param.trim();
                if (param.equals("String[]") || param.equals("String []")) {
                    signature.append("java.lang.String[]");
                } else if (param.equals("int[]") || param.equals("int []")) {
                    signature.append("int[]");
                } else if (param.equals("double[]") || param.equals("double []")) {
                    signature.append("double[]");
                } else if (param.equals("boolean[]") || param.equals("boolean []")) {
                    signature.append("boolean[]");
                } else if (param.equals("String")) {
                    signature.append("java.lang.String");
                } else if (param.equals("int")) {
                    signature.append("int");
                } else if (param.equals("double")) {
                    signature.append("double");
                } else if (param.equals("boolean")) {
                    signature.append("boolean");
                } else if (param.equals("long")) {
                    signature.append("long");
                } else if (param.equals("float")) {
                    signature.append("float");
                } else if (param.equals("char")) {
                    signature.append("char");
                } else if (param.equals("byte")) {
                    signature.append("byte");
                } else if (param.equals("short")) {
                    signature.append("short");
                } else if (param.contains("[]")) {
                    signature.append(param);
                } else {
                    signature.append(param);
                }
                
                if (i < paramParts.size() - 1) {
                    signature.append(",");
                }
            }
            
            signature.append(")");
            return signature.toString();
        }
        
        return methodName + "()";
    }
    
    public void printBothVersions(String simpleJavaCode) {
        String separator = "================================================================================";
        System.out.println(separator);
        System.out.println("SIMPLE JAVA CODE (Generated by NewGenATC):");
        System.out.println(separator);
        System.out.println(simpleJavaCode);
        System.out.println();
        
        String jpfCode = transformToJpfCode(simpleJavaCode);
        System.out.println(separator);
        System.out.println("JPF-TRANSFORMED CODE (Ready for Symbolic PathFinder):");
        System.out.println(separator);
        System.out.println(jpfCode);
        System.out.println();
        
        String[] classInfo = extractClassInfo(simpleJavaCode);
        if (classInfo != null) {
            String packageName = classInfo[0];
            String className = classInfo[1];
            String fullClassName = packageName + "." + className;
            
            List<String> testMethods = extractTestMethods(simpleJavaCode);
            
            if (!testMethods.isEmpty()) {
                System.out.println(separator);
                System.out.println("GENERATED .JPF CONFIGURATION FILES:");
                System.out.println(separator);
                
                String firstMethod = testMethods.get(0);
                try {
                    String jpfContent = generateJpfFile(fullClassName, firstMethod, null, null, null, simpleJavaCode);
                    System.out.println("# Example .jpf file for: " + fullClassName + "." + firstMethod + "()");
                    System.out.println(jpfContent);
                    System.out.println();
                    
                    if (testMethods.size() > 1) {
                        System.out.println("# Note: " + (testMethods.size() - 1) + " more test method(s) found.");
                        System.out.println("# Use generateJpfFilesForMethods() to generate .jpf files for all methods.");
                    }
                } catch (IOException e) {
                    System.err.println("Error generating .jpf file: " + e.getMessage());
                }
            }
        }
    }
    
    public List<ConcreteInput> run(JavaFile atcJavaFile) {
        return run(atcJavaFile, null);
    }
    
    public List<ConcreteInput> run(JavaFile atcJavaFile, AtcClass atcClass) {
        printBothVersions(atcJavaFile.getContent());
        
        String jpfCode = transformToJpfCode(atcJavaFile.getContent());
        
        try {
            saveOutputFiles(atcJavaFile.getContent(), jpfCode, atcClass);
        } catch (IOException e) {
            System.err.println("Error saving output files: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("(SPF execution not yet implemented)");
        
        return new ArrayList<ConcreteInput>();
    }
    
    private void saveOutputFiles(String simpleJavaCode, String jpfCode, AtcClass atcClass) throws IOException {
        System.out.println("\n=== Saving output files ===");
        String outputDir = "outputs";
        java.io.File outputDirFile = new java.io.File(outputDir);
        if (!outputDirFile.exists()) {
            boolean created = outputDirFile.mkdirs();
            System.out.println("Created outputs directory: " + created + " (path: " + outputDirFile.getAbsolutePath() + ")");
        } else {
            System.out.println("Outputs directory already exists: " + outputDirFile.getAbsolutePath());
        }
        
        String packageName = null;
        String className = null;
        String fullClassName = null;
        List<String> testMethods = new ArrayList<>();
        
        if (atcClass != null) {
            packageName = atcClass.getPackageName();
            className = atcClass.getClassName();
            fullClassName = (packageName != null && !packageName.isEmpty()) 
                ? packageName + "." + className 
                : className;
            
            for (AtcTestMethod method : atcClass.getTestMethods()) {
                if (method.isTestAnnotated() && !method.isMain()) {
                    testMethods.add(method.getMethodName());
                }
            }
        } else {
            String[] classInfo = extractClassInfo(simpleJavaCode);
            if (classInfo != null) {
                packageName = classInfo[0];
                className = classInfo[1];
                fullClassName = packageName + "." + className;
                testMethods = extractTestMethods(simpleJavaCode);
            } else {
                className = "GeneratedATCs";
                fullClassName = className;
                testMethods = extractTestMethods(simpleJavaCode);
            }
        }
        
        String javaFileName = className + ".java";
        String javaFilePath = Paths.get(outputDir, javaFileName).toString();
        try (FileWriter writer = new FileWriter(javaFilePath)) {
            writer.write(jpfCode);
            System.out.println("Saved JPF-transformed Java file: " + javaFilePath);
        }
        
        String mainJpfFileName = className + "_main.jpf";
        String mainJpfFilePath = Paths.get(outputDir, mainJpfFileName).toString();
        try {
            generateJpfFile(fullClassName, "main", mainJpfFilePath, null, null, jpfCode);
            System.out.println("Generated main .jpf file: " + mainJpfFilePath);
        } catch (IOException e) {
            System.err.println("Error generating main .jpf file: " + e.getMessage());
        }
        
        if (!testMethods.isEmpty() && fullClassName != null) {
            List<String> jpfFiles = generateJpfFilesForMethods(fullClassName, testMethods, outputDir, null, null, jpfCode);
            System.out.println("Generated " + jpfFiles.size() + " additional .jpf file(s) for individual test methods:");
            for (String jpfFile : jpfFiles) {
                System.out.println("  - " + jpfFile);
            }
        }
        
        String helperJavaPath = Paths.get(outputDir, "in", "ac", "iiitb", "plproject", "atc", "generated", "Helper.java").toString();
        java.io.File helperJavaFile = new java.io.File(helperJavaPath);
        helperJavaFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(helperJavaPath)) {
            writer.write("package in.ac.iiitb.plproject.atc.generated;\n\n");
            writer.write("import java.util.Set;\n");
            writer.write("import java.util.Map;\n");
            writer.write("import java.util.HashSet;\n");
            writer.write("import java.util.HashMap;\n\n");
            writer.write("public class Helper {\n");
            writer.write("    public static void increment(int[] x) {\n");
            writer.write("        if (x != null && x.length > 0) {\n");
            writer.write("            x[0] = x[0] + 1;\n");
            writer.write("        }\n");
            writer.write("    }\n\n");
            writer.write("    public static void process(Set<Integer> data, Map<Integer, Integer> result) {\n");
            writer.write("        if (data != null && result != null) {\n");
            writer.write("            for (Integer item : data) {\n");
            writer.write("                result.put(item, item * 2);\n");
            writer.write("            }\n");
            writer.write("        }\n");
            writer.write("    }\n\n");
            writer.write("    public static Map<?,?> update(Map<Integer, Integer> result, Set<Integer> data) {\n");
            writer.write("        Map<Integer, Integer> updated = new HashMap<>(result);\n");
            writer.write("        if (data != null) {\n");
            writer.write("            for (Integer item : data) {\n");
            writer.write("                updated.put(item, item * 2);\n");
            writer.write("            }\n");
            writer.write("        }\n");
            writer.write("        return updated;\n");
            writer.write("    }\n");
            writer.write("}\n");
            System.out.println("Generated Helper.java: " + helperJavaPath);
        } catch (IOException e) {
            System.err.println("Error generating Helper.java: " + e.getMessage());
        }
    }
    
    public String getJpfCode(String simpleJavaCode) {
        return transformToJpfCode(simpleJavaCode);
    }
    
    public String generateJpfFile(String className, String methodName, String outputPath, 
                                   String classpath, String sourcepath) throws IOException {
        return generateJpfFile(className, methodName, outputPath, classpath, sourcepath, null);
    }
    
    public String generateJpfFile(String className, String methodName, String outputPath, 
                                   String classpath, String sourcepath, String javaCode) throws IOException {
        StringBuilder jpfContent = new StringBuilder();
        
        jpfContent.append("# Target class\n");
        jpfContent.append("target = ").append(className).append("\n\n");
        
        jpfContent.append("# Set the classpath to point to your compiled classes\n");
        if (classpath != null && !classpath.isEmpty()) {
            jpfContent.append("classpath = ").append(classpath).append("\n");
        } else {
            jpfContent.append("classpath = ${jpf-symbc}/build/examples\n");
        }
        jpfContent.append("\n");
        
        jpfContent.append("# Path to source code\n");
        if (sourcepath != null && !sourcepath.isEmpty()) {
            jpfContent.append("sourcepath = ").append(sourcepath).append("\n");
        } else {
            jpfContent.append("sourcepath = ${jpf-symbc}/src/examples\n");
        }
        jpfContent.append("\n");
        
        if (!"main".equals(methodName)) {
            String methodSignature;
            if (javaCode != null && !javaCode.isEmpty()) {
                methodSignature = extractMethodSignature(javaCode, methodName);
            } else {
                methodSignature = methodName + "()";
            }
            
            jpfContent.append("# Define symbolic variables in the method under test\n");
            jpfContent.append("symbolic.method = ").append(className).append(".").append(methodSignature).append("\n\n");
        }
        
        if ("main".equals(methodName)) {
            jpfContent.append("# Constraint solver - required for symbolic execution\n");
            jpfContent.append("symbolic.dp = z3\n\n");
        }
        
        jpfContent.append("# Symbolic string variables created via Debug.makeSymbolicString\n");
        jpfContent.append("symbolic.string_dp = true\n");
        
        if ("main".equals(methodName)) {
            jpfContent.append("\n");
            jpfContent.append("# Add instruction factory for symbolic execution\n");
            jpfContent.append("vm.insn_factory.class = gov.nasa.jpf.symbc.SymbolicInstructionFactory\n\n");
            jpfContent.append("# Enable symbolic array handling (needed for array operations)\n");
            jpfContent.append("symbolic.arrays = true\n\n");
        }
        
        jpfContent.append("\n# Integer ranges\n");
        jpfContent.append("symbolic.minint = -100\n");
        jpfContent.append("symbolic.maxint = 100\n");
        jpfContent.append("symbolic.undefined = -1000\n\n");
        
        jpfContent.append("# Search bounds to prevent infinite exploration\n");
        if ("main".equals(methodName)) {
            jpfContent.append("search.depth_limit = 200\n");
            jpfContent.append("search.time_limit = 60\n");
        } else {
            jpfContent.append("search.depth_limit = 500\n");
            jpfContent.append("search.time_limit = 120\n");
        }
        jpfContent.append("search.multiple_errors = true\n");
        jpfContent.append("search.class = .search.heuristic.BFSHeuristic\n\n");
        
        if ("main".equals(methodName)) {
            jpfContent.append("# Show path conditions and symbolic execution results\n");
            jpfContent.append("# SymbolicPathListener displays path conditions (constraints like \"x > CONST_0\")\n");
            jpfContent.append("jpf.report.console.finished = gov.nasa.jpf.symbc.SymbolicPathListener\n\n");
            jpfContent.append("# Disable verbose debug output - we only want test inputs, not search process\n");
            jpfContent.append("# symbolic.debug = true\n\n");
            jpfContent.append("# Enable output so System.out.println shows test input values\n");
            jpfContent.append("vm.output = true\n");
        } else {
            jpfContent.append("# Listeners for test input extraction and coverage\n");
            jpfContent.append("# SymbolicSequenceListener generates JUnit tests with concrete values\n");
            jpfContent.append("listener = gov.nasa.jpf.symbc.sequences.SymbolicSequenceListener,gov.nasa.jpf.listener.CoverageAnalyzer\n\n");
            
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
            jpfContent.append("# Show method coverage\n");
            jpfContent.append("coverage.include = *.").append(simpleClassName).append("\n");
            jpfContent.append("coverage.show_methods = true\n");
            jpfContent.append("coverage.show_bodies = true\n\n");
            
            jpfContent.append("# To extract concrete test input values from JPF output:\n");
            jpfContent.append("# 1. Look for 'pc X constraint # = Y' sections - these show path conditions\n");
            jpfContent.append("# 2. Look for 'JUnit 4.0 test class' section - shows generated test cases\n");
            jpfContent.append("# 3. The path conditions show constraints; JPF's solver finds values satisfying them\n");
            jpfContent.append("# 4. For actual concrete values, check the constraint solver output or add Debug.printPC() calls\n");
        }
        
        String content = jpfContent.toString();
        
        if (outputPath != null && !outputPath.isEmpty()) {
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(content);
            }
        }
        
        return content;
    }
    
    public List<String> generateJpfFilesForMethods(String className, List<String> methodNames, 
                                                    String outputDir, String classpath, String sourcepath,
                                                    String javaCode) 
            throws IOException {
        List<String> generatedFiles = new ArrayList<>();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        
        for (String methodName : methodNames) {
            String jpfFileName = simpleClassName + "_" + methodName + ".jpf";
            String outputPath = (outputDir != null && !outputDir.isEmpty()) 
                ? Paths.get(outputDir, jpfFileName).toString() 
                : jpfFileName;
            
            generateJpfFile(className, methodName, outputPath, classpath, sourcepath, javaCode);
            generatedFiles.add(outputPath);
        }
        
        return generatedFiles;
    }
    
    public List<String> generateJpfFilesFromAtcClass(AtcClass atcClass, String outputDir, 
                                                     String classpath, String sourcepath, String javaCode) 
            throws IOException {
        String fullClassName = atcClass.getPackageName() + "." + atcClass.getClassName();
        List<String> methodNames = new ArrayList<>();
        
        for (AtcTestMethod method : atcClass.getTestMethods()) {
            methodNames.add(method.getMethodName());
        }
        
        return generateJpfFilesForMethods(fullClassName, methodNames, outputDir, classpath, sourcepath, javaCode);
    }
}
