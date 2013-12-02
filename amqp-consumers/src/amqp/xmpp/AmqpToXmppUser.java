package amqp.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class AmqpToXmppUser extends DefaultConsumer 
{
	public static final String USERS_ROUTING_KEY = "#.user.#";
	
	private XmppSender xmppSender_;

	
	private AmqpToXmppUser(AmqpReceiver amqpReceiver, XmppSender xmppSender) {
		super(amqpReceiver.getChannel());
		xmppSender_ = xmppSender;
	}
	
	
	public static AmqpToXmppUser create(AmqpReceiver amqpReceiver, XmppSender xmppSender) 
			throws IOException {
		AmqpToXmppUser ret = new AmqpToXmppUser(amqpReceiver, xmppSender);
		amqpReceiver.addConsumer(ret, USERS_ROUTING_KEY);
		return ret;
	}
	
	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, 
			AMQP.BasicProperties properties, byte[] message) 
	{		
		String routingKey  = envelope.getRoutingKey();
		String username = AmqpReceiver.getAssignee(routingKey, "user");
		if (username == null) {
			return;
		}
		
		String messageText = routingKey + "\n\t" + new String(message); 
		try {
			if (username.isEmpty()) {
				xmppSender_.sendToAllUsers(messageText);
			}
			else {
				xmppSender_.sendToUser(messageText, username);
			}
		}
		catch(XMPPException e) {
			e.printStackTrace();
		}
	}
	
}
