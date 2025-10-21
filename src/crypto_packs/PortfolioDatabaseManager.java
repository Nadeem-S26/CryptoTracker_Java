package crypto_packs;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Portfolio Data Model
class PortfolioItem {
    private int id;
    private String symbol;
    private String name;
    private double quantity;
    private double buyPrice;
    private double currentPrice;
    
    public PortfolioItem(int id, String symbol, String name, double quantity, double buyPrice, double currentPrice) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.currentPrice = currentPrice;
    }
    
    public int getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public double getBuyPrice() { return buyPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double price) { this.currentPrice = price; }
    
    public double getTotalInvested() { return quantity * buyPrice; }
    public double getCurrentValue() { return quantity * currentPrice; }
    public double getProfitLoss() { return getCurrentValue() - getTotalInvested(); }
    public double getProfitLossPercentage() { 
        return ((getCurrentValue() - getTotalInvested()) / getTotalInvested()) * 100; 
    }
}

// Enhanced Database Manager with Portfolio
public class PortfolioDatabaseManager {
    
    public static void initializePortfolioTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS portfolio (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT NOT NULL," +
            "symbol VARCHAR(20) NOT NULL," +
            "name VARCHAR(100)," +
            "quantity DECIMAL(20, 8) NOT NULL," +
            "buy_price DECIMAL(20, 2) NOT NULL," +
            "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
            "INDEX idx_user_symbol (user_id, symbol)" +
            ")";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Portfolio table initialized successfully");
        } catch (SQLException e) {
            System.err.println("Portfolio table initialization error: " + e.getMessage());
        }
    }
    
    public static boolean addPortfolioItem(int userId, String symbol, String name, 
                                          double quantity, double buyPrice) {
        String query = "INSERT INTO portfolio (user_id, symbol, name, quantity, buy_price) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, symbol);
            pstmt.setString(3, name);
            pstmt.setDouble(4, quantity);
            pstmt.setDouble(5, buyPrice);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding portfolio item: " + e.getMessage());
            return false;
        }
    }
    
    public static List<PortfolioItem> getUserPortfolio(int userId) {
        List<PortfolioItem> portfolio = new ArrayList<>();
        String query = "SELECT id, symbol, name, quantity, buy_price FROM portfolio WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String symbol = rs.getString("symbol");
                String name = rs.getString("name");
                double quantity = rs.getDouble("quantity");
                double buyPrice = rs.getDouble("buy_price");

                // Fetch current price from API with error handling
                double currentPrice = buyPrice; // Default to buy price
                try {
                    System.out.println("Fetching price for portfolio item: " + symbol);
                    CryptoData cryptoData = CryptoAPIService.fetchCryptoData(symbol);
                        if (cryptoData != null && cryptoData.getPrice() > 0) {
                            currentPrice = cryptoData.getPrice();
                            System.out.println("Got price: $" + currentPrice);
                        } else {
                            System.err.println("Failed to get price for " + symbol + ", using buy price");
                        }
                } catch (Exception e) {
                    System.err.println("Error fetching price for " + symbol + ": " + e.getMessage());
                }

                portfolio.add(new PortfolioItem(id, symbol, name, quantity, buyPrice, currentPrice));
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("Error fetching portfolio: " + e.getMessage());
            e.printStackTrace();
        }

        return portfolio;
    }
    
    public static boolean deletePortfolioItem(int portfolioId) {
        String query = "DELETE FROM portfolio WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, portfolioId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting portfolio item: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean updateQuantity(int portfolioId, double newQuantity) {
        String query = "UPDATE portfolio SET quantity = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, newQuantity);
            pstmt.setInt(2, portfolioId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating quantity: " + e.getMessage());
            return false;
        }
    }
}

// Portfolio Table Model
class PortfolioTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Symbol", "Name", "Quantity", "Buy Price", 
                                          "Current Price", "Total Invested", "Current Value", 
                                          "Profit/Loss", "P/L %"};
    private List<PortfolioItem> portfolio;
    private DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private DecimalFormat quantityFormat = new DecimalFormat("#,##0.########");
    private DecimalFormat percentFormat = new DecimalFormat("#0.00");
    
    public PortfolioTableModel() {
        this.portfolio = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return portfolio.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PortfolioItem item = portfolio.get(rowIndex);
        switch (columnIndex) {
            case 0: return item.getSymbol();
            case 1: return item.getName();
            case 2: return quantityFormat.format(item.getQuantity());
            case 3: return "$" + priceFormat.format(item.getBuyPrice());
            case 4: return "$" + priceFormat.format(item.getCurrentPrice());
            case 5: return "$" + priceFormat.format(item.getTotalInvested());
            case 6: return "$" + priceFormat.format(item.getCurrentValue());
            case 7: return "$" + priceFormat.format(item.getProfitLoss());
            case 8: return percentFormat.format(item.getProfitLossPercentage()) + "%";
            default: return "";
        }
    }
    
    public void setPortfolio(List<PortfolioItem> portfolio) {
        this.portfolio = portfolio;
        fireTableDataStructured();
    }
    
    public PortfolioItem getItemAt(int row) {
        return portfolio.get(row);
    }
    
    public void updatePrices() {
        System.out.println("Updating prices for " + portfolio.size() + " items...");

        for (int i = 0; i < portfolio.size(); i++) {
        PortfolioItem item = portfolio.get(i);
            try {
                System.out.println("Fetching price for: " + item.getSymbol());
                CryptoData cryptoData = CryptoAPIService.fetchCryptoData(item.getSymbol());

                if (cryptoData != null && cryptoData.getPrice() > 0) {
                    item.setCurrentPrice(cryptoData.getPrice());
                    System.out.println("Updated " + item.getSymbol() + " to $" + cryptoData.getPrice());
                } else {
                    System.err.println("Could not update price for " + item.getSymbol());
                }

                // Add small delay between each update
                if (i < portfolio.size() - 1) {
                    Thread.sleep(2000); // 2 second delay
                }

            } catch (Exception e) {
                System.err.println("Error updating " + item.getSymbol() + ": " + e.getMessage());
            }
        }

        fireTableDataChanged();
        System.out.println("Price update complete");
    }
    
    private void fireTableDataStructured() {
        fireTableDataChanged();
    }
}

