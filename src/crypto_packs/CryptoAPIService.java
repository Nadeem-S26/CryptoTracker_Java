package crypto_packs;
import java.io.*;
import java.net.*;
import java.util.*;

class CryptoAPIService {
    private static final String API_URL = "https://api.coingecko.com/api/v3/simple/price";
    private static final Map<String, String> SYMBOL_TO_ID = new HashMap<>();
    private static long lastRequestTime = 0;
    private static final long REQUEST_DELAY = 2000; // 2 seconds between requests
    
    static {
        // Common cryptocurrency mappings (symbol -> CoinGecko ID)
        SYMBOL_TO_ID.put("BTC", "bitcoin");
        SYMBOL_TO_ID.put("ETH", "ethereum");
        SYMBOL_TO_ID.put("LTC", "litecoin");
        SYMBOL_TO_ID.put("XRP", "ripple");
        SYMBOL_TO_ID.put("ADA", "cardano");
        SYMBOL_TO_ID.put("DOT", "polkadot");
        SYMBOL_TO_ID.put("LINK", "chainlink");
        SYMBOL_TO_ID.put("BCH", "bitcoin-cash");
        SYMBOL_TO_ID.put("XLM", "stellar");
        SYMBOL_TO_ID.put("DOGE", "dogecoin");
        SYMBOL_TO_ID.put("UNI", "uniswap");
        SYMBOL_TO_ID.put("AVAX", "avalanche-2");
        SYMBOL_TO_ID.put("MATIC", "matic-network");
        SYMBOL_TO_ID.put("ATOM", "cosmos");
        SYMBOL_TO_ID.put("SOL", "solana");
        SYMBOL_TO_ID.put("BNB", "binancecoin");
        SYMBOL_TO_ID.put("SHIB", "shiba-inu");
        SYMBOL_TO_ID.put("TRX", "tron");
        SYMBOL_TO_ID.put("DAI", "dai");
        SYMBOL_TO_ID.put("USDT", "tether");
    }
    
    // Add rate limiting to prevent 429 errors
    private static void enforceRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        
        if (timeSinceLastRequest < REQUEST_DELAY) {
            try {
                Thread.sleep(REQUEST_DELAY - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }
    
    public static CryptoData fetchCryptoData(String symbol) {
        enforceRateLimit(); // Add delay between requests
        
        try {
            String coinId = SYMBOL_TO_ID.get(symbol.toUpperCase());
            if (coinId == null) {
                coinId = symbol.toLowerCase();
            }
            
            String urlStr = API_URL + "?ids=" + coinId + "&vs_currencies=usd&include_24hr_change=true";
            
            System.out.println("Fetching: " + urlStr); // Debug log
            
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode); // Debug log
            
            if (responseCode == 429) {
                System.err.println("Rate limit exceeded. Waiting 60 seconds...");
                Thread.sleep(60000); // Wait 1 minute
                return fetchCryptoData(symbol); // Retry
            }
            
            if (responseCode != 200) {
                System.err.println("API returned status code: " + responseCode);
                return null;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String jsonStr = response.toString();
            System.out.println("API Response: " + jsonStr); // Debug log
            
            // Parse JSON manually
            if (jsonStr.equals("{}") || !jsonStr.contains("usd")) {
                System.err.println("No data found for: " + symbol);
                return null;
            }
            
            // Extract price
            String pricePattern = "\"usd\":";
            int priceIndex = jsonStr.indexOf(pricePattern);
            
            if (priceIndex != -1) {
                priceIndex += pricePattern.length();
                int priceEnd = jsonStr.indexOf(",", priceIndex);
                if (priceEnd == -1) priceEnd = jsonStr.indexOf("}", priceIndex);
                
                String priceStr = jsonStr.substring(priceIndex, priceEnd).trim();
                double price = Double.parseDouble(priceStr);
                
                // Extract 24h change
                double change24h = 0.0;
                String changePattern = "\"usd_24h_change\":";
                int changeIndex = jsonStr.indexOf(changePattern);
                
                if (changeIndex != -1) {
                    changeIndex += changePattern.length();
                    int changeEnd = jsonStr.indexOf("}", changeIndex);
                    String changeStr = jsonStr.substring(changeIndex, changeEnd).trim();
                    try {
                        change24h = Double.parseDouble(changeStr);
                    } catch (NumberFormatException e) {
                        change24h = 0.0;
                    }
                }
                
                String name = getCryptoName(symbol);
                System.out.println("Successfully fetched: " + symbol + " = $" + price);
                return new CryptoData(symbol.toUpperCase(), name, price, change24h);
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching data for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    private static String getCryptoName(String symbol) {
        Map<String, String> names = new HashMap<>();
        names.put("BTC", "Bitcoin");
        names.put("ETH", "Ethereum");
        names.put("LTC", "Litecoin");
        names.put("XRP", "XRP");
        names.put("ADA", "Cardano");
        names.put("DOT", "Polkadot");
        names.put("LINK", "Chainlink");
        names.put("BCH", "Bitcoin Cash");
        names.put("XLM", "Stellar");
        names.put("DOGE", "Dogecoin");
        names.put("UNI", "Uniswap");
        names.put("AVAX", "Avalanche");
        names.put("MATIC", "Polygon");
        names.put("ATOM", "Cosmos");
        names.put("SOL", "Solana");
        names.put("BNB", "Binance Coin");
        names.put("SHIB", "Shiba Inu");
        names.put("TRX", "Tron");
        names.put("DAI", "Dai");
        names.put("USDT", "Tether");
        
        return names.getOrDefault(symbol.toUpperCase(), symbol.toUpperCase());
    }
}