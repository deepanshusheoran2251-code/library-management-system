package library.ui;

import library.db.DatabaseManager;
import library.model.Book;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class BookPanel extends JPanel {

    // ── Colours ──────────────────────────────────────────────────────────────
    static final Color DARK_BLUE  = new Color(26, 35, 126);
    static final Color MID_BLUE   = new Color(57, 73, 171);
    static final Color LIGHT_BG   = new Color(245, 247, 255);
    static final Color WHITE      = Color.WHITE;
    static final Color ACCENT     = new Color(239, 108, 0);

    // ── Data ─────────────────────────────────────────────────────────────────
    private final DatabaseManager db;
    private final String[] COLS = {"ID","Title","ISBN","Author","Category","Total","Available"};
    private DefaultTableModel tableModel;
    private JTable table;

    // ── Form fields ──────────────────────────────────────────────────────────
    private JTextField fTitle, fIsbn, fAuthor, fCategory, fCopies, fSearch;

    public BookPanel(DatabaseManager db) {
        this.db = db;
        setLayout(new BorderLayout(10, 10));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildForm(),      BorderLayout.SOUTH);

        loadBooks(null);
    }

    // ── Search Bar ───────────────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(LIGHT_BG);
        JLabel lbl = new JLabel("📚  Book Catalogue");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(DARK_BLUE);

        fSearch = styledField("Search by title, author, ISBN, category…");
        JButton btnSearch = styledButton("Search", MID_BLUE);
        JButton btnAll    = styledButton("Show All", new Color(97,97,97));

        btnSearch.addActionListener(e -> loadBooks(fSearch.getText().trim()));
        btnAll.addActionListener(e  -> { fSearch.setText(""); loadBooks(null); });
        fSearch.addActionListener(e -> loadBooks(fSearch.getText().trim()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(LIGHT_BG);
        right.add(fSearch); right.add(btnSearch); right.add(btnAll);

        p.add(lbl,   BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        p.setBorder(new EmptyBorder(0,0,8,0));
        return p;
    }

    // ── Table ────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        // Column widths
        int[] widths = {40, 200, 130, 140, 100, 55, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Colour "Available" column
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                int avail = Integer.parseInt(v.toString());
                setForeground(avail == 0 ? Color.RED : new Color(27, 94, 32));
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 240)));
        return sp;
    }

    // ── Form ─────────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(200,210,240)),
            new EmptyBorder(14,16,14,16)
        ));

        // Title row
        JLabel header = new JLabel("Add / Edit Book");
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(DARK_BLUE);

        // Fields grid
        fTitle    = styledField("Book Title");
        fIsbn     = styledField("ISBN");
        fAuthor   = styledField("Author");
        fCategory = styledField("Category");
        fCopies   = styledField("No. of Copies");

        JPanel fields = new JPanel(new GridLayout(2, 5, 8, 8));
        fields.setBackground(WHITE);
        for (JComponent c : new JComponent[]{
            labeledField("Title *",    fTitle),
            labeledField("ISBN *",     fIsbn),
            labeledField("Author *",   fAuthor),
            labeledField("Category *", fCategory),
            labeledField("Copies *",   fCopies)
        }) fields.add(c);
        // Fill remaining cells
        for (int i=0;i<5;i++) fields.add(new JLabel());

        // Buttons
        JButton btnAdd    = styledButton("➕ Add Book",    new Color(27,94,32));
        JButton btnUpdate = styledButton("✏ Update",       MID_BLUE);
        JButton btnDelete = styledButton("🗑 Delete",      new Color(183,28,28));
        JButton btnClear  = styledButton("✖ Clear",       new Color(97,97,97));

        btnAdd.addActionListener(e    -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e  -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(WHITE);
        buttons.add(btnClear); buttons.add(btnDelete);
        buttons.add(btnUpdate); buttons.add(btnAdd);

        card.add(header,  BorderLayout.NORTH);
        card.add(fields,  BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    // ── CRUD Actions ─────────────────────────────────────────────────────────
    private void addBook() {
        try {
            Book b = formToBook(0);
            if (b == null) return;
            db.addBook(b);
            loadBooks(null); clearForm();
            showMsg("Book added successfully!", false);
        } catch (SQLException ex) {
            showMsg("Error: " + ex.getMessage(), true);
        }
    }

    private void updateBook() {
        int row = table.getSelectedRow();
        if (row < 0) { showMsg("Select a book to update.", true); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Book b = formToBook(id);
            if (b == null) return;
            db.updateBook(b);
            loadBooks(null); clearForm();
            showMsg("Book updated successfully!", false);
        } catch (SQLException ex) {
            showMsg("Error: " + ex.getMessage(), true);
        }
    }

    private void deleteBook() {
        int row = table.getSelectedRow();
        if (row < 0) { showMsg("Select a book to delete.", true); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this book?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            db.deleteBook(id);
            loadBooks(null); clearForm();
        } catch (SQLException ex) {
            showMsg("Error: " + ex.getMessage(), true);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void loadBooks(String keyword) {
        try {
            List<Book> books = keyword == null || keyword.isEmpty()
                ? db.getAllBooks() : db.searchBooks(keyword);
            tableModel.setRowCount(0);
            for (Book b : books)
                tableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getIsbn(),
                    b.getAuthor(), b.getCategory(),
                    b.getTotalCopies(), b.getAvailableCopies()
                });
        } catch (SQLException ex) {
            showMsg("Failed to load books: " + ex.getMessage(), true);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        fTitle.setText(tableModel.getValueAt(row, 1).toString());
        fIsbn.setText(tableModel.getValueAt(row, 2).toString());
        fAuthor.setText(tableModel.getValueAt(row, 3).toString());
        fCategory.setText(tableModel.getValueAt(row, 4).toString());
        fCopies.setText(tableModel.getValueAt(row, 5).toString());
    }

    private void clearForm() {
        fTitle.setText(""); fIsbn.setText(""); fAuthor.setText("");
        fCategory.setText(""); fCopies.setText("");
        table.clearSelection();
    }

    private Book formToBook(int id) {
        String title    = fTitle.getText().trim();
        String isbn     = fIsbn.getText().trim();
        String author   = fAuthor.getText().trim();
        String category = fCategory.getText().trim();
        String copiesStr= fCopies.getText().trim();
        if (title.isEmpty() || isbn.isEmpty() || author.isEmpty() || category.isEmpty() || copiesStr.isEmpty()) {
            showMsg("All fields are required.", true); return null;
        }
        int copies;
        try { copies = Integer.parseInt(copiesStr); }
        catch (NumberFormatException e) { showMsg("Copies must be a number.", true); return null; }
        return new Book(id, title, isbn, author, category, copies, copies);
    }

    // ── Static UI helpers ────────────────────────────────────────────────────
    static void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(WHITE);
        t.setSelectionBackground(new Color(197, 202, 233));
        t.setSelectionForeground(Color.BLACK);
        JTableHeader h = t.getTableHeader();
        h.setBackground(DARK_BLUE);
        h.setForeground(WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setReorderingAllowed(false);
        // Alternating rows
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? WHITE : new Color(240, 243, 255));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    static JTextField styledField(String placeholder) {
        JTextField f = new JTextField(14);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 220)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        f.setForeground(Color.GRAY);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(Color.GRAY); }
            }
        });
        return f;
    }

    static JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(bg.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(bg);
            }
        });
        return b;
    }

    static JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(80,80,80));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    static void showMsg(JPanel parent, String msg, boolean error) {
        JOptionPane.showMessageDialog(parent, msg,
            error ? "Error" : "Success",
            error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMsg(String msg, boolean error) { showMsg(this, msg, error); }
}
