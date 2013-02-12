package org.wiredwidgets.cow.ac.workflowsummary.controller;

import java.util.Arrays;
import java.util.List;
import org.wiredwidgets.cow.ac.util.server.ServerConnectionEventListener;
import org.wiredwidgets.cow.ac.workflowsummary.model.ChicletTableModel;
import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser;
import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser.CompletionState;
import org.wiredwidgets.cow.ac.workflowsummary.notification.ChicletNotificationManager;

/**
 * Controller class that is responsible for updating the {@link ChicletTableModel}.
 * It does so by implementing {@link ChicletEventListener} to be notified when
 * updates are recieved from the server. This ensures that the data displayed in
 * the chiclet chart remains live. It can also be told to manually poll the
 * server for the latest data. Each controller creates its <b>own</b>
 * {@link ChicletNotificationManager} to receive updates from the server.
 *
 * @author MJRUSSELL
 */
public final class ChicletTableModelController
        implements ChicletEventListener, ServerConnectionEventListener {

    private ChicletNotificationManager notificationManager;  //notification manager that sends chiclet events to this class
    private ChicletTableModel chicletTableModel;

    /**
     * Creates a controller to manage a
     * <code>ChicletTableModel</code>, and creates
     * <code>ChicletNotificationManager</code> to trigger updates when
     * notifications come from the server.
     *
     * @param chicletTableModel the model for this controller to manager
     */
    public ChicletTableModelController(ChicletTableModel chicletTableModel) {
        this.chicletTableModel = chicletTableModel;
        setNotifcationListener();
    }

    private void setNotifcationListener() {
        notificationManager = new ChicletNotificationManager();
        notificationManager.addChicletEventListener(this);
    }

    @Override
    synchronized public void addProcess(ProcessStateByGroupAndUser processState) {
        chicletTableModel.addProcess(processState);
    }

    @Override
    synchronized public void updateProcess(ProcessStateByGroupAndUser processState) {
        chicletTableModel.updateProcess(processState);
    }

    @Override
    synchronized public void deleteProcess(String completeProcessId) {
        chicletTableModel.removeProcess(completeProcessId);
    }

    @Override
    synchronized public void deleteProcesses(String processId) {
        List<String> processes = chicletTableModel.getProcessIds();
        for (String fpid : processes) {
            // dissect the id to get just the process identifier and drop the .ext
            String pid = fpid.substring(0, fpid.indexOf("."));
            if (pid.equals(processId)) {
                chicletTableModel.removeProcess(fpid);
            }

        }
    }

    @Override
    public void deleteAllProcesses() {
        chicletTableModel.removeAllProcesses();
    }

    /**
     * grabs the latest information from the server by re-initializing the
     * connection to the server. this has the side effect of first clearing all
     * process data, this "refreshing" all the information.
     */
    public void pollServer() {
        notificationManager.initializeFromServer();
    }

    /**
     * clears all workflows in the data model that have been completed. This
     * means their completion state was set to COMPLETED, APPROVED, REJECTED, or
     * INVALID.
     */
    public void clearCompleted() {
        // TODO this loop should probably be refactored to avoid indexing errors
        // if the row count changes during processing.
        for (int row = 0; row < chicletTableModel.getRowCount(); row++) {
            if (CompletionState.COMPLETED_OR_APPROVED.equals(chicletTableModel.getValueAt(row, 0))
                    || CompletionState.REJECTED_OR_INVALID.equals(chicletTableModel.getValueAt(row, 0))) {
                chicletTableModel.removeProcess(chicletTableModel.getRowName(row));
                row--; // decrement the row count since we just removed a row
            }
        }
    }

    @Override
    public void serverUpdated() {
        pollServer();
    }

    @Override
    public void serverDown() {
        deleteAllProcesses();
    }
}