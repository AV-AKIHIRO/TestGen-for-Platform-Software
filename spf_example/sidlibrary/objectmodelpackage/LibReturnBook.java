// SPDX-License-Identifier: MIT
package sidlibrary.objectmodelpackage;

import gov.nasa.jpf.symbc.Debug;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibReturnBook {
    private static final int perDayFine = 10;
    private static ArrayList<Student> students;
    private static Library comsatsLibrary;
    private static ArrayList<Books> booksArrayList;
    private static boolean outOfStock = false;

    public static void main(String[] args) {
        // Default to original test, but allow selection via args
        if (args.length > 0) {
            String testName = args[0];
            switch (testName) {
                case "polymorphicBooks":
                    testPolymorphicBooks();
                    break;
                case "polymorphicStudents":
                    testPolymorphicStudents();
                    break;
                case "polymorphicLibraries":
                    testPolymorphicLibraries();
                    break;
                case "multipleInheritance":
                    testMultipleInheritance();
                    break;
                case "returnBook":
                default:
                    testReturnBook();
                    break;
            }
        } else {
            testReturnBook();
        }
    }
    // ✅ Library added -> ✅ Student added → ✅ Library card issued → ✅ Book added -> ✅ Book issued → ✅ Book returned
    public static void testReturnBook() {
        // Symbolic library name and initialization
        String libraryName1 = Debug.makeSymbolicString("libraryName1");
        comsatsLibrary = new Library(libraryName1);
    
        // Add books to the library
        Books books = new Books();
        books.setBooks(new Book().displayBooks());
        comsatsLibrary.addBookToLibrary(books);
        booksArrayList = comsatsLibrary.getBooksArrayList();
    
        // Symbolic book
        String bookName1 = Debug.makeSymbolicString("bookName1");
        String bookAuthor1 = Debug.makeSymbolicString("bookAuthor1");
        Book book = new Book(bookName1, bookAuthor1);
        addBookFunction(comsatsLibrary, book);
    
        students = new ArrayList<>();
    
        // Student 1 (base student)
        String studentName1 = Debug.makeSymbolicString("studentName1");
        String studentId1 = Debug.makeSymbolicString("studentId1");
        String studentProgram1 = Debug.makeSymbolicString("studentProgram1");
        Student student1 = new Student(studentName1, studentId1, studentProgram1);
        addStudents(student1);
    
        // --- Student 2 ---
        String studentId2 = Debug.makeSymbolicString("studentId2");
        Student student2 = new Student("Student2", studentId2, "CSE");
        addStudents(student2);
        issueLibraryCard(new Library(libraryName1), studentId2);
    
        // --- Student 3 ---
        String bookName2 = Debug.makeSymbolicString("bookName2");
        String studentId3 = Debug.makeSymbolicString("studentId3");
        Student student3 = new Student("Student3", studentId3, "CSE");
        addStudents(student3);
        issueLibraryCard(new Library(libraryName1), studentId3);
    
        // Add the book to library3 too
        Book book2 = new Book(bookName2, "Author2");
        addBookFunction(comsatsLibrary, book2); // reuse comsatsLibrary for consistency
    
        issueBook(comsatsLibrary, bookName2, studentId3);
    
        // --- Student 4 ---
        String studentId4 = Debug.makeSymbolicString("studentId4");
        Student student4 = new Student("Student4", studentId4, "CSE");
        addStudents(student4);
        issueLibraryCard(new Library(libraryName1), studentId4);
    
        // Student 4 manually issued a book
        Book borrowedBook = new Book(bookName2, "AuthorX");
        borrowedBook.setBorrowDate("01/06/2021");
        borrowedBook.setReturnDate("15/06/2021");
        borrowedBook.setPendingReturn(true);
        borrowedBook.setBorrowerId(studentId4);
        student4.addBookToIssueList(borrowedBook);
        addBookFunction(comsatsLibrary, borrowedBook); // make sure library has this book too
    
        returnBook(comsatsLibrary, studentId4, bookName2);
    }

    // ✅ Library added -> ✅ Student added → ✅ Library card issued → ✅ Book added -> ❌ Book issued → ❌ Book returned
    public static void testReturnBook_Fail_NotIssued() {
        String libraryName = Debug.makeSymbolicString("libraryName");
        comsatsLibrary = new Library(libraryName);
    
        Books books = new Books();
        books.setBooks(new Book().displayBooks());
        comsatsLibrary.addBookToLibrary(books);
        booksArrayList = comsatsLibrary.getBooksArrayList();
    
        // Add a book symbolically
        String bookName = Debug.makeSymbolicString("bookName");
        String bookAuthor = Debug.makeSymbolicString("bookAuthor");
        Book book = new Book(bookName, bookAuthor);
        addBookFunction(comsatsLibrary, book);
    
        students = new ArrayList<>();
    
        // Create student symbolically
        String studentName = Debug.makeSymbolicString("studentName");
        String studentId = Debug.makeSymbolicString("studentId");
        String studentProgram = Debug.makeSymbolicString("studentProgram");
        Student student = new Student(studentName, studentId, studentProgram);
        addStudents(student);
        issueLibraryCard(new Library(libraryName), studentId);
    
        // Return without issuing
        returnBook(comsatsLibrary, studentId, bookName); // Should fail or no-op
    }

    // ✅ Library added -> ✅ Student added → ❌ Library card issued → ✅ Book added -> ❌ Book issued → ❌ Book returned
    public static void testReturnBook_Fail_NoLibraryCard() {
        String libraryName = Debug.makeSymbolicString("libraryName");
        comsatsLibrary = new Library(libraryName);
    
        Books books = new Books();
        books.setBooks(new Book().displayBooks());
        comsatsLibrary.addBookToLibrary(books);
        booksArrayList = comsatsLibrary.getBooksArrayList();
    
        String bookName = Debug.makeSymbolicString("bookName");
        String bookAuthor = Debug.makeSymbolicString("bookAuthor");
        Book book = new Book(bookName, bookAuthor);
        addBookFunction(comsatsLibrary, book);
    
        students = new ArrayList<>();
    
        String studentName = Debug.makeSymbolicString("studentName");
        String studentId = Debug.makeSymbolicString("studentId");
        String studentProgram = Debug.makeSymbolicString("studentProgram");
        Student student = new Student(studentName, studentId, studentProgram);
        addStudents(student);
    
        // No library card issued
        returnBook(comsatsLibrary, studentId, bookName); // Should fail due to missing access
    }
    
    // ✅ Library added -> ✅ Student added → ✅ Library card issued → ✅ Book added -> ✅ Book issued → ❌ Book returned
    public static void testReturnBook_Fail_WrongBook() {
        String libraryName = Debug.makeSymbolicString("libraryName");
        comsatsLibrary = new Library(libraryName);
    
        Books books = new Books();
        books.setBooks(new Book().displayBooks());
        comsatsLibrary.addBookToLibrary(books);
        booksArrayList = comsatsLibrary.getBooksArrayList();
    
        // Book 1 and Book 2 (symbolic)
        String issuedBookName = Debug.makeSymbolicString("issuedBookName");
        String wrongBookName = Debug.makeSymbolicString("wrongBookName");
    
        Book book1 = new Book(issuedBookName, "Author1");
        Book book2 = new Book(wrongBookName, "Author2");
    
        addBookFunction(comsatsLibrary, book1);
        addBookFunction(comsatsLibrary, book2);
    
        students = new ArrayList<>();
    
        String studentId = Debug.makeSymbolicString("studentId");
        Student student = new Student("StudentX", studentId, "CSE");
        addStudents(student);
        issueLibraryCard(new Library(libraryName), studentId);
    
        // Manually issue book1 to student
        book1.setBorrowDate("01/06/2021");
        book1.setReturnDate("15/06/2021");
        book1.setPendingReturn(true);
        book1.setBorrowerId(studentId);
        student.addBookToIssueList(book1);
    
        // Try returning book2 (not issued)
        returnBook(comsatsLibrary, studentId, wrongBookName);
    }

    // ✅ Library added -> ❌ Student added → ✅ Library card issued → ✅ Book added -> ❌ Book issued → ❌ Book returned
    public static void testReturnBook_Fail_NoStudent() {
        String libraryName = Debug.makeSymbolicString("libraryName");
        comsatsLibrary = new Library(libraryName);
    
        Books books = new Books();
        books.setBooks(new Book().displayBooks());
        comsatsLibrary.addBookToLibrary(books);
        booksArrayList = comsatsLibrary.getBooksArrayList();
    
        String bookName = Debug.makeSymbolicString("bookName");
        Book book = new Book(bookName, "AuthorX");
        addBookFunction(comsatsLibrary, book);
    
        students = new ArrayList<>(); // Empty student list
    
        String unknownStudentId = Debug.makeSymbolicString("unknownStudentId");
        returnBook(comsatsLibrary, unknownStudentId, bookName); // Should fail: student not found
    }
    

    public static void addBookFunction(Library lib, Book book) {
        //@ assume book != null;
        book.addBook();
        book.setPendingReturn(false);

        //@ assert !book.isPendingReturn();

        Book foundBook = checkBookAvailable(lib, book.getBookName());
        //@ assert foundBook == null || foundBook.getBookName().equals(book.getBookName());

        if (foundBook != null) {
            //@ assume lib.findBooksCollection(book.getBookName()) != null;
            Books books = lib.findBooksCollection(book.getBookName());
            if (books != null) {
                Book newBook = new Book(foundBook);
                books.addBookToList(newBook);
                books.setNumOfCopies(books.getNumOfCopies() + 1);
                //@ assert books.getNumOfCopies() > 0;
            }
        } else {
            Books books = new Books();
            books.setNumOfCopies(1);
            books.addBookToList(book);
            lib.addBookToLibrary(books);
            //@ assert books.getNumOfCopies() == 1;
        }
    }

    private static void returnBook(Library library, String studentId, String bookName) {
        Student student = findStudent(studentId);
        if (student != null) {
            ArrayList<Book> bookList = student.getIssuedBooks();
            if (!bookList.isEmpty()) {
                for (Book book : bookList) {
                    calculateFine(student, book.getBorrowDate(), book.getReturnDate());
                }
                for (int i = 0; i < bookList.size(); i++) {
                    Book book = bookList.get(i);
                    if (book.getBookName().equalsIgnoreCase(bookName)) {
                        bookList.remove(i);
                        book.setPendingReturn(false);
                        break;
                    }
                }
            }
        }
    }

    private static void issueBook(Library lib, String bookName, String studentId) {
        Book bookFound = checkBookAvailable(lib, bookName);
        if (bookFound == null) return;

        Student student = findStudent(studentId);
        if (student != null && student.hasLibraryAccess("COMSATS")) {
            RulesResultSet resultSet = maxBookStudentCanIssue("COMSATS", student.getInstituteName(), student.getProgramEnrolledIn());
            if (resultSet != null && student.getIssuedBooks().size() < resultSet.getNumOfBooks()) {
                bookFound.setBorrowerId(student.getStudentId());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Calendar c = Calendar.getInstance();
                bookFound.setBorrowDate(sdf.format(c.getTime()));
                c.add(Calendar.DATE, resultSet.getNumOfDays());
                bookFound.setReturnDate(sdf.format(c.getTime()));
                bookFound.setPendingReturn(true);
                student.addBookToIssueList(bookFound);
            }
        }
    }

    private static RulesResultSet maxBookStudentCanIssue(String bookLocation, String instituteName, String programEnrolledIn) {
        if (bookLocation.equals("COMSATS")) {
            return comsatsLibrary.comsatsRules(instituteName, programEnrolledIn);
        }
        return null;
    }

    public static Student findStudent(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null; // Return null if no student is found
    }

    private static Book checkBookAvailable(Library library, String getBookName) {
        outOfStock = false;
        for (Books books : booksArrayList) {
            for (Book book : books.getBooks()) {
                if (book.getBookName().equalsIgnoreCase(getBookName) && !book.isPendingReturn()) {
                    return book;
                }
            }
        }
        outOfStock = true;
        return null;
    }

    private static void addStudents(Student student) {
        student.addStudent();
        students.add(student);
    }

    private static void calculateFine(Student student, String borrowDate, String returnDate) {
        String todaysDate = "26/06/2021";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate borDate = LocalDate.parse(returnDate, formatter);
            LocalDate retDate = LocalDate.parse(todaysDate, formatter);
            long days = ChronoUnit.DAYS.between(retDate, borDate);

            if (days < 0) {
                int numOfDays = (int) Math.abs(days);
                int totalFine = student.getFine() + numOfDays * perDayFine;
                student.setFine(totalFine);
            }
        } catch (Exception e) {
            System.out.println("Date parsing failed: " + e.getMessage());
        }
    }

    private static void issueLibraryCard(Library library, String studentId) {
        Student student = findStudent(studentId);
        if (student != null) {
            student.issueCard(library.getInstituteName());
        }
    }

    // ========== NEW POLYMORPHIC TEST METHODS ==========

    /**
     * Test method demonstrating polymorphism with different book types
     */
    public static void testPolymorphicBooks() {
        String libraryName = Debug.makeSymbolicString("libraryName");
        UniversityLibrary library = new UniversityLibrary(libraryName);

        students = new ArrayList<>();
        booksArrayList = library.getBooksArrayList();

        // Create different types of books (polymorphism)
        String textbookName = Debug.makeSymbolicString("textbookName");
        String textbookAuthor = Debug.makeSymbolicString("textbookAuthor");
        String subject = Debug.makeSymbolicString("subject");
        Textbook textbook = new Textbook(textbookName, textbookAuthor, subject, "5th");
        addBookFunction(library, textbook);

        String novelName = Debug.makeSymbolicString("novelName");
        String novelAuthor = Debug.makeSymbolicString("novelAuthor");
        String genre = Debug.makeSymbolicString("genre");
        Novel novel = new Novel(novelName, novelAuthor, genre);
        addBookFunction(library, novel);

        String refBookName = Debug.makeSymbolicString("refBookName");
        String refBookAuthor = Debug.makeSymbolicString("refBookAuthor");
        ReferenceBook refBook = new ReferenceBook(refBookName, refBookAuthor, "Dictionary");
        addBookFunction(library, refBook);

        String eBookName = Debug.makeSymbolicString("eBookName");
        String eBookAuthor = Debug.makeSymbolicString("eBookAuthor");
        EBook eBook = new EBook(eBookName, eBookAuthor, "PDF", 1024000);
        addBookFunction(library, eBook);

        // Test polymorphic behavior - all books implement Borrowable
        testBorrowableInterface(textbook);
        testBorrowableInterface(novel);
        testBorrowableInterface(refBook);
        testBorrowableInterface(eBook);

        // Test Searchable interface
        String query = Debug.makeSymbolicString("query");
        testSearchableInterface(textbook, query);
        testSearchableInterface(novel, query);
        testSearchableInterface(refBook, query);
        testSearchableInterface(eBook, query);
    }

    /**
     * Test method demonstrating polymorphism with different student types
     */
    public static void testPolymorphicStudents() {
        String libraryName = Debug.makeSymbolicString("libraryName");
        UniversityLibrary library = new UniversityLibrary(libraryName);

        students = new ArrayList<>();
        booksArrayList = library.getBooksArrayList();

        // Create different types of students (polymorphism)
        String ugName = Debug.makeSymbolicString("ugName");
        String ugId = Debug.makeSymbolicString("ugId");
        int year = Debug.makeSymbolicInteger("year");
        String major = Debug.makeSymbolicString("major");
        Undergraduate ugStudent = new Undergraduate(ugName, ugId, "UG", year, major);
        addStudents(ugStudent);
        testLibraryMemberInterface(ugStudent, library);

        String gradName = Debug.makeSymbolicString("gradName");
        String gradId = Debug.makeSymbolicString("gradId");
        String researchArea = Debug.makeSymbolicString("researchArea");
        String advisor = Debug.makeSymbolicString("advisor");
        Graduate gradStudent = new Graduate(gradName, gradId, "PG", researchArea, advisor);
        addStudents(gradStudent);
        testLibraryMemberInterface(gradStudent, library);

        String phdName = Debug.makeSymbolicString("phdName");
        String phdId = Debug.makeSymbolicString("phdId");
        String dissertation = Debug.makeSymbolicString("dissertation");
        PhDStudent phdStudent = new PhDStudent(phdName, phdId, "PHD", 
                                               researchArea, advisor, dissertation);
        addStudents(phdStudent);
        testLibraryMemberInterface(phdStudent, library);

        String exchangeName = Debug.makeSymbolicString("exchangeName");
        String exchangeId = Debug.makeSymbolicString("exchangeId");
        String homeUni = Debug.makeSymbolicString("homeUni");
        String exchangeProgram = Debug.makeSymbolicString("exchangeProgram");
        int duration = Debug.makeSymbolicInteger("duration");
        ExchangeStudent exchangeStudent = new ExchangeStudent(exchangeName, exchangeId, "UG",
                                                               homeUni, exchangeProgram, duration);
        addStudents(exchangeStudent);
        testLibraryMemberInterface(exchangeStudent, library);

        // Test polymorphic behavior - all students extend Student
        testStudentPolymorphism(ugStudent);
        testStudentPolymorphism(gradStudent);
        testStudentPolymorphism(phdStudent);
        testStudentPolymorphism(exchangeStudent);
    }

    /**
     * Test method demonstrating polymorphism with different library types
     */
    public static void testPolymorphicLibraries() {
        String instituteName = Debug.makeSymbolicString("instituteName");
        
        // Create different library types
        UniversityLibrary uniLib = new UniversityLibrary(instituteName);
        PublicLibrary pubLib = new PublicLibrary(instituteName);
        DigitalLibrary digLib = new DigitalLibrary(instituteName);

        // Test polymorphic behavior - all libraries extend Library
        testLibraryPolymorphism(uniLib);
        testLibraryPolymorphism(pubLib);
        testLibraryPolymorphism(digLib);

        // Test abstract methods from Institute
        String memberId = Debug.makeSymbolicString("memberId");
        String memberType = Debug.makeSymbolicString("memberType");
        testInstituteAbstractMethods(uniLib, memberId, memberType);
        testInstituteAbstractMethods(pubLib, memberId, memberType);
        testInstituteAbstractMethods(digLib, memberId, memberType);
    }

    /**
     * Test method demonstrating multiple inheritance via interfaces
     */
    public static void testMultipleInheritance() {
        // Novel implements Borrowable, Renewable, and Searchable
        String novelName = Debug.makeSymbolicString("novelName");
        String novelAuthor = Debug.makeSymbolicString("novelAuthor");
        String genre = Debug.makeSymbolicString("genre");
        Novel novel = new Novel(novelName, novelAuthor, genre);

        // Test Borrowable interface
        String borrowerId = Debug.makeSymbolicString("borrowerId");
        if (novel.canBorrow(borrowerId)) {
            novel.markAsBorrowed(borrowerId);
        }

        // Test Renewable interface
        if (novel.canRenew(borrowerId)) {
            int newDays = novel.renew(10);
        }

        // Test Searchable interface
        String query = Debug.makeSymbolicString("query");
        boolean matches = novel.matches(query);
        double score = novel.getRelevanceScore(query);

        // ReferenceBook implements Borrowable, Reservable, and Searchable
        String refName = Debug.makeSymbolicString("refName");
        String refAuthor = Debug.makeSymbolicString("refAuthor");
        ReferenceBook refBook = new ReferenceBook(refName, refAuthor, "Encyclopedia");

        // Test Reservable interface
        String reserverId = Debug.makeSymbolicString("reserverId");
        refBook.reserve(reserverId);
        boolean isReserved = refBook.isReserved();
        String nextReserver = refBook.getNextReserver();
    }

    // Helper methods for testing interfaces
    private static void testBorrowableInterface(Borrowable borrowable) {
        String borrowerId = Debug.makeSymbolicString("borrowerId");
        if (borrowable.canBorrow(borrowerId)) {
            borrowable.markAsBorrowed(borrowerId);
            int maxDays = borrowable.getMaxBorrowDays();
            int overdueDays = Debug.makeSymbolicInteger("overdueDays");
            double fee = borrowable.calculateLateFee(overdueDays);
        }
    }

    private static void testSearchableInterface(Searchable searchable, String query) {
        boolean matches = searchable.matches(query);
        double score = searchable.getRelevanceScore(query);
        String content = searchable.getSearchableContent();
    }

    private static void testLibraryMemberInterface(LibraryMember member, Library library) {
        String memberId = member.getMemberId();
        String memberName = member.getMemberName();
        boolean hasMembership = member.hasActiveMembership();
        if (!hasMembership) {
            member.activateMembership();
        }
        int limit = member.getBorrowingLimit();
    }

    private static void testStudentPolymorphism(Student student) {
        String instituteName = Debug.makeSymbolicString("instituteName");
        int result = student.issueCard(instituteName);
        boolean hasAccess = student.hasLibraryAccess(instituteName);
        String studentId = student.getStudentId();
        String studentName = student.getStudentName();
    }

    private static void testLibraryPolymorphism(Library library) {
        String instituteName = library.getInstituteName();
        boolean isValid = library.validateMembership("TEST123");
        int limit = library.getMaxBorrowingLimit("UG");
        java.util.List<String> services = library.getAvailableServices();
        double fee = library.calculateMembershipFee("UG");
    }

    private static void testInstituteAbstractMethods(Institute institute, String memberId, String memberType) {
        boolean valid = institute.validateMembership(memberId);
        int limit = institute.getMaxBorrowingLimit(memberType);
        java.util.List<String> services = institute.getAvailableServices();
        double fee = institute.calculateMembershipFee(memberType);
        boolean active = institute.isActive();
        int age = institute.getAge();
    }
}
