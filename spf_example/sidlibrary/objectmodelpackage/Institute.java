package sidlibrary.objectmodelpackage;

import java.util.List;

/**
 * Abstract base class for all institutes
 * Demonstrates abstract methods and inheritance
 */
public abstract class Institute {
    private String instituteName;
    private String location;
    private int establishedYear;

    public Institute() {}

    public Institute(String instituteName) {
        this.instituteName = instituteName;
    }

    public Institute(String instituteName, String location, int establishedYear) {
        this.instituteName = instituteName;
        this.location = location;
        this.establishedYear = establishedYear;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getEstablishedYear() {
        return establishedYear;
    }

    public void setEstablishedYear(int establishedYear) {
        this.establishedYear = establishedYear;
    }

    // Abstract methods - must be implemented by subclasses
    public abstract boolean validateMembership(String memberId);
    public abstract int getMaxBorrowingLimit(String memberType);
    public abstract List<String> getAvailableServices();
    public abstract double calculateMembershipFee(String memberType);

    // Concrete methods with default implementation
    public boolean isActive() {
        return instituteName != null && !instituteName.isEmpty();
    }

    public int getAge() {
        if (establishedYear > 0) {
            return 2024 - establishedYear; // Assuming current year is 2024
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Institute{" +
                "name='" + instituteName + '\'' +
                ", location='" + location + '\'' +
                ", established=" + establishedYear +
                '}';
    }
}
