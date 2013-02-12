package org.wiredwidgets.cow.ac.util.taskworker;

import java.util.HashMap;
import java.util.Map;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * Listener manager class which contains a map of task names to taskworkers.
 * When a task work event has been fired, the TaskWorkerManager notifies the
 * appropriate task worker listener. If no task worker has registered then a
 * default worker is used. The default worker can be changed, but starts as
 * {@link CowTaskWorker}. A task can only have a single worker.
 *
 * @author MJRUSSELL
 */
public class TaskWorkerManager {

    private static TaskWorkerManager instance = new TaskWorkerManager();
    private Map<String, TaskWorkerEventListener> taskWorkerListenerMap;
    private TaskWorkerEventListener defaultListener;

    private TaskWorkerManager() {
        taskWorkerListenerMap = new HashMap<String, TaskWorkerEventListener>();

        defaultListener = new CowTaskWorker();
    }

    /**
     * returns the instance of the TaskWorkerManager. This ensures only a single
     * worker gets called for tasks.
     * @return the instance
     */
    public static TaskWorkerManager getInstance() {
        return instance;
    }

    /**
     * Adds a task worker listener to be notified when the given task name is to
     * be "worked." If a worker was already registered, it will be replaced.
     *
     * @param taskName Task name to be worked with the given taskWorkerListener
     * @param taskWorkerListener The listener to work the given task
     */
    public void addTaskWorkerListener(String taskName, TaskWorkerEventListener taskWorkerListener) {
        taskWorkerListenerMap.put(taskName, taskWorkerListener);
    }

    /**
     * Removes the worker, if any, associated with a task (provided via its
     * name).
     *
     * @param taskName The name of the task whose worker to remove
     * @return TaskWorkerListener removed that was associated <b>taskName</b>.
     * Returns
     * <code>null</code> if no taskWorkerListener was associated with the task
     * name
     */
    public TaskWorkerEventListener removeTaskWorkerListener(String taskName) {
        return taskWorkerListenerMap.remove(taskName);
    }

    /**
     * Notifies the registered {@link TaskWorkerEventListener} for the task as
     * set by
     * <code>addTaskWorkerListener</code>. If none is registered then the
     * default worker listener is notified.
     * <p/>
     * TODO: the following contract was discussed by not implemented or
     * enforced:
     * <p/>
     * Any class that receives a worktask notification must later notify the
     * TaskWorkerManager via the taskCompleted function or the taskAborted
     * function
     *
     * @param task The task whose worker will be called.
     */
    public void fireTaskWorkerEvent(Task task) {
        TaskWorkerEventListener listener = taskWorkerListenerMap.get(task.getName());
        if (listener != null) {
            listener.workTask(task);
        } else {
            defaultListener.workTask(task);
        }
    }

    /**
     * Sets the default taskWorkerListener that will get called if no specific
     * {@link TaskWorkerEventListener} has been associated with a given task
     * name.
     *
     * @param taskWorkerListener
     */
    public void setDefaulTaskWorker(TaskWorkerEventListener taskWorkerListener) {
        defaultListener = taskWorkerListener;
    }

    /**
     * TODO: Not implemented
     *
     * @param t
     */
    public void workCompleted(Task t) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * TODO: Not implemented
     *
     * @param t
     */
    public void workAborted(Task t) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
