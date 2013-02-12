package org.wiredwidgets.cow.ac.workflowsummary.controller;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;
import org.wiredwidgets.cow.ac.workflowsummary.model.ChicletTableModel;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;

/**
 * Controller class that implements the ListSelectionListener to provide access
 * the full process information for a selected process in a list. Wraps the
 * <code>Process</code> object into a
 * <code>ProcessForSelection</code> object in order to carry the process id
 * along for future reference. The process information is retrieved from the
 * server using {@link BpmClientController} whenever the selection changes
 * without any caching.
 *
 * @author MJRUSSELL
 */
public class ProcessSelectionManager implements ListSelectionListener {

    /**
     * A simple class that wraps a {@link org.wiredwidgets.cow.server.api.model.v2.Process}
     * retrieved from the server with a String providing it a unique id. This
     * facilitates easier use elsewhere.
     */
    public static class ProcessWithId {

        private String processFullId; //process full id that includes id and ext
        private org.wiredwidgets.cow.server.api.model.v2.Process process; //process object from the server

        public ProcessWithId(String processFullId, org.wiredwidgets.cow.server.api.model.v2.Process process) {
            this.processFullId = processFullId;
            this.process = process;
        }

        public String getProcessFullId() {
            return processFullId;
        }

        public Process getProcess() {
            return process;
        }
    }
    private ChicletTableModel chicletTableModel;
    private ProcessWithId process = null;

    public ProcessSelectionManager(ChicletTableModel chicletTableModel) {
        this.chicletTableModel = chicletTableModel;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            for (int row = e.getFirstIndex(); row <= e.getLastIndex(); row++) {
                if (lsm.isSelectedIndex(row)) {
                    String processId = chicletTableModel.getRowName(row);
                    ProcessInstance activeProcessInstance =
                            BpmClientController.getInstance().getActiveProcessInstanceStatus(processId);
                    if (activeProcessInstance != null) {
                        process = new ProcessWithId(processId, activeProcessInstance.getProcess());
                    }
                }
            }
        }
    }

    /**
     * Returns the currently selected Process, wrapped into a
     * <code>ProcessWithId</code> helper class that also provides a unique, full
     * identifier.
     *
     * @return the currently selected process information retrieved from the
     * server. Can be null if nothing is selected or if the selection value is
     * changing just as this is called.
     */
    public ProcessWithId getSelectedProcess() {
        return process;
    }
}
