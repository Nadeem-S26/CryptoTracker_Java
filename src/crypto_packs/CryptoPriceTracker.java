package crypto_packs;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CryptoPriceTracker extends JFrame {
    private CryptoTableModel tableModel;
    private JTable cryptoTable;
    private JTextField symbolField;
    private JButton addButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private JLabel lastUpdateLabel;
    private javax.swing.Timer refreshTimer;
    private List<String> trackedSymbols;
    
    // Color scheme
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private static final Color PANEL_COLOR = new Color(28, 28, 28);
    private static final Color ACCENT_COLOR = new Color(64, 224, 208);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SECONDARY_TEXT = new Color(180, 180, 180);
    private static final Color SUCCESS_COLOR = new Color(0, 200, 83);
    private static final Color DANGER_COLOR = new Color(234, 57, 67);
    
    public CryptoPriceTracker() {
        trackedSymbols = new ArrayList<>();
        initializeGUI();
        setupTimer();
        addDefaultCryptos();
    }
    
    private void initializeGUI() {
        setTitle("Crypto Price Tracker - Live Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        createHeaderPanel();
        createTablePanel();
        createControlPanel();
        createStatusPanel();
        setSize(900, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            // Use default
        }
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Crypto Price Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Real-time cryptocurrency price monitoring");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(SECONDARY_TEXT);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PANEL_COLOR);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Last update info
        lastUpdateLabel = new JLabel("Last updated: Never");
        lastUpdateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        lastUpdateLabel.setForeground(SECONDARY_TEXT);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(lastUpdateLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createTablePanel() {
        tableModel = new CryptoTableModel();
        cryptoTable = new JTable(tableModel);
        
        // Custom table styling
        cryptoTable.setBackground(PANEL_COLOR);
        cryptoTable.setForeground(TEXT_COLOR);
        cryptoTable.setSelectionBackground(ACCENT_COLOR.darker());
        cryptoTable.setSelectionForeground(Color.WHITE);
        cryptoTable.setGridColor(new Color(60, 60, 60));
        cryptoTable.setRowHeight(35);
        cryptoTable.setFont(new Font("Arial", Font.PLAIN, 13));
        cryptoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cryptoTable.setShowVerticalLines(false);
        
        // Header styling
        JTableHeader header = cryptoTable.getTableHeader();
        header.setBackground(PANEL_COLOR.brighter());
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_COLOR));
        
        // Custom renderers
        cryptoTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        cryptoTable.getColumnModel().getColumn(3).setCellRenderer(new PercentageRenderer());
        
        // Column widths
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
        
        // Add symbol section
        JLabel addLabel = new JLabel("Add Symbol:");
        addLabel.setForeground(TEXT_COLOR);
        addLabel.setFont(new Font("Arial", Font.BOLD, 13));
        
        symbolField = new JTextField(12);
        styleTextField(symbolField);
        symbolField.addActionListener(_e -> addCrypto());
        
        addButton = createStyledButton("Add Crypto", ACCENT_COLOR);
        addButton.addActionListener(_e -> addCrypto());
        
        refreshButton = createStyledButton("Refresh All", SUCCESS_COLOR);
        refreshButton.addActionListener(_e -> refreshAllData());
        
        // Auto-refresh indicator
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
        
        // Add a small separator above status
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(60, 60, 60));
        
        JPanel statusContainer = new JPanel(new BorderLayout());
        statusContainer.setBackground(BACKGROUND_COLOR);
        statusContainer.add(separator, BorderLayout.NORTH);
        statusContainer.add(statusPanel, BorderLayout.CENTER);
        
        add(statusContainer, BorderLayout.SOUTH);
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
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void setupTimer() {
        refreshTimer = new javax.swing.Timer(30000, _e -> refreshAllData());
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
    
    // Custom table cell renderer
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? PANEL_COLOR : PANEL_COLOR.darker());
                c.setForeground(TEXT_COLOR);
            }
            
            setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (column == 2) { // Price column
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
            
            return c;
        }
    }
    private class PercentageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? PANEL_COLOR : PANEL_COLOR.darker());
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
            
            if (isSelected) {
                c.setForeground(Color.WHITE);
            }
            
            setHorizontalAlignment(SwingConstants.RIGHT);
            return c;
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CryptoPriceTracker().setVisible(true);
        });
    }
}