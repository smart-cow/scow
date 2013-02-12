package org.wiredwidgets.cow.ac.util.taskworker;

import org.apache.log4j.Logger;
import org.wiredwidgets.cow.ac.client.controllers.TaskController;
import org.wiredwidgets.cow.ac.client.ui.CompleteTaskDialog;
import org.wiredwidgets.cow.ac.client.ui.StatusBar;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * The default means to complete a task in the COW plugin. Other workers can be
 * added for specific tasks by implementing {@link TaskWorkerEventListener} and
 * registering with {@link TaskWorkerManager}.
 *
 * This worker pops up {@link CompleteTaskDialog} to collect some additional
 * information and uses
 * <code>TaskController</code> to notify the server.
 *
 * @author RYANMILLER
 */
public class CowTaskWorker implements TaskWorkerEventListener {

    static final Logger log = Logger.getLogger(CowTaskWorker.class);

    public CowTaskWorker() {
    }

    /**
     * This worker pops up {@link CompleteTaskDialog} to collect some additional
     * information and uses <code>TaskController</code> to notify the server.
     * <p>
     * If the server does not get updated there is no notification via the API.
     * The user is alerted through log messages and an update on the 
     * {@link StatusBar}.
     */
    @Override
    public void workTask(Task task) {
        // trigger a popup dialog to provide additional info task completion info
        CompleteTaskDialog ctd = new CompleteTaskDialog(null, true, task.getOutcomes());
        ctd.setTitle("Complete Task: " + task.getName());
        ctd.setVisible(true);

        // if the user cancelled completeing the task, do nothing
        if (false == ctd.taskCompleted()) {
            return;
        }

        // Continue, getting the results for the dialog box and sending the update to the server

        // call the server with updates via the tasks controller
        boolean ret = TaskController.getInstance().completeTask(task, ctd.getSelectedOutcome(), ctd.getNotes());
        if (false == ret) {
            // server was not updated due to an error. 
            StatusBar.getInstance().setStatusText("Error occured while sending task information to the server. Task was NOT completed.");
            log.error("Error occured while trying to complete task \"" + task + "\". Task was NOT completed or server returned an incorrect response.");
        } else {
            StatusBar.getInstance().setStatusText("Task has been marked as complete.");
            log.info("\"" + task.getName() + "\" has been marked as complete.");
        }

        // Clear the dialog and selection
        ctd.dispose();
    }
}