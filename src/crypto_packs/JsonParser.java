package crypto_packs;

import java.util.*;
class JsonParser {
    public static Map<String, Object> parse(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return result;
        
        json = json.substring(1, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]\"[^\"]\")[^\"]$)");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String value = keyValue[1].trim();
                
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    result.put(key, value.substring(1, value.length() - 1));
                } else {
                    try {
                        result.put(key, Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }
}