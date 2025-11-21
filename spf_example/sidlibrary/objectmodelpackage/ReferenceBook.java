package sidlibrary.objectmodelpackage;

import java.util.ArrayList;
import java.util.List;

/**
 * ReferenceBook class - extends Book and implements Borrowable, Reservable, and Searchable
 * Reference books cannot be borrowed but can be reserved for in-library use
 */
public class ReferenceBook extends Book implements Borrowable, Reservable, Searchable {
    private String referenceType; // Dictionary, Encyclopedia, Atlas, etc.
    private List<String> reservationQueue;
    private static final int REFERENCE_MAX_DAYS = 3; // Very short period

    public ReferenceBook(String bookName, String bookAuthor, String referenceType) {
        super(bookName, bookAuthor);
        this.referenceType = referenceType;
        this.reservationQueue = new ArrayList<>();
    }

    public ReferenceBook(ReferenceBook refBook) {
        super(refBook);
        this.referenceType = refBook.getReferenceType();
        this.reservationQueue = new ArrayList<>(refBook.getReservationQueue());
    }

    public ReferenceBook() {
        super();
        this.reservationQueue = new ArrayList<>();
    }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    // Borrowable interface implementation (limited for reference books)
    @Override
    public boolean canBorrow(String borrowerId) {
        // Reference books can only be borrowed for very short periods
        return !isPendingReturn() && 
               borrowerId != null && 
               !borrowerId.isEmpty() &&
               reservationQueue.isEmpty();
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
        return REFERENCE_MAX_DAYS;
    }

    @Override
    public double calculateLateFee(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0;
        // Reference books have very high late fees
        return daysOverdue * 25.0;
    }

    // Reservable interface implementation
    @Override
    public boolean reserve(String borrowerId) {
        if (borrowerId == null || borrowerId.isEmpty()) return false;
        if (!reservationQueue.contains(borrowerId)) {
            reservationQueue.add(borrowerId);
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelReservation(String borrowerId) {
        return reservationQueue.remove(borrowerId);
    }

    @Override
    public List<String> getReservationQueue() {
        return new ArrayList<>(reservationQueue);
    }

    @Override
    public boolean isReserved() {
        return !reservationQueue.isEmpty();
    }

    @Override
    public String getNextReserver() {
        return reservationQueue.isEmpty() ? null : reservationQueue.get(0);
    }

    // Searchable interface implementation
    @Override
    public boolean matches(String query) {
        if (query == null) return false;
        String lowerQuery = query.toLowerCase();
        return getBookName().toLowerCase().contains(lowerQuery) ||
               getBookAuthor().toLowerCase().contains(lowerQuery) ||
               (referenceType != null && referenceType.toLowerCase().contains(lowerQuery));
    }

    @Override
    public double getRelevanceScore(String query) {
        if (!matches(query)) return 0.0;
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        if (getBookName().toLowerCase().contains(lowerQuery)) score += 12.0;
        if (getBookAuthor().toLowerCase().contains(lowerQuery)) score += 5.0;
        if (referenceType != null && referenceType.toLowerCase().contains(lowerQuery)) score += 8.0;
        return score;
    }

    @Override
    public String getSearchableContent() {
        return getBookName() + " " + getBookAuthor() + " " + 
               (referenceType != null ? referenceType : "");
    }

    @Override
    public void addBook() {
        System.out.println("Reference book added: " + getBookName() + ", " + getBookAuthor() + 
                          ", Type: " + referenceType);
    }

    @Override
    public ArrayList<Book> displayBooks() {
        ArrayList<Book> books = new ArrayList<>();
        books.add(new ReferenceBook("Oxford Dictionary", "Oxford Press", "Dictionary"));
        books.add(new ReferenceBook("World Atlas", "National Geographic", "Atlas"));
        return books;
    }
}

