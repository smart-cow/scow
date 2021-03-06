package org.wiredwidgets.cow.ac.client.models;

import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variables;

/**
 * The <code>Task</code> class is autogenerated by JAXB and does not provide adequate
 * methods to compare Tasks already retrieved from the server with updates coming in.
 * So, this class wraps the tasks and provides an equality method based on ID.
 * <p>
 * TODO: Adjust JAXB to generate the needed methods.
 * <p>
 * TODO: Check {@link TasksListModel#removeTaskById(java.lang.String) } 
 * (and possibly other methods) if the equality is redone to work on things other
 * than just ID.
 * @author MJRUSSELL
 */
public class TaskWithEquality extends Task {
    private Task task;
    
    public TaskWithEquality(Task t) {
          task = t;
    }

    @Override
    public void setVariables(Variables value) {
        task.setVariables(value);
    }

    @Override
    public void setState(String value) {
        task.setState(value);
    }

    @Override
    public void setProgress(Integer value) {
        task.setProgress(value);
    }

    @Override
    public void setProcessInstanceUrl(String value) {
        task.setProcessInstanceUrl(value);
    }

    @Override
    public void setProcessInstanceId(String value) {
        task.setProcessInstanceId(value);
    }

    @Override
    public void setPriority(Integer value) {
        task.setPriority(value);
    }

    @Override
    public void setName(String value) {
        task.setName(value);
    }

    @Override
    public void setId(String value) {
        task.setId(value);
    }

    @Override
    public void setEndTime(XMLGregorianCalendar value) {
        task.setEndTime(value);
    }

    @Override
    public void setDuration(Long value) {
        task.setDuration(value);
    }

    @Override
    public void setDueDate(XMLGregorianCalendar value) {
        task.setDueDate(value);
    }

    @Override
    public void setDescription(String value) {
        task.setDescription(value);
    }

    @Override
    public void setCreateTime(XMLGregorianCalendar value) {
        task.setCreateTime(value);
    }

    @Override
    public void setAssignee(String value) {
        task.setAssignee(value);
    }

    @Override
    public void setActivityName(String value) {
        task.setActivityName(value);
    }

    @Override
    public Variables getVariables() {
        return task.getVariables();
    }

    @Override
    public String getState() {
        return task.getState();
    }

    @Override
    public Integer getProgress() {
        return task.getProgress();
    }

    @Override
    public String getProcessInstanceUrl() {
        return task.getProcessInstanceUrl();
    }

    @Override
    public String getProcessInstanceId() {
        return task.getProcessInstanceId();
    }

    @Override
    public Integer getPriority() {
        return task.getPriority();
    }

    @Override
    public List<String> getOutcomes() {
        return task.getOutcomes();
    }

    @Override
    public String getName() {
        return task.getName();
    }

    @Override
    public String getId() {
        return task.getId();
    }

    @Override
    public XMLGregorianCalendar getEndTime() {
        return task.getEndTime();
    }

    @Override
    public Long getDuration() {
        return task.getDuration();
    }

    @Override
    public XMLGregorianCalendar getDueDate() {
        return task.getDueDate();
    }

    @Override
    public String getDescription() {
        return task.getDescription();
    }

    @Override
    public XMLGregorianCalendar getCreateTime() {
        return task.getCreateTime();
    }

    @Override
    public String getAssignee() {
        return task.getAssignee();
    }

    @Override
    public String getActivityName() {
        return task.getActivityName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Task) {
            if (this.getId().equals(((Task) obj).getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
