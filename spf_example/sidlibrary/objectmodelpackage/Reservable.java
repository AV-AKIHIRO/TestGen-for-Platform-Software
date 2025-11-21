package sidlibrary.objectmodelpackage;

import java.util.List;

/**
 * Interface for items that can be reserved
 */
public interface Reservable {
    boolean reserve(String borrowerId);
    boolean cancelReservation(String borrowerId);
    List<String> getReservationQueue();
    boolean isReserved();
    String getNextReserver();
}

