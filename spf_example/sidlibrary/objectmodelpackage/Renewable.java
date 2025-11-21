package sidlibrary.objectmodelpackage;

/**
 * Interface for items that can be renewed
 */
public interface Renewable {
    boolean canRenew(String borrowerId);
    int renew(int currentDays);
    int getMaxRenewals();
    int getRenewalCount();
}

