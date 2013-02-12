package org.wiredwidgets.cow.ac.workflowsummary.table;

import org.wiredwidgets.cow.ac.workflowsummary.model.ChicletTableModel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser.CompletionState;

/**
 * A table designed specifically to display {@link ChicletTableModel} data by
 * coloring the background of each cell based on the value of its {@link CompletionState}.
 * (Uses a custom cell renderer). The table will automatically add/remove
 * columns as the data model is updated.
 * <p/>
 * The rendering of the tables row headers is delegated to another class by
 * implementing from {@link RowHeaderlessTable}. It is up to the user of this
 * class to do something with that ability.
 *
 * @author MJRUSSELL, documented by RYANMILLER
 */
public class ChicletTable extends RowHeaderlessTable {

    /**
     * Creates a
     * <code>ChicletTable</code>.
     */
    public ChicletTable() {
        setAutoCreateColumnsFromModel(true);
        setDefaultRenderer(CompletionState.class, new ChicletTableCellRenderer());
        setGridColor(Color.black);
    }

    @Override
    public String getRowName(int row) {
        // the responsibility for the row name will come from the ChicletTableModel
        // backing the table
        return ((ChicletTableModel) dataModel).getRowName(row);
    }

    /**
     * Use {@link #setModel(org.wiredwidgets.cow.ac.workflowsummary.table.ChicletTableModel)}
     * instead. Other models other than ChicletTableModel are ignored.
     */
    @Override
    public void setModel(TableModel dataModel) {
        if (dataModel instanceof ChicletTableModel) {
            super.setModel(dataModel);
        }
    }

    /**
     * Sets the underlying data model for this table.
     *
     * @param dataModel The model to use for this table.
     */
    public void setModel(ChicletTableModel dataModel) {
        super.setModel(dataModel);
    }

    /**
     * Returns the model being used for this table.
     *
     * @return the model being used for this table.
     */
    public ChicletTableModel getChicletTableModel() {
        return (ChicletTableModel) dataModel;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return CompletionState.class;
    }

    /**
     * A special renderer for the table so that {@link CompletionState}s are
     * used to drive background coloring of the cells.
     */
    private static class ChicletTableCellRenderer extends DefaultTableCellRenderer {

        static final Logger log = Logger.getLogger(ChicletTable.class);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                if (value instanceof CompletionState) {
                    CompletionState state = (CompletionState) value;
                    setBackground(state.getCompletionColor());
                } else {
                    log.debug("Value to be rendered in a cell was not an "
                            + "instance of CompletionState. Check the model "
                            + "contents. Value was: " + value);
                }
            } else {
                //There was no completion state, so the group/user
                //is not in the workflow
                setBackground(Color.BLACK);
            }
            return this;
        }
    }
}
