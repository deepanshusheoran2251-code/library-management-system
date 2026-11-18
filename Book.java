package library.model;

public class Book extends LibraryItem implements Fineable {

    private String author;
    private String category;
    private int    totalCopies;
    private int    availableCopies;

    // ── Full constructor ─────────────────────────────────────────────────────
    public Book(int id, String title, String isbn,
                String author, String category,
                int totalCopies, int availableCopies) {
        super(id, title, isbn);
        this.author          = author;
        this.category        = category;
        this.totalCopies     = totalCopies;
        this.availableCopies = availableCopies;
    }

    // ── New-book constructor (no id yet) ─────────────────────────────────────
    public Book(String title, String isbn,
                String author, String category,
                int totalCopies) {
        this(0, title, isbn, author, category, totalCopies, totalCopies);
    }

    // ── LibraryItem ──────────────────────────────────────────────────────────
    @Override
    public String getType() { return "Book"; }

    // ── Fineable: Rs 2 per day late ──────────────────────────────────────────
    @Override
    public double calculateFine(int daysLate) {
        return daysLate <= 0 ? 0.0 : daysLate * 2.0;
    }

    public boolean isAvailable() { return availableCopies > 0; }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getAuthor()          { return author; }
    public String getCategory()        { return category; }
    public int    getTotalCopies()     { return totalCopies; }
    public int    getAvailableCopies() { return availableCopies; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setAuthor(String a)           { this.author          = a; }
    public void setCategory(String c)         { this.category        = c; }
    public void setTotalCopies(int t)         { this.totalCopies     = t; }
    public void setAvailableCopies(int a)     { this.availableCopies = a; }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " by " + author;
    }
}
