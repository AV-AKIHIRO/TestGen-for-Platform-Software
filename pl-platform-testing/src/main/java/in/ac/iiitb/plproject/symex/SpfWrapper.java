package in.ac.iiitb.plproject.symex;

import in.ac.iiitb.plproject.atc.ConcreteInput;
import in.ac.iiitb.plproject.atc.ir.AtcClass;
import in.ac.iiitb.plproject.atc.ir.AtcTestMethod;
import in.ac.iiitb.plproject.atc.ir.AtcIrCodeGenerator;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class SpfWrapper {
    
    private AtcIrToSymbolicIrTransformer transformer;
    private AtcIrCodeGenerator codeGenerator;
    
    public SpfWrapper() {
        this.transformer = new AtcIrToSymbolicIrTransformer();
        this.codeGenerator = new AtcIrCodeGenerator();
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
    
    public void printBothVersions(AtcClass atcClass) {
        String simpleJavaCode = codeGenerator.generateJavaFile(atcClass);
        AtcClass symbolicIr = transformer.transform(atcClass);
        String jpfCode = codeGenerator.generateSymbolicJavaFile(symbolicIr);
        printBothVersions(simpleJavaCode, jpfCode, atcClass);
    }
    
    private void printBothVersions(String simpleJavaCode, String jpfCode, AtcClass atcClass) {
        String separator = "================================================================================";
        
        System.out.println("\n" + separator);
        System.out.println("ORIGINAL ATC CODE (Generated by NewGenATC - Non-Symbolic):");
        System.out.println(separator);
        System.out.println(simpleJavaCode);
        System.out.println();
        
        System.out.println(separator);
        System.out.println("JPF-TRANSFORMED CODE (Ready for Symbolic PathFinder):");
        System.out.println(separator);
        System.out.println(jpfCode);
        System.out.println();
        
        String packageName = atcClass.getPackageName();
        String className = atcClass.getClassName();
        String fullClassName = (packageName != null && !packageName.isEmpty()) 
            ? packageName + "." + className 
            : className;
        
        List<String> testMethods = new ArrayList<>();
        for (AtcTestMethod method : atcClass.getTestMethods()) {
            if (method.isTestAnnotated() && !method.isMain()) {
                testMethods.add(method.getMethodName());
            }
        }
        
        if (fullClassName != null && !testMethods.isEmpty()) {
            System.out.println(separator);
            System.out.println("GENERATED .JPF CONFIGURATION FILES:");
            System.out.println(separator);
            
            String firstMethod = testMethods.get(0);
            try {
                String jpfContent = generateJpfFile(fullClassName, firstMethod, null, null, null, jpfCode);
                System.out.println("# Example .jpf file for: " + fullClassName + "." + firstMethod + "()");
                System.out.println(jpfContent);
                System.out.println();
                
                if (testMethods.size() > 1) {
                    System.out.println("# " + (testMethods.size() - 1) + " more test method(s) found.");
                }
            } catch (IOException e) {
                System.err.println("Error generating .jpf file: " + e.getMessage());
            }
        }
    }
    
    public List<ConcreteInput> run(AtcClass atcClass) {
        printBothVersions(atcClass);
        
        AtcClass symbolicIr = transformer.transform(atcClass);
        String jpfCode = codeGenerator.generateSymbolicJavaFile(symbolicIr);
        String simpleJavaCode = codeGenerator.generateJavaFile(atcClass);
        
        try {
            saveOutputFiles(simpleJavaCode, jpfCode, symbolicIr);
        } catch (IOException e) {
            System.err.println("Error saving output files: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("(SPF execution not yet implemented)");
        return new ArrayList<ConcreteInput>();
    }
    
    public String transformIrToJpfCode(AtcClass atcClass) {
        AtcClass symbolicIr = transformer.transform(atcClass);
        return codeGenerator.generateSymbolicJavaFile(symbolicIr);
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
        
        String packageName = atcClass.getPackageName();
        String className = atcClass.getClassName();
        String fullClassName = (packageName != null && !packageName.isEmpty()) 
            ? packageName + "." + className 
            : className;
        
        List<String> testMethods = new ArrayList<>();
        for (AtcTestMethod method : atcClass.getTestMethods()) {
            if (method.isTestAnnotated() && !method.isMain()) {
                testMethods.add(method.getMethodName());
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
            writer.write("    public static void appendExclamation(String s) {\n");
            writer.write("        if (s != null) {\n");
            writer.write("        }\n");
            writer.write("    }\n\n");
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
            writer.write("        if (result == null) {\n");
            writer.write("            return new HashMap<>();\n");
            writer.write("        }\n");
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
    
    public String getJpfCode(AtcClass atcClass) {
        return transformIrToJpfCode(atcClass);
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
