package org.wiredwidgets.cow.ac.workflowsummary.table;

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * A RowHeaderTable uses a
 * <code>JTable</code> to hold row headers for a main
 * <code>JTable</code> and provides a custom renderer for that information. (In
 * a normal
 * <code>JTable</code> the row headers are just numbers.) The main table, which
 * must implement
 * {@link RowHeaderlessTable}, is provided via the constructor.  Think of this
 * as the first column of a larger table, with a <code>RowHeaderlessTable</code> providing
 * the column headers and the content.
 * <p/><b>This</b>
 * class (which is a
 * <code>JTable</code>) must be added to the row header of the
 * <code>JScrollpane<code> that contains the main
 * table to keep scrolling between the two in sync. Adapted from
 * http://tips4java.wordpress.com/2008/11/18/row-number-table.
 *
 * @author MJRUSSELL, documented by RYANMILLER
 * @see http://tips4java.wordpress.com/2008/11/18/row-number-table
 */
public class RowHeaderTable extends JTable
        implements ChangeListener, PropertyChangeListener {

    private RowHeaderlessTable mainTable;

    /**
     * Creates the row header table based on the heading labels provided 
     * through mainTable's getRowName(int) method.
     * @param mainTable The table for which to create row headers
     */
    public RowHeaderTable(RowHeaderlessTable mainTable) {
        this.mainTable = mainTable;
        initListeners();

        setFocusable(false);
        setAutoCreateColumnsFromModel(false);
        setModel(mainTable.getModel());
        setSelectionModel(mainTable.getSelectionModel());

        TableColumn column = new TableColumn();
        column.setHeaderValue(" ");
        column.setResizable(true);
        addColumn(column);
        column.setCellRenderer(new RowRenderer(mainTable));

        getColumnModel().getColumn(0).setPreferredWidth(175);
        setPreferredScrollableViewportSize(getPreferredSize());

    }

    private void initListeners() {
        mainTable.addPropertyChangeListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Component c = getParent();

        //  Keep the scrolling of the row table in sync with the main table.

        if (c instanceof JViewport) {
            JViewport viewport = (JViewport) c;
            viewport.addChangeListener(this);
        }
    }

    /*
     * Delegate method to main table
     */
    @Override
    public int getRowCount() {
        return mainTable.getRowCount();
    }

    @Override
    public int getRowHeight(int row) {
        return mainTable.getRowHeight(row);
    }

    /*
     * This table does not use any data from the main TableModel, so just return
     * a value based on the row parameter.
     */
    @Override
    public Object getValueAt(int row, int column) {
        return Integer.toString(row + 1);
    }

    /*
     * Don't edit data in the main TableModel by mistake
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        //  Keep the scrolling of the row table in sync with main table

        JViewport viewport = (JViewport) e.getSource();
        JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        //  Keep the row table in sync with the main table

        if ("selectionModel".equals(e.getPropertyName())) {
            setSelectionModel(mainTable.getSelectionModel());
        }

        if ("model".equals(e.getPropertyName())) {
            setModel(mainTable.getModel());
        }
    }

    /**
     * Borrow the renderer from JDK1.4.2 table header.
     */
    private static class RowRenderer extends DefaultTableCellRenderer {

        private RowHeaderlessTable rowAndColumnTable;

        public RowRenderer(RowHeaderlessTable jtable) {
            setHorizontalAlignment(JLabel.CENTER);
            this.rowAndColumnTable = jtable;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();

                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (isSelected) {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            // pull the row's header value from the main table, making it blank 
            // if no row name is being provided
            setText((value == null) ? "" : rowAndColumnTable.getRowName(row));
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));

            return this;
        }
    }
}