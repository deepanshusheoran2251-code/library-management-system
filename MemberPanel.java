package library.ui;

import library.db.DatabaseManager;
import library.model.Member;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

import static library.ui.BookPanel.*;

public class MemberPanel extends JPanel {

    private final DatabaseManager db;
    private final String[] COLS = {"ID","Name","Email","Phone","Type","UID","Department"};
    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField fName, fEmail, fPhone, fUid, fDept, fSearch;
    private JComboBox<String> cbType;

    public MemberPanel(DatabaseManager db) {
        this.db = db;
        setLayout(new BorderLayout(10, 10));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildForm(),      BorderLayout.SOUTH);

        loadMembers(null);
    }

    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(LIGHT_BG);
        JLabel lbl = new JLabel("👤  Member Registration & Records");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(DARK_BLUE);

        fSearch = styledField("Search by name, UID, department…");
        JButton btnSearch = styledButton("Search", MID_BLUE);
        JButton btnAll    = styledButton("Show All", new Color(97,97,97));

        btnSearch.addActionListener(e -> loadMembers(fSearch.getText().trim()));
        btnAll.addActionListener(e    -> { fSearch.setText(""); loadMembers(null); });
        fSearch.addActionListener(e   -> loadMembers(fSearch.getText().trim()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(LIGHT_BG);
        right.add(fSearch); right.add(btnSearch); right.add(btnAll);

        p.add(lbl,   BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        p.setBorder(new EmptyBorder(0,0,8,0));
        return p;
    }

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

        // Type badge coloring
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if ("Faculty".equals(v)) {
                    setForeground(new Color(230, 81, 0));
                } else {
                    setForeground(new Color(26, 35, 126));
                }
                setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        int[] widths = {40, 140, 160, 100, 70, 100, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 240)));
        return sp;
    }

    private JPanel buildForm() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(200,210,240)),
            new EmptyBorder(14,16,14,16)
        ));

        JLabel header = new JLabel("Add / Edit Member");
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(DARK_BLUE);

        fName  = styledField("Full Name");
        fEmail = styledField("Email Address");
        fPhone = styledField("Phone Number");
        fUid   = styledField("UID / Roll No.");
        fDept  = styledField("Department");
        cbType = new JComboBox<>(new String[]{"Student", "Faculty"});
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel fields = new JPanel(new GridLayout(2, 6, 8, 8));
        fields.setBackground(WHITE);
        fields.add(labeledField("Name *",  fName));
        fields.add(labeledField("Email *", fEmail));
        fields.add(labeledField("Phone *", fPhone));
        fields.add(labeledField("UID *",   fUid));
        fields.add(labeledField("Dept *",  fDept));
        fields.add(labeledCombo("Type *",  cbType));
        for (int i=0;i<6;i++) fields.add(new JLabel());

        JButton btnAdd    = styledButton("➕ Add Member",  new Color(27,94,32));
        JButton btnUpdate = styledButton("✏ Update",       MID_BLUE);
        JButton btnDelete = styledButton("🗑 Delete",      new Color(183,28,28));
        JButton btnClear  = styledButton("✖ Clear",       new Color(97,97,97));

        btnAdd.addActionListener(e    -> addMember());
        btnUpdate.addActionListener(e -> updateMember());
        btnDelete.addActionListener(e -> deleteMember());
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

    private void addMember() {
        Member m = formToMember(0);
        if (m == null) return;
        try {
            db.addMember(m);
            loadMembers(null); clearForm();
            showMsg(this, "Member added successfully!", false);
        } catch (SQLException ex) {
            showMsg(this, "Error: " + ex.getMessage(), true);
        }
    }

    private void updateMember() {
        int row = table.getSelectedRow();
        if (row < 0) { showMsg(this, "Select a member to update.", true); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        Member m = formToMember(id);
        if (m == null) return;
        try {
            db.updateMember(m);
            loadMembers(null); clearForm();
            showMsg(this, "Member updated!", false);
        } catch (SQLException ex) {
            showMsg(this, "Error: " + ex.getMessage(), true);
        }
    }

    private void deleteMember() {
        int row = table.getSelectedRow();
        if (row < 0) { showMsg(this, "Select a member to delete.", true); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this member?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            db.deleteMember(id);
            loadMembers(null); clearForm();
        } catch (SQLException ex) {
            showMsg(this, "Error: " + ex.getMessage(), true);
        }
    }

    private void loadMembers(String keyword) {
        try {
            List<Member> members = keyword == null || keyword.isEmpty()
                ? db.getAllMembers() : db.searchMembers(keyword);
            tableModel.setRowCount(0);
            for (Member m : members)
                tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getEmail(),
                    m.getPhone(), m.getMemberType(), m.getUid(), m.getDepartment()
                });
        } catch (SQLException ex) {
            showMsg(this, "Failed to load members: " + ex.getMessage(), true);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        fName.setText(tableModel.getValueAt(row, 1).toString());  fName.setForeground(Color.BLACK);
        fEmail.setText(tableModel.getValueAt(row, 2).toString()); fEmail.setForeground(Color.BLACK);
        fPhone.setText(tableModel.getValueAt(row, 3).toString()); fPhone.setForeground(Color.BLACK);
        cbType.setSelectedItem(tableModel.getValueAt(row, 4).toString());
        fUid.setText(tableModel.getValueAt(row, 5).toString());   fUid.setForeground(Color.BLACK);
        fDept.setText(tableModel.getValueAt(row, 6).toString());  fDept.setForeground(Color.BLACK);
    }

    private void clearForm() {
        fName.setText(""); fEmail.setText(""); fPhone.setText("");
        fUid.setText("");  fDept.setText("");  cbType.setSelectedIndex(0);
        table.clearSelection();
    }

    private Member formToMember(int id) {
        String name  = fName.getText().trim();
        String email = fEmail.getText().trim();
        String phone = fPhone.getText().trim();
        String uid   = fUid.getText().trim();
        String dept  = fDept.getText().trim();
        String type  = cbType.getSelectedItem().toString();
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || uid.isEmpty() || dept.isEmpty()) {
            showMsg(this, "All fields are required.", true); return null;
        }
        return new Member(id, name, email, phone, type, uid, dept);
    }

    static JPanel labeledCombo(String label, JComboBox<?> cb) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(80,80,80));
        p.add(lbl, BorderLayout.NORTH);
        p.add(cb,  BorderLayout.CENTER);
        return p;
    }
}
