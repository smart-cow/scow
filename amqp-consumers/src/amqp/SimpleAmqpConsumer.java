package amqp;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Class to simplify receiving AMQP messages.
 * 
 * @author brosenberg
 *
 */
public abstract class SimpleAmqpConsumer extends DefaultConsumer {

	
	/**
	 * Instantiates an AMQP consumer. Adds this as a consumer to the specified amqpReceiver and
	 * begins consuming.  
	 * 
	 * @param amqpReceiver The AmqpReceiver that will provide the AmqpMessages
	 * @param routingKey The AMQP routing key this will be registered with
	 * @throws IOException
	 */
	public SimpleAmqpConsumer(AmqpReceiver amqpReceiver, String routingKey) throws IOException {
		super(amqpReceiver.getChannel());
		amqpReceiver.addConsumer(this, routingKey);		
	}
	
	/**
	 * Called when an AMQP message is received.
	 * 
	 * @param amqpMessage The AMQP message that was received
	 */
	public abstract void handleAmqpMessage(AmqpMessage amqpMessage);
	
	
	@Override
	final public void handleDelivery(String consumerTag, Envelope envelope, 
			AMQP.BasicProperties properties, byte[] message) {
		
		try {
			AmqpMessage amqpMessage = new AmqpMessage(envelope.getRoutingKey(), new String(message));
			handleAmqpMessage(amqpMessage);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
