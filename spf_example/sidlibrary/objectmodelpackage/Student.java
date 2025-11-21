package sidlibrary.objectmodelpackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Student extends Institute {

    private String studentName;
    private String studentId;
    private String programEnrolledIn;
    private int fine;
    private ArrayList<Book> issuedBooks;
    private boolean cmsatsLibraryCard;

    public Student() {}

    public Student(String studentName, String studentId, String programEnrolledIn) {
        super("COMSATS");
        this.studentName = studentName;
        this.studentId = studentId;
        this.programEnrolledIn = programEnrolledIn;
        issuedBooks = new ArrayList<>();
    }

    public Student(String instituteName, String studentName, String studentId, String programEnrolledIn) {
        super("COMSATS");
        this.studentName = studentName;
        this.studentId = studentId;
        this.programEnrolledIn = programEnrolledIn;
        this.cmsatsLibraryCard = false;
        issuedBooks = new ArrayList<>();
    }

    public int issueCard(String instituteName) {
        if (instituteName != null && instituteName.equals("COMSATS")) {
            if (cmsatsLibraryCard) {
                return 2; // Already has card
            }
            cmsatsLibraryCard = true;
            return 1; // Card issued successfully
        }
        return 0; // Invalid institute or failed
    }

    public boolean hasLibraryAccess(String instituteName) {
        return "COMSATS".equals(instituteName) && cmsatsLibraryCard;
    }

    public void addBookToIssueList(Book book) {
        this.issuedBooks.add(book);
    }

    public String getStudentName() { return studentName; }

    public String getStudentId() { return studentId; }

    public String getProgramEnrolledIn() { return programEnrolledIn; }

    public ArrayList<Book> getIssuedBooks() { return issuedBooks; }

    public int getFine() { return fine; }

    public void setFine(int fine) { this.fine = fine; }

    // File handling removed for SPF compatibility
    public void addStudent() {
        // Simulated logic for adding student (no actual file I/O)
        System.out.println("Student added: " + studentName + ", " + studentId);
    }

    // Return a mock list of students for symbolic testing
    public ArrayList<Student> displayStudents() {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student("Student1", "S001", "UG"));
        students.add(new Student("Student2", "S002", "PG"));
        return students;
    }

    // Implementation of abstract methods from Institute
    @Override
    public boolean validateMembership(String memberId) {
        return memberId != null && 
               !memberId.isEmpty() && 
               memberId.equals(getStudentId());
    }

    @Override
    public int getMaxBorrowingLimit(String memberType) {
        // Use programEnrolledIn if memberType matches, otherwise default
        if (memberType != null && memberType.equals(programEnrolledIn)) {
            switch (programEnrolledIn) {
                case "UG": return 5;
                case "PG": return 3;
                case "PHD": return 2;
                default: return 3;
            }
        }
        return 3; // Default limit
    }

    @Override
    public List<String> getAvailableServices() {
        return Arrays.asList("Book Borrowing", "Reading Room Access", 
                           "Reference Services", "Study Space");
    }

    @Override
    public double calculateMembershipFee(String memberType) {
        // Students typically don't pay membership fees
        return 0.0;
    }
}
