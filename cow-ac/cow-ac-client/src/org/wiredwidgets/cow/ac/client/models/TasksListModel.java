package org.wiredwidgets.cow.ac.client.models;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * A specialized table model that keeps track of tasks in two buckets to
 * distinguish between those that are assigned to the user and those that are
 * available for assignment. Being able to distinguish helps make driving some
 * of the UI elements easier, but a single model was needed to make displaying
 * all the tasks in a single.
 * <p>
 * <Code>TaskWithEquality</code> objects are actually used rather than tasks due
 * to a weakness in the generated Task class that doesn't include an adequate
 * equality method.
 * <p>
 * Much of the standard ListModel stuff is just copied out of <code>AbstractListModel</code>
 *
 * @see TaskWithEquality
 * @author RYANMILLER, modified by MJRUSSEL to use TaskWithEquality
 */
public class TasksListModel implements ListModel {

    /**
     * list of listeners of events about updates to the elements
     */
    protected EventListenerList listenerList = new EventListenerList();
    /**
     * the combined list of tasks
     */
    private List<TaskWithEquality> delegate;
    /**
     * tasks which have been assigned to a user (presumably the user logged in
     * to AgileClient, but this is not checked for by the model)
     */
    private List<TaskWithEquality> assignedTasks;
    /**
     * tasks which are not assigned to any user
     */
    private List<TaskWithEquality> availableTasks;

    public TasksListModel() {
        delegate = new ArrayList<TaskWithEquality>();
// rdm 3/15/2012 - This was the alterate way of adjusting for the inadequate 
//        equality method (on all three lists). kept around for reference.
//        
//        delegate = new ArrayList<Task>(); 
//        {
//            @Override
//            public boolean contains(Object o) {
//                if (o instanceof Task) {
//                    Task testEqualsTask = (Task) o;
//                    for (Task t : this) {
//                        if (testEqualsTask.getId().equals(t.getId())) {
//                            return true;
//                        }
//                    }
//                    return false;
//                } else {
//                    return false;
//                }
//            }
//        };
        assignedTasks = new ArrayList<TaskWithEquality>();

        availableTasks = new ArrayList<TaskWithEquality>();
    }

    @Override
    public int getSize() {
        return delegate.size();
    }

