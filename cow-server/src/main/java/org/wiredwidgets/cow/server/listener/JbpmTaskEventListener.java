package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.jbpm.task.Task;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.task.service.local.LocalTaskService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.manager.TaskServiceFactory;
import org.wiredwidgets.cow.server.service.TaskService;

@Component
public class JbpmTaskEventListener implements TaskEventListener {
	
    private static Logger log = Logger.getLogger(JbpmTaskEventListener.class);
    
    @Autowired
    AmqpTemplate amqp;	
    
    //@Autowired
    //TaskService taskService;
    
	@Autowired
	//LocalTaskService jbpmTaskService;
        TaskServiceFactory taskServiceFactory;

	@Override
	public void taskCreated(TaskUserEvent event) {
                System.out.println("taskCreated = " + event.getTaskId());
		Long taskId = event.getTaskId();
		//Task task = jbpmTaskService.getTask(taskId);
                Task task = taskServiceFactory.getTaskService().getTask(taskId);
		String processId = task.getTaskData().getProcessId() 
				+ "." + String.valueOf(task.getTaskData().getProcessInstanceId());
		
		String info = "eventType=TaskReady;processID=" + processId + ";taskID=" + taskId;
		
		log.info("sending message: " + info);
		amqp.convertAndSend("amqp.topic", "process", info);
    }		


	@Override
	public void taskClaimed(TaskUserEvent event) {
		
                System.out.println("\ntaskClaimed = " + event.getTaskId());
                Long taskId = event.getTaskId();
		//Task task = jbpmTaskService.getTask(taskId);
                try{
                Task task = taskServiceFactory.getTaskService().getTask(taskId);
		String processId = task.getTaskData().getProcessId() 
				+ "." + String.valueOf(task.getTaskData().getProcessInstanceId());
		
		String info = "eventType=TaskClaimed;processID=" + processId + ";taskID=" + taskId;
		
		log.info("sending message: " + info);
		amqp.convertAndSend("amqp.topic", "process", info);
                }catch(Exception e){
                 log.info("task claimed error: " + e);    
                }
	}

	@Override
	public void taskStarted(TaskUserEvent event) {
		
                System.out.println("\ntaskStarted = " + event.getTaskId());       
                Long taskId = event.getTaskId();
		//Task task = jbpmTaskService.getTask(taskId);
                Task task = taskServiceFactory.getTaskService().getTask(taskId);
		String processId = task.getTaskData().getProcessId() 
				+ "." + String.valueOf(task.getTaskData().getProcessInstanceId());
		
		String info = "eventType=TaskReady;processID=" + processId + ";taskID=" + taskId;
		
		log.info("sending message: " + info);
		amqp.convertAndSend("amqp.topic", "process", info);
	}

	@Override
	public void taskStopped(TaskUserEvent event) {
		// TODO Auto-generated method stub
System.out.println("taskStopped = " + event.getTaskId());
	}

	@Override
	public void taskReleased(TaskUserEvent event) {
		// TODO Auto-generated method stub
System.out.println("taskReleased = " + event.getTaskId());
	}

	@Override
	public void taskCompleted(TaskUserEvent event) {
		// TODO Auto-generated method stub
System.out.println("taskCompleted = " + event.getTaskId());
	}

	@Override
	public void taskFailed(TaskUserEvent event) {
		// TODO Auto-generated method stub
System.out.println("taskFailed = " + event.getTaskId());
	}

	@Override
	public void taskSkipped(TaskUserEvent event) {
		// TODO Auto-generated method stub
System.out.println("taskSkipped = " + event.getTaskId());
	}

	@Override
	public void taskForwarded(TaskUserEvent event) {
		// TODO Auto-generated method stub
System.out.println("taskForwarded = " + event.getTaskId());
	}

}
