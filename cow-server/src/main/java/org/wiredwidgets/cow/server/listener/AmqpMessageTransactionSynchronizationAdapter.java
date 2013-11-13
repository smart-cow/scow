package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

@Component(value = "prototype")
public class AmqpMessageTransactionSynchronizationAdapter extends
		TransactionSynchronizationAdapter {

	private static Logger log = Logger
			.getLogger(AmqpMessageTransactionSynchronizationAdapter.class);

	@Autowired
	private AmqpTemplate template;

	private String topicName;
	private String message;

	@Override
	public void afterCompletion(int arg0) {
		sendMessage();
	}
	
	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private void sendMessage() {
		TimeoutRetryPolicy retry = new TimeoutRetryPolicy();
		retry.setTimeout(600000L);
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retry);
		final String msg = message;
		try {
			String result = retryTemplate.execute(new RetryCallback<String>() {

				public String doWithRetry(RetryContext context) {
					log.debug("sending amqp message: " + msg);
					template.convertAndSend("amq.topic", topicName, msg);
					return "\nMessage Sent: \n" + msg + "\n";
				}
			});

			log.info(result);

		} catch (Exception e) {
			log.error(e);
		}
	}

}
