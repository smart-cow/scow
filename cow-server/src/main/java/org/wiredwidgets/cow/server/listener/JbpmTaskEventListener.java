/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.server.listener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.User;
import org.jbpm.task.event.DefaultTaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StopWatch.TaskInfo;

@Component
public class JbpmTaskEventListener extends DefaultTaskEventListener  {

	private static Logger log = Logger.getLogger(JbpmTaskEventListener.class);

	@Autowired
	org.jbpm.task.TaskService taskClient;
	
	@Autowired 
	ConversionService converter;
		
	private List<TasksEventListener> tasksListeners_;

	@Resource(name="tasksListeners")
	public void setListeners(List<TasksEventListener> listeners) {
		tasksListeners_ = listeners;
	}
	
	/**
	 * This event is triggered when a task becomes available, EXCEPT in the case where 
	 * the task is directly assigned to a user, in which case the taskClaimed event is
	 * fired without any taskCreated
	 */
	@Override
	public void taskCreated(TaskUserEvent event) {		
		final TasksEventListener.EventParameters evtParams = getEventParams(event);	
		
		registerSyncCallback(new TransactionSynchronizationAdapter() {	
			
			public void afterCompletion(int i) {
				for (TasksEventListener listener : tasksListeners_) {
					listener.onCreateTask(evtParams);
				}
			}
		});
	}

	/**
	 * This event is triggered when a task is assigned to a user, OR when a task that is
	 * directly assigned to the user is first created.  When the task is directly assigned,
	 * it DOES NOT trigger a TaskCreated event.
	 */
	@Override
	public void taskClaimed(TaskUserEvent event) {
		final TasksEventListener.EventParameters evtParams = getEventParams(event);
		
		registerSyncCallback(new TransactionSynchronizationAdapter() {
			public void afterCompletion(int i) {
				for (TasksEventListener listener : tasksListeners_) {
					listener.onTakeTask(evtParams);
				}
			}
		});
	}

	/**
	 * This event is triggered when a task is started.  As currently implemented, this happens 
	 * at the same time as the task is completed, as we do not support a separate "start" event.
	 */
//	@Override
//	public void taskStarted(TaskUserEvent event) {
//
//		log.info("taskStarted = " + event.getTaskId());
//		Long taskId = event.getTaskId();
//		try {
//			org.jbpm.task.Task task = taskClient.getTask(taskId);
//			String processId = task.getTaskData().getProcessId() + "."
//					+ String.valueOf(task.getTaskData().getProcessInstanceId());
//
//			String info = "eventType=TaskReady;processID=" + processId
//					+ ";taskID=" + taskId;
//
//			AmqpMessageTransactionSynchronizationAdapter adapter = beanFactory.getBean(AmqpMessageTransactionSynchronizationAdapter.class);
//			adapter.setMessage(info);
//			adapter.setTopicName("process");
//			TransactionSynchronizationManager.registerSynchronization(adapter);
//			
//			// log.info("sending message: " + info);
//			// amqp.convertAndSend("amqp.topic", "process", info);
//		} catch (Exception e) {
//			log.info("ERROR in taskStarted event: " + e);
//		}/**/
//	}
	
	
//	@Override
//	public void taskStarted(TaskUserEvent event) {
//
//		log.info("taskStarted = " + event.getTaskId());
//		Long taskId = event.getTaskId();
//		try {
//			org.jbpm.task.Task jbpmTask = taskClient.getTask(taskId);
//			//tasksListener.onCompleteTask(convert(jbpmTask));
//			
//			// log.info("sending message: " + info);
//			// amqp.convertAndSend("amqp.topic", "process", info);
//		} catch (Exception e) {
//			log.info("ERROR in taskStarted event: " + e);
//		}/**/
//	}
	
	@Override
	public void taskStarted(TaskUserEvent event) {
		log.info("taskStarted = " + event.getTaskId());
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
	 * This event occurs when a task is completed by a user.  This is triggered after the 
	 * afterNodeLeft event on the ProcessEventListener
	 */
	
	@Override
	public void taskCompleted(TaskUserEvent event) {
		log.info("taskCompleted=" + event.getTaskId());
		
		final TasksEventListener.EventParameters evtParams = getEventParams(event);
		registerSyncCallback(new TransactionSynchronizationAdapter() {
			public void afterCompletion(int i) {
				for (TasksEventListener listener : tasksListeners_) {
					listener.onCompleteTask(evtParams);
				}
			}
		});

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


	
	
	
	private org.wiredwidgets.cow.server.api.service.Task convert(org.jbpm.task.Task jbpmtask) {
		return converter.convert(jbpmtask, org.wiredwidgets.cow.server.api.service.Task.class);
	}
	
	
	private static void registerSyncCallback(TransactionSynchronizationAdapter syncAdapter) {
		TransactionSynchronizationManager.registerSynchronization(syncAdapter);
	}

	
	private TasksEventListener.EventParameters getEventParams(TaskUserEvent jbpmEvent) {
		org.jbpm.task.Task jbpmTask = taskClient.getTask(jbpmEvent.getTaskId());
		String eventUser = jbpmEvent.getUserId();
	
		return new TasksEventListener.EventParameters(convert(jbpmTask), getGroups(jbpmTask), 
				getUsers(eventUser, jbpmTask));
	}
	
	
	private static List<String> getGroups(org.jbpm.task.Task jbpmTask) {
		List<String> groups = new ArrayList<String>();
		
		List<OrganizationalEntity> owners = jbpmTask.getPeopleAssignments().getPotentialOwners();
		for (OrganizationalEntity owner : owners) {
			if (owner instanceof Group) {
				groups.add(owner.getId());
			}
		}
		return groups;
	}
	
	
	private static List<String> getUsers(String eventUser, org.jbpm.task.Task jbpmTask) {
		List<String> users = new ArrayList<String>();
		if (eventUser != null && !eventUser.isEmpty()) {
			users.add(eventUser);
		}
		
		List<OrganizationalEntity> owners = jbpmTask.getPeopleAssignments().getPotentialOwners();
		for (OrganizationalEntity owner : owners) {
			if (owner instanceof User) {
				users.add(owner.getId());
			}
		}
		return users;
	}
	
}
