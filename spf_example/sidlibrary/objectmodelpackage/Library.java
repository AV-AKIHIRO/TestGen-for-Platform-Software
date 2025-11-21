package sidlibrary.objectmodelpackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Library class - extends Institute and implements abstract methods
 */
public class Library extends Institute {
    private ArrayList<Books> booksArrayList = new ArrayList<>();
    private int forUG, forPG, forPHD, maxDays;

    public Library(String instituteName) {
        super(instituteName);
        if (instituteName.equals("COMSATS")) {
            forUG = 5;
            forPG = 3;
            forPHD = 2;
            maxDays = 12;
        }
    }

    public Library(String instituteName, String location, int establishedYear) {
        super(instituteName, location, establishedYear);
        if (instituteName.equals("COMSATS")) {
            forUG = 5;
            forPG = 3;
            forPHD = 2;
            maxDays = 12;
        }
    }

    public ArrayList<Books> getBooksArrayList() {
        return booksArrayList;
    }

    public void addBookToLibrary(Books books) {
        booksArrayList.add(books);
    }

    public Books findBooksCollection(String findThisBook) {
        for (Books books : booksArrayList) {
            for (Book book : books.getBooks()) {
                if (book.getBookName().equals(findThisBook)) {
                    return books;
                }
            }
        }
        return null;
    }

    public RulesResultSet comsatsRules(String instituteName, String programEnrolledIn) {
        if (instituteName.equals("COMSATS")) {
            switch (programEnrolledIn) {
                case "UG": return new RulesResultSet(forUG, maxDays);
                case "PG": return new RulesResultSet(forPG, maxDays);
                case "PHD": return new RulesResultSet(forPHD, maxDays);
            }
        }
        return null;
    }

    // Implementation of abstract methods from Institute
    @Override
    public boolean validateMembership(String memberId) {
        return memberId != null && !memberId.isEmpty() && 
               memberId.length() >= 3;
    }

    @Override
    public int getMaxBorrowingLimit(String memberType) {
        switch (memberType) {
            case "UG": return forUG;
            case "PG": return forPG;
            case "PHD": return forPHD;
            default: return 3;
        }
    }

    @Override
    public List<String> getAvailableServices() {
        return Arrays.asList("Book Borrowing", "Reading Room", 
                           "Reference Services", "Inter-library Loan");
    }

    @Override
    public double calculateMembershipFee(String memberType) {
        switch (memberType) {
            case "UG": return 0.0; // Free for students
            case "PG": return 0.0;
            case "PHD": return 0.0;
            default: return 500.0; // Fee for others
        }
    }
}
