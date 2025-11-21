package sidlibrary.objectmodelpackage;

import java.util.ArrayList;

public class Books {
    private ArrayList<Book> books = new ArrayList<>();
    private int numOfCopies;

    public Books() {}

    public ArrayList<Book> getBooks() { return books; }
    public int getNumOfCopies() { return numOfCopies; }
    public void setNumOfCopies(int numOfCopies) { this.numOfCopies = numOfCopies; }

    public void addBookToList(Book book) { books.add(book); }

    public void setBooks(ArrayList<Book> books) {
        this.books = books;
    }
}
