package crypto_packs;
import java.time.LocalDateTime;
class UserSession {
    private static UserSession instance;
    private String currentUsername;
    private int userId;
    private LocalDateTime loginTime;
    
    private UserSession() {}
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void login(String username, int userId) {
        this.currentUsername = username;
        this.userId = userId;
        this.loginTime = LocalDateTime.now();
    }
    
    public void logout() {
        this.currentUsername = null;
        this.userId = -1;
        this.loginTime = null;
    }
    
    public boolean isLoggedIn() {
        return currentUsername != null;
    }
    
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}