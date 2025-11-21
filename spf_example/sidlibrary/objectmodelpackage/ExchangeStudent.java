package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * Exchange Student - extends Student and implements LibraryMember
 * Has different borrowing rules
 */
public class ExchangeStudent extends Student implements LibraryMember {
    private String homeUniversity;
    private String exchangeProgram;
    private int exchangeDurationMonths;
    private boolean homeLibraryAccess;

    public ExchangeStudent(String studentName, String studentId, String programEnrolledIn,
                          String homeUniversity, String exchangeProgram, int exchangeDurationMonths) {
        super(studentName, studentId, programEnrolledIn);
        this.homeUniversity = homeUniversity;
        this.exchangeProgram = exchangeProgram;
        this.exchangeDurationMonths = exchangeDurationMonths;
        this.homeLibraryAccess = false;
    }

    public ExchangeStudent(String instituteName, String studentName, String studentId,
                          String programEnrolledIn, String homeUniversity, 
                          String exchangeProgram, int exchangeDurationMonths) {
        super(instituteName, studentName, studentId, programEnrolledIn);
        this.homeUniversity = homeUniversity;
        this.exchangeProgram = exchangeProgram;
        this.exchangeDurationMonths = exchangeDurationMonths;
        this.homeLibraryAccess = false;
    }

    public ExchangeStudent() {
        super();
    }

    public String getHomeUniversity() { return homeUniversity; }
    public void setHomeUniversity(String homeUniversity) { this.homeUniversity = homeUniversity; }
    public String getExchangeProgram() { return exchangeProgram; }
    public void setExchangeProgram(String exchangeProgram) { 
        this.exchangeProgram = exchangeProgram; 
    }
    public int getExchangeDurationMonths() { return exchangeDurationMonths; }
    public void setExchangeDurationMonths(int exchangeDurationMonths) { 
        this.exchangeDurationMonths = exchangeDurationMonths; 
    }
    public boolean hasHomeLibraryAccess() { return homeLibraryAccess; }
    public void setHomeLibraryAccess(boolean homeLibraryAccess) { 
        this.homeLibraryAccess = homeLibraryAccess; 
    }

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
        // Exchange students have limited borrowing (3 books)
        return 3;
    }

    // Override methods with Exchange-specific behavior
    @Override
    public int issueCard(String instituteName) {
        if (instituteName.equals("COMSATS")) {
            if (hasLibraryAccess(instituteName)) {
                return 2; // Already has card
            }
            // Exchange students need valid exchange program
            if (exchangeProgram != null && !exchangeProgram.isEmpty() && 
                exchangeDurationMonths > 0) {
                super.issueCard(instituteName);
                return 1;
            }
            return 0; // Not eligible
        }
        return 0;
    }

    @Override
    public void addStudent() {
        System.out.println("Exchange student added: " + getStudentName() + 
                          ", " + getStudentId() + ", Home: " + homeUniversity + 
                          ", Program: " + exchangeProgram);
    }

    @Override
    public ArrayList<Student> displayStudents() {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new ExchangeStudent("Exchange Student 1", "E001", "UG", 
                                        "MIT", "Erasmus", 6));
        students.add(new ExchangeStudent("Exchange Student 2", "E002", "PG", 
                                        "Stanford", "Fulbright", 12));
        return students;
    }
}

