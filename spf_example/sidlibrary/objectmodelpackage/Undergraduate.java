package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * Undergraduate student - extends Student and implements LibraryMember
 */
public class Undergraduate extends Student implements LibraryMember {
    private int year; // 1st, 2nd, 3rd, 4th year
    private String major;
    private boolean honorsProgram;

    public Undergraduate(String studentName, String studentId, String programEnrolledIn, 
                        int year, String major) {
        super(studentName, studentId, programEnrolledIn);
        this.year = year;
        this.major = major;
        this.honorsProgram = false;
    }

    public Undergraduate(String instituteName, String studentName, String studentId, 
                        String programEnrolledIn, int year, String major) {
        super(instituteName, studentName, studentId, programEnrolledIn);
        this.year = year;
        this.major = major;
        this.honorsProgram = false;
    }

    public Undergraduate() {
        super();
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public boolean isHonorsProgram() { return honorsProgram; }
    public void setHonorsProgram(boolean honorsProgram) { this.honorsProgram = honorsProgram; }

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
        // Undergraduates can borrow 5 books
        return 5;
    }

    // Override methods with UG-specific behavior
    @Override
    public int issueCard(String instituteName) {
        if (instituteName.equals("COMSATS")) {
            if (hasLibraryAccess(instituteName)) {
                return 2; // Already has card
            }
            // Undergraduates need to be at least 2nd year
            if (year >= 2 || honorsProgram) {
                super.issueCard(instituteName);
                return 1;
            }
            return 0; // Not eligible
        }
        return 0;
    }

    @Override
    public void addStudent() {
        System.out.println("Undergraduate student added: " + getStudentName() + 
                          ", " + getStudentId() + ", Year: " + year + ", Major: " + major);
    }

    @Override
    public ArrayList<Student> displayStudents() {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Undergraduate("UG Student 1", "UG001", "UG", 2, "Computer Science"));
        students.add(new Undergraduate("UG Student 2", "UG002", "UG", 3, "Mathematics"));
        return students;
    }
}

