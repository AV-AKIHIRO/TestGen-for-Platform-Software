package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * PhD Student - extends Graduate and implements LibraryMember
 * Demonstrates multi-level inheritance
 * Note: LibraryMember is already implemented by Graduate, but we keep it for clarity
 */
public class PhDStudent extends Graduate {
    private String dissertationTopic;
    private int yearsInProgram;
    private boolean comprehensiveExamPassed;

    public PhDStudent(String studentName, String studentId, String programEnrolledIn,
                     String researchArea, String advisorName, String dissertationTopic) {
        super(studentName, studentId, programEnrolledIn, researchArea, advisorName);
        this.dissertationTopic = dissertationTopic;
        this.yearsInProgram = 0;
        this.comprehensiveExamPassed = false;
    }

    public PhDStudent(String instituteName, String studentName, String studentId,
                     String programEnrolledIn, String researchArea, String advisorName,
                     String dissertationTopic) {
        super(instituteName, studentName, studentId, programEnrolledIn, researchArea, advisorName);
        this.dissertationTopic = dissertationTopic;
        this.yearsInProgram = 0;
        this.comprehensiveExamPassed = false;
    }

    public PhDStudent() {
        super();
    }

    public String getDissertationTopic() { return dissertationTopic; }
    public void setDissertationTopic(String dissertationTopic) { 
        this.dissertationTopic = dissertationTopic; 
    }
    public int getYearsInProgram() { return yearsInProgram; }
    public void setYearsInProgram(int yearsInProgram) { 
        this.yearsInProgram = yearsInProgram; 
    }
    public boolean isComprehensiveExamPassed() { return comprehensiveExamPassed; }
    public void setComprehensiveExamPassed(boolean comprehensiveExamPassed) { 
        this.comprehensiveExamPassed = comprehensiveExamPassed; 
    }

    // LibraryMember interface implementation (overriding parent)
    @Override
    public int getBorrowingLimit() {
        // PhD students can borrow 2 books (more restrictive)
        return 2;
    }

    // Override methods with PhD-specific behavior
    @Override
    public int issueCard(String instituteName) {
        if (instituteName.equals("COMSATS")) {
            if (hasLibraryAccess(instituteName)) {
                return 2; // Already has card
            }
            // PhD students always eligible
            super.issueCard(instituteName);
            return 1;
        }
        return 0;
    }

    @Override
    public void addStudent() {
        System.out.println("PhD student added: " + getStudentName() + 
                          ", " + getStudentId() + ", Dissertation: " + dissertationTopic + 
                          ", Years: " + yearsInProgram);
    }

    @Override
    public ArrayList<Student> displayStudents() {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new PhDStudent("PhD Student 1", "P001", "PHD", 
                                   "Machine Learning", "Dr. Brown", "Deep Learning"));
        students.add(new PhDStudent("PhD Student 2", "P002", "PHD", 
                                   "Cryptography", "Dr. White", "Quantum Crypto"));
        return students;
    }
}

