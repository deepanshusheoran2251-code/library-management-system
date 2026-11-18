package library.model;

import java.time.LocalDate;

public class Transaction {

    public enum Status { ISSUED, RETURNED }

    private int        id;
    private int        bookId;
    private int        memberId;
    private String     bookTitle;    // for display
    private String     memberName;   // for display
    private LocalDate  issueDate;
    private LocalDate  dueDate;
    private LocalDate  returnDate;   // null if not yet returned
    private double     fine;
    private Status     status;

    // ── Full constructor (from DB) ───────────────────────────────────────────
    public Transaction(int id, int bookId, int memberId,
                       String bookTitle, String memberName,
                       LocalDate issueDate, LocalDate dueDate,
                       LocalDate returnDate, double fine, Status status) {
        this.id         = id;
        this.bookId     = bookId;
        this.memberId   = memberId;
        this.bookTitle  = bookTitle;
        this.memberName = memberName;
        this.issueDate  = issueDate;
        this.dueDate    = dueDate;
        this.returnDate = returnDate;
        this.fine       = fine;
        this.status     = status;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int       getId()         { return id; }
    public int       getBookId()     { return bookId; }
    public int       getMemberId()   { return memberId; }
    public String    getBookTitle()  { return bookTitle; }
    public String    getMemberName() { return memberName; }
    public LocalDate getIssueDate()  { return issueDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double    getFine()       { return fine; }
    public Status    getStatus()     { return status; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setReturnDate(LocalDate d) { this.returnDate = d; }
    public void setFine(double f)          { this.fine       = f; }
    public void setStatus(Status s)        { this.status     = s; }
}
