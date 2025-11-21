package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * Novel class - extends Book and implements Borrowable and Renewable
 * Novels can be renewed multiple times
 */
public class Novel extends Book implements Borrowable, Renewable, Searchable {
    private String genre;
    private int maxBorrowDays;
    private int renewalCount;
    private int maxRenewals;
    private static final int NOVEL_MAX_DAYS = 21;
    private static final int NOVEL_MAX_RENEWALS = 2;

    public Novel(String bookName, String bookAuthor, String genre) {
        super(bookName, bookAuthor);
        this.genre = genre;
        this.maxBorrowDays = NOVEL_MAX_DAYS;
        this.renewalCount = 0;
        this.maxRenewals = NOVEL_MAX_RENEWALS;
    }

    public Novel(Novel novel) {
        super(novel);
        this.genre = novel.getGenre();
        this.maxBorrowDays = novel.getMaxBorrowDays();
        this.renewalCount = novel.getRenewalCount();
        this.maxRenewals = novel.getMaxRenewals();
    }

    public Novel() {
        super();
        this.maxBorrowDays = NOVEL_MAX_DAYS;
        this.renewalCount = 0;
        this.maxRenewals = NOVEL_MAX_RENEWALS;
    }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

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
        renewalCount = 0; // Reset on return
    }

    @Override
    public int getMaxBorrowDays() {
        return maxBorrowDays;
    }

    @Override
    public double calculateLateFee(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0;
        // Novels have standard late fees
        return daysOverdue * 10.0;
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
               (genre != null && genre.toLowerCase().contains(lowerQuery));
    }

    @Override
    public double getRelevanceScore(String query) {
        if (!matches(query)) return 0.0;
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        if (getBookName().toLowerCase().contains(lowerQuery)) score += 10.0;
        if (getBookAuthor().toLowerCase().contains(lowerQuery)) score += 8.0;
        if (genre != null && genre.toLowerCase().contains(lowerQuery)) score += 6.0;
        return score;
    }

    @Override
    public String getSearchableContent() {
        return getBookName() + " " + getBookAuthor() + " " + (genre != null ? genre : "");
    }

    @Override
    public void addBook() {
        System.out.println("Novel added: " + getBookName() + ", " + getBookAuthor() + 
                          ", Genre: " + genre);
    }

    @Override
    public ArrayList<Book> displayBooks() {
        ArrayList<Book> books = new ArrayList<>();
        books.add(new Novel("1984", "George Orwell", "Dystopian"));
        books.add(new Novel("Pride and Prejudice", "Jane Austen", "Romance"));
        return books;
    }
}

