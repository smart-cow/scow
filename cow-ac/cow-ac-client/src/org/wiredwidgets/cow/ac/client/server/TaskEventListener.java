package org.wiredwidgets.cow.ac.client.server;

import org.wiredwidgets.cow.ac.util.server.BpmClientController;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * A set of methods needed to control receiving updates for various events
 * related to tasks. These would be triggered via notifications from the COW
 * server, for example. <p>
 * <code>Tasks</code> are related to user, but to keep the interface simpler all
 * methods are assumed to work on the whose information is being processed by
 * the COW UI. This can be retrieved via
 * {@link BpmClientController#getUser()}, however a class implementing this
 * method could choose to handle this another way.
 * <p/>
 * @author MJRUSSEL, documented by RYANMILLER
 * @see TaskEventManager
 */
public interface TaskEventListener {

    /**
     * Called when a
     * <code>Task</code> has been made available by the server which is assigned
     * to the user.
     *
     * @param t The task recently assigned
     */
    public void addAssignedTask(Task t);

    /**
     * Called when a
     * <code>Task</code> has been made available by the server which is not
     * assigned to the user, but is assigned to one of the user's groups (e.g.
     * it is available for the user to assign to themselves).
     *
     * @param t The task recently made available
     */
    public void addAvailableTask(Task t);

    /**
     * Called when a
     * <code>Task</code> which was available to the user has been made
     * unavailable--perhaps by being claimed by another user, part of a canceled
     * or terminated workflow, etc.
     *
     * @param t The task which is no longer available
     * @return true if the task was present and removed, otherwise false.
     */
    public boolean removeAvailableTask(Task t);

    /**
     * Called when a
     * <code>Task</code> which was assigned to the user has been made
     * unavailable--perhaps by being part of a canceled or terminated workflow,
     * etc.
     * <p/>
     * TODO: document why this is by id rather than object. Because of the way
     * the equals method is implemented for
     * <code>Task</code>?
     *
     * @param taskId
     * @return true if a task with the id was assigned and removed, otherwise
     * false.
     */
    public boolean removeAssignedTask(String taskId);

    /**
     * Called to trigger a refresh of all tasks with the latest information
     * from the server. Used when changes have been made on the server
     * that don't necessarily make sense to handle one by one (or at least it's
     * not easy to do so).
     */
    public void refreshTasks();
}
