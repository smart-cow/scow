package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JbpmTaskEventListener implements TaskEventListener {
	
    private static Logger log = Logger.getLogger(JbpmTaskEventListener.class);
    
    @Autowired
    AmqpTemplate amqp;	

	@Override
	public void taskCreated(TaskUserEvent event) {
		Long taskId = event.getTaskId();
		
		String info = "eventType=TaskReady;taskID=" + taskId;
		
		log.info("sending message: " + info);
		amqp.convertAndSend("amqp.topic", "process", info);
    }		


	@Override
	public void taskClaimed(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskStarted(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskStopped(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskReleased(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskCompleted(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskFailed(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskSkipped(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskForwarded(TaskUserEvent event) {
		// TODO Auto-generated method stub

	}

}
