package amqp.translators;

import amqp.AmqpMessage;

/**
 * Converts an AMQP message to a String
 * @author brosenberg
 *
 */
public interface ITranslateAmqpMessage {
	
	public String translate(AmqpMessage message);
}
