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
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.service.ProcessInstanceService;

@Component
public class AmqpSender {
	
	private static Logger log = Logger.getLogger(AmqpSender.class);
	
	@Autowired
	AmqpTemplate amqpTemplate;

	private RetryTemplate retryTemplate_;
	
	public AmqpSender() {
		TimeoutRetryPolicy retry = new TimeoutRetryPolicy();
		retry.setTimeout(600000L);
		retryTemplate_ = new RetryTemplate();
		retryTemplate_.setRetryPolicy(retry);
	}
	

	
	public void send(String pid, String category, String action, Object message, 
			List<String> groups, List<String> users) {
		
		if (!hasAssignees(groups, users)) {
			send(getRoutingKey(pid, category, action), message);
			return;
		}
		
		if (groups != null) {
			for (String grpName : groups) {
				send(getRoutingKey(pid, category, action, grpName, "group"), message);
			}
		}
		
		if (users != null) {
			for (String username : users) {
				send(getRoutingKey(pid, category, action, username, "user"), message);
			}
		}
	}
	
	public void send(String pid, String category, String action, Object message) {
		send(getRoutingKey(pid, category, action), message);
	}
	
	
	private static boolean hasAssignees(List<String> groups, List<String> users) {
		return (groups != null && !groups.isEmpty()) || (users != null && !users.isEmpty());
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
	

	
	private String getRoutingKey(String pid, String category, String action) {
		return String.format("%s.%s.%s", pid, category, action);
	}
	
	
	private String getRoutingKey(String pid, String category, String action, 
				String assignee, String typeName) {
		
		String rk = getRoutingKey(pid, category, action);
		return String.format("%s.%s.%s", rk, typeName, assignee);
	}
	
}
