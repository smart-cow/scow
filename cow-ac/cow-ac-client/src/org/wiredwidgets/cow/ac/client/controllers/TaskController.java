package org.wiredwidgets.cow.ac.client.controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.ListModel;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.ac.client.models.TasksListModel;
import org.wiredwidgets.cow.ac.client.server.TaskEventListener;
import org.wiredwidgets.cow.ac.client.server.TaskEventManager;
import org.wiredwidgets.cow.ac.client.ui.CowTopComponent;
import org.wiredwidgets.cow.ac.client.ui.StatusBar;
import org.wiredwidgets.cow.ac.client.utils.TaskComparators;
import org.wiredwidgets.cow.ac.options.CowSettingsPanel;
import org.wiredwidgets.cow.ac.util.SortedListModel;
import org.wiredwidgets.cow.ac.util.server.BpmClientController;
import org.wiredwidgets.cow.ac.util.server.ServerConnectionEventListener;
import org.wiredwidgets.cow.ac.util.taskworker.CowTaskWorker;
import org.wiredwidgets.cow.ac.util.taskworker.TaskWorkerEventListener;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * Handles the logic for updating task information stored in the data models
 * that back the GUI. Tasks are stored into a {@link TasksListModel} which is
 * then sorted using {@link SortedListModel}. Update events are received from
 * the server via the
 * {@link TaskEventListener} interface by a {@link TaskEventManager} spawned
 * into its own thread (see {@link #initNotifier()}).
 * <p/>
 * Will display messages to the user via  {@link StatusBar} for visible, but
 * non-intrusive error and status updates. The main GUI is the
 * <code>CowTopComponent</code>.
 *
 * @author RYANMILLER
 */
public class TaskController implements TaskEventListener, ServerConnectionEventListener {

    static final Logger log = Logger.getLogger(TaskController.class);
    /** model for the composite set of all tasks available to the user */
    private ListModel tasksModel;
    /** a sorted version of the model of better GUI display */
    private SortedListModel sortedTasksModel;
    /** singleton instance of this class */
    private static TaskController instance = new TaskController();
    /** time when the task data was last refreshed */
    private Date lastUpdateTime;
    /** * Create a reference to the worker to use when a task is selected */
    private CowTaskWorker taskWorker;

    /**
     * Initializes the data model components holding task information. Also sets
     * up the {@link TaskWorkerEventListener} so that this controller will be
     * the default worker for tasks. This is a singleton class so that only one
     * object is updating the data models to make things simpler.
     */
    private TaskController() {
        // create the data models to hold the tasks
        // a TasksListModel is similar to a regular list model, but keeps track
        // of which tasks are available vs. assigned so that although they 
        // can be presented in a single list, they can be distinguished with 
        // help from the model so they can be displayed differently or acted on
        // differently
        tasksModel = new TasksListModel();

        sortedTasksModel = new SortedListModel(tasksModel,
                SortedListModel.SortOrder.DESCENDING,
                TaskComparators.StateThenCreationTimeComparator());

        // set up this class to recieve server notifications
        initNotifier();
    }

    /**
     * Spawns a separate thread which uses the {@link TaskEventManager} to
     * listen for updates from the server. This class listens for updates from
     * the TaskEventManager using the {@link TaskEventListener}.
     */
    private void initNotifier() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                TaskEventManager notifier = new TaskEventManager();
                notifier.addTaskEventListener(TaskController.this);
            }
        }).start();
    }

    /**
     * Singleton-enforcing constructor.
     *
     * @return the instance of the class.
     */
    public static TaskController getInstance() {
        return instance;
    }

    @Override
    /**
     * Cloning not allowed for a singleton object.
     */
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Retrieves the ListModel being used to hold all the tasks for the UI. This
     * returns the sorted model (
     * <code>SortedListModel</code>), but it is untyped to allow for greater
     * flexibility by the caller.
     *
     * @return Sorted
     * <code>ListModel</code> holding tasks.
     */
    public ListModel getTasksModel() {
        return sortedTasksModel;
    }

    /**
     * Returns the typed version of the ListModel being used to hold all the
     * tasks.
     *
     * @return
     * <code>SortedListModel</code> holding tasks.
     */
    public SortedListModel getSortedTasksModel() {
        return sortedTasksModel;
    }

    /**
     * Retrieves the specialized
     * <code>ListModel</code> being used to hold all the tasks to allow
     * stronger-typed access to the model's special features. UI elements should
     * typically use
     * <code>getTasksModel()</code>.
     *
     * @return
     * <code>TasksListModel</code> holding tasks.
     */
    public TasksListModel getSpecializedTasksModel() {
        return (TasksListModel) tasksModel;
    }

    /**
     * Sends a task completion command to the server with the provided outcome
     * and notes (both are optional; no checking exists so make sure it is valid
     * content).
     *
     * @param task Task to set as complete
     * @param outcome The selected outcome for a task (use
     * <code>null</code> if none)
     * @param notes Optionally any notes to submit with the task (use
     * <code>null</code> for none).
     * @return true if server completes the request
     */
    public boolean completeTask(Task task, String outcome, Map<String, String> notes) {
        boolean ret = BpmClientController.getInstance().completeTask(task, outcome, notes);

        // will not need to manually refresh the tasks now that notifications are used

        return ret;
    }

    /**
     * Instructs the server to assign the task to the user currently logged into
     * the server (set via the COW preferences ({@link CowSettingsPanel}).
     *
     * @param task Task to claim for the user
     * @return true if the server completes the request
     */
    public boolean claimTask(Task task) {

        boolean ret = BpmClientController.getInstance().claimTaskForUser(
                task, BpmClientController.getInstance().getUser());

        // will not need to manually refresh the tasks now that notifications are used

        return ret;
    }

    /**
     * Clears all task information from the data models and gets the latest
     * results from the server.
     *
     * @return returns true if successfully updated, false if update failed
     * (contents will still be cleared)
     */
    public boolean refreshTasksFromServer() {
        List<Task> newUserTasks;
        List<Task> newAvailableTasks;
        final Date date = new Date();
        String msg;

        log.info("Tasks requested to be refreshed from the server at " + date.toString());

        try {
            if (!BpmClientController.getInstance().isInitialized()) {
                // try to initialize the controller if this is the first call
                if (!BpmClientController.getInstance().initialize()) {
                    log.error("BpmClientController did not initialize");
                    return false;
                }
            }
            newUserTasks = BpmClientController.getInstance().getTasksForUser(BpmClientController.getInstance().getUser());
            newAvailableTasks = BpmClientController.getInstance().getUnassignedTasksForUser(BpmClientController.getInstance().getUser());
        } catch (IllegalStateException e) {
            log.error("Client lost connection to server.\n" + e.getLocalizedMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected exception while calling BpmClientController.\n" + e.getLocalizedMessage());
            return false;
        }

        // TODO we will get a null back if we can't get to the server at all,
        // need better error notification!

        if (newUserTasks == null) {
            msg = "No tasks assigned to user. ";
        } else {
            msg = newUserTasks.size() + " tasks assigned to user. ";
        }
        if (newAvailableTasks == null) {
            msg += "No unassigned tasks available.";
        } else {
            msg += newAvailableTasks.size() + " unassigned tasks available to user.";
        }

        log.info(msg + " Now updating model...");
        clearModelTasks();

        lastUpdateTime = date;

        if (newUserTasks != null) {
            for (Task t : newUserTasks) {
                addAssignedTask(t);
            }
        }
        if (newAvailableTasks != null) {
            for (Task t : newAvailableTasks) {
                addAvailableTask(t);
            }
        }

        if (newUserTasks.isEmpty() && newAvailableTasks.isEmpty()) {
            log.info("No tasks received from server. Either no tasks were available or server could not be reached. ("
                    + lastUpdateTime.toString() + ")");
        } else {
            log.info("Tasks updated from server at " + lastUpdateTime.toString());
        }

        return true;
    }

    /**
     * Removes all of the tasks from the model. The model will be empty after
     * this call returns.
     */
    public void clearModelTasks() {
        getSpecializedTasksModel().clearAllTasks();
    }

    @Override
    synchronized public void addAssignedTask(Task t) {
        getSpecializedTasksModel().addAssignedTask(t);
        //System.out.println("Assigned task added to tasks model " + t.getId());

        // blink the top component briefly since this is an important notification
        CowTopComponent.getDefault().requestAttention(true);
        // TODO - could allow for user to control this feature via the options
    }

    @Override
    synchronized public void addAvailableTask(Task t) {
        getSpecializedTasksModel().addUnassignedTask(t);
        //System.out.println("Available task added to tasks model " + t.getId());

    }

    @Override
    synchronized public boolean removeAvailableTask(Task t) {
        //System.out.println("Available task removed from tasks model " + t.getId());
        return getSpecializedTasksModel().removeTask(t);
    }

    @Override
    public boolean removeAssignedTask(String taskId) {
        //System.out.println("Assigned task removed from tasks model " + taskId);
        return getSpecializedTasksModel().removeTaskById(taskId);
    }

    @Override
    public void refreshTasks() {
        refreshTasksFromServer();
    }

    @Override
    public void serverUpdated() {
        refreshTasks();
    }

    @Override
    public void serverDown() {
        clearModelTasks();
        StatusBar.getInstance().setStatusText(
                "Connection to server could not be established. Please check the connection settings.");
    }
}
