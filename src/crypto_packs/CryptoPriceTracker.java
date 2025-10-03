package crypto_packs;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CryptoPriceTracker extends JFrame {
    private CryptoTableModel tableModel;
    private JTable cryptoTable;
    private JTextField symbolField;
    private JButton addButton;
    private JButton refreshButton;
    private JButton logoutButton;
    private JButton profileButton;
    private JLabel statusLabel;
    private JLabel userLabel;
    private JLabel lastUpdateLabel;
    private javax.swing.Timer refreshTimer;
    private java.util.List<String> trackedSymbols;
    
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private static final Color PANEL_COLOR = new Color(28, 28, 28);
    private static final Color ACCENT_COLOR = new Color(64, 224, 208);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SECONDARY_TEXT = new Color(180, 180, 180);
    private static final Color SUCCESS_COLOR = new Color(0, 200, 83);
    private static final Color DANGER_COLOR = new Color(234, 57, 67);
    
    public CryptoPriceTracker() {
        trackedSymbols = new java.util.ArrayList<>();
        initializeGUI();
        setupTimer();
        addDefaultCryptos();
    }
    
    private void initializeGUI() {
        setTitle("Crypto Price Tracker - " + UserSession.getInstance().getCurrentUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        createHeaderPanel();
        createTablePanel();
        createControlPanel();
        createStatusPanel();
        
        setSize(950, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Left side - Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PANEL_COLOR);
        
        JLabel titleLabel = new JLabel("Crypto Price Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        
        JLabel subtitleLabel = new JLabel("Real-time cryptocurrency price monitoring");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(SECONDARY_TEXT);
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Right side - User info and buttons
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setBackground(PANEL_COLOR);
        
        // User info
        UserSession session = UserSession.getInstance();
        String loginTimeStr = session.getLoginTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        userLabel = new JLabel("Logged in as: " + session.getCurrentUsername() + " (" + loginTimeStr + ")");
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));
        userLabel.setForeground(ACCENT_COLOR);
        
        // Profile button
        profileButton = createHeaderButton("View Profile");
        profileButton.addActionListener(e -> showProfileDialog());
        
        // Logout button
        logoutButton = createHeaderButton("Logout");
        logoutButton.setBackground(DANGER_COLOR);
        logoutButton.addActionListener(e -> performLogout());
        
        userPanel.add(userLabel);
        userPanel.add(profileButton);
        userPanel.add(logoutButton);
        
        // Last update label
        lastUpdateLabel = new JLabel("Last updated: Never");
        lastUpdateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        lastUpdateLabel.setForeground(SECONDARY_TEXT);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(PANEL_COLOR);
        bottomPanel.add(lastUpdateLabel);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(PANEL_COLOR);
        rightPanel.add(userPanel, BorderLayout.NORTH);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        // In createHeaderPanel() method, add this button:
        JButton portfolioButton = createHeaderButton("My Portfolio");
        portfolioButton.addActionListener(e -> openPortfolio());
        userPanel.add(portfolioButton);

    }
    // Add this method:
    private void openPortfolio() {
        new PortfolioFrame().setVisible(true);
    }
    
    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PANEL_COLOR.brighter());
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));
        
        button.addMouseListener(new MouseAdapter() {
            Color originalBg = button.getBackground();
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });
        
        return button;
    }
    
    private void createTablePanel() {
        tableModel = new CryptoTableModel();
        cryptoTable = new JTable(tableModel);
        
        cryptoTable.setBackground(PANEL_COLOR);
        cryptoTable.setForeground(TEXT_COLOR);
        cryptoTable.setSelectionBackground(ACCENT_COLOR.darker());
        cryptoTable.setSelectionForeground(Color.WHITE);
        cryptoTable.setGridColor(new Color(60, 60, 60));
        cryptoTable.setRowHeight(35);
        cryptoTable.setFont(new Font("Arial", Font.PLAIN, 13));
        cryptoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cryptoTable.setShowVerticalLines(false);
        
        JTableHeader header = cryptoTable.getTableHeader();
        header.setBackground(PANEL_COLOR.brighter());
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_COLOR));
        
        cryptoTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        
        cryptoTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        cryptoTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        cryptoTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        cryptoTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        cryptoTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(cryptoTable);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 20, 10, 20),
            BorderFactory.createLineBorder(new Color(60, 60, 60))
        ));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(PANEL_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel addLabel = new JLabel("Add Symbol:");
        addLabel.setForeground(TEXT_COLOR);
        addLabel.setFont(new Font("Arial", Font.BOLD, 13));
        
        symbolField = new JTextField(12);
        styleTextField(symbolField);
        symbolField.addActionListener(e -> addCrypto());
        
        addButton = createStyledButton("Add Crypto", ACCENT_COLOR);
        addButton.addActionListener(e -> addCrypto());
        
        refreshButton = createStyledButton("Refresh All", SUCCESS_COLOR);
        refreshButton.addActionListener(e -> refreshAllData());
        
        JLabel autoRefreshLabel = new JLabel("Auto-refresh: 30s");
        autoRefreshLabel.setForeground(SECONDARY_TEXT);
        autoRefreshLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        controlPanel.add(addLabel);
        controlPanel.add(symbolField);
        controlPanel.add(addButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(refreshButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(autoRefreshLabel);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(SECONDARY_TEXT);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
    }
    
    private void styleTextField(JTextField field) {
        field.setBackground(BACKGROUND_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void showProfileDialog() {
        UserSession session = UserSession.getInstance();
        String email = DatabaseManager.getUserEmail(session.getUserId());
        
        JDialog profileDialog = new JDialog(this, "User Profile", true);
        profileDialog.setSize(400, 300);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(PANEL_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel titleLabel = new JLabel("Profile Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        addProfileField(contentPanel, "Username:", session.getCurrentUsername());
        addProfileField(contentPanel, "User ID:", String.valueOf(session.getUserId()));
        addProfileField(contentPanel, "Email:", email != null ? email : "Not provided");
        addProfileField(contentPanel, "Login Time:", 
            session.getLoginTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
        
        JButton closeButton = createStyledButton("Close", ACCENT_COLOR);
        closeButton.addActionListener(e -> profileDialog.dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(closeButton);
        
        profileDialog.add(contentPanel);
        profileDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        profileDialog.setVisible(true);
    }
    
    private void addProfileField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.setBackground(PANEL_COLOR);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 13));
        labelComp.setForeground(SECONDARY_TEXT);
        labelComp.setPreferredSize(new Dimension(100, 20));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 13));
        valueComp.setForeground(TEXT_COLOR);
        
        fieldPanel.add(labelComp);
        fieldPanel.add(valueComp);
        
        panel.add(fieldPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            refreshTimer.stop();
            UserSession.getInstance().logout();
            
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }
    
    private void setupTimer() {
        refreshTimer = new javax.swing.Timer(30000, e -> refreshAllData());
        refreshTimer.start();
    }
    
    private void addDefaultCryptos() {
        String[] defaultSymbols = {"BTC", "ETH", "SOL", "ADA", "DOGE"};
        for (String symbol : defaultSymbols) {
            addCryptoSymbol(symbol);
        }
    }
    
    private void addCrypto() {
        String symbol = symbolField.getText().trim().toUpperCase();
        if (!symbol.isEmpty() && !trackedSymbols.contains(symbol)) {
            addCryptoSymbol(symbol);
            symbolField.setText("");
        }
    }
    
    private void addCryptoSymbol(String symbol) {
        statusLabel.setText("Fetching data for " + symbol + "...");
        
        SwingWorker<CryptoData, Void> worker = new SwingWorker<CryptoData, Void>() {
            @Override
            protected CryptoData doInBackground() {
                return CryptoAPIService.fetchCryptoData(symbol);
            }
            
            @Override
            protected void done() {
                try {
                    CryptoData crypto = get();
                    if (crypto != null) {
                        tableModel.addCrypto(crypto);
                        trackedSymbols.add(symbol);
                        statusLabel.setText("Added " + symbol + " successfully");
                        updateLastUpdateTime();
                    } else {
                        statusLabel.setText("Failed to fetch data for " + symbol);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error adding " + symbol + ": " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void refreshAllData() {
        if (trackedSymbols.isEmpty()) return;
        
        statusLabel.setText("Refreshing all data...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < trackedSymbols.size(); i++) {
                    String symbol = trackedSymbols.get(i);
                    CryptoData newData = CryptoAPIService.fetchCryptoData(symbol);
                    if (newData != null) {
                        final int index = i;
                        SwingUtilities.invokeLater(() -> tableModel.updateCrypto(index, newData));
                    }
                }
                return null;
            }
            
            @Override
            protected void done() {
                statusLabel.setText("All data refreshed successfully");
                updateLastUpdateTime();
            }
        };
        worker.execute();
    }
    
    private void updateLastUpdateTime() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss"));
        lastUpdateLabel.setText("Last updated: " + timestamp);
    }
    
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? PANEL_COLOR : PANEL_COLOR.darker());
                c.setForeground(TEXT_COLOR);
            }
            
            if (column == 3 && value != null) {
                String valueStr = value.toString();
                if (valueStr.startsWith("-")) {
                    c.setForeground(DANGER_COLOR);
                } else {
                    c.setForeground(SUCCESS_COLOR);
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (column == 2 || column == 3) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
            
            return c;
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CryptoPriceTracker().setVisible(true);
        });
    }
}