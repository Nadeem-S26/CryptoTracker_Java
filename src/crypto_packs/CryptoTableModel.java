package crypto_packs;
import java.util.*;
import java.util.List;
import javax.swing.table.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
class CryptoTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Symbol", "Name", "Price (USD)", "24h Change (%)", "Last Updated"};
    private List<CryptoData> cryptoList;
    private DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private DecimalFormat percentFormat = new DecimalFormat("#0.00");
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public CryptoTableModel() {
        this.cryptoList = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return cryptoList.size();
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
        CryptoData crypto = cryptoList.get(rowIndex);
        switch (columnIndex) {
            case 0: return crypto.getSymbol();
            case 1: return crypto.getName();
            case 2: return "$" + priceFormat.format(crypto.getPrice());
            case 3: return percentFormat.format(crypto.getChange24h()) + "%";
            case 4: return crypto.getLastUpdated().format(timeFormat);
            default: return "";
        }
    }
    
    public void addCrypto(CryptoData crypto) {
        cryptoList.add(crypto);
        fireTableRowsInserted(cryptoList.size() - 1, cryptoList.size() - 1);
    }
    
    public void updateCrypto(int index, CryptoData crypto) {
        if (index >= 0 && index < cryptoList.size()) {
            cryptoList.set(index, crypto);
            fireTableRowsUpdated(index, index);
        }
    }
    
    public CryptoData getCrypto(int index) {
        return cryptoList.get(index);
    }
    
    public void clear() {
        int size = cryptoList.size();
        cryptoList.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
}