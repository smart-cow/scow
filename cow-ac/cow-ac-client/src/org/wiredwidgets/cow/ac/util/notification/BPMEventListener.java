package org.wiredwidgets.cow.ac.util.notification;

import java.util.EventListener;

/**
 * Interface for classes that wish to register themselves as listeners when bpm
 * (business processing model) events are sent from the bpm server. This occurs,
 * for example, when tasks are completed or assigned, or processes are started
 * or terminated. The notifying class is {@link BPMNotificatioReceiver}, which
 * also contains information about the method used to receive communications
 * from the server.
 *
 * @author MJRUSSELL, documentation cleaned up by RYANMILLER
 * @see BPMNotificationReceiver
 */
public interface BPMEventListener extends EventListener {

    /**
     * Called when the server notifies the client that a task has been completed
     * @param processId Process complete id (in the format id.ext) of the task
     * that was completed
     * @param taskId Task id of the task that was completed
     * @param assignee Assignee of the task that was completed
     */
    public void taskCompletedEvent(String processId, String taskId, String assignee);

    /**
     * Called when the server notifies the client that a task has been assigned
     * to a particular user
     * @param processId Process complete id (in the format id.ext) of the task
     * that was assigned
     * @param taskId Task id of the task that was assigned
     * @param assignee Id for the user to whom the task was assigned
     */
    public void taskAssignedEvent(String processId, String taskId, String assignee);

    /**
     * Called when the server notifies the client that a process has been 
     * started (i.e. a workflow has been instantiated)
     * @param processId Process complete id (in the format id.ext) of the
     * process that was started
     */
    public void processStartedEvent(String processId);

    /**
     * Called when the server notifies the client that a process has been 
     * removed (i.e. an entire process has been deleted or terminated)
     * @param processId Process id (in the format id only) of the
     * process that was removed. All instances of this process are assumed to
     * be removed.
     */
    public void processRemovedEvent(String processId);
    
    /**
     * Called when the server notifies the client that a process instance has been 
     * removed (i.e. a workflow has been deleted or terminated). If the parent
     * process itself was deleted, only {@link #processRemovedEvent(java.lang.String)}
     * will be called, not a notification for every process instance.
     * @param processId Process complete id (in the format id.ext) of the
     * process that was removed
     */
    public void processInstanceRemovedEvent(String processId);

    
}
