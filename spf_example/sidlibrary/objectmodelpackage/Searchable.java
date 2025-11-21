package sidlibrary.objectmodelpackage;

/**
 * Interface for items that can be searched
 */
public interface Searchable {
    boolean matches(String query);
    double getRelevanceScore(String query);
    String getSearchableContent();
}

