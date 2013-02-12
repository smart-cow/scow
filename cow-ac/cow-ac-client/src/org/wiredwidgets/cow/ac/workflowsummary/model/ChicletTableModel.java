package org.wiredwidgets.cow.ac.workflowsummary.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.User;

/**
 * A
 * <code>TableModel</code> that maps the relationships between the state of a
 * workflow, i.e. a
 * <code>Process</code> and
 * <code>Group</code>s or
 * <code>User</code>s involved in that process. The state of a process is
 * provided via {@link ProcessStateByGroupAndUser}.
 * <p/>
 * This mapping allows for a "summary" of a process's tasks showing the status
 * of work (completed, not started, etc) in a process organized by the users and
 * groups involved in the workflow. For example, Group "A" has work that needs
 * to be completed, but User "B" has already completed their assigned tasks.
 * <p/>
 * The model uses sorted list of users and groups for the column header and adds
 * rows for each process just as they are presented to the model (i.e. no sorted
 * order). The data is fetched by a map that maps groups or users to completion
 * states.
 *
 * @author MJRUSSELL, documented by RYANMILLER
 * @see
 * org.wiredwidgets.cow.ac.workflowsummary.controller.ChicletTableModelController
 * @see ProcessStateByGroupAndUser
 */
public class ChicletTableModel extends AbstractTableModel {

    /** List of groups/users/strings which are the columns in order in the table */
    private ArrayList<Object> chicletColumns;
    /** List of rows which are the process id.ext */
    private ArrayList<String> processRows;
    /** Map from process id.ext to a process state */
    private Map<String, ProcessStateByGroupAndUser> processStateMap;
    private final static String HEADER_COLUMN_TEXT = "MSN State";

    /**
     * Creates the table model. The first column is the mission (or workflow's,
     * which is a
     * <code>Process</code> class) overall state (labeled as "MSN State"). The
     * individual workflows are each a row. All columns but the first are the
     * users and groups involved with any workflows in the table. This will be
     * dynamic as workflows are added or removed from the table.
     */
    public ChicletTableModel() {
        chicletColumns = new ArrayList<Object>();
        chicletColumns.add(HEADER_COLUMN_TEXT);
        processRows = new ArrayList<String>(); // this object is cloned in getProcessIds, so change the type there if it is changed here
        processStateMap = new HashMap<String, ProcessStateByGroupAndUser>();
    }

    private String getIdFromGroupOrUser(Object o) {
        if (o instanceof Group) {
            return ((Group) o).getId();
        } else if (o instanceof User) {
            return ((User) o).getId();
        } else {
            throw new IllegalArgumentException("Parameter is neither group nor user");
        }
    }

    /**
     * Finds the index in the column list to insert the object
     */
    private int findColumnIndexForInsertion(Object insertObject) {
        //currently alphabetized
        int insertIndex = 0;
        String insertObjectStr = "";
        if (insertObject instanceof Group) {
            insertObjectStr = ((Group) insertObject).getId();
        } else if (insertObject instanceof User) {
            insertObjectStr = ((User) insertObject).getId();
        } else {
            throw new IllegalArgumentException("Tried to find index of neither a group nor user");
        }

        while (insertIndex < chicletColumns.size()) {
            Object compareObj = chicletColumns.get(insertIndex);
            String compareObjStr = "";
            if (compareObj instanceof Group) {
                compareObjStr = ((Group) compareObj).getId();
            }
            if (compareObj instanceof User) {
                compareObjStr = ((User) compareObj).getId();
            }
            if (insertObjectStr.compareToIgnoreCase(compareObjStr) < 0) {
                break;
            }
            insertIndex++;

        }
        return insertIndex;
    }

    private int findIndexOfGroup(Group g) {
        for (int i = 0; i < chicletColumns.size(); i++) {
            Object o = chicletColumns.get(i);
            if (o instanceof Group && ((Group) o).getId().equals(g.getId())) {
                return i + 1;
            }
        }
        return -1;
    }

    private int findIndexOfUser(User u) {
        for (int i = 0; i < chicletColumns.size(); i++) {
            Object o = chicletColumns.get(i);
            if (o instanceof User && ((User) o).getId().equals(u.getId())) {
                return i + 1;
            }
        }
        return -1;
    }

