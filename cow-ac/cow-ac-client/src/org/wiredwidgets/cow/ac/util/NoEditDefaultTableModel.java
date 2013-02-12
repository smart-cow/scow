package org.wiredwidgets.cow.ac.util;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import org.wiredwidgets.cow.ac.client.ui.TaskDetailsPanel;

/**
 * Creates a default table model except with an override on the editable flag (for all cells).
 * Why the default model doesn't have a toggle for this is beyond me.
 * @author RYANMILLER
 * @see TaskDetailsPanel
 */
public class NoEditDefaultTableModel extends DefaultTableModel {
    
    public NoEditDefaultTableModel() {
        super();
    }

    public NoEditDefaultTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public NoEditDefaultTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public NoEditDefaultTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    public NoEditDefaultTableModel(Vector columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public NoEditDefaultTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
       //all cells false
       return false;
    }
}
