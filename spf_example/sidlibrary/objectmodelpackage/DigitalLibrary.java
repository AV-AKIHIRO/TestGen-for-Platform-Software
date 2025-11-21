package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * DigitalLibrary - extends Library
 * Specialized for digital/electronic resources
 */
public class DigitalLibrary extends Library {
    private String platform; // Web, Mobile App, etc.
    private boolean requiresInternet;
    private int maxConcurrentUsers;
    private int currentUsers;
    private ArrayList<String> supportedFormats;

    public DigitalLibrary(String instituteName) {
        super(instituteName);
        this.platform = "Web";
        this.requiresInternet = true;
        this.maxConcurrentUsers = 1000;
        this.currentUsers = 0;
        this.supportedFormats = new ArrayList<>();
        supportedFormats.add("PDF");
        supportedFormats.add("EPUB");
    }

    public DigitalLibrary(String instituteName, String platform, int maxConcurrentUsers) {
        super(instituteName);
        this.platform = platform;
        this.requiresInternet = true;
        this.maxConcurrentUsers = maxConcurrentUsers;
        this.currentUsers = 0;
        this.supportedFormats = new ArrayList<>();
    }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public boolean requiresInternet() { return requiresInternet; }
    public void setRequiresInternet(boolean requiresInternet) { 
        this.requiresInternet = requiresInternet; 
    }
    public int getMaxConcurrentUsers() { return maxConcurrentUsers; }
    public void setMaxConcurrentUsers(int maxConcurrentUsers) { 
        this.maxConcurrentUsers = maxConcurrentUsers; 
    }
    public int getCurrentUsers() { return currentUsers; }
    public void setCurrentUsers(int currentUsers) { this.currentUsers = currentUsers; }
    public ArrayList<String> getSupportedFormats() { 
        return new ArrayList<>(supportedFormats); 
    }
    public void addSupportedFormat(String format) {
        if (!supportedFormats.contains(format)) {
            supportedFormats.add(format);
        }
    }

    // Override methods with Digital Library-specific behavior
    @Override
    public RulesResultSet comsatsRules(String instituteName, String programEnrolledIn) {
        // Digital libraries have more lenient rules
        if (instituteName.equals("COMSATS")) {
            switch (programEnrolledIn) {
                case "UG": return new RulesResultSet(10, 30);
                case "PG": return new RulesResultSet(8, 25);
                case "PHD": return new RulesResultSet(5, 20);
            }
        }
        return null;
    }

    public boolean canAddUser() {
        return currentUsers < maxConcurrentUsers;
    }

    public void incrementUsers() {
        if (canAddUser()) {
            currentUsers++;
        }
    }

    public void decrementUsers() {
        if (currentUsers > 0) {
            currentUsers--;
        }
    }

    // Override abstract methods with Digital Library-specific behavior
    @Override
    public boolean validateMembership(String memberId) {
        // Digital libraries may have different validation (e.g., email-based)
        return super.validateMembership(memberId) && 
               memberId != null && 
               (memberId.contains("@") || memberId.length() >= 3);
    }

    @Override
    public double calculateMembershipFee(String memberType) {
        // Digital libraries often have lower or no fees
        double baseFee = super.calculateMembershipFee(memberType);
        return baseFee * 0.5; // 50% discount for digital access
    }
}

