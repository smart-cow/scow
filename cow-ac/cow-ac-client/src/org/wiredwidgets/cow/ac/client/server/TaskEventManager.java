package org.wiredwidgets.cow.ac.client.server;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.ac.client.controllers.TaskController;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.ac.util.notification.BPMEventListener;
import org.wiredwidgets.cow.ac.util.notification.BPMNotificationReceiver;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;

/**
 * The
 * <code>TaskEventManager</code> listens for updates from the server using
 * {@link BPMNotificationReceiver}. As appropriate, it then alerts listeners for
 * different types of events. For now, only task-related events are handled (as
 * defined by {@link TaskEventListener} in {@link ServerEventNotifier}.
 * <p/>
 * TODO: This class wasn't documented by Matt before he left and is pretty
 * confusing. Needs improved.
 *
 * @author MJRUSSELL
 * @see BPMEventListener
 * @see BPMNotificationReceiver
 * @see ServerEventNotifier
 * @see TaskController
 */
public class TaskEventManager implements ServerEventNotifier, BPMEventListener {

    static final Logger log = Logger.getLogger(TaskEventManager.class);
    private List<TaskEventListener> taskEventListeners;

    public TaskEventManager() {
        taskEventListeners = new ArrayList<TaskEventListener>();
        BPMNotificationReceiver.getInstance().addBpmEventListener(this);
    }

    @Override
    public void addTaskEventListener(TaskEventListener listener) {
        taskEventListeners.add(listener);
    }

    @Override
    public void removeTaskEventListener(TaskEventListener listener) {
        taskEventListeners.remove(listener);
    }

    @Override
    public void taskCompletedEvent(String processId, String taskId, String assignee) {
        /* Remove old if not done so already */
        /* Is this possible??? Can someone have completed "your" task */
        for (TaskEventListener list : taskEventListeners) {
            list.removeAssignedTask(taskId);
        }

        /* Add new task if available or assigned */
        List<Task> assignedTasks = BpmClientController.getInstance().getTasksForUser(BpmClientController.getInstance().getUser());
        for (Task t : assignedTasks) {
            if (!TaskController.getInstance().getSpecializedTasksModel().isAssignedTask(t)) {
                for (TaskEventListener list : taskEventListeners) {
                    list.addAssignedTask(t);
                }
            }
        }
        List<Task> availableTasks = BpmClientController.getInstance().getUnassignedTasksForUser(BpmClientController.getInstance().getUser());
        for (Task t : availableTasks) {
            if (!TaskController.getInstance().getSpecializedTasksModel().isAvailableTask(t)) {
                for (TaskEventListener list : taskEventListeners) {
                    list.addAvailableTask(t);
                }
            }
        }
    }

    @Override
    public void taskAssignedEvent(String processId, String taskId, String assignee) {
        Task t = BpmClientController.getInstance().getTask(taskId);
        if (t == null) {
            // task was apparently assigned, but then disappeared or lost connection to server
            return;
        }

        if (TaskController.getInstance().getSpecializedTasksModel().isAvailableTask(t)) {
            for (TaskEventListener list : taskEventListeners) {
                list.removeAvailableTask(t);
            }
        }
//        if (user.equals(t.getAssignee())
//                && !TaskController.getInstance().getSpecializedTasksModel().isAssignedTask(t)) {
//            for (TaskEventListener list : taskEventListeners) {
//                list.addAssignedTask(t);
//            }
//        }
        List<Task> assignedTasks = BpmClientController.getInstance().getTasksForUser(BpmClientController.getInstance().getUser());
        for (Task at : assignedTasks) {
            log.debug("Received assigned task " + at.getId());
            if (!TaskController.getInstance().getSpecializedTasksModel().isAssignedTask(at)) {
                for (TaskEventListener list : taskEventListeners) {
                    list.addAssignedTask(at);
                }
            }
        }
    }

    @Override
    public void processStartedEvent(String processId) {
        /* Check if new assigned or avaiable tasks from the new process */
        List<Task> assignedTasks = BpmClientController.getInstance().getTasksForUser(BpmClientController.getInstance().getUser());
        for (Task t : assignedTasks) {
            log.debug("Received assigned task " + t.getId());
            if (!TaskController.getInstance().getSpecializedTasksModel().isAssignedTask(t)) {
                for (TaskEventListener list : taskEventListeners) {
                    list.addAssignedTask(t);
                }
            }
        }
        List<Task> availableTasks = BpmClientController.getInstance().getUnassignedTasksForUser(BpmClientController.getInstance().getUser());
        for (Task t : availableTasks) {
            log.debug("Received available task " + t.getId());
            if (!TaskController.getInstance().getSpecializedTasksModel().isAvailableTask(t)) {
                for (TaskEventListener list : taskEventListeners) {
                    list.addAvailableTask(t);
                }
            }
        }
    }

    @Override
    public void processRemovedEvent(String processId) {
        log.debug("request to delete all tasks for all processes with process id: " + processId);
        // find ALL the tasks that relate to this process and remove them

        // XXX - we have no way to know what tasks are currently held by the client from this
        // interface, and it doesn't make since to keep pushing process events out,
        // so the easiest thing to do without redoing this whole design is just cause a refresh from the server
        for (TaskEventListener list : taskEventListeners) {
            list.refreshTasks();
        }
    }
    
    @Override
    public void processInstanceRemovedEvent(String processId) {
        log.debug("request to delete tasks for the process id.ext: " + processId);
        // find ALL the tasks that relate to this process instance and remove them

        // XXX - we have no way to know what tasks are currently held by the client from this
        // interface, and it doesn't make since to keep pushing process events out,
        // so the easiest thing to do without redoing this whole design is just cause a refresh from the server
        for (TaskEventListener list : taskEventListeners) {
            list.refreshTasks();
        }
    }
}