// Portfolio Frame
class PortfolioFrame extends JFrame {
    private PortfolioTableModel tableModel;
    private JTable portfolioTable;
    private JButton addButton;
    private JButton refreshButton;
    private JButton deleteButton;
    private JLabel totalInvestedLabel;
    private JLabel currentValueLabel;
    private JLabel totalProfitLossLabel;
    private JLabel statusLabel;
    
    private static final Color BG_COLOR = new Color(18, 18, 18);
    private static final Color PANEL_COLOR = new Color(28, 28, 28);
    private static final Color ACCENT_COLOR = new Color(64, 224, 208);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SECONDARY_TEXT = new Color(180, 180, 180);
    private static final Color SUCCESS_COLOR = new Color(0, 200, 83);
    private static final Color DANGER_COLOR = new Color(234, 57, 67);
    
    public PortfolioFrame() {
        setTitle("My Portfolio - " + UserSession.getInstance().getCurrentUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG_COLOR);
        
        initializeUI();
        loadPortfolio();
        
        setSize(1100, 600);
        setLocationRelativeTo(null);
    }
    
    private void initializeUI() {
        // Header
        createHeaderPanel();
        
        // Summary Cards
        createSummaryPanel();
        
        // Table
        createTablePanel();
        
        // Control Panel
        createControlPanel();
        
        // Status
        createStatusPanel();
    }
    
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLabel = new JLabel("My Crypto Portfolio");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_COLOR);
        
        JLabel subtitleLabel = new JLabel("Track your cryptocurrency investments");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(SECONDARY_TEXT);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PANEL_COLOR);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setBackground(BG_COLOR);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        totalInvestedLabel = createSummaryCard("Total Invested", "$0.00", ACCENT_COLOR);
        currentValueLabel = createSummaryCard("Current Value", "$0.00", SUCCESS_COLOR);
        totalProfitLossLabel = createSummaryCard("Total P/L", "$0.00", SUCCESS_COLOR);
        
        summaryPanel.add(createCardPanel("Total Invested", totalInvestedLabel, ACCENT_COLOR));
        summaryPanel.add(createCardPanel("Current Value", currentValueLabel, SUCCESS_COLOR));
        summaryPanel.add(createCardPanel("Total P/L", totalProfitLossLabel, SUCCESS_COLOR));
        
        add(summaryPanel, BorderLayout.NORTH);
    }
    
    private JPanel createCardPanel(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(SECONDARY_TEXT);
        
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_COLOR);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JLabel createSummaryCard(String title, String value, Color color) {
        JLabel label = new JLabel(value);
        label.setForeground(color);
        return label;
    }
    
    private void createTablePanel() {
        tableModel = new PortfolioTableModel();
        portfolioTable = new JTable(tableModel);
        
        portfolioTable.setBackground(PANEL_COLOR);
        portfolioTable.setForeground(TEXT_COLOR);
        portfolioTable.setSelectionBackground(ACCENT_COLOR.darker());
        portfolioTable.setSelectionForeground(Color.WHITE);
        portfolioTable.setGridColor(new Color(60, 60, 60));
        portfolioTable.setRowHeight(40);
        portfolioTable.setFont(new Font("Arial", Font.PLAIN, 13));
        portfolioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = portfolioTable.getTableHeader();
        header.setBackground(PANEL_COLOR.brighter());
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Custom renderer for profit/loss colors
        portfolioTable.setDefaultRenderer(Object.class, new PortfolioCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 25, 10, 25),
            BorderFactory.createLineBorder(new Color(60, 60, 60))
        ));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBackground(PANEL_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        
        addButton = createStyledButton("Add to Portfolio", ACCENT_COLOR);
        addButton.addActionListener(e -> showAddDialog());
        
        refreshButton = createStyledButton("Refresh Prices", SUCCESS_COLOR);
        refreshButton.addActionListener(e -> refreshPrices());
        
        deleteButton = createStyledButton("Remove Selected", DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteSelected());
        
        controlPanel.add(addButton);
        controlPanel.add(refreshButton);
        controlPanel.add(deleteButton);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void createStatusPanel() {
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(SECONDARY_TEXT);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
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
    
    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Add to Portfolio", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JTextField symbolField = createDialogField(formPanel, "Cryptocurrency Symbol (e.g., BTC):");
        JTextField quantityField = createDialogField(formPanel, "Quantity:");
        JTextField buyPriceField = createDialogField(formPanel, "Buy Price (USD):");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(PANEL_COLOR);
        
        JButton saveButton = createStyledButton("Add to Portfolio", ACCENT_COLOR);
        JButton cancelButton = createStyledButton("Cancel", new Color(80, 80, 80));
        
        saveButton.addActionListener(e -> {
            String symbol = symbolField.getText().trim().toUpperCase();
            String quantityStr = quantityField.getText().trim();
            String priceStr = buyPriceField.getText().trim();
            
            if (symbol.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double quantity = Double.parseDouble(quantityStr);
                double buyPrice = Double.parseDouble(priceStr);
                
                if (quantity <= 0 || buyPrice <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity and price must be positive", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Fetch crypto name
                CryptoData cryptoData = CryptoAPIService.fetchCryptoData(symbol);
                String name = cryptoData != null ? cryptoData.getName() : symbol;
                
                int userId = UserSession.getInstance().getUserId();
                if (PortfolioDatabaseManager.addPortfolioItem(userId, symbol, name, quantity, buyPrice)) {
                    JOptionPane.showMessageDialog(dialog, "Added to portfolio successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadPortfolio();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add to portfolio", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid quantity or price format", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        formPanel.add(buttonPanel);
        dialog.add(formPanel);
        dialog.getContentPane().setBackground(BG_COLOR);
        dialog.setVisible(true);
    }
    
    private JTextField createDialogField(JPanel parent, String label) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(TEXT_COLOR);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 13));
        
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBackground(BG_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        
        parent.add(fieldLabel);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
        parent.add(field);
        parent.add(Box.createRigidArea(new Dimension(0, 20)));
        
        return field;
    }
    
    private void loadPortfolio() {
        int userId = UserSession.getInstance().getUserId();
        List<PortfolioItem> portfolio = PortfolioDatabaseManager.getUserPortfolio(userId);
        tableModel.setPortfolio(portfolio);
        updateSummary(portfolio);
    }
    
    private void refreshPrices() {
        refreshButton.setEnabled(false);
        refreshButton.setText("Refreshing...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                System.out.println("Starting portfolio refresh...");
                tableModel.updatePrices();
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Reload portfolio from database to get fresh data
                    int userId = UserSession.getInstance().getUserId();
                    List<PortfolioItem> portfolio = PortfolioDatabaseManager.getUserPortfolio(userId);
                    tableModel.setPortfolio(portfolio);
                    updateSummary(portfolio);

                    JOptionPane.showMessageDialog(PortfolioFrame.this, 
                    "Prices refreshed successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PortfolioFrame.this, 
                    "Error refreshing prices: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("Refresh Prices");
                }
            }
        };
        worker.execute();
    }
    
    private void deleteSelected() {
        int selectedRow = portfolioTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove this item from your portfolio?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            PortfolioItem item = tableModel.getItemAt(selectedRow);
            if (PortfolioDatabaseManager.deletePortfolioItem(item.getId())) {
                loadPortfolio();
                JOptionPane.showMessageDialog(this, "Item removed successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove item", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateSummary(List<PortfolioItem> portfolio) {
        double totalInvested = 0;
        double currentValue = 0;
        
        for (PortfolioItem item : portfolio) {
            totalInvested += item.getTotalInvested();
            currentValue += item.getCurrentValue();
        }
        
        double profitLoss = currentValue - totalInvested;
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        totalInvestedLabel.setText("$" + df.format(totalInvested));
        currentValueLabel.setText("$" + df.format(currentValue));
        totalProfitLossLabel.setText("$" + df.format(profitLoss));
        
        if (profitLoss >= 0) {
            totalProfitLossLabel.setForeground(SUCCESS_COLOR);
        } else {
            totalProfitLossLabel.setForeground(DANGER_COLOR);
        }
    }
    
    private class PortfolioCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? PANEL_COLOR : PANEL_COLOR.darker());
                c.setForeground(TEXT_COLOR);
            }
            
            // Color profit/loss columns
            if ((column == 7 || column == 8) && value != null) {
                String valueStr = value.toString();
                if (valueStr.startsWith("-") || valueStr.startsWith("$-")) {
                    c.setForeground(DANGER_COLOR);
                } else {
                    c.setForeground(SUCCESS_COLOR);
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setHorizontalAlignment(column >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            
            return c;
        }
    }
}