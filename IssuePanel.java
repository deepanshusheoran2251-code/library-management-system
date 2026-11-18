package library.ui;

import library.db.DatabaseManager;
import library.exception.BookNotAvailableException;
import library.model.Book;
import library.model.Member;
import library.model.Transaction;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static library.ui.BookPanel.*;

public class IssuePanel extends JPanel {

    private final DatabaseManager db;

    // Issue form
    private JComboBox<Book>   cbBook;
    private JComboBox<Member> cbMember;
    private JLabel            lblDueDate;

    // Return form
    private JTextField fReturnId;

    // Table
    private final String[] COLS = {"Txn ID","Book","Member","Issue Date","Due Date","Return Date","Fine (₹)","Status"};
    private DefaultTableModel tableModel;
    private JTable table;

    public IssuePanel(DatabaseManager db) {
        this.db = db;
        setLayout(new BorderLayout(10, 10));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildActionCards(), BorderLayout.NORTH);
        add(buildTable(),       BorderLayout.CENTER);

        refresh();
    }

    // ── Top action cards ─────────────────────────────────────────────────────
    private JPanel buildActionCards() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(LIGHT_BG);
        row.add(buildIssueCard());
        row.add(buildReturnCard());
        return row;
    }

    private JPanel buildIssueCard() {
        JPanel card = card("📖  Issue Book");

        cbBook   = new JComboBox<>();
        cbMember = new JComboBox<>();
        lblDueDate = new JLabel("Due Date: —");
        lblDueDate.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDueDate.setForeground(new Color(80,80,80));

        cbBook.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMember.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cbMember.addActionListener(e -> updateDueDate());
        cbBook.addActionListener(e   -> updateDueDate());

        JButton btnIssue = styledButton("Issue Book ➜", new Color(27,94,32));
        btnIssue.addActionListener(e -> issueBook());

        JPanel form = new JPanel(new GridLayout(4, 1, 0, 8));
        form.setBackground(WHITE);
        form.add(MemberPanel.labeledCombo("Select Book *",   cbBook));
        form.add(MemberPanel.labeledCombo("Select Member *", cbMember));
        form.add(lblDueDate);
        form.add(btnIssue);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildReturnCard() {
        JPanel card = card("↩  Return Book");

        fReturnId = styledField("Enter Transaction ID");
        JLabel hint = new JLabel("(Find Transaction ID in the table below)");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(100,100,100));

        JButton btnReturn = styledButton("Process Return ➜", new Color(26,35,126));
        btnReturn.addActionListener(e -> returnBook());

        JPanel form = new JPanel(new GridLayout(4, 1, 0, 8));
        form.setBackground(WHITE);
        form.add(labeledField("Transaction ID *", fReturnId));
        form.add(hint);
        form.add(new JLabel());
        form.add(btnReturn);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(200,210,240)),
            new EmptyBorder(14,16,14,16)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(DARK_BLUE);
        lbl.setBorder(new EmptyBorder(0,0,8,0));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    // ── Table ────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        int[] widths = {55, 190, 130, 90, 90, 90, 70, 75};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status & overdue colouring
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String status = v.toString();
                if ("ISSUED".equals(status)) {
                    setForeground(new Color(27,94,32)); setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(new Color(100,100,100));
                }
                return this;
            }
        });

        // Fine colouring
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                double fine = Double.parseDouble(v.toString());
                setForeground(fine > 0 ? Color.RED : Color.BLACK);
                setFont(fine > 0 ? getFont().deriveFont(Font.BOLD) : getFont());
                setHorizontalAlignment(RIGHT);
                return this;
            }
        });

        // Click row → prefill return id
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Object txnId = tableModel.getValueAt(table.getSelectedRow(), 0);
                Object status = tableModel.getValueAt(table.getSelectedRow(), 7);
                if ("ISSUED".equals(status.toString())) {
                    fReturnId.setText(txnId.toString());
                    fReturnId.setForeground(Color.BLACK);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200,210,240)));

        JPanel wrapper = new JPanel(new BorderLayout(0,6));
        wrapper.setBackground(LIGHT_BG);
        JLabel tbl = new JLabel("📋  All Issue Records");
        tbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tbl.setForeground(DARK_BLUE);
        wrapper.add(tbl, BorderLayout.NORTH);
        wrapper.add(sp,  BorderLayout.CENTER);
        return sp;
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void issueBook() {
        Book   book   = (Book)   cbBook.getSelectedItem();
        Member member = (Member) cbMember.getSelectedItem();
        if (book == null || member == null) {
            showMsg(this, "Select both a book and a member.", true); return;
        }
        try {
            db.issueBook(book.getId(), member.getId(), member.getLoanPeriodDays());
            refresh();
            showMsg(this, "Book issued successfully!\nDue in " + member.getLoanPeriodDays() + " days.", false);
        } catch (BookNotAvailableException ex) {
            showMsg(this, ex.getMessage(), true);
        } catch (SQLException ex) {
            showMsg(this, "DB Error: " + ex.getMessage(), true);
        }
    }

    private void returnBook() {
        String idStr = fReturnId.getText().trim();
        if (idStr.isEmpty()) { showMsg(this, "Enter a Transaction ID.", true); return; }
        int txnId;
        try { txnId = Integer.parseInt(idStr); }
        catch (NumberFormatException e) { showMsg(this, "Transaction ID must be a number.", true); return; }
        try {
            double fine = db.returnBook(txnId);
            refresh();
            fReturnId.setText("");
            String msg = fine > 0
                ? String.format("Book returned. Fine collected: ₹%.2f", fine)
                : "Book returned successfully. No fine.";
            showMsg(this, msg, false);
        } catch (SQLException ex) {
            showMsg(this, "DB Error: " + ex.getMessage(), true);
        }
    }

    private void updateDueDate() {
        Member m = (Member) cbMember.getSelectedItem();
        if (m != null) {
            java.time.LocalDate due = java.time.LocalDate.now().plusDays(m.getLoanPeriodDays());
            lblDueDate.setText("Due Date: " + due + "  (" + m.getLoanPeriodDays() + " days)");
        }
    }

    public void refresh() {
        // Reload combos
        try {
            List<Book>   books   = db.getAllBooks();
            List<Member> members = db.getAllMembers();
            cbBook.removeAllItems();
            cbMember.removeAllItems();
            for (Book b   : books)   cbBook.addItem(b);
            for (Member m : members) cbMember.addItem(m);
        } catch (SQLException ex) { /* ignore */ }

        // Reload table
        try {
            List<Transaction> txns = db.getAllTransactions();
            tableModel.setRowCount(0);
            for (Transaction t : txns)
                tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getBookTitle(),
                    t.getMemberName(),
                    t.getIssueDate(),
                    t.getDueDate(),
                    t.getReturnDate() != null ? t.getReturnDate() : "—",
                    String.format("%.2f", t.getFine()),
                    t.getStatus().name()
                });
        } catch (SQLException ex) {
            showMsg(this, "Failed to load transactions: " + ex.getMessage(), true);
        }
        updateDueDate();
    }
}
