package org.wiredwidgets.cow.server.listener.amqp;

import java.util.List;

import org.apache.log4j.Logger;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.User;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.service.ProcessInstanceService;

@Component
public class AmqpSender {
	
	private static Logger log = Logger.getLogger(AmqpSender.class);
	
	@Autowired
	AmqpTemplate amqpTemplate;
	
    @Autowired
    ProcessInstanceService processInstanceService;

	private RetryTemplate retryTemplate_;
	
	public AmqpSender() {
		TimeoutRetryPolicy retry = new TimeoutRetryPolicy();
		retry.setTimeout(600000L);
		retryTemplate_ = new RetryTemplate();
		retryTemplate_.setRetryPolicy(retry);
	}
	

	
	public void send(String pid, String category, String action, final Object message, 
			List<OrganizationalEntity> owners) {
		
		if (owners == null || owners.isEmpty()) {
			send(getRoutingKey(pid, category, action), message);
			return;
		}
		for (OrganizationalEntity owner : owners) {
			send(getRoutingKey(pid, category, action, owner), message);
		}
	}
	
	
	
	private void send(final String routingKey, final Object body) {
		try {
			String result = retryTemplate_.execute(new RetryCallback<String>() {
				
				public String doWithRetry(RetryContext context) throws Exception {	
					log.debug("sending amqp message to: " + routingKey);
					amqpTemplate.convertAndSend(routingKey, body);
					return "\nMessage Sent to: \n" + routingKey + "\n";
				}
			});
			log.info(result);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	
	
	private String getWorkflowKey(String pid) {
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
	
	
	
	private String getRoutingKey(String pid, String category, String action) {
		String workflowKey = getWorkflowKey(pid);
		String rk = String.format("%s.%s.%s", workflowKey, category, action);
		
		return rk;
	}
	
	
	private String getRoutingKey(String pid, String category, String action, 
			OrganizationalEntity owner) {
		String rk = getRoutingKey(pid, category, action);
		if (owner instanceof Group) {
			rk += ".group.";
		}
		else if (owner instanceof User) {
			rk += ".user.";
		}
		return rk + owner.getId();
	}
}
