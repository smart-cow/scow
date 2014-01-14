package org.wiredwidgets.cow.server.listener.amqp;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * Utility class to simplify publishing AMQP message.
 * 
 * @author BROSENBERG
 *
 */
@Component
public class AmqpSender {
	
	private static Logger log = Logger.getLogger(AmqpSender.class);
	
	@Autowired
	AmqpTemplate amqpTemplate;

	private RetryTemplate retryTemplate_;
	

	/**
	 * The retries happen on the HTTP thread, so if AMQP is unavailable it will take a long time
	 * for the HTTP response.The timeout of 600000ms (which was there already) means that if 
	 * AMQP is down every HTTP response will take 10 minutes. So I decided to run the retries on a
	 * separate thread.
	 * @author brosenberg
	 */
	private ExecutorService retryExecutor_;

	
	public AmqpSender() {
		initializeExecutor();
		
        TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
        retryPolicy.setTimeout(600000);
        
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(200);
		backOffPolicy.setMaxInterval(4000);
		
		retryTemplate_ = new RetryTemplate();
		retryTemplate_.setRetryPolicy(retryPolicy);
		retryTemplate_.setBackOffPolicy(backOffPolicy);
	}
	
	
	/**
	 * This initializes a single thread thread pool. I couldn't use 
	 * {@code Executors.newSingleThreadExecutor()} because I wanted to use 
	 * {@code ThreadPoolExecutor.DiscardOldestPolicy} in case AMQP is down for an extended
	 * period of time.
	 */
	private void initializeExecutor() {
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, 
				TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>(), 
				new ThreadPoolExecutor.DiscardOldestPolicy());
		retryExecutor_ = executor;
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
	
	
	
	private String getRoutingKey(String pid, String category, String action) {
		return String.format("%s.%s.%s", pid, category, action);
	}
	
	
	
	private String getRoutingKey(String pid, String category, String action, 
				String assignee, String typeName) {
		
		String rk = getRoutingKey(pid, category, action);
		return String.format("%s.%s.%s", rk, typeName, assignee);
	}
	
	
	
	private static boolean hasAssignees(List<String> groups, List<String> users) {
		return (groups != null && !groups.isEmpty()) || (users != null && !users.isEmpty());
	}

	
	/**
	 * Schedules the retryTemplate call on the threadExecutor
	 * @param routingKey
	 * @param body
	 */
	private void send(final String routingKey, final Object body) {
		retryExecutor_.execute(new Runnable() {
			public void run() {
				retryTemplateSend(routingKey, body);
			}
		});
	}
	
	private void retryTemplateSend(final String routingKey, final Object body) {
		try {
			retryTemplate_.execute(new RetryCallback<Void>() {
				public Void doWithRetry(RetryContext ctx) throws Exception {
					
					amqpTemplate.convertAndSend(routingKey, body);
					return null;
				}
			});
			log.info("\nMessage Sent to: \n" + routingKey + "\n");
		} 
		catch (Exception e) {
			log.error("Message: \"" + routingKey + 
					"\" will not be sent because it expired its retry limit");
			e.printStackTrace();
		}
	}
}
