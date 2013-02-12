package org.wiredwidgets.cow.ac.client.utils;

import java.util.Comparator;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * A Set of comparator classes for <code>Task</code>s that assists in sorting
 * by various criteria
 * @author RYANMILLER
 */
public class TaskComparators {
    
    private TaskComparators() {
        // nothing
    }
    
    /**
     * Creates a Comparator for Task objects based on the create time (Uses the
     * XMLGregorianCalendar compare.
     * @return the comparator
     */
    public static Comparator CreationTimeComparator() {
        return new CreationTimeComparator();
    }

    /**
     * Creates a Comparator for Task objects based on the due date
     * @return the comparator
     */
    public static Comparator DueDateComparator() {
        return new DueDateComparator();
    }
        
    /**
     * Creates a Comparator for Task objects based on the priority
     * @return the comparator
     */
    public static Comparator PriorityComparator() {
        return new PriorityComparator();
    }
    
    /**
     * Creates a Comparator for Task objects based first on the state, then by
     * creation time. (where available > assigned such that assigned and 
     * NEWEST tasks are listed first when using DESCENDING order). A Task is 
     * considered assigned if the assignee is not an empty string or null.
     * @return the comparator
     */
    public static Comparator StateThenCreationTimeComparator() {
        return new StateThenCreationTimeComparator();
    }
    
}

class CreationTimeComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Task) || !(o2 instanceof Task)) // screen non-task objects
            return 0;
    
        Task t1 = (Task)o1;
        Task t2 = (Task)o2;
        return t1.getCreateTime().compare(t2.getCreateTime());
    }
}

class DueDateComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Task) || !(o2 instanceof Task)) // screen non-task objects
            return 0;
    
        Task t1 = (Task)o1;
        Task t2 = (Task)o2;
        return t1.getDueDate().compare(t2.getDueDate());
    }
}
 
class PriorityComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Task) || !(o2 instanceof Task)) // screen non-task objects
            return 0;
    
        Task t1 = (Task)o1;
        Task t2 = (Task)o2;
        
        if (t1.getPriority() > t2.getPriority())
            return 1;
        else if (t1.getPriority() < t2.getPriority())
            return -1;
        else
            return 0;
    }
}

class StateThenCreationTimeComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Task) || !(o2 instanceof Task)) // screen non-task objects
            return 0;
    
        Task t1 = (Task)o1;
        Task t2 = (Task)o2;
        
        if (t1.getAssignee() == null || t1.getAssignee().equals("")) { // task 1 available
            if (t2.getAssignee() == null || t2.getAssignee().equals("")) { // task 2 available
                return t1.getCreateTime().compare(t2.getCreateTime());  // both available, compare normally
            } else { // t2 is assigned while t1 is available
                return -1;   
            }
        } else { // task 1 assigned
            if (t2.getAssignee() == null || t2.getAssignee().equals("")) { // task 2 available
                return 1;    // t1 is assigned while t2 is available
            } else {  // both assigned, compare normally
                return t1.getCreateTime().compare(t2.getCreateTime());  
            }
        }
    }
}