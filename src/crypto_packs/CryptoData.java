package crypto_packs;
import java.time.LocalDateTime;
class CryptoData {
    private String symbol;
    private String name;
    private double price;
    private double change24h;
    private LocalDateTime lastUpdated;
    
    public CryptoData(String symbol, String name, double price, double change24h) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change24h = change24h;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getChange24h() { return change24h; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    
    // Setters
    public void setPrice(double price) { 
        this.price = price; 
        this.lastUpdated = LocalDateTime.now();
    }
    public void setChange24h(double change24h) { this.change24h = change24h; }
}

