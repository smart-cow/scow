package amqp.xmpp.translators;

import amqp.AmqpMessage;

public interface ITranslateAmqpToXmpp {
	public String translate(AmqpMessage message);
}
