package crypto_packs;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
// Database Manager for handling user authentication and registration
class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/cryptodb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Mysql@Root@123";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }
    
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "email VARCHAR(100)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createTableSQL);
            
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
    public static Integer authenticateUser(String username, String password) {
    String query = "SELECT id FROM users WHERE username = ? AND password = ?";
    
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int userId = rs.getInt("id");
            updateLastLogin(userId);
            return userId;
        }
        
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    private static void updateLastLogin(int userId) {
        String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    public static String getUserEmail(int userId) {
        String query = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching email: " + e.getMessage());
        }
        return null;
    }
    public static boolean registerUser(String username, String password, String email) {
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }
}
class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel;
    private static final Color BG_COLOR = new Color(18, 18, 18);
    private static final Color PANEL_COLOR = new Color(28, 28, 28);
    private static final Color ACCENT_COLOR = new Color(64, 224, 208);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ERROR_COLOR = new Color(234, 57, 67);
    
    public LoginFrame() {
        setTitle("Crypto Tracker - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeUI();
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Logo/Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG_COLOR);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("Crypto Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        passwordField.addActionListener(e -> performLogin());
        
        // Message label
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        formPanel.add(usernameLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(messageLabel);
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        loginButton = createStyledButton("Login", ACCENT_COLOR);
        loginButton.addActionListener(e -> performLogin());
        
        registerButton = createStyledButton("Create Account", new Color(80, 80, 80));
        registerButton.addActionListener(e -> openRegistrationFrame());
        
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(registerButton);
        
        // Add all to main panel
        mainPanel.add(titlePanel);
        mainPanel.add(formPanel);
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBackground(BG_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(300, 40));
        
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
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }
        
        Integer userId = DatabaseManager.authenticateUser(username, password);
        if (userId != null) {
        UserSession.getInstance().login(username, userId);
        messageLabel.setForeground(new Color(0, 200, 83));
        messageLabel.setText("Login successful!");
    
        SwingUtilities.invokeLater(() -> {
        new CryptoPriceTracker().setVisible(true);
        dispose();
        });
        } else {
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setText("Invalid username or password");
    }
}
    
    private void openRegistrationFrame() {
        new RegistrationFrame(this).setVisible(true);
        setVisible(false);
    }
}

// Registration Frame
class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel messageLabel;
    private LoginFrame loginFrame;
    
    private static final Color BG_COLOR = new Color(18, 18, 18);
    private static final Color PANEL_COLOR = new Color(28, 28, 28);
    private static final Color ACCENT_COLOR = new Color(64, 224, 208);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ERROR_COLOR = new Color(234, 57, 67);
    private static final Color SUCCESS_COLOR = new Color(0, 200, 83);
    
    public RegistrationFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        
        setTitle("Crypto Tracker - Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeUI();
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG_COLOR);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Join Crypto Tracker today");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        usernameField = createFormField("Username", formPanel);
        emailField = createFormField("Email", formPanel);
        passwordField = (JPasswordField) createFormField("Password", formPanel, true);
        confirmPasswordField = (JPasswordField) createFormField("Confirm Password", formPanel, true);
        
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(messageLabel);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        registerButton = createStyledButton("Register", ACCENT_COLOR);
        registerButton.addActionListener(e -> performRegistration());
        
        backButton = createStyledButton("Back to Login", new Color(80, 80, 80));
        backButton.addActionListener(e -> {
            loginFrame.setVisible(true);
            dispose();
        });
        
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(backButton);
        
        mainPanel.add(titlePanel);
        mainPanel.add(formPanel);
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private JTextField createFormField(String label, JPanel parent) {
        return createFormField(label, parent, false);
    }
    
    private JTextField createFormField(String label, JPanel parent, boolean isPassword) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(TEXT_COLOR);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JTextField field = isPassword ? new JPasswordField(20) : new JTextField(20);
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
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(field);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        return field;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(300, 40));
        
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
    
    private void performRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setForeground(ERROR_COLOR);
            messageLabel.setText("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setForeground(ERROR_COLOR);
            messageLabel.setText("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            messageLabel.setForeground(ERROR_COLOR);
            messageLabel.setText("Password must be at least 6 characters");
            return;
        }
        
        if (DatabaseManager.registerUser(username, password, email)) {
            messageLabel.setForeground(SUCCESS_COLOR);
            messageLabel.setText("Registration successful! Redirecting...");
            
            Timer timer = new Timer(1500, e -> {
                loginFrame.setVisible(true);
                dispose();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            messageLabel.setForeground(ERROR_COLOR);
            messageLabel.setText("Username already exists");
        }
    }
}
// Main Application Entry Point
public class UserLogin {
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.initializeDatabase();
        
        // Launch login frame
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}