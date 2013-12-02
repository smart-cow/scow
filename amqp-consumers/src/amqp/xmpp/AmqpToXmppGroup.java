package amqp.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class AmqpToXmppGroup extends DefaultConsumer{

	public static final String GROUP_ROUTING_KEY = "#.group.#";
	
	private XmppSender xmppSender_;
	
	private AmqpToXmppGroup(AmqpReceiver amqpReceiver, XmppSender xmppSender) {
		super(amqpReceiver.getChannel());
		xmppSender_ = xmppSender;
	}
	
	public static AmqpToXmppGroup create(AmqpReceiver amqpReceiver, XmppSender xmppSender) 
			throws IOException {
		AmqpToXmppGroup ret = new AmqpToXmppGroup(amqpReceiver, xmppSender);
		amqpReceiver.addConsumer(ret, GROUP_ROUTING_KEY);
		return ret;
	}
	
	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, 
			AMQP.BasicProperties properties, byte[] message) 
	{
		String routingKey = envelope.getRoutingKey();
		String groupName = AmqpReceiver.getAssignee(routingKey, "group");
		if (groupName == null || groupName.isEmpty()) {
			return;
		}
		
		String messageText = routingKey + "\n\t" + new String(message); 
		try {
			xmppSender_.sendToGroup(messageText, groupName);
		}
		catch(XMPPException e) {
			e.printStackTrace();
		}
	}
	
}
