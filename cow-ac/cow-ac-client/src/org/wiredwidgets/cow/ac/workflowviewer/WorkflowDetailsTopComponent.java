package org.wiredwidgets.cow.ac.workflowviewer;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.wiredwidgets.cow.ac.util.notification.BPMEventListener;
import org.wiredwidgets.cow.ac.util.notification.BPMNotificationReceiver;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;
import org.wiredwidgets.cow.ac.util.server.ServerConnectionEventListener;
import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser.CompletionState;
import org.wiredwidgets.cow.ac.workflowviewer.nodes.ActivitiesChildFactory;
import org.wiredwidgets.cow.ac.workflowviewer.nodes.ActivityNode;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Process;

/**
 * TopComponent class that utilizes a
 * <code>TreeTableView</code> to display the current execution status of a
 * workflow via the {@link Node}s framework. Each type of COW workflow activity
 * (Loop, Decision, Task, etc) has its own specialized node. The properties of
 * the nodes make up the columns of the
 * <code>TreeTableView</code>.
 *
 * @author MJRUSSEL
 * @see org.wiredwidgets.cow.ac.workflowviewer.nodes
 */
public final class WorkflowDetailsTopComponent extends TopComponent
        implements ExplorerManager.Provider, BPMEventListener, ServerConnectionEventListener {

    ExplorerManager em = new ExplorerManager();
    /** the id of the process whose data is currently being shown. this is compared to
     * the notifications received as part of the BPMEventListener to detect when
     * notifications are relevant to this class */
    private String processFullId;
    /** the process whose data is being shown */
    private Process process;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/wiredwidgets/cow/ac/client/images/cowicon16x16.png";
    private static final String PREFERRED_ID = "WorkflowDetailsTopComponent";

    /**
     * Private class used to set specific cell renderers for the TreeTableView
     */
    private static class myTreeTableView extends TreeTableView {

        public myTreeTableView(NodeTableModel ntm) {
            super(ntm);
            init();
        }

        public myTreeTableView() {
            super();
            init();
        }

        private void init() {
            treeTable.setRowSelectionAllowed(false);
        }

        public void setRenderers() {
            //Column 0 is the tree and the rest of the columns are property 
            // columns so their classes are different. myCellRenderer handles the distinction.
            treeTable.setDefaultRenderer(treeTable.getColumnClass(0), new myCellRenderer(treeTable.getDefaultRenderer(treeTable.getColumnClass(0))));
            treeTable.setDefaultRenderer(treeTable.getColumnClass(1), new myCellRenderer(treeTable.getDefaultRenderer(treeTable.getColumnClass(1))));
        }

        @Override
        public void expandNode(Node n) {
            TreePath path = tree.getSelectionPath();
            tree.expandPath(path);
        }
    }

    /**
     * Private cell renderer which gets the completion state located in column 1
     * and sets the background color of the table cell to the color which
     * matches the completion state
     */
    private static class myCellRenderer extends DefaultTableCellRenderer {

        private TableCellRenderer tcr;

        public myCellRenderer(TableCellRenderer tcr) {
            this.tcr = tcr;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = tcr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Property completionProp = (Property) table.getValueAt(row, 1);

            if (completionProp != null) {
                String state = "";
                try {
                    state = (String) completionProp.getValue();
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Color backgroundColor;
                if (state.equals("In Progress")) {
                    backgroundColor = CompletionState.UNDER_REVIEW.getCompletionColor();
                } else if (state.equals("Planned")) {
                    backgroundColor = CompletionState.AWAITING_NOTIFICATION.getCompletionColor();
                } else if (state.equals("Contingent")) {
                    backgroundColor = CompletionState.AWAITING_NOTIFICATION.getCompletionColor();
                } else if (state.equals("Precluded")) {
                    backgroundColor = CompletionState.AWAITING_NOTIFICATION.getCompletionColor();
                } else if (state.equals("Completed/Approved")) {
                    backgroundColor = CompletionState.COMPLETED_OR_APPROVED.getCompletionColor();
                } else if (state.equals("Rejected/Invalid")) {
                    backgroundColor = CompletionState.REJECTED_OR_INVALID.getCompletionColor();
                } else {
                    backgroundColor = Color.GRAY;
                }
                c.setBackground(backgroundColor);
            }
            return c;
        }
    }

    public WorkflowDetailsTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(WorkflowDetailsTopComponent.class, "CTL_WorkflowDetailsTopComponent"));
        setToolTipText(NbBundle.getMessage(WorkflowDetailsTopComponent.class, "HINT_WorkflowDetailsTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        treeTableView.setRootVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        treeTableView = new myTreeTableView(new NodeTableModel());

        jScrollPane1.setViewportView(treeTableView);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 422, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private org.openide.explorer.view.TreeTableView treeTableView;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        BPMNotificationReceiver.getInstance().addBpmEventListener(this);
    }

    @Override
    public void componentClosed() {
        BPMNotificationReceiver.getInstance().removeBpmEventListener(this);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    /**
     * Sets the {@link Process} whose information to display
     *
     * @param processFullId
     * @param p
     */
    public void setData(String processFullId, Process p) {
        this.processFullId = processFullId;
        process = p;
        setName(processFullId);
        Activities activities = ((Activities) p.getActivity().getValue());
        AbstractNode root = new AbstractNode(Children.create(new ActivitiesChildFactory(activities), false), Lookups.singleton(p));
        root.setDisplayName(processFullId);

        em.setRootContext(root);
        Node n = root.getChildren().getNodes()[0];
        Property[] columnProperties = n.getPropertySets()[0].getProperties();
        treeTableView.setProperties(columnProperties);
        treeTableView.setTreePreferredWidth(600);
        treeTableView.setTableColumnPreferredWidth(0, 75);
        treeTableView.setTableColumnPreferredWidth(1, 150);
        ((myTreeTableView) treeTableView).setRenderers();
        openTreeToCurrentState();
    }

    @Override
    public void taskCompletedEvent(final String processId, String taskId, String assignee) {
        SwingUtilities.invokeLater(new Runnable() {

            // at the next chance, refresh the tree with the latest data
            // ideally this would only update the necessary nodes, but for now
            // it is much simpler and seems performant enough to refresh the entire contents
            // each time we learn of the need
            @Override
            public void run() {
                // check that the process whose task was completed is the one we care about
                if (processFullId.equals(processId)) {
                    // get the latest data from the server
                    process = BpmClientController.getInstance().getActiveProcessInstanceStatus(processId).getProcess();

                    // TODO: error handling if bad results are returned, like null 

                    // update the display with the new data
                    setData(processFullId, process);
                }
            }
        });
    }

    @Override
    public void taskAssignedEvent(String processId, String taskId, String assignee) {
        // do nothing for now
    }

    @Override
    public void processStartedEvent(String processId) {
        // do nothing
    }

    @Override
    public void processRemovedEvent(String processId) {
        // do nothing
        // this process may have been terminated, but it would
        // probably be confusing and distracting to the user to close the window
        // TODO: could make it greyed out or something 
    }

    @Override
    public void processInstanceRemovedEvent(String processId) {
        // do nothing
        // this process may have been terminated, but it would
        // probably be confusing and distracting to the user to close the window
        // TODO: could make it greyed out or something 
    }

    /**
     * Causes the tree to open up and set the selection to the point of the
     * first activity which is set to "Not Started" -- i.e. the latest active
     * point in workflow.
     */
    private void openTreeToCurrentState() {
        openTreeToCurrentStateRecursivly(em.getRootContext());
    }

    /**
     * Expands the node if the node's state is not equal to "Not started" (i.e.
     * it has been completed) and selects it. Then, continues down it's children
     * by calling this same method.
     * <p/>
     * The recursion stops as soon as it gets to a node which has not been
     * started.
     *
     * @param n The node down which to recurse
     */
    private void openTreeToCurrentStateRecursivly(Node n) {
        if (n instanceof ActivityNode) {
            if (!((ActivityNode) n).getCompletionState().equals("Planned") && !((ActivityNode) n).getCompletionState().equals("Contingent") && !((ActivityNode) n).getCompletionState().equals("Precluded")) {
                try {
                    em.setSelectedNodes(new Node[]{n});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
                treeTableView.expandNode(n);
            } else {
            }
        } else {
        }
        for (Node child : n.getChildren().getNodes()) {
            openTreeToCurrentStateRecursivly(child);
        }
    }

    @Override
    public void serverUpdated() {
        // TODO - ryanmiller 3/22/2012 - don't see that Matt added an easy way
        // to attempt to refresh the contents if a notification gets missed 
        // or the server connection changed.  will have to revisit when there's
        // time for a more robust design modification
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void serverDown() {
        // leave things up, even though they aren't the latest
    }
}
