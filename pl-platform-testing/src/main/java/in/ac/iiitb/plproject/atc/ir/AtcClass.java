package in.ac.iiitb.plproject.atc.ir;

import java.util.List;

/**
 * Represents the entire test file (e.g., "GeneratedATCs.java").
 * This is the root of your new IR.
 */
public class AtcClass {
    private String packageName;
    private String className;
    private List<String> imports;
    private List<AtcTestMethod> testMethods; // Now includes helper methods
    private List<AtcStatement> mainMethodStatements; // New field for main method calls
    private String runWithAnnotationClass;

    public AtcClass(String packageName, String className, List<String> imports, List<AtcTestMethod> testMethods, List<AtcStatement> mainMethodStatements, String runWithAnnotationClass) {
        this.packageName = packageName;
        this.className = className;
        this.imports = imports;
        this.testMethods = testMethods;
        this.mainMethodStatements = mainMethodStatements; // Initialize new field
        this.runWithAnnotationClass = runWithAnnotationClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getImports() {
        return imports;
    }

    public List<AtcTestMethod> getTestMethods() {
        return testMethods;
    }

    public String getRunWithAnnotationClass() {
        return runWithAnnotationClass;
    }

    public List<AtcStatement> getMainMethodStatements() {
        return mainMethodStatements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AtcClass(");
        sb.append("package: ").append(packageName).append(", ");
        sb.append("class: ").append(className).append(", ");
        sb.append("imports: ").append(imports).append(", ");
        sb.append("testMethods: ").append(testMethods.size()).append(" methods, ");
        if (runWithAnnotationClass != null && !runWithAnnotationClass.isEmpty()) {
            sb.append("@RunWith: ").append(runWithAnnotationClass);
        }
        sb.append(")");
        return sb.toString();
    }
}
