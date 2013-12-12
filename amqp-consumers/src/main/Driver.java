package main;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpReceiver;
import amqp.xmpp.AmqpToXmppGroup;
import amqp.xmpp.AmqpToXmppUser;
import amqp.xmpp.XmppSender;

public class Driver {

	private static final String HOST = "localhost";
	private static final String NOTIFIER_NAME = "notifications";
	private static final String NOTIFIER_PASSWORD = "password";
	private static final String AMQP_EXCHANGE = "amq.topic";
	

	public static void main(String[] args) throws XMPPException, IOException 
	{
		XmppSender xmppSender = new XmppSender(HOST, NOTIFIER_NAME, NOTIFIER_PASSWORD);
		AmqpReceiver amqpReceiver = new AmqpReceiver(HOST, AMQP_EXCHANGE);
		
		new AmqpToXmppUser(amqpReceiver, xmppSender);
		new AmqpToXmppGroup(amqpReceiver, xmppSender);
		
	}
}