    @Override
    public Object getElementAt(int index) {
        return delegate.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    /**
     * Returns an array of the objects registered as listeners to this class
     *
     * @return the listeners registered with this class
     */
    public ListDataListener[] getListDataListeners() {
        return listenerList.getListeners(
                ListDataListener.class);
    }

    /**
     * <b>Copied from <code>AbstractListModel</code></b>
     * <p>
     * <code>AbstractListModel</code> subclasses must call this method
     * <b>after</b> one or more elements of the list change. The changed
     * elements are specified by the closed interval index0, index1 -- the
     * endpoints are included. Note that index0 need not be less than or equal
     * to index1.
     *
     * @param source the
     * <code>ListModel</code> that changed, typically "this"
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     * @see DefaultListModel
     */
    protected void fireContentsChanged(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }

    /**
     * <b>Copied from <code>AbstractListModel</code></b>
     * <p>
     * <code>AbstractListModel</code> subclasses must call this method
     * <b>after</b> one or more elements are added to the model. The new
     * elements are specified by a closed interval index0, index1 -- the
     * enpoints are included. Note that index0 need not be less than or equal to
     * index1.
     *
     * @param source the
     * <code>ListModel</code> that changed, typically "this"
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     * @see DefaultListModel
     */
    protected void fireIntervalAdded(Object source, int index0, int index1) {
        // TODO - check and clean up
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalAdded(e);
            }
        }
    }

    /**
     * <b>Copied from <code>AbstractListModel</code></b>
     * <p>
     * <code>AbstractListModel</code> subclasses must call this method
     * <b>after</b> one or more elements are removed from the model.
     * <code>index0</code> and
     * <code>index1</code> are the end points of the interval that's been
     * removed. Note that
     * <code>index0</code> need not be less than or equal to
     * <code>index1</code>.
     *
     * @param source the
     * <code>ListModel</code> that changed, typically "this"
     * @param index0 one end of the removed interval, including
     * <code>index0</code>
     * @param index1 the other end of the removed interval, including
     * <code>index1</code>
     * @see EventListenerList
     * @see DefaultListModel
     */
    protected void fireIntervalRemoved(Object source, int index0, int index1) {
        // TODO - check and clean up
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalRemoved(e);
            }
        }
    }

    /**
     * Returns the registered event listeners
     *
     * @param <T>
     * @param listenerType
     * @return registered event listeners
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return listenerList.getListeners(listenerType);
    }

    /**
     * Indicates if the list has no data.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Indicates of the list contains a particular task
     * @param task the task for which to check
     * @return true if the list contains <b>task</b>
     */
    public boolean contains(Task task) {
        return delegate.contains(new TaskWithEquality(task));
    }

    /**
     * Returns the index of a task in the list. 
     * @param task
     * @return the index number
     * @see List#indexOf(java.lang.Object) 
     */
    public int indexOf(Task task) {
        return delegate.indexOf(new TaskWithEquality(task));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Custom methods that actually need to realize we're dealing with BPM Tasks
    // vice more generic list data model methods
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Checks if the task is in the list of tasks available to the user
     *
     * @param task the task to check for
     * @return true if <b>task</b> is an available task
     */
    public boolean isAvailableTask(Task task) {
        if (availableTasks.contains(new TaskWithEquality(task))) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the task is in the list of tasks assigned to the user
     *
     * @param task the task to check for
     * @return true if <b>task</b> is an assigned task
     */
    public boolean isAssignedTask(Task task) {
        if (assignedTasks.contains(new TaskWithEquality(task))) {
            return true;
        }
        return false;
    }

    /**
     * The count of all tasks assigned specifically to the user
     *
     * @return the count
     */
    public int getUserTaskCount() {
        return assignedTasks.size();
    }

    /**
     * The count of all tasks available for assignment in the user's group
     *
     * @return the count
     */
    public int getUnassignedTaskCount() {
        return availableTasks.size();
    }

    /**
     * Adds a task which has been assigned to the user (the model does not check
     * the user and it is up to the caller to add the 
     * <code>Task</code> via the correct method)
     *
     * @param task the task to add into the model
     */
    public void addAssignedTask(Task task) {
        assignedTasks.add(new TaskWithEquality(task));
        delegate.add(new TaskWithEquality(task));
        fireIntervalAdded(this, delegate.size() - 1, delegate.size() - 1);
    }

    /**
     * Adds a task which has been assigned to the user (the model does not check
     * the user and it is up to the caller to add the 
     * <code>Task</code> via the correct method)
     *
     * @param task the task to add into the model
     */
    public void addUnassignedTask(Task task) {
        availableTasks.add(new TaskWithEquality(task));
        delegate.add(new TaskWithEquality(task));
        fireIntervalAdded(this, delegate.size() - 1, delegate.size() - 1);
    }

    /**
     * Removes the first occurrence of task from the model (if present). If the
     * model does not contain the task, the model is unchanged.
     *
     * @param task the task to remove
     * @return returns false of the task was not in the model and therefore not
     * removed, otherwise true
     */
    public boolean removeTask(Task task) {
        int ret = delegate.indexOf(new TaskWithEquality(task));

        if (-1 == ret) {  // not found
            return false;
        }

        delegate.remove(ret);
        // don't do any checking since the task should be in one of the lists if we got here
        availableTasks.remove(new TaskWithEquality(task));
        assignedTasks.remove(new TaskWithEquality(task));
        // TODO for now, firing all contents changed, but should be improved)
        fireIntervalRemoved(this, ret, ret);  
        return true;
    }

    /**
     * Removes the first occurrence of task from the model with the specified
     * taskId (if present). If the model does not contain a task with the id,
     * the model is unchanged.
     *
     * @param task the task to remove
     * @return returns false of the task was not in the model and therefore not
     * removed, otherwise true
     */
    public boolean removeTaskById(String taskId) {
        Task searchTask = new Task(); 
        searchTask.setId(taskId);
        
        // TODO - it seems dangerous to call removeTask since it removes things
        // based on an equality test rather than just by the ID.
        return removeTask(searchTask);
    }

    /**
     * Removes all of the tasks from the model. The model will be empty after
     * this call returns.
     */
    public void clearAllTasks() {
        availableTasks.clear();
        assignedTasks.clear();

        // we need to track how big the delegate size was in order to properly
        // fire the interval removed event
        int lastIndex = delegate.size() - 1;
        if (-1 == lastIndex) {
            lastIndex = 0;
        }

        delegate.clear();
        fireIntervalRemoved(this, 0, lastIndex);
    }
}