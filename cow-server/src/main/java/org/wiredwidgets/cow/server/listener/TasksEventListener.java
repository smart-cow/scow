package org.wiredwidgets.cow.server.listener;

import java.util.List;

import org.wiredwidgets.cow.server.api.service.Task;

public interface TasksEventListener {
	    
	    public void onCreateTask(EventParameters evtParams);
	    
	    public void onCompleteTask(EventParameters evtParams);
	    
	    public void onTakeTask(EventParameters evtParams);
	    
	    /*   
	    public void onUpdateTask(Task task);
	    
	    public void onAddGroupParticipation(Task task, Group group);
	    
	    public void onDeleteGroupParticipation(Task task, Group group);
	    
	    public void onAddUserParticipation(Task task, User user);
	    
	    public void onDeleteUserParticipation(Task task, User user);
	    */
	    
	    public class EventParameters {
			private Task task_;
	    	private List<String> groups_;
	    	private List<String> users_;
	    	
	    	public EventParameters(Task task, List<String> groups, List<String> users) {
	    		task_ = task;
	    		groups_ = groups;
	    		users_ = users;
	    	}
	    	
	    	public Task getTask() {
				return task_;
			}
			public List<String> getGroups() {
				return groups_;
			}
			public List<String> getUsers() {
				return users_;
			}

	    }
}
