package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

public class Book {
    private String bookName;
    private String bookAuthor;
    private String borrowerId;
    private String borrowDate;
    private String returnDate;
    private boolean pendingReturn;

    public Book(Book book) {
        this.bookName = book.getBookName();
        this.bookAuthor = book.getBookAuthor();
    }

    public Book(String bookName, String bookAuthor) {
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
    }

    public Book() {}

    public String getBookName() { return bookName; }
    public String getBookAuthor() { return bookAuthor; }
    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public boolean isPendingReturn() { return pendingReturn; }
    public void setPendingReturn(boolean pendingReturn) { this.pendingReturn = pendingReturn; }

    // File I/O removed for symbolic execution
    public void addBook() {
        // Simulated logic, no file I/O
        System.out.println("Book added: " + bookName + ", " + bookAuthor);
    }

    // Return mock list for symbolic testing
    public ArrayList<Book> displayBooks() {
        ArrayList<Book> books = new ArrayList<>();
        books.add(new Book("SymbolicBook1", "Author1"));
        books.add(new Book("SymbolicBook2", "Author2"));
        return books;
    }
}
