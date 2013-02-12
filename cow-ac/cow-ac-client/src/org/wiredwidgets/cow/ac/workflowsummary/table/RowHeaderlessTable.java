package org.wiredwidgets.cow.ac.workflowsummary.table;

import javax.swing.JTable;

/**
 * A simple abstract interface designed for a
 * <code>JTable</code> which would like to provide a title (header) column for
 * its rows (e.g. {@link ChicletTable}, but delegates the rendering thereof to a
 * different class (e.g. {@link RowHeaderTable}.
 *
 * @author MJRUSSELL, documented by RYANMILLER
 * @see ChicletTable
 * @see RowHeaderTable
 */
abstract public class RowHeaderlessTable extends JTable {

    /**
     * Returns the desired name for this row to be displayed in a heading column.
     * @param row the row number whose name to retrieve
     * @return the name of the row
     */
    abstract public String getRowName(int row);
}