package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * Textbook class - extends Book and implements multiple interfaces
 * Textbooks have limited borrow periods and cannot be renewed
 */
public class Textbook extends Book implements Borrowable, Searchable {
    private String subject;
    private String edition;
    private int maxBorrowDays;
    private static final int TEXTBOOK_MAX_DAYS = 14;

    public Textbook(String bookName, String bookAuthor, String subject, String edition) {
        super(bookName, bookAuthor);
        this.subject = subject;
        this.edition = edition;
        this.maxBorrowDays = TEXTBOOK_MAX_DAYS;
    }

    public Textbook(Textbook textbook) {
        super(textbook);
        this.subject = textbook.getSubject();
        this.edition = textbook.getEdition();
        this.maxBorrowDays = textbook.getMaxBorrowDays();
    }

    public Textbook() {
        super();
        this.maxBorrowDays = TEXTBOOK_MAX_DAYS;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    // Borrowable interface implementation
    @Override
    public boolean canBorrow(String borrowerId) {
        return !isPendingReturn() && borrowerId != null && !borrowerId.isEmpty();
    }

    @Override
    public void markAsBorrowed(String borrowerId) {
        setBorrowerId(borrowerId);
        setPendingReturn(true);
    }

    @Override
    public void markAsReturned() {
        setPendingReturn(false);
        setBorrowerId(null);
    }

    @Override
    public int getMaxBorrowDays() {
        return maxBorrowDays;
    }

    @Override
    public double calculateLateFee(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0;
        // Textbooks have higher late fees
        return daysOverdue * 15.0;
    }

    // Searchable interface implementation
    @Override
    public boolean matches(String query) {
        if (query == null) return false;
        String lowerQuery = query.toLowerCase();
        return getBookName().toLowerCase().contains(lowerQuery) ||
               getBookAuthor().toLowerCase().contains(lowerQuery) ||
               (subject != null && subject.toLowerCase().contains(lowerQuery)) ||
               (edition != null && edition.toLowerCase().contains(lowerQuery));
    }

    @Override
    public double getRelevanceScore(String query) {
        if (!matches(query)) return 0.0;
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        if (getBookName().toLowerCase().contains(lowerQuery)) score += 10.0;
        if (getBookAuthor().toLowerCase().contains(lowerQuery)) score += 5.0;
        if (subject != null && subject.toLowerCase().contains(lowerQuery)) score += 7.0;
        return score;
    }

    @Override
    public String getSearchableContent() {
        return getBookName() + " " + getBookAuthor() + " " + 
               (subject != null ? subject : "") + " " + 
               (edition != null ? edition : "");
    }

    @Override
    public void addBook() {
        System.out.println("Textbook added: " + getBookName() + ", " + getBookAuthor() + 
                          ", Subject: " + subject + ", Edition: " + edition);
    }

    @Override
    public ArrayList<Book> displayBooks() {
        ArrayList<Book> books = new ArrayList<>();
        books.add(new Textbook("Calculus", "Stewart", "Mathematics", "8th"));
        books.add(new Textbook("Physics", "Halliday", "Physics", "10th"));
        return books;
    }
}

