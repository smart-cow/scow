package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Task;
import org.jbpm.task.event.DefaultTaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class JbpmTaskEventListener extends DefaultTaskEventListener implements BeanFactoryAware  {

	private static Logger log = Logger.getLogger(JbpmTaskEventListener.class);

	@Autowired
	org.jbpm.task.TaskService taskClient;
	
	private BeanFactory beanFactory;

	/**
	 * This event is triggered when a task becomes available, EXCEPT in the case where 
	 * the task is directly assigned to a user, in which case the taskClaimed event is
	 * fired without any taskCreated
	 */
	@Override
	public void taskCreated(TaskUserEvent event) {
		log.info("taskCreated = " + event.getTaskId());
		Task task = taskClient.getTask(event.getTaskId());
		log.info("User: " + event.getUserId());
		log.info("potential owners: ");
		for (OrganizationalEntity e : task.getPeopleAssignments().getPotentialOwners()) {
			log.info(e.toString());
		}
	}

	/**
	 * This event is triggered when a task is assigned to a user, OR when a task that is
	 * directly assigned to the user is first created.  When the task is directly assigned,
	 * it DOES NOT trigger a TaskCreated event.
	 */
	@Override
	public void taskClaimed(TaskUserEvent event) {
		log.info("taskClaimed = " + event.getTaskId());
		Task task = taskClient.getTask(event.getTaskId());
		log.info("User: " + event.getUserId());
		log.info("potential owners: ");
		for (OrganizationalEntity e : task.getPeopleAssignments().getPotentialOwners()) {
			log.info(e.toString());
			if (e.getId().equals(event.getUserId())) {
				log.info("New task created and assigned to owner");
			}
		}		
	}

	/**
	 * This event is triggered when a task is started.  As currently implemented, this happens at the same time
	 * as the task is completed, as we do not support a separate "start" event.
	 */
	@Override
	public void taskStarted(TaskUserEvent event) {

		log.info("taskStarted = " + event.getTaskId());
		Long taskId = event.getTaskId();
		try {
			Task task = taskClient.getTask(taskId);
			String processId = task.getTaskData().getProcessId() + "."
					+ String.valueOf(task.getTaskData().getProcessInstanceId());

			String info = "eventType=TaskReady;processID=" + processId
					+ ";taskID=" + taskId;

			AmqpMessageTransactionSynchronizationAdapter adapter = beanFactory.getBean(AmqpMessageTransactionSynchronizationAdapter.class);
			adapter.setMessage(info);
			adapter.setTopicName("process");
			TransactionSynchronizationManager.registerSynchronization(adapter);
			
			// log.info("sending message: " + info);
			// amqp.convertAndSend("amqp.topic", "process", info);
		} catch (Exception e) {
			log.info("ERROR in taskStarted event: " + e);
		}/**/
	}

	@Override
	public void taskStopped(TaskUserEvent event) {
		log.info("taskStopped = " + event.getTaskId());
	}

	@Override
	public void taskReleased(TaskUserEvent event) {
		log.info("taskReleased = " + event.getTaskId());
	}

	/**
	 * This event occurs when a task is completed by a user.  This is triggered after the afterNodeLeft event
	 * on the ProcessEventListener
	 */
	@Override
	public void taskCompleted(TaskUserEvent event) {
		log.info("taskCompleted = " + event.getTaskId());
	}

	@Override
	public void taskFailed(TaskUserEvent event) {
		log.info("taskFailed = " + event.getTaskId());
	}

	@Override
	public void taskSkipped(TaskUserEvent event) {
		log.info("taskSkipped = " + event.getTaskId());
	}

	@Override
	public void taskForwarded(TaskUserEvent event) {
		log.info("taskForwarded = " + event.getTaskId());
	}

	@Override
	public void setBeanFactory(BeanFactory factory) throws BeansException {
		this.beanFactory = factory;
	}

}
