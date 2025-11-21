package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * UniversityLibrary - extends Library
 * Specialized for university settings
 */
public class UniversityLibrary extends Library {
    private int totalCapacity;
    private int currentOccupancy;
    private boolean researchSection;
    private ArrayList<String> specialCollections;

    public UniversityLibrary(String instituteName) {
        super(instituteName);
        this.totalCapacity = 500;
        this.currentOccupancy = 0;
        this.researchSection = true;
        this.specialCollections = new ArrayList<>();
    }

    public UniversityLibrary(String instituteName, int totalCapacity) {
        super(instituteName);
        this.totalCapacity = totalCapacity;
        this.currentOccupancy = 0;
        this.researchSection = true;
        this.specialCollections = new ArrayList<>();
    }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(int currentOccupancy) { 
        this.currentOccupancy = currentOccupancy; 
    }
    public boolean hasResearchSection() { return researchSection; }
    public void setResearchSection(boolean researchSection) { 
        this.researchSection = researchSection; 
    }
    public ArrayList<String> getSpecialCollections() { 
        return new ArrayList<>(specialCollections); 
    }
    public void addSpecialCollection(String collectionName) {
        if (!specialCollections.contains(collectionName)) {
            specialCollections.add(collectionName);
        }
    }

    // Override methods with University-specific behavior
    @Override
    public RulesResultSet comsatsRules(String instituteName, String programEnrolledIn) {
        RulesResultSet baseRules = super.comsatsRules(instituteName, programEnrolledIn);
        if (baseRules != null && researchSection) {
            // University libraries with research sections allow longer periods
            return new RulesResultSet(baseRules.getNumOfBooks(), 
                                    baseRules.getNumOfDays() + 3);
        }
        return baseRules;
    }

    @Override
    public void addBookToLibrary(Books books) {
        if (currentOccupancy < totalCapacity) {
            super.addBookToLibrary(books);
            currentOccupancy += books.getNumOfCopies();
        }
    }

    public boolean hasSpace() {
        return currentOccupancy < totalCapacity;
    }

    // Override abstract methods with University-specific behavior
    @Override
    public boolean validateMembership(String memberId) {
        // University libraries have stricter validation
        return super.validateMembership(memberId) && 
               memberId != null && 
               memberId.length() >= 4;
    }

    @Override
    public double calculateMembershipFee(String memberType) {
        // University libraries may charge fees for non-students
        double baseFee = super.calculateMembershipFee(memberType);
        if (baseFee > 0) {
            return baseFee * 1.2; // 20% premium for university library
        }
        return baseFee;
    }
}