    private int findIndexOfGroupOrUser(Object o) {
        if (o instanceof User) {
            return findIndexOfUser((User) o);
        } else if (o instanceof Group) {
            return findIndexOfGroup((Group) o);
        } else {
            throw new IllegalArgumentException("Parameter not Group or User");
        }
    }

    @Override
    public int getRowCount() {
        return processRows.size();
    }

    @Override
    public int getColumnCount() {
        return chicletColumns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String processId = processRows.get(rowIndex);
        Object o = chicletColumns.get(columnIndex);
        ProcessStateByGroupAndUser process = processStateMap.get(processId);
        if (o instanceof Group) {
            return process.getStatesGroupMap().get((Group) o);
        } else if (o instanceof User) {
            return process.getStatesUserMap().get((User) o);
        } else if (o.equals(HEADER_COLUMN_TEXT)) {
            return process.getCompletionState();
        } else {
            throw new RuntimeException(
                    "Found object in data model column that was not a Group, User, or "
                    + HEADER_COLUMN_TEXT + ".");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        Object column = chicletColumns.get(columnIndex);
        if (column.equals(HEADER_COLUMN_TEXT)) {
            return HEADER_COLUMN_TEXT;
        } else {
            return getIdFromGroupOrUser(column);
        }
    }

    /**
     * Provides a title, or header text, for each row in the table
     *
     * @param row the row whose title to retrieve.
     * @return the string to use as a title for the row.
     */
    public String getRowName(int row) {
        return processRows.get(row);
    }

    /**
     * Adds a process's state into the model.
     * <p/>
     * TODO: Note by RYANMILLER 3/16/2012: Documenting after the fact so I'm not
     * sure, but it looks like if the process has already been added into the
     * model it will not good re-added, but the event fireTableRowsInserted will
     * get called anyway as if the last row in the table was re-added. Since
     * this works I don't want to touch it, but this could probably be made much
     * more robust.
     *
     * @param processState the process state to add
     */
    public void addProcess(ProcessStateByGroupAndUser processState) {
        int insertionIndex;
        for (Group g : processState.getStatesGroupMap().keySet()) {
            if (findIndexOfGroup(g) == -1) {
                insertionIndex = findColumnIndexForInsertion(g);
                chicletColumns.add(insertionIndex, g);
            }
        }
        for (User u : processState.getStatesUserMap().keySet()) {
            if (findIndexOfUser(u) == -1) {
                insertionIndex = findColumnIndexForInsertion(u);
                chicletColumns.add(insertionIndex, u);
            }
        }
        fireTableStructureChanged();
        processStateMap.put(processState.getProcessFullId(), processState);
        processRows.add(processState.getProcessFullId());
        int insertIndex = processRows.size() - 1;
        fireTableRowsInserted(insertIndex, insertIndex);
    }

    /**
     * Update a particular process's state in the model. If the process does not
     * exist, it will be added.
     *
     * @param processState the process state to update
     */
    public void updateProcess(ProcessStateByGroupAndUser processState) {
        if (processStateMap.containsKey(processState.getProcessFullId())) {
            processStateMap.put(processState.getProcessFullId(), processState);
            int updateIndex = processRows.indexOf(processState.getProcessFullId());
            fireTableRowsUpdated(updateIndex, updateIndex);
        } else {
            addProcess(processState);
        }

    }

    /**
     * Removes a process from the list via its id. <b>Users or groups that were
     * unique to that process, as well as those shared, still remain.</b>
     *
     * @param processFullId the id of the process to remove
     */
    public void removeProcess(String processFullId) {
        // TODO remove any groups or users that remain that were unique to this
        // process.  Be sure to update the method comments once completed.
        int removeIndex = processRows.indexOf(processFullId);
        processRows.remove(processFullId);
        processStateMap.remove(processFullId);
        fireTableRowsDeleted(removeIndex, removeIndex);
    }

    /**
     * Remove all process information and all columns of users and groups.
     */
    public void removeAllProcesses() {
        chicletColumns.clear();
        chicletColumns.add(HEADER_COLUMN_TEXT);
        processRows.clear();
        processStateMap.clear();
        fireTableStructureChanged();
    }

    /**
     * Allows for inspecting which processes are in the model. Each element is
     * the processFullId for the process information in the model.
     *
     * @return A copy of the list of all the processFullIds for processes in the
     * model.
     */
    public ArrayList<String> getProcessIds() {
        return (ArrayList<String>) processRows.clone();
    }
}
