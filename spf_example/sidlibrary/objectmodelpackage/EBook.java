package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * EBook class - extends Book and implements Borrowable, Renewable, and Searchable
 * EBooks have different borrowing rules and can be renewed more times
 */
public class EBook extends Book implements Borrowable, Renewable, Searchable {
    private String format; // PDF, EPUB, MOBI, etc.
    private long fileSize; // in bytes
    private int maxBorrowDays;
    private int renewalCount;
    private int maxRenewals;
    private static final int EBOOK_MAX_DAYS = 30;
    private static final int EBOOK_MAX_RENEWALS = 5;

    public EBook(String bookName, String bookAuthor, String format, long fileSize) {
        super(bookName, bookAuthor);
        this.format = format;
        this.fileSize = fileSize;
        this.maxBorrowDays = EBOOK_MAX_DAYS;
        this.renewalCount = 0;
        this.maxRenewals = EBOOK_MAX_RENEWALS;
    }

    public EBook(EBook eBook) {
        super(eBook);
        this.format = eBook.getFormat();
        this.fileSize = eBook.getFileSize();
        this.maxBorrowDays = eBook.getMaxBorrowDays();
        this.renewalCount = eBook.getRenewalCount();
        this.maxRenewals = eBook.getMaxRenewals();
    }

    public EBook() {
        super();
        this.maxBorrowDays = EBOOK_MAX_DAYS;
        this.renewalCount = 0;
        this.maxRenewals = EBOOK_MAX_RENEWALS;
    }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

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
        renewalCount = 0;
    }

    @Override
    public int getMaxBorrowDays() {
        return maxBorrowDays;
    }

    @Override
    public double calculateLateFee(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0;
        // EBooks have lower late fees
        return daysOverdue * 5.0;
    }

    // Renewable interface implementation
    @Override
    public boolean canRenew(String borrowerId) {
        return isPendingReturn() && 
               getBorrowerId() != null && 
               getBorrowerId().equals(borrowerId) &&
               renewalCount < maxRenewals;
    }

    @Override
    public int renew(int currentDays) {
        if (renewalCount < maxRenewals) {
            renewalCount++;
            return currentDays + maxBorrowDays;
        }
        return currentDays;
    }

    @Override
    public int getMaxRenewals() {
        return maxRenewals;
    }

    @Override
    public int getRenewalCount() {
        return renewalCount;
    }

    // Searchable interface implementation
    @Override
    public boolean matches(String query) {
        if (query == null) return false;
        String lowerQuery = query.toLowerCase();
        return getBookName().toLowerCase().contains(lowerQuery) ||
               getBookAuthor().toLowerCase().contains(lowerQuery) ||
               (format != null && format.toLowerCase().contains(lowerQuery));
    }

    @Override
    public double getRelevanceScore(String query) {
        if (!matches(query)) return 0.0;
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        if (getBookName().toLowerCase().contains(lowerQuery)) score += 10.0;
        if (getBookAuthor().toLowerCase().contains(lowerQuery)) score += 8.0;
        if (format != null && format.toLowerCase().contains(lowerQuery)) score += 3.0;
        return score;
    }

    @Override
    public String getSearchableContent() {
        return getBookName() + " " + getBookAuthor() + " " + (format != null ? format : "");
    }

    @Override
    public void addBook() {
        System.out.println("EBook added: " + getBookName() + ", " + getBookAuthor() + 
                          ", Format: " + format + ", Size: " + fileSize + " bytes");
    }

    @Override
    public ArrayList<Book> displayBooks() {
        ArrayList<Book> books = new ArrayList<>();
        books.add(new EBook("Digital Guide", "Tech Author", "PDF", 1024000));
        books.add(new EBook("Online Manual", "Manual Writer", "EPUB", 2048000));
        return books;
    }
}

