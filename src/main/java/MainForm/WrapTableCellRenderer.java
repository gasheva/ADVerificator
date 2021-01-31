package MainForm;


import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class WrapTableCellRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
    public WrapTableCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(false);
        //setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        setSize(table.getColumnModel().getColumn(column).getWidth(),getPreferredSize().height);
        if (isSelected){
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        }
        else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        if (table.getRowHeight(row) != getPreferredSize().height) {
            table.setRowHeight(row, getPreferredSize().height);
        }
        return this;
    }

}
