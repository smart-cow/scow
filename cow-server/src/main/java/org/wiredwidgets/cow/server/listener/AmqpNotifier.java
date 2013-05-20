/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.User;
import org.wiredwidgets.cow.server.repo.TaskRepository;

/**
 * 
 * @author FITZPATRICK
 */
@Component
public class AmqpNotifier {
	private static Logger log = Logger.getLogger(AmqpNotifier.class);

	@Autowired
	TaskRepository taskRepo;

	@Autowired
	AmqpTemplate amqpTemplate;

	/**
	 * Publishes a message to amqp that describes what action a user has just
	 * performed on a task. These actions may be either TaskTaken or
	 * TaskCompleted. This calls checkInitialized to see if there is a valid
	 * connection to qpid.
	 * 
	 * @param task
	 *            the task that has been acted upon
	 * @param exchangeName
	 *            the exchange name for messaging
	 * @param eventName
	 *            the event that correlates with the action
	 * @param taskId
	 *            the id of the task that was acted upon
	 */
	public void amqpTaskPublish(Task task, String exchangeName,
			String eventName, String taskId) {
		String info = "";
		if (eventName.equals("TaskTaken")) {

			try {
				info = "eventType=" + eventName + ";" + "processID="
						+ task.getProcessInstanceId() + ";" + "taskID="
						+ task.getId() + ";" + "assignee=" + task.getAssignee()
						+ ";";
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
		} else if (eventName.equals("TaskCompleted")) {
			try {
				org.jbpm.task.Task ht = taskRepo.findById(Long.decode(taskId));
				String processName = ht.getTaskData().getProcessId();
				String processId = Long.toString(ht.getTaskData()
						.getProcessInstanceId());
				String assignee = ht.getTaskData().getActualOwner().getId();
				info = "eventType=" + eventName + ";" + "processID="
						+ processName + "." + processId + ";" + "taskID="
						+ taskId + ";" + "assignee=" + assignee + ";";
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
		} else if (eventName.equals("TaskReady")) {
			try {
				org.jbpm.task.Task ht = taskRepo.findById(Long.decode(taskId));
				String processName = ht.getTaskData().getProcessId();
				String processId = Long.toString(ht.getTaskData()
						.getProcessInstanceId());
				String assignee = ht.getTaskData().getActualOwner().getId();
				info = "eventType=" + eventName + ";" + "processID="
						+ processName + "." + processId + ";" + "taskID="
						+ taskId + ";" + "assignee=" + assignee + ";";
			} catch (Exception e) {
				log.debug(e.getMessage());
			}
		}
		sendMessage(info, exchangeName);
	}

	/**
	 * Publishes a message that describes what action a user has just performed
	 * on a process.
	 * 
	 * @param processId
	 *            the id of the process that was acted upon
	 * @param exchangeName
	 *            the exchange name for messaging
	 * @param eventName
	 *            the event that correlates with the action
	 */
	public void amqpProcessPublish(String processId, String exchangeName,
			String eventName) {
		String info = "eventType=" + eventName + ";" + "processID=" + processId
				+ ";";
		sendMessage(info, exchangeName);
	}

	/**
	 * Publishes a message that a new user was created.
	 * 
	 * @param user
	 *            the user that was just created
	 * @param exchangeName
	 *            the exchange name for messaging
	 * @param eventName
	 *            the event that correlates with the action
	 */
	public void amqpNewUserPublish(User user, String exchangeName,
			String eventName) {
		String info = "eventType=" + eventName + ";" + "userID=" + user.getId()
				+ ";";
		sendMessage(info, exchangeName);
	}
	

	// Sends a message using the exchange name over rabbit
	private void sendMessage(String message, String exchangeName) {

		/*try {
			log.debug("sending amqp message: " +  message);
			amqpTemplate.convertAndSend("amq.topic", "process", message);
		} catch (AmqpException e) {
			log.error(e);
		}*/
                TimeoutRetryPolicy retry = new TimeoutRetryPolicy();
                retry.setTimeout(600000L);
                RetryTemplate retryTemplate = new RetryTemplate();
                retryTemplate.setRetryPolicy(retry);
                final String msg = message;
                try{                     
                    String result = retryTemplate.execute(new RetryCallback<String>() {
                        
                        public String doWithRetry(RetryContext context) {
                            log.debug("sending amqp message: " +  msg);
                            amqpTemplate.convertAndSend("amq.topic", "process", msg);
                            return "Message Sent";                            
                        }                       
                     });
                    
                    log.info(result); 
                    
                }catch(Exception e){
                    log.error(e);
                }
	}

}
