package org.wiredwidgets.cow.ac.workflowsummary.controller;

import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser;

/**
 * Interface to standardize receipt of process updates from the server. This
 * provides a means for the
 * <code>ChicletTableModelController</code> to tell the
 * <code>ChicletTableModel</code> when processes have been added, updated, or
 * deleted on the server.
 *
 * @author MJRUSSELL
 * @see ChicletTableModelController
 * @see
 * org.wiredwidgets.cow.ac.workflowsummary.notification.ChicletNotificationManager
 */
public interface ChicletEventListener {

    /**
     * Called when a process has been added (created) on the server.
     *
     * @param ps the new process
     */
    public void addProcess(ProcessStateByGroupAndUser ps);

    /**
     * Called when a process has been updated on the server
     *
     * @param ps the updated process
     */
    public void updateProcess(ProcessStateByGroupAndUser ps);

    /**
     * Called with a process instance has been deleted from the server (perhaps the
     * process instance was terminated, the server workflows were reset, etc).
     *
     * @param completeProcessId the id of the process which was deleted in the form
     * of processId.extension (id.ext)
     */
    public void deleteProcess(String completeProcessId);

    /**
     * Called with a process has been deleted from the server.  
     *
     * @param processId the id of the process which was deleted in the form
     * of processId with no extension (id only)
     */
    public void deleteProcesses(String processId);
    
    /**
     * A request to delete all processes. Could be call if the workflows on the
     * server are reset, but most likely just to completely reset all process
     * information as the connection to the server is (re)-established.
     */
    public void deleteAllProcesses();
}
