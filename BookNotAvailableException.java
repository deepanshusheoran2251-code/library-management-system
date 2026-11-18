package library.exception;

public class BookNotAvailableException extends Exception {

    private final String bookTitle;

    public BookNotAvailableException(String bookTitle) {
        super("Book not available: \"" + bookTitle + "\" – all copies are currently issued.");
        this.bookTitle = bookTitle;
    }

    public String getBookTitle() { return bookTitle; }
}
