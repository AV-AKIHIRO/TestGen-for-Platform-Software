package sidlibrary.objectmodelpackage;

/**
 * Interface for library members
 */
public interface LibraryMember {
    String getMemberId();
    String getMemberName();
    boolean hasActiveMembership();
    void activateMembership();
    void deactivateMembership();
    int getBorrowingLimit();
}

