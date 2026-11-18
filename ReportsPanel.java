package library.ui;

import library.db.DatabaseManager;
import library.model.Transaction;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static library.ui.BookPanel.*;

public class ReportsPanel extends JPanel {

    private final DatabaseManager db;

    // Stat labels
    private JLabel lblTotalBooks, lblAvailable, lblIssued, lblMembers,
                   lblFines, lblOverdue;

    // Overdue table
    private DefaultTableModel overdueModel;
    private JTable overdueTable;

    // All transactions table
    private DefaultTableModel allModel;
    private JTable allTable;

    private final String[] TXN_COLS = {"Txn ID","Book","Member","Issue Date","Due Date","Status","Fine (₹)"};

    public ReportsPanel(DatabaseManager db) {
        this.db = db;
        setLayout(new BorderLayout(10, 12));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel top = new JPanel(new BorderLayout(0,10));
        top.setBackground(LIGHT_BG);

        JLabel title = new JLabel("📊  Reports & Statistics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(DARK_BLUE);
        title.setBorder(new EmptyBorder(0,0,8,0));

        // Stat cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 6, 10, 0));
        statsRow.setBackground(LIGHT_BG);

        lblTotalBooks = new JLabel("—");
        lblAvailable  = new JLabel("—");
        lblIssued     = new JLabel("—");
        lblMembers    = new JLabel("—");
        lblFines      = new JLabel("—");
        lblOverdue    = new JLabel("—");

        statsRow.add(statCard("📚 Total Books",     lblTotalBooks, new Color(26,35,126)));
        statsRow.add(statCard("✅ Available",        lblAvailable,  new Color(27,94,32)));
        statsRow.add(statCard("📤 Issued",           lblIssued,     new Color(230,81,0)));
        statsRow.add(statCard("👤 Members",          lblMembers,    new Color(74,20,140)));
        statsRow.add(statCard("💰 Fines (₹)",        lblFines,      new Color(183,28,28)));
        statsRow.add(statCard("⚠ Overdue",          lblOverdue,    new Color(130,80,0)));

        top.add(title,    BorderLayout.NORTH);
        top.add(statsRow, BorderLayout.CENTER);
        return top;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 12));
        center.setBackground(LIGHT_BG);
        center.add(buildOverduePanel());
        center.add(buildAllTxnPanel());
        return center;
    }

    private JPanel buildOverduePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(LIGHT_BG);

        JLabel lbl = new JLabel("⚠  Overdue Books");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(183,28,28));

        overdueModel = new DefaultTableModel(TXN_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        overdueTable = new JTable(overdueModel);
        styleTable(overdueTable);
        overdueTable.setBackground(new Color(255,250,250));

        JScrollPane sp = new JScrollPane(overdueTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,180,180)));

        p.add(lbl, BorderLayout.NORTH);
        p.add(sp,  BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAllTxnPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(LIGHT_BG);

        JLabel lbl = new JLabel("📋  Issued & Available Books Summary");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(DARK_BLUE);

        JButton btnRefresh = styledButton("🔄 Refresh", MID_BLUE);
        btnRefresh.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIGHT_BG);
        top.add(lbl,        BorderLayout.WEST);
        top.add(btnRefresh, BorderLayout.EAST);

        allModel = new DefaultTableModel(TXN_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        allTable = new JTable(allModel);
        styleTable(allTable);

        // Status column colouring
        allTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setForeground("ISSUED".equals(v) ? new Color(27,94,32) : new Color(100,100,100));
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(allTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200,210,240)));

        p.add(top, BorderLayout.NORTH);
        p.add(sp,  BorderLayout.CENTER);
        return p;
    }

    public void refresh() {
        try {
            lblTotalBooks.setText(String.valueOf(db.getTotalBooks()));
            lblAvailable.setText(String.valueOf(db.getAvailableBooks()));
            lblIssued.setText(String.valueOf(db.getActiveIssues()));
            lblMembers.setText(String.valueOf(db.getTotalMembers()));
            lblFines.setText(String.format("%.2f", db.getTotalFinesCollected()));

            // Overdue
            List<Transaction> overdue = db.getOverdueTransactions();
            lblOverdue.setText(String.valueOf(overdue.size()));
            overdueModel.setRowCount(0);
            for (Transaction t : overdue)
                overdueModel.addRow(txnRow(t));

            // All transactions
            List<Transaction> all = db.getAllTransactions();
            allModel.setRowCount(0);
            for (Transaction t : all)
                allModel.addRow(txnRow(t));

        } catch (SQLException ex) {
            showMsg(this, "Failed to load reports: " + ex.getMessage(), true);
        }
    }

    private Object[] txnRow(Transaction t) {
        return new Object[]{
            t.getId(),
            t.getBookTitle(),
            t.getMemberName(),
            t.getIssueDate(),
            t.getDueDate(),
            t.getStatus().name(),
            String.format("%.2f", t.getFine())
        };
    }

    private JPanel statCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(200,210,240)),
            new EmptyBorder(14,12,14,12)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(80,80,80));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLbl,  BorderLayout.SOUTH);
        return card;
    }
}
