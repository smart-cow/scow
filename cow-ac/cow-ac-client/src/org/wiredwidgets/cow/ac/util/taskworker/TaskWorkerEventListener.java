package org.wiredwidgets.cow.ac.util.taskworker;

import java.util.EventListener;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * Event listener class which all classes that wish to be notified by the
 * TaskWorkerManager class must implement
 *
 * @author MJRUSSELL
 */
public interface TaskWorkerEventListener extends EventListener {

    /**
     * Notifies the listener to work the specified task
     *
     * @param t The task to be worked.
     */
    public void workTask(Task t);
}
