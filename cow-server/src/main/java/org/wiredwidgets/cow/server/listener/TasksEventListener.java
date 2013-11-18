package org.wiredwidgets.cow.server.listener;

import java.util.List;

import org.jbpm.task.OrganizationalEntity;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.User;

public interface TasksEventListener {
	
	    public void onCreateTask(Task task);
	    
	    public void onCreateTask(Task task, List<OrganizationalEntity> owners);
	    
	    public void onCompleteTask(Task task, List<OrganizationalEntity> owners);
	    
	    public void onCompleteTask(Task task);
	    
	    public void onTakeTask(Task task);
	    
	    public void onTakeTask(Task task, List<OrganizationalEntity> owners);
	    
	    /*   
	    public void onUpdateTask(Task task);
	    
	    public void onAddGroupParticipation(Task task, Group group);
	    
	    public void onDeleteGroupParticipation(Task task, Group group);
	    
	    public void onAddUserParticipation(Task task, User user);
	    
	    public void onDeleteUserParticipation(Task task, User user);
	    */
}
