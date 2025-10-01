package crypto_packs;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
class PercentageRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (column == 3 && value != null) { // 24h Change column
            String valueStr = value.toString();
            if (valueStr.startsWith("-")) {
                c.setForeground(Color.RED);
            } else {
                c.setForeground(new Color(0, 150, 0));
            }
        } else {
            c.setForeground(Color.BLACK);
        }
        
        if (isSelected) {
            c.setForeground(Color.WHITE);
        }
        
        return c;
    }
}