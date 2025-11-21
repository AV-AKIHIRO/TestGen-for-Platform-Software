package sidlibrary.objectmodelpackage;

/**
 * Interface for items that can be borrowed
 */
public interface Borrowable {
    boolean canBorrow(String borrowerId);
    void markAsBorrowed(String borrowerId);
    void markAsReturned();
    int getMaxBorrowDays();
    double calculateLateFee(int daysOverdue);
}

