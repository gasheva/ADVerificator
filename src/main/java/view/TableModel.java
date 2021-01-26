package view;

import javax.swing.table.DefaultTableModel;

public class TableModel extends DefaultTableModel{

    public TableModel(String[] columns) {
        for(String col: columns){
            addColumn(col);
        }
    }

    @Override
    public boolean isCellEditable(int arg0, int arg1) {
        return false;
    }
    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }
}

