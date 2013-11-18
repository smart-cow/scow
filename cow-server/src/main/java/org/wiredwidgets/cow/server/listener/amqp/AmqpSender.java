package org.wiredwidgets.cow.server.listener.amqp;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

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
	
	public void send(final String routingKey, final Object message) {
		//amqpTemplate.convertAndSend(routingKey, message);
		
		try {
			String result = retryTemplate_.execute(new RetryCallback<String>() {
				
				public String doWithRetry(RetryContext context) throws Exception {	
					log.debug("sending amqp message to: " + routingKey);
					amqpTemplate.convertAndSend(routingKey, message);
					return "\nMessage Sent to: \n" + routingKey + "\n";
				}
			});
			
			log.info(result);
		} catch (Exception e) {
			log.error(e);
		}
	}
}
