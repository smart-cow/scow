package main;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpReceiver;
import amqp.xmpp.AmqpToXmppGroup;
import amqp.xmpp.AmqpToXmppUser;
import amqp.xmpp.XmppSender;

/**
 * Starts the application that takes messages from AMQP and sends them over XMPP.
 * Takes configuration parameters from command line using the
 * {@code -D<property name>=<property value>} syntax.
 * 
 * Properties are:
 * <dl>
 * 	<dt>amqp.host</dt>
 * 		<dd> Host name where AMQP is running. Defaults to localhost</dd> 
 * 	<dt>amqp.exchange</dt>
 * 		<dd> Name of AMQP exchange to read from. Defaults to amq.topic </dd>
 * 
 * 	<dt>xmpp.notifier.username</dt>
 * 		<dd> 
 * 			XMPP username for the user that will send out the AMQP message. 
 * 			Defaults to notifications 
 * 		</dd> 
 * 
 * 	<dt>xmpp.notifier.password</dt>
 * 		<dd> Password for xmpp.notifier.username. Defaults to password </dd>
 * </dl>
 * 
 * @author brosenberg
 *
 */
public class Driver {

	private static final String AMQP_HOST;
	private static final String AMQP_EXCHANGE;
	
	private static final String XMPP_NOTIFIER_USERNAME;
	private static final String XMPP_NOTIFIER_PASSWORD;

	static {
		AMQP_HOST = System.getProperty("amqp.host", "localhost");
		AMQP_EXCHANGE = System.getProperty("amqp.exchange", "amq.topic");
		
		XMPP_NOTIFIER_USERNAME = System.getProperty("xmpp.notifier.username", "notifications");
		XMPP_NOTIFIER_PASSWORD = System.getProperty("xmpp.notifier.password", "password");
	}

	
	public static void main(String[] args) throws XMPPException, IOException 
	{
		XmppSender xmppSender = new XmppSender(AMQP_HOST, XMPP_NOTIFIER_USERNAME, 
				XMPP_NOTIFIER_PASSWORD);
		AmqpReceiver amqpReceiver = new AmqpReceiver(AMQP_HOST, AMQP_EXCHANGE);
		
		new AmqpToXmppUser(amqpReceiver, xmppSender);
		new AmqpToXmppGroup(amqpReceiver, xmppSender);
		
	}
}
