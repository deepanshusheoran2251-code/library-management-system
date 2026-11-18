package library.model;

public abstract class LibraryItem {
    protected int id;
    protected String title;
    protected String isbn;

    public LibraryItem(int id, String title, String isbn) {
        this.id    = id;
        this.title = title;
        this.isbn  = isbn;
    }

    public abstract String getType();

    public int    getId()    { return id; }
    public String getTitle() { return title; }
    public String getIsbn()  { return isbn; }

    public void setId(int id)       { this.id    = id; }
    public void setTitle(String t)  { this.title = t;  }
    public void setIsbn(String i)   { this.isbn  = i;  }
}
