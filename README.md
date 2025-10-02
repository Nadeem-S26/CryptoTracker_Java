# Crypto Price Tracker

A real-time cryptocurrency price monitoring application with user authentication, built with Java Swing and MySQL.
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
## Features

### User Authentication
- Secure user registration and login system
- MySQL database integration with JDBC
- Modern dark-themed UI for login/registration

### Price Tracking
- Real-time cryptocurrency price monitoring
- Support for major cryptocurrencies (BTC, ETH, SOL, ADA, DOGE, and more)
- 24-hour price change percentage with color indicators
- Auto-refresh every 30 seconds
- Manual refresh option
- Add custom cryptocurrencies by symbol

### User Interface
- Modern dark theme with professional aesthetics
- Color-coded price changes (green for gains, red for losses)
- Responsive table design with alternating row colors
- Real-time status updates
- Last update timestamp display

### Login Screen
Clean and modern authentication interface with dark theme.

### Main Dashboard
Real-time cryptocurrency prices with automatic updates.

## Technology Stack

- **Language**: Java 17+
- **GUI Framework**: Java Swing
- **Database**: MySQL 8.0+
- **API**: CoinGecko API (Free tier)
- **JDBC Driver**: MySQL Connector/J

## Prerequisites

Before running the application, ensure you have:

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   java -version
   ```

2. **MySQL Server 8.0 or higher**
   ```bash
   mysql --version
   ```

3. **MySQL Connector/J** (JDBC Driver)
   - Download from [MySQL Official Site](https://dev.mysql.com/downloads/connector/j/)

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/crypto-price-tracker.git
cd crypto-price-tracker
```

### 2. Database Setup

Create the database:
```sql
CREATE DATABASE crypto_db;
USE crypto_db;
```

The application will automatically create the `users` table on first run.

### 3. Configure Database Connection

Edit `DatabaseManager.java` and update your MySQL credentials:
```java
private static final String URL = "jdbc:mysql://localhost:3306/crypto_db";
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### 4. Add MySQL Connector JAR

#### Using IDE (Eclipse/IntelliJ)
- **Eclipse**: Right-click project → Build Path → Add External JARs
- **IntelliJ IDEA**: File → Project Structure → Libraries → Add → Select JAR

#### Using Command Line
Place `mysql-connector-java.jar` in your project directory.

## Running the Application

### Using IDE
1. Open the project in your IDE
2. Run `CryptoLoginSystem.java`

### Using Command Line

**Compile:**
```bash
javac -cp .:mysql-connector-java.jar *.java
```

**Run:**
```bash
# Linux/Mac
java -cp .:mysql-connector-java.jar CryptoLoginSystem

# Windows
java -cp .;mysql-connector-java.jar CryptoLoginSystem
```

## Usage

### First Time Setup
1. Launch the application
2. Click "Create Account"
3. Fill in registration details
4. Login with your credentials

### Tracking Cryptocurrencies
1. Default cryptocurrencies (BTC, ETH, SOL, ADA, DOGE) load automatically
2. Add custom crypto by entering symbol (e.g., "LINK", "MATIC") and clicking "Add Crypto"
3. Prices auto-refresh every 30 seconds
4. Click "Refresh All" for manual update

## Project Structure

```
crypto-price-tracker/
├── CryptoLoginSystem.java      # Main entry point
├── LoginFrame.java              # Login UI
├── RegistrationFrame.java       # Registration UI
├── DatabaseManager.java         # Database operations
├── CryptoPriceTracker.java     # Main tracker UI
├── CryptoData.java             # Data model
├── CryptoAPIService.java       # API integration
├── CryptoTableModel.java       # Table data model
├── PercentageRenderer.java     # Custom cell renderer
└── README.md
```

## API Integration

This application uses the **CoinGecko API** (free tier):
- **Endpoint**: `https://api.coingecko.com/api/v3/simple/price`
- **Rate Limit**: 50 calls/minute (free tier)
- **No API key required**

### Supported Cryptocurrencies
BTC, ETH, LTC, XRP, ADA, DOT, LINK, BCH, XLM, DOGE, UNI, AVAX, MATIC, ATOM, SOL, and more.

## Security Considerations

⚠️ **Important**: This is a demonstration project. For production use:

1. **Password Security**
   - Implement password hashing (BCrypt, PBKDF2, or Argon2)
   - Add salt to passwords
   - Never store plain text passwords

2. **Database Security**
   - Use SSL/TLS for database connections
   - Implement proper user roles and permissions
   - Regular security audits

3. **API Security**
   - Implement rate limiting
   - Add error handling for API failures
   - Consider using API keys for higher limits

## Troubleshooting

### Common Issues

**1. MySQL Connection Error**
```
SQLException: Access denied for user
```
**Solution**: Verify database credentials in `DatabaseManager.java`

**2. ClassNotFoundException: com.mysql.cj.jdbc.Driver**
```
java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver
```
**Solution**: Ensure MySQL Connector JAR is in classpath

**3. API Rate Limit Exceeded**
```
API returned status code: 429
```
**Solution**: Wait 60 seconds or reduce refresh frequency

**4. Table Already Exists Error**
**Solution**: This is normal - the app checks if table exists before creating

## Future Enhancements

- [ ] Password encryption (BCrypt)
- [ ] Portfolio tracking with buy/sell history
- [ ] Price alerts and notifications
- [ ] Charts and graphs for price history
- [ ] Export data to CSV/Excel
- [ ] Multiple currency support (EUR, GBP, etc.)
- [ ] Favorites/watchlist feature
- [ ] Dark/Light theme toggle

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [CoinGecko API](https://www.coingecko.com/en/api) for cryptocurrency data
- Java Swing for GUI framework
- MySQL for database management



**Disclaimer**: This application is for educational purposes only. Cryptocurrency investments carry risk. Always do your own research before investing.
