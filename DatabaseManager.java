package library.db;

import library.exception.BookNotAvailableException;
import library.model.Book;
import library.model.Member;
import library.model.Transaction;
import library.model.Transaction.Status;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager – JDBC layer using SQLite.
 * Database file: library.db in the application working directory.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static DatabaseManager instance;
    private Connection connection;

    // ── Singleton ────────────────────────────────────────────────────────────
    private DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTables();
        insertSampleData();
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() { return connection; }

    // ── Schema ───────────────────────────────────────────────────────────────
    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS books (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    title            TEXT    NOT NULL,
                    isbn             TEXT    UNIQUE NOT NULL,
                    author           TEXT    NOT NULL,
                    category         TEXT    NOT NULL,
                    total_copies     INTEGER NOT NULL DEFAULT 1,
                    available_copies INTEGER NOT NULL DEFAULT 1
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS members (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT NOT NULL,
                    email       TEXT NOT NULL,
                    phone       TEXT NOT NULL,
                    member_type TEXT NOT NULL,
                    uid         TEXT UNIQUE NOT NULL,
                    department  TEXT NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS issue_records (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id     INTEGER NOT NULL REFERENCES books(id),
                    member_id   INTEGER NOT NULL REFERENCES members(id),
                    issue_date  TEXT    NOT NULL,
                    due_date    TEXT    NOT NULL,
                    return_date TEXT,
                    fine        REAL    NOT NULL DEFAULT 0,
                    status      TEXT    NOT NULL DEFAULT 'ISSUED'
                )
            """);
        }
    }

    private void insertSampleData() throws SQLException {
        // Only insert if tables are empty
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM books")) {
            ResultSet rs = ps.executeQuery();
            if (rs.getInt(1) > 0) return;
        }
        // Sample books
        String[] bookData = {
            "Clean Code","978-0132350884","Robert C. Martin","Programming","3",
            "Effective Java","978-0134685991","Joshua Bloch","Programming","2",
            "Head First Java","978-0596009205","Kathy Sierra","Programming","4",
            "Data Structures","978-0486485829","Seymour Lipschutz","CS Theory","2",
            "DBMS Concepts","978-0073523323","Abraham Silberschatz","Database","3"
        };
        String sql = "INSERT INTO books(title,isbn,author,category,total_copies,available_copies) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < bookData.length; i += 5) {
                ps.setString(1, bookData[i]);
                ps.setString(2, bookData[i+1]);
                ps.setString(3, bookData[i+2]);
                ps.setString(4, bookData[i+3]);
                int copies = Integer.parseInt(bookData[i+4]);
                ps.setInt(5, copies);
                ps.setInt(6, copies);
                ps.executeUpdate();
            }
        }
        // Sample members
        String[][] memberData = {
            {"Sachin","sachin@email.com","9876543210","Student","2510001276","CSE"},
            {"Nitin","nitin@email.com","9876543211","Student","2510001281","CSE"},
            {"Harsh","harsh@email.com","9876543212","Student","2510001227","CSE"},
            {"Deepanshu","deep@email.com","9876543213","Student","2510001279","CSE"},
            {"Prof. Kumar","kumar@email.com","9876500001","Faculty","FAC001","CSE"}
        };
        String msql = "INSERT INTO members(name,email,phone,member_type,uid,department) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(msql)) {
            for (String[] m : memberData) {
                for (int i = 0; i < m.length; i++) ps.setString(i+1, m[i]);
                ps.executeUpdate();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOOKS – CRUD
    // ══════════════════════════════════════════════════════════════════════════

    public List<Book> getAllBooks() throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBook(rs));
        }
        return list;
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(category) LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBook(rs));
        }
        return list;
    }

    public void addBook(Book b) throws SQLException {
        String sql = "INSERT INTO books(title,isbn,author,category,total_copies,available_copies) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getIsbn());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getCategory());
            ps.setInt(5, b.getTotalCopies());
            ps.setInt(6, b.getAvailableCopies());
            ps.executeUpdate();
        }
    }

    public void updateBook(Book b) throws SQLException {
        String sql = "UPDATE books SET title=?,isbn=?,author=?,category=?,total_copies=?,available_copies=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getIsbn());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getCategory());
            ps.setInt(5, b.getTotalCopies());
            ps.setInt(6, b.getAvailableCopies());
            ps.setInt(7, b.getId());
            ps.executeUpdate();
        }
    }

    public void deleteBook(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM books WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("isbn"),
            rs.getString("author"),
            rs.getString("category"),
            rs.getInt("total_copies"),
            rs.getInt("available_copies")
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MEMBERS – CRUD
    // ══════════════════════════════════════════════════════════════════════════

    public List<Member> getAllMembers() throws SQLException {
        List<Member> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM members ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMember(rs));
        }
        return list;
    }

    public List<Member> searchMembers(String keyword) throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE LOWER(name) LIKE ? OR LOWER(uid) LIKE ? OR LOWER(department) LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapMember(rs));
        }
        return list;
    }

    public void addMember(Member m) throws SQLException {
        String sql = "INSERT INTO members(name,email,phone,member_type,uid,department) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getMemberType());
            ps.setString(5, m.getUid());
            ps.setString(6, m.getDepartment());
            ps.executeUpdate();
        }
    }

    public void updateMember(Member m) throws SQLException {
        String sql = "UPDATE members SET name=?,email=?,phone=?,member_type=?,uid=?,department=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getMemberType());
            ps.setString(5, m.getUid());
            ps.setString(6, m.getDepartment());
            ps.setInt(7, m.getId());
            ps.executeUpdate();
        }
    }

    public void deleteMember(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM members WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Member mapMember(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("member_type"),
            rs.getString("uid"),
            rs.getString("department")
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TRANSACTIONS – Issue / Return
    // ══════════════════════════════════════════════════════════════════════════

    public void issueBook(int bookId, int memberId, int loanDays)
            throws SQLException, BookNotAvailableException {
        // Check availability
        Book book = getBookById(bookId);
        if (book == null || book.getAvailableCopies() <= 0) {
            String title = book != null ? book.getTitle() : "ID=" + bookId;
            throw new BookNotAvailableException(title);
        }
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate   = issueDate.plusDays(loanDays);

        String sql = "INSERT INTO issue_records(book_id,member_id,issue_date,due_date,status) VALUES(?,?,?,?,'ISSUED')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            ps.setString(3, issueDate.toString());
            ps.setString(4, dueDate.toString());
            ps.executeUpdate();
        }
        // Decrement available copies
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE books SET available_copies = available_copies - 1 WHERE id=?")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    public double returnBook(int transactionId) throws SQLException {
        Transaction t = getTransactionById(transactionId);
        if (t == null) return 0;

        LocalDate returnDate = LocalDate.now();
        long overdueDays = returnDate.isAfter(t.getDueDate())
            ? returnDate.toEpochDay() - t.getDueDate().toEpochDay() : 0;
        double fine = overdueDays * 2.0;

        String sql = "UPDATE issue_records SET return_date=?,fine=?,status='RETURNED' WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, returnDate.toString());
            ps.setDouble(2, fine);
            ps.setInt(3, transactionId);
            ps.executeUpdate();
        }
        // Increment available copies
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE books SET available_copies = available_copies + 1 WHERE id=?")) {
            ps.setInt(1, t.getBookId());
            ps.executeUpdate();
        }
        return fine;
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT ir.*, b.title AS book_title, m.name AS member_name
            FROM issue_records ir
            JOIN books   b ON ir.book_id   = b.id
            JOIN members m ON ir.member_id = m.id
            ORDER BY ir.id DESC
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapTransaction(rs));
        }
        return list;
    }

    public List<Transaction> getActiveTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT ir.*, b.title AS book_title, m.name AS member_name
            FROM issue_records ir
            JOIN books   b ON ir.book_id   = b.id
            JOIN members m ON ir.member_id = m.id
            WHERE ir.status='ISSUED'
            ORDER BY ir.due_date
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapTransaction(rs));
        }
        return list;
    }

    public List<Transaction> getOverdueTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = """
            SELECT ir.*, b.title AS book_title, m.name AS member_name
            FROM issue_records ir
            JOIN books   b ON ir.book_id   = b.id
            JOIN members m ON ir.member_id = m.id
            WHERE ir.status='ISSUED' AND ir.due_date < ?
            ORDER BY ir.due_date
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTransaction(rs));
        }
        return list;
    }

    private Book getBookById(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM books WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBook(rs);
        }
        return null;
    }

    private Transaction getTransactionById(int id) throws SQLException {
        String sql = """
            SELECT ir.*, b.title AS book_title, m.name AS member_name
            FROM issue_records ir
            JOIN books   b ON ir.book_id   = b.id
            JOIN members m ON ir.member_id = m.id
            WHERE ir.id=?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapTransaction(rs);
        }
        return null;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        String retStr = rs.getString("return_date");
        return new Transaction(
            rs.getInt("id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            rs.getString("book_title"),
            rs.getString("member_name"),
            LocalDate.parse(rs.getString("issue_date")),
            LocalDate.parse(rs.getString("due_date")),
            retStr != null ? LocalDate.parse(retStr) : null,
            rs.getDouble("fine"),
            Status.valueOf(rs.getString("status"))
        );
    }

    // ── Stats for Reports ─────────────────────────────────────────────────────
    public int getTotalBooks() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(total_copies) FROM books");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getAvailableBooks() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(available_copies) FROM books");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTotalMembers() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM members");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getActiveIssues() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM issue_records WHERE status='ISSUED'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getTotalFinesCollected() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(fine) FROM issue_records WHERE status='RETURNED'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}
