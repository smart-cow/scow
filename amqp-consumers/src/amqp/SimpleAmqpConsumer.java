package amqp;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class SimpleAmqpConsumer extends DefaultConsumer {

	public SimpleAmqpConsumer(AmqpReceiver amqpReceiver, String routingKey) throws IOException {
		super(amqpReceiver.getChannel());
		amqpReceiver.addConsumer(this, routingKey);		
	}
	
	public abstract void handleAmqpMessage(AmqpMessage msg);
	
	
	@Override
	final public void handleDelivery(String consumerTag, Envelope envelope, 
			AMQP.BasicProperties properties, byte[] message) {
		
		AmqpMessage amqpMessage = new AmqpMessage(envelope.getRoutingKey(), new String(message));
		handleAmqpMessage(amqpMessage);
	}
}
