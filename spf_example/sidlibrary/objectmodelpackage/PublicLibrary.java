package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

/**
 * PublicLibrary - extends Library
 * Different rules for public access
 */
public class PublicLibrary extends Library {
    private boolean communityAccess;
    private int membershipFee;
    private ArrayList<String> communityPrograms;

    public PublicLibrary(String instituteName) {
        super(instituteName);
        this.communityAccess = true;
        this.membershipFee = 0;
        this.communityPrograms = new ArrayList<>();
    }

    public PublicLibrary(String instituteName, int membershipFee) {
        super(instituteName);
        this.communityAccess = true;
        this.membershipFee = membershipFee;
        this.communityPrograms = new ArrayList<>();
    }

    public boolean hasCommunityAccess() { return communityAccess; }
    public void setCommunityAccess(boolean communityAccess) { 
        this.communityAccess = communityAccess; 
    }
    public int getMembershipFee() { return membershipFee; }
    public void setMembershipFee(int membershipFee) { 
        this.membershipFee = membershipFee; 
    }
    public ArrayList<String> getCommunityPrograms() { 
        return new ArrayList<>(communityPrograms); 
    }
    public void addCommunityProgram(String programName) {
        if (!communityPrograms.contains(programName)) {
            communityPrograms.add(programName);
        }
    }

    // Override methods with Public Library-specific behavior
    @Override
    public RulesResultSet comsatsRules(String instituteName, String programEnrolledIn) {
        // Public libraries have different rules - more lenient
        if (instituteName.equals("COMSATS") || communityAccess) {
            // Public libraries allow more books and longer periods
            switch (programEnrolledIn) {
                case "UG": return new RulesResultSet(7, 15);
                case "PG": return new RulesResultSet(5, 10);
                case "PHD": return new RulesResultSet(3, 8);
                default: return new RulesResultSet(5, 12);
            }
        }
        return null;
    }

    // Override abstract methods with Public Library-specific behavior
    @Override
    public boolean validateMembership(String memberId) {
        // Public libraries have more lenient validation
        return memberId != null && !memberId.isEmpty();
    }

    @Override
    public double calculateMembershipFee(String memberType) {
        // Public libraries may have different fee structure
        if (membershipFee > 0) {
            return membershipFee;
        }
        return super.calculateMembershipFee(memberType);
    }
}

