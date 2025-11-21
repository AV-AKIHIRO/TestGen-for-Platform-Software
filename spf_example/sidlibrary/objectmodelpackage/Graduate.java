package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * Graduate student - extends Student and implements LibraryMember
 */
public class Graduate extends Student implements LibraryMember {
    private String researchArea;
    private String advisorName;
    private boolean thesisDefended;

    public Graduate(String studentName, String studentId, String programEnrolledIn,
                   String researchArea, String advisorName) {
        super(studentName, studentId, programEnrolledIn);
        this.researchArea = researchArea;
        this.advisorName = advisorName;
        this.thesisDefended = false;
    }

    public Graduate(String instituteName, String studentName, String studentId,
                   String programEnrolledIn, String researchArea, String advisorName) {
        super(instituteName, studentName, studentId, programEnrolledIn);
        this.researchArea = researchArea;
        this.advisorName = advisorName;
        this.thesisDefended = false;
    }

    public Graduate() {
        super();
    }

    public String getResearchArea() { return researchArea; }
    public void setResearchArea(String researchArea) { this.researchArea = researchArea; }
    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }
    public boolean isThesisDefended() { return thesisDefended; }
    public void setThesisDefended(boolean thesisDefended) { this.thesisDefended = thesisDefended; }

    // LibraryMember interface implementation
    @Override
    public String getMemberId() {
        return getStudentId();
    }

    @Override
    public String getMemberName() {
        return getStudentName();
    }

    @Override
    public boolean hasActiveMembership() {
        return hasLibraryAccess("COMSATS");
    }

    @Override
    public void activateMembership() {
        issueCard("COMSATS");
    }

    @Override
    public void deactivateMembership() {
        // Implementation for deactivating membership
    }

    @Override
    public int getBorrowingLimit() {
        // Graduate students can borrow 3 books
        return 3;
    }

    // Override methods with Graduate-specific behavior
    @Override
    public int issueCard(String instituteName) {
        if (instituteName.equals("COMSATS")) {
            if (hasLibraryAccess(instituteName)) {
                return 2; // Already has card
            }
            // Graduate students always eligible
            super.issueCard(instituteName);
            return 1;
        }
        return 0;
    }

    @Override
    public void addStudent() {
        System.out.println("Graduate student added: " + getStudentName() + 
                          ", " + getStudentId() + ", Research: " + researchArea + 
                          ", Advisor: " + advisorName);
    }

    @Override
    public ArrayList<Student> displayStudents() {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Graduate("Grad Student 1", "G001", "PG", "AI", "Dr. Smith"));
        students.add(new Graduate("Grad Student 2", "G002", "PG", "Networks", "Dr. Jones"));
        return students;
    }
}

