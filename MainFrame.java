package library.ui;

import library.db.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final Color DARK_BLUE = new Color(26, 35, 126);
    private static final Color WHITE     = Color.WHITE;

    private final DatabaseManager db;
    private IssuePanel   issuePanel;
    private ReportsPanel reportsPanel;

    public MainFrame(DatabaseManager db) {
        this.db = db;
        setTitle("Library Management System – Group 1 | CSE2003");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        setContentPane(buildContent());
        setVisible(true);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopBar(),   BorderLayout.NORTH);
        root.add(buildTabs(),     BorderLayout.CENTER);
        root.add(buildStatusBar(),BorderLayout.SOUTH);
        return root;
    }

    // ── Top Banner ───────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DARK_BLUE);
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel left = new JLabel("📚  Library Management System");
        left.setFont(new Font("Segoe UI", Font.BOLD, 18));
        left.setForeground(WHITE);

        JLabel right = new JLabel("Group 1  |  CSE2003  |  B.Tech CSE Sem 3");
        right.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        right.setForeground(new Color(197, 202, 233));

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Tabbed Pane ──────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(new Color(240, 243, 255));

        // Tab 1: Books
        tabs.addTab("  📚  Books  ", new BookPanel(db));

        // Tab 2: Members
        tabs.addTab("  👤  Members  ", new MemberPanel(db));

        // Tab 3: Issue / Return
        issuePanel = new IssuePanel(db);
        tabs.addTab("  📤  Issue / Return  ", issuePanel);

        // Tab 4: Reports
        reportsPanel = new ReportsPanel(db);
        tabs.addTab("  📊  Reports  ", reportsPanel);

        // Refresh reports when switching to tabs 3 & 4
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 2) issuePanel.refresh();
            if (idx == 3) reportsPanel.refresh();
        });

        // Start overdue-notification thread
        startOverdueThread(tabs);

        return tabs;
    }

    // ── Status Bar ───────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bar.setBackground(new Color(232, 234, 246));
        bar.setBorder(new MatteBorder(1,0,0,0, new Color(200,210,240)));

        JLabel lbl = new JLabel("🟢 Connected to library.db  |  Fine rate: ₹2/day  |  Student loan: 14 days  |  Faculty loan: 30 days");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(60,60,100));
        bar.add(lbl);
        return bar;
    }

    // ── Overdue Notification Thread ──────────────────────────────────────────
    private void startOverdueThread(JTabbedPane tabs) {
        Thread notifier = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000L); // check every minute
                    int count = db.getOverdueTransactions().size();
                    if (count > 0) {
                        SwingUtilities.invokeLater(() ->
                            tabs.setTitleAt(3,
                                "  📊  Reports (" + count + " overdue)  ")
                        );
                    } else {
                        SwingUtilities.invokeLater(() ->
                            tabs.setTitleAt(3, "  📊  Reports  ")
                        );
                    }
                } catch (Exception ignored) {}
            }
        });
        notifier.setDaemon(true);
        notifier.start();
    }
}
