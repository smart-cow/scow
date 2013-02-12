package org.wiredwidgets.cow.ac.workflowsummary.notification;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.ac.workflowsummary.controller.ChicletEventListener;
import org.wiredwidgets.cow.ac.workflowsummary.controller.ProcessesController;
import org.wiredwidgets.cow.ac.workflowsummary.model.ProcessStateByGroupAndUser;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.ProcessInstances;
import org.wiredwidgets.cow.ac.util.notification.BPMEventListener;
import org.wiredwidgets.cow.ac.util.notification.BPMNotificationReceiver;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;

/**
 * Notification manager class which registers itself with the
 * BPMNotificationReceiver to receive events from the bpm server. This class
 * utilizes the {@link ProcessesController} class to give {@link ProcessStateByGroupAndUser}
 * data objects to any registered {@link ChicletEventListener}s.
 *
 * @author MJRUSSELL
 *
 * @see BPMNotificationReceiver
 * @see ProcessesController
 */
public class ChicletNotificationManager implements BPMEventListener {

    private List<ChicletEventListener> chicletEventListeners;
    static final Logger log = Logger.getLogger(ChicletNotificationManager.class);

    /**
     * Creates the notification manager and adds it as a listener for events
     * from {@link BPMNotificationReceiver}
     */
    public ChicletNotificationManager() {
        chicletEventListeners = new ArrayList<ChicletEventListener>();
        BPMNotificationReceiver.getInstance().addBpmEventListener(this);
        
        // make sure the controller is initialized so the calls will work
        if (!BpmClientController.getInstance().isInitialized()) {
            // try to initialize the controller if this is the first call
            if (!BpmClientController.getInstance().initialize()) {
                log.error("BpmClientController did not initialize");
                // todo - need some ui notification?
            }
        }
    }

    @Override
    public void taskCompletedEvent(String processId, String taskId, String assignee) {
        ProcessStateByGroupAndUser processState = ProcessesController.getInstance().getProcessState(processId);
        if (processState != null) {
            updateProcess(processState);
        }
    }

    @Override
    public void taskAssignedEvent(String processId, String taskId, String assignee) {
        // nothing
    }

    @Override
    public void processStartedEvent(String processId) {
        ProcessStateByGroupAndUser processState = ProcessesController.getInstance().getProcessState(processId);
        if (processState != null) {
            addNewProcess(processState);
        }
    }

    @Override
    public void processRemovedEvent(String processId) {
        deleteAllProcessInstances(processId);
    }

    @Override
    public void processInstanceRemovedEvent(String processId) {
        deleteProcessInstance(processId);
    }

    private void addNewProcess(ProcessStateByGroupAndUser ps) {
        for (ChicletEventListener listener : chicletEventListeners) {
            listener.addProcess(ps);
        }
    }

    private void updateProcess(ProcessStateByGroupAndUser ps) {
        for (ChicletEventListener listener : chicletEventListeners) {
            listener.updateProcess(ps);
        }
    }

    private void deleteAllProcessInstances(String processId) {
        for (ChicletEventListener listener : chicletEventListeners) {
            listener.deleteProcesses(processId);
        }
    }

    private void deleteProcessInstance(String completeProcessId) {
        for (ChicletEventListener listener : chicletEventListeners) {
            listener.deleteProcess(completeProcessId);
        }
    }

    private void deleteAllProcesses() {
        for (ChicletEventListener listener : chicletEventListeners) {
            listener.deleteAllProcesses();
        }
    }

    /**
     * Sends notifications to delete all processes and then gets all active
     * processes and fire events to the listeners
     */
    public void initializeFromServer() {
        deleteAllProcesses();
        ProcessInstances activeProcessInstances = BpmClientController.getInstance().getActiveProcessInstances();
        for (ProcessInstance pi : activeProcessInstances.getProcessInstances()) {
            processStartedEvent(pi.getId());
        }
    }

    /**
     * Allows for class to register to receive {@link ChicletEventListener}
     * events.
     *
     * @param listener
     */
    public void addChicletEventListener(ChicletEventListener listener) {
        chicletEventListeners.add(listener);
    }

    /**
     * Removes a listener, if it had been added
     *
     * @param listener the listener to remove
     */
    public void removeChicletEventListener(ChicletEventListener listener) {
        chicletEventListeners.remove(listener);
    }
}
