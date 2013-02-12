package org.wiredwidgets.cow.ac.client.server;

/**
 * An interface to specify how listeners can register for different types
 * of server-related events. 
 * @author MJRUSSEL
 * @see TaskEventManager
 * @see TaskEventListener
 */
public interface ServerEventNotifier {
    
    /**
     * Register a listener to be notified when any task-related events occur.
     * @param listener the listener to register
     */
    public void addTaskEventListener(TaskEventListener listener);

    /**
     * Remove a registered listener of task-related events.
     * <p>
     * If the listener is not registered, not action will be taken.
     * @param listener the listener to remove.
     */
    public void removeTaskEventListener(TaskEventListener listener);
}
