package main;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpReceiver;
import amqp.xmpp.AmqpToXmppGroup;
import amqp.xmpp.AmqpToXmppUser;
import amqp.xmpp.XmppSender;

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
