package crypto_packs;
import java.io.*;
import java.net.*;
import java.util.*;
class CryptoAPIService {
    private static final String API_URL = "https://api.coingecko.com/api/v3/simple/price";
    private static final Map<String, String> SYMBOL_TO_ID = new HashMap<>();
    
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
    }
    
    public static CryptoData fetchCryptoData(String symbol) {
        try {
            String coinId = SYMBOL_TO_ID.get(symbol.toUpperCase());
            if (coinId == null) {
                // Try using symbol as ID directly (lowercase)
                coinId = symbol.toLowerCase();
            }
            
            String urlStr = API_URL + "?ids=" + coinId + "&vs_currencies=usd&include_24hr_change=true";
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "CryptoPriceTracker/1.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("API returned status code: " + responseCode);
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            // Parse CoinGecko JSON response
            String jsonStr = response.toString();
            
            // Extract price and 24h change from CoinGecko format
            // Example: {"bitcoin":{"usd":43250.5,"usd_24h_change":2.3}}
            String pricePattern = "\"usd\":";
            String changePattern = "\"usd_24h_change\":";
            
            int priceIndex = jsonStr.indexOf(pricePattern);
            int changeIndex = jsonStr.indexOf(changePattern);
            
            if (priceIndex != -1) {
                priceIndex += pricePattern.length();
                int priceEnd = jsonStr.indexOf(",", priceIndex);
                if (priceEnd == -1) priceEnd = jsonStr.indexOf("}", priceIndex);
                
                String priceStr = jsonStr.substring(priceIndex, priceEnd);
                double price = Double.parseDouble(priceStr);
                
                double change24h = 0.0;
                if (changeIndex != -1) {
                    changeIndex += changePattern.length();
                    int changeEnd = jsonStr.indexOf("}", changeIndex);
                    String changeStr = jsonStr.substring(changeIndex, changeEnd);
                    try {
                        change24h = Double.parseDouble(changeStr);
                    } catch (NumberFormatException e) {
                        change24h = 0.0;
                    }
                }
                
                // Get proper name from mapping or use symbol
                String name = getCryptoName(symbol);
                return new CryptoData(symbol.toUpperCase(), name, price, change24h);
            }
        } catch (Exception e) {
            System.err.println("Error fetching data for " + symbol + ": " + e.getMessage());
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
        
        return names.getOrDefault(symbol.toUpperCase(), symbol.toUpperCase());
    }
}