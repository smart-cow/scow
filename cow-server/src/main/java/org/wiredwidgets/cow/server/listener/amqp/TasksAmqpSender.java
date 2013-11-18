package org.wiredwidgets.cow.server.listener.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.listener.TasksEventListener;
import org.wiredwidgets.cow.server.service.ProcessInstanceService;

public class TasksAmqpSender implements TasksEventListener {

	private static final String TASK_CATEGORY = "tasks";
	
	@Autowired
	AmqpSender sender;
	
    @Autowired
    ProcessInstanceService processInstanceService;
	
	@Override
	public void onCreateTask(Task task) {
		send("create", task);
	}

	@Override
	public void onCompleteTask(Task task) {
		send("complete", task);
	}

	@Override
	public void onTakeTask(Task task) {
		send("take", task);
	}
	
	
	private void send(String action, Task task) {
		String rk = getRoutingKey(action, task);
		sender.send(rk, task);
	}
	
	
	private String getRoutingKey(String action, Task task) {
		String workflowId = getWorkflowId(task);
		return String.format("%s.%s.%s", workflowId, TASK_CATEGORY, action);
	}
	
	
	private String getWorkflowId(Task task) {
		String pid = task.getProcessInstanceId();
		if (pid.contains(".")) {
			return pid;
		}
		
		ProcessInstance pi = processInstanceService.getProcessInstance(Long.parseLong(pid));
		pid = pi.getId();
		if (pid.contains(".")) {
			return pid;
		}
		return pi.getProcessDefinitionKey() + '.' + pid;
	}

}
