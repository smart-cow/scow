package org.wiredwidgets.cow.ac.client.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.wiredwidgets.cow.ac.client.controllers.TaskController;
import org.wiredwidgets.cow.ac.client.utils.StringUrl;
import org.wiredwidgets.cow.ac.client.utils.StringUrlMouseClickListener;
import org.wiredwidgets.cow.ac.util.CowUtils;
import org.wiredwidgets.cow.ac.util.NoEditDefaultTableModel;
import org.wiredwidgets.cow.ac.util.taskworker.TaskWorkerManager;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;

/**
 * A UI panel providing detailed information for a Task. Tailors itself
 * (slightly) depending on the task's attributes (such as assigned to the
 * current user or not).
 *
 * @author RYANMILLER
 * @see TasksPanel
 */
public class TaskDetailsPanel extends javax.swing.JPanel
        implements ListSelectionListener {

    static Color textFieldForeground;

    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        textFieldForeground = uid.getColor("TextField.foreground");
    }
    // TODO: makes these enums if time allows
    public static final int NO_TASK = 0;
    public static final int USER_TASK = 1;
    public static final int AVAILABLE_TASK = 2;
    public static final String USER_TASK_BTN_TEXT = "Complete Task";
    public static final String AVAILABLE_TASK_BTN_TEXT = "Assign To Me";
    public static final String NO_TASK_DIRECTIONS_TEXT = "Choose a task from the list"
            + " to the left or press the \"Refresh Tasks\" button to check for new tasks";
    private int taskState;
    /** the selected task whose information is getting displayed */
    private Task task;
    private StatusBar statusBar;
    private TableCellRenderer urlRenderer;

    /**
     * Creates new form TaskDetailsPanel. Defaults to not displaying information
     * for a task.
     */
    public TaskDetailsPanel() {
        initComponents();

        setTaskState(NO_TASK);
        task = new Task();

        statusBar = StatusBar.getInstance();

        urlRenderer = new UrlRenderer();
        
        // support handling of clicks on custom renders
        tblProperties.addMouseListener(new StringUrlMouseClickListener());
    }

    /**
     * Controls the state of the panel contents based on the task setting. These
     * are available as public static int definitions. After changing the state,
     * the display is updated accordingly.
     *
     * @param task_id Can be one of USER_TASKS or AVAILABLE_TASKS or (NO_TASK)
     * @return true if state set, false if invalid task_id was provided or
     * updating the panel contents was not successful.
     */
    public boolean setTaskState(int task_id) {
        if (task_id != NO_TASK
                && task_id != USER_TASK
                && task_id != AVAILABLE_TASK) {
            return false;
        }

        taskState = task_id;

        updateDisplay();

        return true;
    }

    /**
     * Returns the state of the Task being displayed. See the public static int
     * definitions.
     *
     * @return One of the defined task states.
     */
    public int getTaskState() {
        return taskState;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProperties = new javax.swing.JTable();
        panelButtons = new javax.swing.JPanel();
        btnTaskAction = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TaskDetailsPanel.class, "TaskDetailsPanel.border.title"))); // NOI18N
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        tblProperties.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblProperties.setCellSelectionEnabled(true);
        tblProperties.setGridColor(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(tblProperties);
        tblProperties.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        btnTaskAction.setText(org.openide.util.NbBundle.getMessage(TaskDetailsPanel.class, "TaskDetailsPanel.btnTaskAction.text")); // NOI18N
        btnTaskAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTaskActionActionPerformed(evt);
            }
        });
        panelButtons.add(btnTaskAction);

        add(panelButtons, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Triggers an action depending on the state of the Task being displayed.
     * <p/>
     * If the task is a
     * <code>USER_TASK</code> it will trigger the worker for that task as
     * managed by {@link TaskWorkerManager}. If it is a
     * <code>AVAILABLE_TASK</code> it will be claimed for the user using {@link TaskController}.
     * Other states will be ignored
     *
     * @param evt not used
     */
    private void btnTaskActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaskActionActionPerformed
        if (taskState == USER_TASK) {

            TaskWorkerManager.getInstance().fireTaskWorkerEvent(task);

        } else if (taskState == AVAILABLE_TASK) {
            // call the server with updates
            boolean ret = TaskController.getInstance().claimTask(task);
            if (false == ret) {
                // server was not updated due to an error. 
                statusBar.setStatusText("Could not send task update to the server. Assignment was NOT completed.");
            } else {
                statusBar.setStatusText("Task has been assigned to the user and tasks refreshed from the server.");
            }
        }
    }//GEN-LAST:event_btnTaskActionActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTaskAction;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JTable tblProperties;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            // wait till the selection settles out.  Otherwise triggered on mouse click AND mouse release
            return;
        }
        if (!(e.getSource() instanceof JList)) {
            // incorrect event fired, ignore
            return;
        }

        Object selected = ((JList) e.getSource()).getSelectedValue();
        if (!(selected instanceof Task)
                && !(selected instanceof Object)) {
            // list selection was cleared
            setTaskState(NO_TASK);
            return;
        }

        task = (Task) selected;
        // see what kind of task has been selected and adjust the UI accordingly
        if (TaskController.getInstance().getSpecializedTasksModel().isAvailableTask(task)) {
            taskState = AVAILABLE_TASK;
            updateDisplay();
        } else {
            taskState = USER_TASK;
            updateDisplay();
        }

        // adjust what we're showing based on state (available vs assigned task)
        // TODO: avoid creating a new table model each time

        DefaultTableModel tm = new NoEditDefaultTableModel(new Object[]{"Name", "Value"}, 0);

        // add the contents to the table
        tm.addRow(new Object[]{"Task Name", task.getName()});
        tm.addRow(new Object[]{"Task ID", task.getId()});
        tm.addRow(new Object[]{"Task Process ID", task.getProcessInstanceId()});
        if (task.getAssignee() != null) {
            tm.addRow(new Object[]{"Assignee", task.getAssignee()});
        } else {
            tm.addRow(new Object[]{"Assignee", "Available for assignment"});
        }
        tm.addRow(new Object[]{"Create Time", task.getCreateTime()});
        // Due Date does not work on the server yet
        //tm.addRow(new Object[]{"Due Date", task.getDueDate()});
        tm.addRow(new Object[]{"Activity Name", task.getActivityName()});
        tm.addRow(new Object[]{"Priority", task.getPriority()});
        tm.addRow(new Object[]{"Description", task.getDescription()});

        // apparently task variables will by uninitialized if there are no variables rather
        // than of zero size, so to avoid a null pointer exception, we check first
        if (task.getVariables() != null && !task.getVariables().getVariables().isEmpty()) {
            tm.addRow(new Object[]{"Workflow Notes: ", ""});
            for (Variable v : task.getVariables().getVariables()) {
                String urlNote = v.getValue();

                if (CowUtils.isUrl(urlNote)) {  // dress it up with a special class to assist in rendering and click handling
                    // don't worry about masking the url with a human-readable 
                    // label for now when creating the special text wrapper.  
                    // this was planned for a future release.
                    tm.addRow(new Object[]{"  " + v.getName(), new StringUrl(urlNote, urlNote)});
                } else {
                    tm.addRow(new Object[]{"  " + v.getName(), v.getValue()});
                }
            }
        }

        // display our customized model with all the task's info in a friendly format 
        tblProperties.setModel(tm);

        // adjust the initial width of the first column since we know the contents
        tblProperties.getColumnModel().getColumn(0).setPreferredWidth(175);
        // make the remainder huge so the first column gets set to the preferred size without extra
        tblProperties.getColumnModel().getColumn(1).setPreferredWidth(500);

        // adjust the render of the second column to handle things other than strings
        tblProperties.getColumn("Value").setCellRenderer(urlRenderer);
    }

    /**
     * Updates the way the component is being displayed based on the current
     * internal definition of the Task state.
     */
    private void updateDisplay() {
        if (taskState == USER_TASK) {
            btnTaskAction.setText(USER_TASK_BTN_TEXT);
            panelButtons.setVisible(true);
        } else if (taskState == AVAILABLE_TASK) {
            btnTaskAction.setText(AVAILABLE_TASK_BTN_TEXT);
            panelButtons.setVisible(true);
            this.validate();
            this.repaint();  // needed?
        } else { // assume a bad state or the NO_TASK state
            // clean out the old contents of the table and set a placeholder
            DefaultTableModel tm = new NoEditDefaultTableModel(new Object[]{"Name", "Value"}, 0);
            tm.addRow(new Object[]{NO_TASK_DIRECTIONS_TEXT, ""});
            tblProperties.setModel(tm);
            tblProperties.getColumnModel().getColumn(0).setPreferredWidth(275);

            panelButtons.setVisible(false);  // nothing for the user to click
        }
    }

    /**
     * Renders extra types of classes into JTable, namely something with a URL
     * that gets special treatment. There's probably a much better way to
     * achieve this that makes things look nicer, but after several different
     * pursuits I ran out of time.
     */
    public static class UrlRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof StringUrl) {
                if (((StringUrl) value).getUrl() != null) {
                    // make it look blue like a hyperlink if there's a URL
                    this.setForeground(Color.BLUE);
                }
                this.setText("<html>" + ((StringUrl) value).getText() + "</html>");
                return this;
            } else {
                this.setForeground(textFieldForeground);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

        }
    }
}
