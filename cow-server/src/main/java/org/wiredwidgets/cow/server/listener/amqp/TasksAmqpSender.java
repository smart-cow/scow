package org.wiredwidgets.cow.server.listener.amqp;

import java.util.Collections;
import java.util.List;

import org.jbpm.task.OrganizationalEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.listener.TasksEventListener;

public class TasksAmqpSender implements TasksEventListener {

	private static final String TASK_CATEGORY = "tasks";
	
	@Autowired
	AmqpSender sender;
	

	
	@Override
	public void onCreateTask(Task task) {
		send("create", task, Collections.<OrganizationalEntity> emptyList());
	}
	
	@Override
	public void onCreateTask(Task task, List<OrganizationalEntity> owners) {
		send("create", task, owners);
	}

	
	@Override
	public void onCompleteTask(Task task) {
		send("complete", task, null);
	}

	@Override
	public void onTakeTask(Task task) {
		send("take", task, null);
	}

	@Override
	public void onCompleteTask(Task task, List<OrganizationalEntity> owners) {
		send("complete", task, owners);
		
	}

	@Override
	public void onTakeTask(Task task, List<OrganizationalEntity> owners) {
		send("take", task, owners);
	}
	
	private void send(String action, Task task, List<OrganizationalEntity> owners) {
		sender.send(task.getProcessInstanceId(), TASK_CATEGORY, action, task, owners);
	}
}
