package org.wiredwidgets.cow.server.listener.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.listener.TasksEventListener;

public class TasksAmqpSender implements TasksEventListener {

	private static final String TASK_CATEGORY = "tasks";
	
	@Autowired
	AmqpSender sender;
	

	@Override
	public void onCreateTask(EventParameters evtParams) {
		send("create", evtParams);
	}


	
	@Override
	public void onCompleteTask(EventParameters evtParams) {
		send("complete", evtParams);
		
	}


	@Override
	public void onTakeTask(EventParameters evtParams) {
		send("take", evtParams);
	}
	
	
	
	private void send(String action, EventParameters evtParams) {
		sender.send(
				evtParams.getTask().getProcessInstanceId(), 
				TASK_CATEGORY, 
				action, 
				evtParams.getTask(), 
				evtParams.getGroups(), 
				evtParams.getUsers());
	}
}
