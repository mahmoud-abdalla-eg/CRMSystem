package views;

import models.Customer;
import models.Interaction;
import utils.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private DataStore dataStore = DataStore.getInstance();

    private JPanel sidebar;
    private JPanel contentArea;
    private CardLayout contentCardLayout;

    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<Object> statusFilter;
    private JPanel detailPanel;
    private JLabel titleLabel;

    private Customer selectedCustomer;
    private JPanel interactionListPanel;
    private int selectedNavIndex = 1;
    private JButton[] navButtons;
    private JPanel quickSummaryPanel;
    private String currentViewKey = "CUSTOMERS";

    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color SIDEBAR_BG = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color PURPLE = new Color(168, 85, 247);
    private static final Color YELLOW = new Color(234, 179, 8);

    public MainFrame() {
        setTitle("CRM System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        contentCardLayout = new CardLayout();
        contentArea = new JPanel(contentCardLayout);
        contentArea.setOpaque(true);
        contentArea.setBackground(new Color(248, 250, 252));

        addView("DASHBOARD", createDashboardPanel());
        addView("CUSTOMERS", createCustomersPanel());
        addView("INTERACTIONS", createInteractionsPanel());
        addView("REPORTS", createReportsPanel());
        addView("SETTINGS", createSettingsPanel());

        add(contentArea, BorderLayout.CENTER);

        showView("CUSTOMERS");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel logoLabel = new JLabel("CRM");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Customer Manager");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logoLabel);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitleLabel);
        sidebar.add(Box.createVerticalStrut(30));

        String[] navLabels = {"Dashboard", "Customers", "Interactions", "Reports", "Settings"};
        String[] navKeys = {"DASHBOARD", "CUSTOMERS", "INTERACTIONS", "REPORTS", "SETTINGS"};

        navButtons = new JButton[navLabels.length];
        for (int i = 0; i < navLabels.length; i++) {
            final int navIndex = i;
            final String viewKey = navKeys[i];
            JButton navBtn = createNavButton(navLabels[i], i == selectedNavIndex);
            navBtn.addActionListener(e -> selectNavItem(navIndex, viewKey));
            navButtons[i] = navBtn;
            sidebar.add(navBtn);
            sidebar.add(Box.createVerticalStrut(5));
        }

        sidebar.add(Box.createVerticalStrut(28));

        quickSummaryPanel = createStatsPanel();
        sidebar.add(quickSummaryPanel);

        return sidebar;
    }

    private JButton createNavButton(String text, boolean selected) {
        JButton btn = new JButton("  " + text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(selected ? Color.WHITE : new Color(148, 163, 184));
        btn.setBackground(selected ? PRIMARY : SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(190, 44));
        btn.setPreferredSize(new Dimension(190, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectNavItem(int index, String viewKey) {
        if (index == selectedNavIndex) return;
        selectedNavIndex = index;

        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].setBackground(SIDEBAR_BG);
            navButtons[i].setForeground(new Color(148, 163, 184));
        }
        navButtons[index].setBackground(PRIMARY);
        navButtons[index].setForeground(Color.WHITE);

        showView(viewKey);
    }

    private void showView(String key) {
        currentViewKey = key;
        refreshView(key);
        contentCardLayout.show(contentArea, key);

        String[] titles = {"Dashboard", "Customers", "Interactions", "Reports", "Settings"};
        if (titleLabel != null) {
            titleLabel.setText(titles[selectedNavIndex]);
        }
    }

    private void addView(String key, JPanel panel) {
        panel.setName(key);
        contentArea.add(panel, key);
    }

    private void refreshView(String key) {
        if ("DASHBOARD".equals(key)) {
            replaceView("DASHBOARD", createDashboardPanel());
        } else if ("CUSTOMERS".equals(key)) {
            filterCustomers();
            updateDetailPanel();
        } else if ("INTERACTIONS".equals(key)) {
            replaceView("INTERACTIONS", createInteractionsPanel());
        } else if ("REPORTS".equals(key)) {
            replaceView("REPORTS", createReportsPanel());
        }
    }

    private void replaceView(String key, JPanel panel) {
        for (Component component : contentArea.getComponents()) {
            if (key.equals(component.getName())) {
                contentArea.remove(component);
                break;
            }
        }
        addView(key, panel);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setBackground(new Color(30, 41, 59));
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(190, 230));
        statsPanel.setPreferredSize(new Dimension(190, 230));

        JLabel title = new JLabel("Quick Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(title);
        statsPanel.add(Box.createVerticalStrut(10));

        List<Customer> customers = dataStore.getAllCustomers();
        Customer latestCustomer = customers.isEmpty() ? null : customers.get(customers.size() - 1);
        Interaction latestInteraction = getLatestInteraction();

        statsPanel.add(createSummaryItem("Customers", customers.size() + " total records", PRIMARY));
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(createSummaryItem("Newest", latestCustomer != null ? shortenText(latestCustomer.getName(), 22) : "No customers yet", GREEN));
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(createSummaryItem("Last Activity", describeLatestInteraction(latestInteraction), PURPLE));

        return statsPanel;
    }

    private JPanel createSummaryItem(String label, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(51, 65, 85));
        panel.setLayout(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(7, 10, 7, 10));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        panel.setPreferredSize(new Dimension(0, 54));

        JLabel dot = new JLabel();
        dot.setBackground(color);
        dot.setOpaque(true);
        dot.setPreferredSize(new Dimension(7, 24));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(new Color(148, 163, 184));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel();
        textPanel.setBackground(new Color(0, 0, 0, 0));
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(nameLabel);
        textPanel.add(valueLabel);

        panel.add(dot, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    private void updateStats() {
        if (quickSummaryPanel == null || sidebar == null) return;
        sidebar.remove(quickSummaryPanel);
        quickSummaryPanel = createStatsPanel();
        sidebar.add(quickSummaryPanel);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private Interaction getLatestInteraction() {
        Interaction latest = null;
        for (Customer customer : dataStore.getAllCustomers()) {
            for (Interaction interaction : dataStore.getInteractions(customer.getId())) {
                if (latest == null || interaction.getDate().isAfter(latest.getDate())) {
                    latest = interaction;
                }
            }
        }
        return latest;
    }

    private String describeLatestInteraction(Interaction interaction) {
        if (interaction == null) return "No activity yet";
        Customer customer = dataStore.getCustomer(interaction.getCustomerId());
        String customerName = customer != null ? customer.getName() : "Unknown";
        return shortenText(formatEnumLabel(interaction.getType()) + " with " + customerName, 22);
    }

    private String shortenText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private void refreshAfterDataChange() {
        updateStats();
        refreshView(currentViewKey);
    }

    // ==================== DASHBOARD PANEL ====================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(248, 250, 252));
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setBackground(new Color(0, 0, 0, 0));
        cardsPanel.setLayout(new GridLayout(1, 4, 14, 0));
        cardsPanel.setPreferredSize(new Dimension(0, 92));

        List<Customer> customers = dataStore.getAllCustomers();
        int active = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.ACTIVE).count();
        int leads = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.LEAD).count();
        int prospects = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.PROSPECT).count();
        int inactive = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.INACTIVE).count();

        cardsPanel.add(createDashboardCard("Total Customers", customers.size() + "", PRIMARY));
        cardsPanel.add(createDashboardCard("Active Customers", active + "", GREEN));
        cardsPanel.add(createDashboardCard("Leads & Prospects", (leads + prospects) + "", YELLOW));
        cardsPanel.add(createDashboardCard("Inactive", inactive + "", TEXT_SECONDARY));

        JPanel recentPanel = new JPanel();
        recentPanel.setBackground(CARD_BG);
        recentPanel.setLayout(new BorderLayout(0, 10));
        recentPanel.setBorder(new EmptyBorder(22, 24, 24, 24));

        JLabel recentTitle = new JLabel("Recent Customers");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentTitle.setForeground(TEXT_PRIMARY);

        JPanel customerList = new JPanel();
        customerList.setBackground(CARD_BG);
        customerList.setLayout(new BoxLayout(customerList, BoxLayout.Y_AXIS));

        for (Customer c : customers) {
            customerList.add(createCustomerListItem(c));
            customerList.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(customerList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        recentPanel.add(recentTitle, BorderLayout.NORTH);
        recentPanel.add(scroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 0, 0, 0));
        bottomPanel.setLayout(new BorderLayout(0, 18));
        bottomPanel.add(cardsPanel, BorderLayout.NORTH);
        bottomPanel.add(recentPanel, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.CENTER);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.setBackground(new Color(248, 250, 252));

        return wrapper;
    }

    private JPanel createDashboardCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        JPanel textPanel = new JPanel();
        textPanel.setBackground(CARD_BG);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(titleLabel);

        card.add(textPanel, BorderLayout.NORTH);
        card.setPreferredSize(new Dimension(160, 92));

        return card;
    }

    private JPanel createCustomerListItem(Customer c) {
        JPanel item = new JPanel();
        item.setBackground(new Color(248, 250, 252));
        item.setLayout(new BorderLayout(15, 0));
        item.setBorder(new EmptyBorder(12, 18, 12, 18));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel nameLabel = new JLabel(c.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        JLabel emailLabel = new JLabel(c.getEmail());
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setBackground(new Color(0, 0, 0, 0));
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(nameLabel);
        textPanel.add(emailLabel);

        JPanel statusBadge = createStatusBadge(c.getStatus());
        statusBadge.setAlignmentY(Component.CENTER_ALIGNMENT);

        item.add(textPanel, BorderLayout.CENTER);
        item.add(statusBadge, BorderLayout.EAST);

        return item;
    }

    // ==================== CUSTOMERS PANEL ====================
    private JPanel createCustomersPanel() {
        JPanel main = new JPanel();
        main.setBackground(new Color(248, 250, 252));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel();
        header.setBackground(new Color(0, 0, 0, 0));
        header.setLayout(new BorderLayout(0, 0));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        titleLabel = new JLabel("Customers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(0, 0, 0, 0));
        searchPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 38));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterCustomers(); }
            public void removeUpdate(DocumentEvent e) { filterCustomers(); }
            public void changedUpdate(DocumentEvent e) { filterCustomers(); }
        });

        statusFilter = new JComboBox<>();
        statusFilter.addItem("All Statuses");
        statusFilter.addItem(Customer.CustomerStatus.LEAD);
        statusFilter.addItem(Customer.CustomerStatus.PROSPECT);
        statusFilter.addItem(Customer.CustomerStatus.ACTIVE);
        statusFilter.addItem(Customer.CustomerStatus.INACTIVE);
        statusFilter.setPreferredSize(new Dimension(140, 38));
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Object displayValue = value instanceof Customer.CustomerStatus
                    ? formatEnumLabel((Customer.CustomerStatus) value)
                    : value;
                return super.getListCellRendererComponent(list, displayValue, index, isSelected, cellHasFocus);
            }
        });
        statusFilter.addActionListener(e -> filterCustomers());

        JButton addBtn = createAddButton();

        searchPanel.add(searchField);
        searchPanel.add(statusFilter);
        searchPanel.add(addBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);

        main.add(header);
        main.add(Box.createVerticalStrut(15));
        main.add(createCustomerContent());

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(main, BorderLayout.CENTER);
        wrapper.setBackground(new Color(248, 250, 252));
        return wrapper;
    }

    private JButton createAddButton() {
        JButton addBtn = new JButton("+ Add Customer");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(PRIMARY);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(null);
        addBtn.setPreferredSize(new Dimension(130, 38));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddCustomerDialog());
        return addBtn;
    }

    private JPanel createCustomerContent() {
        JPanel content = new JPanel();
        content.setBackground(new Color(0, 0, 0, 0));
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));

        content.add(createCustomerTable());
        content.add(Box.createHorizontalStrut(15));
        content.add(createDetailPanel());

        return content;
    }

    private JPanel createCustomerTable() {
        JPanel tablePanel = new JPanel();
        tablePanel.setBackground(CARD_BG);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new LineBorder(BORDER, 1, true));

        String[] columns = {"Name", "Email", "Company", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        customerTable = new JTable(tableModel);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerTable.setRowHeight(48);
        customerTable.setShowGrid(false);
        customerTable.setSelectionBackground(new Color(239, 246, 255));
        customerTable.setSelectionForeground(PRIMARY);
        customerTable.setBorder(null);
        customerTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = customerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
                if (sel) {
                    c.setBackground(new Color(239, 246, 255));
                }
                return c;
            }
        };
        cellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        customerTable.setDefaultRenderer(Object.class, cellRenderer);

        customerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = customerTable.getSelectedRow();
                if (row >= 0) {
                    String email = (String) tableModel.getValueAt(row, 1);
                    selectedCustomer = dataStore.getAllCustomers().stream()
                        .filter(c -> c.getEmail().equals(email)).findFirst().orElse(null);
                    if (selectedCustomer != null) updateDetailPanel();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        loadCustomers();

        return tablePanel;
    }

    private JPanel createDetailPanel() {
        detailPanel = new JPanel();
        detailPanel.setBackground(CARD_BG);
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(new LineBorder(BORDER, 1, true));
        detailPanel.setPreferredSize(new Dimension(320, 0));

        updateDetailPanel();
        return detailPanel;
    }

    private void updateDetailPanel() {
        detailPanel.removeAll();

        if (selectedCustomer == null) {
            JLabel placeholder = new JLabel("Select a customer");
            placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            placeholder.setForeground(TEXT_SECONDARY);
            placeholder.setHorizontalAlignment(SwingConstants.CENTER);
            detailPanel.add(placeholder, BorderLayout.CENTER);
            detailPanel.revalidate();
            detailPanel.repaint();
            return;
        }

        JPanel content = new JPanel();
        content.setBackground(CARD_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel header = new JPanel();
        header.setBackground(CARD_BG);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 20, 15, 20));

        JLabel nameLabel = new JLabel(selectedCustomer.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel companyLabel = new JLabel(selectedCustomer.getCompany());
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        companyLabel.setForeground(TEXT_SECONDARY);
        companyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statusBadge = createStatusBadge(selectedCustomer.getStatus());

        header.add(nameLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(companyLabel);
        header.add(Box.createVerticalStrut(10));
        header.add(statusBadge);

        JPanel actions = new JPanel();
        actions.setBackground(CARD_BG);
        actions.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBorder(new EmptyBorder(0, 20, 10, 20));

        JButton editBtn = createActionButton("Edit", PRIMARY);
        editBtn.addActionListener(e -> showEditCustomerDialog());
        JButton deleteBtn = createActionButton("Delete", RED);
        deleteBtn.addActionListener(e -> deleteCustomer());
        JButton interactBtn = createActionButton("Log Interaction", GREEN);
        interactBtn.addActionListener(e -> showInteractionDialog());

        actions.add(editBtn);
        actions.add(deleteBtn);
        actions.add(interactBtn);

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(248, 250, 252));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        infoPanel.add(createInfoRow("Email", selectedCustomer.getEmail()));
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(createInfoRow("Phone", selectedCustomer.getPhone()));
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(createInfoRow("Status", selectedCustomer.getStatus().toString()));
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(createInfoRow("Created", selectedCustomer.getCreatedAt().toLocalDate().toString()));

        JPanel interactionSection = new JPanel();
        interactionSection.setBackground(CARD_BG);
        interactionSection.setLayout(new BorderLayout(0, 10));

        JLabel interactionTitle = new JLabel("Recent Interactions");
        interactionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        interactionTitle.setForeground(TEXT_PRIMARY);
        interactionTitle.setBorder(new EmptyBorder(15, 20, 10, 20));

        interactionListPanel = new JPanel();
        interactionListPanel.setBackground(CARD_BG);
        interactionListPanel.setLayout(new BoxLayout(interactionListPanel, BoxLayout.Y_AXIS));
        interactionListPanel.setBorder(new EmptyBorder(0, 20, 10, 20));

        List<Interaction> interactions = dataStore.getInteractions(selectedCustomer.getId());
        if (interactions.isEmpty()) {
            JLabel noInteract = new JLabel("No interactions yet");
            noInteract.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noInteract.setForeground(TEXT_SECONDARY);
            interactionListPanel.add(noInteract);
        } else {
            for (Interaction i : interactions.stream().limit(5).collect(Collectors.toList())) {
                interactionListPanel.add(createInteractionItem(i));
            }
        }

        JScrollPane interactScroll = new JScrollPane(interactionListPanel);
        interactScroll.setBorder(null);
        interactScroll.getViewport().setBackground(CARD_BG);
        interactScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        interactionSection.add(interactionTitle, BorderLayout.NORTH);
        interactionSection.add(interactScroll, BorderLayout.CENTER);

        content.add(header);
        content.add(actions);
        content.add(infoPanel);
        content.add(interactionSection);

        detailPanel.add(content, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel();
        row.setBackground(new Color(0, 0, 0, 0));
        row.setLayout(new BorderLayout(0, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);

        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "-");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        return row;
    }

    private JPanel createStatusBadge(Customer.CustomerStatus status) {
        JPanel badge = new JPanel();
        badge.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badge.setOpaque(false);

        Color bg;
        Color fg;
        switch (status) {
            case ACTIVE: bg = new Color(34, 197, 94, 30); fg = GREEN; break;
            case PROSPECT: bg = new Color(59, 130, 246, 30); fg = PRIMARY; break;
            case LEAD: bg = new Color(234, 179, 8, 30); fg = new Color(202, 138, 4); break;
            default: bg = new Color(148, 163, 184, 30); fg = TEXT_SECONDARY; break;
        }

        JLabel label = new JLabel("  " + status.toString() + "  ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setBackground(bg);
        label.setForeground(fg);
        label.setOpaque(true);
        label.setBorder(null);

        badge.add(label);
        return badge;
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(color);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(color, 1, true));
        btn.setPreferredSize(new Dimension(90, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createInteractionItem(Interaction i) {
        JPanel item = new JPanel();
        item.setBackground(new Color(248, 250, 252));
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel typeLabel = new JLabel(i.getType().toString());
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        typeLabel.setForeground(PRIMARY);

        JLabel descLabel = new JLabel(i.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_PRIMARY);

        JLabel dateLabel = new JLabel(i.getDate().toLocalDate().toString());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLabel.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setBackground(new Color(0, 0, 0, 0));
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(typeLabel);
        textPanel.add(descLabel);

        item.add(textPanel, BorderLayout.CENTER);
        item.add(dateLabel, BorderLayout.EAST);

        return item;
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);
        for (Customer c : dataStore.getAllCustomers()) {
            tableModel.addRow(new Object[]{c.getName(), c.getEmail(), c.getCompany(), c.getStatus()});
        }
    }

    private void filterCustomers() {
        if (tableModel == null || searchField == null || statusFilter == null) return;
        String query = searchField.getText();
        Object selectedStatusOption = statusFilter.getSelectedItem();
        Customer.CustomerStatus status = selectedStatusOption instanceof Customer.CustomerStatus
            ? (Customer.CustomerStatus) selectedStatusOption
            : null;

        List<Customer> filtered = dataStore.getAllCustomers();

        if (query != null && !query.trim().isEmpty()) {
            filtered = dataStore.searchCustomers(query);
        }

        if (status != null) {
            filtered = filtered.stream().filter(c -> c.getStatus() == status).collect(Collectors.toList());
        }

        tableModel.setRowCount(0);
        for (Customer c : filtered) {
            tableModel.addRow(new Object[]{c.getName(), c.getEmail(), c.getCompany(), c.getStatus()});
        }
    }

    // ==================== INTERACTIONS PANEL ====================
    private JPanel createInteractionsPanel() {
        JPanel main = new JPanel();
        main.setBackground(new Color(248, 250, 252));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Interactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);

        JPanel filterBar = new JPanel();
        filterBar.setBackground(new Color(0, 0, 0, 0));
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JComboBox<Object> typeFilter = new JComboBox<>();
        typeFilter.addItem("All Types");
        typeFilter.addItem(Interaction.InteractionType.CALL);
        typeFilter.addItem(Interaction.InteractionType.EMAIL);
        typeFilter.addItem(Interaction.InteractionType.MEETING);
        typeFilter.addItem(Interaction.InteractionType.NOTE);
        typeFilter.setPreferredSize(new Dimension(150, 35));
        typeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeFilter.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Object displayValue = value instanceof Interaction.InteractionType
                    ? formatEnumLabel((Interaction.InteractionType) value)
                    : value;
                return super.getListCellRendererComponent(list, displayValue, index, isSelected, cellHasFocus);
            }
        });

        JComboBox<Object> customerFilter = new JComboBox<>();
        customerFilter.addItem("All Customers");
        for (Customer c : dataStore.getAllCustomers()) {
            customerFilter.addItem(c);
        }
        customerFilter.setPreferredSize(new Dimension(180, 35));
        customerFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerFilter.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Object displayValue = value instanceof Customer ? ((Customer) value).getName() : value;
                return super.getListCellRendererComponent(list, displayValue, index, isSelected, cellHasFocus);
            }
        });

        filterBar.add(typeFilter);
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(customerFilter);

        main.add(filterBar);
        main.add(Box.createVerticalStrut(15));
        JPanel listHolder = new JPanel(new BorderLayout());
        listHolder.setBackground(CARD_BG);
        listHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        listHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        Runnable refreshInteractions = () -> {
            Object selectedTypeOption = typeFilter.getSelectedItem();
            Interaction.InteractionType selectedType = selectedTypeOption instanceof Interaction.InteractionType
                ? (Interaction.InteractionType) selectedTypeOption
                : null;
            Object selectedCustomerOption = customerFilter.getSelectedItem();
            String selectedCustomerId = selectedCustomerOption instanceof Customer
                ? ((Customer) selectedCustomerOption).getId()
                : null;

            listHolder.removeAll();
            listHolder.add(createInteractionsList(selectedType, selectedCustomerId), BorderLayout.CENTER);
            listHolder.revalidate();
            listHolder.repaint();
        };

        typeFilter.addActionListener(e -> refreshInteractions.run());
        customerFilter.addActionListener(e -> refreshInteractions.run());

        refreshInteractions.run();
        main.add(listHolder);

        return main;
    }

    private JPanel createInteractionsList() {
        return createInteractionsList(null, null);
    }

    private JPanel createInteractionsList(Interaction.InteractionType typeFilter, String customerIdFilter) {
        JPanel listPanel = new JPanel();
        listPanel.setBackground(CARD_BG);
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(new LineBorder(BORDER, 1, true));

        JPanel content = new JPanel();
        content.setBackground(CARD_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<Customer> customers = dataStore.getAllCustomers();
        boolean hasInteractions = false;

        for (Customer c : customers) {
            if (customerIdFilter != null && !customerIdFilter.equals(c.getId())) {
                continue;
            }

            List<Interaction> interactions = dataStore.getInteractions(c.getId());
            if (typeFilter != null) {
                interactions = interactions.stream()
                    .filter(i -> i.getType() == typeFilter)
                    .collect(Collectors.toList());
            }

            if (!interactions.isEmpty()) {
                hasInteractions = true;
                JLabel customerTitle = new JLabel(c.getName() + " (" + c.getEmail() + ")");
                customerTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
                customerTitle.setForeground(PRIMARY);
                customerTitle.setBorder(new EmptyBorder(10, 0, 5, 0));
                content.add(customerTitle);

                for (Interaction i : interactions) {
                    content.add(createInteractionRow(i));
                    content.add(Box.createVerticalStrut(5));
                }
                content.add(Box.createVerticalStrut(10));
            }
        }

        if (!hasInteractions) {
            JLabel noInteract = new JLabel("No interactions recorded yet");
            noInteract.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noInteract.setForeground(TEXT_SECONDARY);
            noInteract.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(noInteract);
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD_BG);

        listPanel.add(scroll, BorderLayout.CENTER);
        return listPanel;
    }

    private JPanel createInteractionRow(Interaction i) {
        JPanel row = new JPanel();
        row.setBackground(new Color(248, 250, 252));
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel typeIcon = new JLabel("[" + i.getType().toString() + "]");
        typeIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        typeIcon.setForeground(PRIMARY);
        typeIcon.setPreferredSize(new Dimension(80, 0));

        JLabel desc = new JLabel(i.getDescription());
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(TEXT_PRIMARY);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JLabel date = new JLabel(i.getDate().format(fmt));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        date.setForeground(TEXT_SECONDARY);

        row.add(typeIcon, BorderLayout.WEST);
        row.add(desc, BorderLayout.CENTER);
        row.add(date, BorderLayout.EAST);

        return row;
    }

    private String formatEnumLabel(Enum<?> value) {
        String text = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    // ==================== REPORTS PANEL ====================
    private JPanel createReportsPanel() {
        JPanel main = new JPanel();
        main.setBackground(new Color(248, 250, 252));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);

        JPanel cards = new JPanel();
        cards.setBackground(new Color(0, 0, 0, 0));
        cards.setLayout(new GridLayout(2, 2, 15, 15));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        cards.add(createReportCard("Customer Status Report", "Overview of all customers by status", PRIMARY));
        cards.add(createReportCard("Interaction Summary", "Summary of all interactions", GREEN));
        cards.add(createReportCard("Lead Conversion Report", "Track lead to customer conversion", YELLOW));
        cards.add(createReportCard("Activity Report", "Recent customer activity", PURPLE));

        main.add(cards);

        JPanel reportContent = new JPanel();
        reportContent.setBackground(CARD_BG);
        reportContent.setLayout(new BorderLayout());
        reportContent.setBorder(new EmptyBorder(20, 0, 0, 0));
        reportContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        JLabel reportTitle = new JLabel("Customer Status Distribution");
        reportTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reportTitle.setForeground(TEXT_PRIMARY);
        reportTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel stats = new JPanel();
        stats.setBackground(CARD_BG);
        stats.setLayout(new GridLayout(1, 4, 10, 10));

        List<Customer> customers = dataStore.getAllCustomers();
        int active = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.ACTIVE).count();
        int leads = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.LEAD).count();
        int prospects = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.PROSPECT).count();
        int inactive = (int) customers.stream().filter(c -> c.getStatus() == Customer.CustomerStatus.INACTIVE).count();

        stats.add(createStatCard("Active", active + "", GREEN));
        stats.add(createStatCard("Leads", leads + "", PRIMARY));
        stats.add(createStatCard("Prospects", prospects + "", YELLOW));
        stats.add(createStatCard("Inactive", inactive + "", TEXT_SECONDARY));

        reportContent.add(reportTitle, BorderLayout.NORTH);
        reportContent.add(stats, BorderLayout.CENTER);

        main.add(reportContent);

        return main;
    }

    private JPanel createReportCard(String title, String desc, Color color) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new LineBorder(BORDER, 1, true));

        JPanel header = new JPanel();
        header.setBackground(color);
        header.setPreferredSize(new Dimension(0, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);

        JPanel content = new JPanel();
        content.setBackground(CARD_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(descLabel);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel();
        card.setBackground(new Color(248, 250, 252));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(TEXT_SECONDARY);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(valueLabel);
        card.add(labelLabel);

        return card;
    }

    // ==================== SETTINGS PANEL ====================
    private JPanel createSettingsPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(248, 250, 252));

        JPanel content = new JPanel();
        content.setBackground(new Color(248, 250, 252));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        JPanel settingsCard = new JPanel();
        settingsCard.setBackground(CARD_BG);
        settingsCard.setLayout(new BoxLayout(settingsCard, BoxLayout.Y_AXIS));
        settingsCard.setBorder(new LineBorder(BORDER, 1, true));
        settingsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 141));
        settingsCard.setPreferredSize(new Dimension(0, 141));

        settingsCard.add(createSettingItem("Application Name", "CRM System", "General"));
        settingsCard.add(createSeparator());
        settingsCard.add(createSettingItem("Data Storage", "In-Memory", "Data"));

        JPanel aboutPanel = new JPanel();
        aboutPanel.setBackground(CARD_BG);
        aboutPanel.setLayout(new BorderLayout(0, 10));
        aboutPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        aboutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        aboutPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        aboutPanel.setPreferredSize(new Dimension(0, 210));

        JLabel aboutTitle = new JLabel("About");
        aboutTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        aboutTitle.setForeground(TEXT_PRIMARY);

        JTextArea aboutText = new JTextArea("Consumer Relationship Management System\n\n" +
            "A Java Swing application for managing customer relationships,\n" +
            "tracking interactions, and generating reports.\n\n" +
            "Built with Java Swing");
        aboutText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aboutText.setForeground(TEXT_SECONDARY);
        aboutText.setBackground(CARD_BG);
        aboutText.setEditable(false);
        aboutText.setLineWrap(true);
        aboutText.setWrapStyleWord(true);

        aboutPanel.add(aboutTitle, BorderLayout.NORTH);
        aboutPanel.add(aboutText, BorderLayout.CENTER);

        content.add(settingsCard);
        content.add(Box.createVerticalStrut(24));
        content.add(aboutPanel);
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(248, 250, 252));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        main.add(scrollPane, BorderLayout.CENTER);

        return main;
    }

    private JPanel createSettingItem(String label, String value, String category) {
        JPanel item = new JPanel();
        item.setBackground(CARD_BG);
        item.setLayout(new BorderLayout(20, 0));
        item.setBorder(new EmptyBorder(14, 24, 14, 24));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        item.setPreferredSize(new Dimension(0, 70));

        JLabel catLabel = new JLabel(category);
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        catLabel.setForeground(TEXT_SECONDARY);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(TEXT_SECONDARY);

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(CARD_BG);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        leftPanel.add(nameLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(catLabel);

        item.add(leftPanel, BorderLayout.WEST);
        item.add(valueLabel, BorderLayout.EAST);

        return item;
    }

    private JPanel createSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        return sep;
    }

    // ==================== DIALOGS ====================
    private void showAddCustomerDialog() {
        JDialog dialog = new JDialog(this, "Add Customer", true);
        dialog.setSize(450, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 40, 28, 40));

        JLabel title = new JLabel("New Customer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.setMaximumSize(new Dimension(320, 330));

        JTextField nameField = createTextField();
        JTextField emailField = createTextField();
        JTextField phoneField = createTextField();
        JTextField companyField = createTextField();
        JComboBox<Customer.CustomerStatus> statusCombo = new JComboBox<>(Customer.CustomerStatus.values());
        statusCombo.setPreferredSize(new Dimension(320, 40));
        statusCombo.setMaximumSize(new Dimension(320, 40));
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        form.add(createFormRow("Name", nameField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Email", emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Phone", phoneField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Company", companyField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Status", statusCombo));

        JPanel buttons = new JPanel();
        buttons.setBackground(Color.WHITE);
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(320, 48));
        buttons.setPreferredSize(new Dimension(320, 48));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        cancelBtn.setPreferredSize(new Dimension(110, 40));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save Customer");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(PRIMARY);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(null);
        saveBtn.setPreferredSize(new Dimension(140, 40));
        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Email are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Customer c = new Customer(nameField.getText().trim(), emailField.getText().trim(),
                phoneField.getText().trim(), companyField.getText().trim());
            c.setStatus((Customer.CustomerStatus) statusCombo.getSelectedItem());
            dataStore.addCustomer(c);
            refreshAfterDataChange();
            dialog.dispose();
        });

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        panel.add(title);
        panel.add(Box.createVerticalStrut(24));
        panel.add(form);
        panel.add(Box.createVerticalStrut(18));
        panel.add(buttons);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditCustomerDialog() {
        if (selectedCustomer == null) return;

        JDialog dialog = new JDialog(this, "Edit Customer", true);
        dialog.setSize(450, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 40, 28, 40));

        JLabel title = new JLabel("Edit Customer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.setMaximumSize(new Dimension(320, 330));

        JTextField nameField = createTextField();
        nameField.setText(selectedCustomer.getName());
        JTextField emailField = createTextField();
        emailField.setText(selectedCustomer.getEmail());
        JTextField phoneField = createTextField();
        phoneField.setText(selectedCustomer.getPhone());
        JTextField companyField = createTextField();
        companyField.setText(selectedCustomer.getCompany());
        JComboBox<Customer.CustomerStatus> statusCombo = new JComboBox<>(Customer.CustomerStatus.values());
        statusCombo.setPreferredSize(new Dimension(320, 40));
        statusCombo.setMaximumSize(new Dimension(320, 40));
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setSelectedItem(selectedCustomer.getStatus());

        form.add(createFormRow("Name", nameField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Email", emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Phone", phoneField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Company", companyField));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormRow("Status", statusCombo));

        JPanel buttons = new JPanel();
        buttons.setBackground(Color.WHITE);
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(320, 48));
        buttons.setPreferredSize(new Dimension(320, 48));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        cancelBtn.setPreferredSize(new Dimension(110, 40));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(PRIMARY);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(null);
        saveBtn.setPreferredSize(new Dimension(140, 40));
        saveBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Email are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedCustomer.setName(nameField.getText().trim());
            selectedCustomer.setEmail(emailField.getText().trim());
            selectedCustomer.setPhone(phoneField.getText().trim());
            selectedCustomer.setCompany(companyField.getText().trim());
            selectedCustomer.setStatus((Customer.CustomerStatus) statusCombo.getSelectedItem());
            dataStore.updateCustomer(selectedCustomer);
            updateDetailPanel();
            refreshAfterDataChange();
            dialog.dispose();
        });

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        panel.add(title);
        panel.add(Box.createVerticalStrut(24));
        panel.add(form);
        panel.add(Box.createVerticalStrut(18));
        panel.add(buttons);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteCustomer() {
        if (selectedCustomer == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + selectedCustomer.getName() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataStore.deleteCustomer(selectedCustomer.getId());
            selectedCustomer = null;
            updateDetailPanel();
            refreshAfterDataChange();
        }
    }

    private void showInteractionDialog() {
        if (selectedCustomer == null) return;

        JDialog dialog = new JDialog(this, "Log Interaction", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Log Interaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<Interaction.InteractionType> typeCombo = new JComboBox<>(Interaction.InteractionType.values());
        typeCombo.setMaximumSize(new Dimension(300, 40));
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextArea descArea = new JTextArea(5, 25);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        form.add(createFieldLabel("Type"));
        form.add(typeCombo);
        form.add(Box.createVerticalStrut(15));
        form.add(createFieldLabel("Description"));
        form.add(descArea);

        JPanel buttons = new JPanel();
        buttons.setBackground(Color.WHITE);
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(PRIMARY);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(null);
        saveBtn.setPreferredSize(new Dimension(100, 38));
        saveBtn.addActionListener(e -> {
            if (descArea.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Description is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Interaction i = new Interaction(selectedCustomer.getId(),
                (Interaction.InteractionType) typeCombo.getSelectedItem(), descArea.getText());
            dataStore.addInteraction(i);
            updateDetailPanel();
            refreshAfterDataChange();
            dialog.dispose();
        });

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(form);
        panel.add(Box.createVerticalStrut(25));
        panel.add(buttons);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(320, 40));
        field.setMaximumSize(new Dimension(320, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setBackground(Color.WHITE);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(320, 62));

        JLabel label = createFieldLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(label);
        row.add(Box.createVerticalStrut(5));
        row.add(field);
        return row;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
