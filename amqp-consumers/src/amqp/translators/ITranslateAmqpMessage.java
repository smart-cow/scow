package amqp.translators;

import amqp.AmqpMessage;

public interface ITranslateAmqpMessage {
	public String translate(AmqpMessage message);
}
