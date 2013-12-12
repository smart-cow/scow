package amqp.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpMessage;
import amqp.AmqpReceiver;
import amqp.SimpleAmqpConsumer;
import amqp.xmpp.translators.AmqpToEnglish;
import amqp.xmpp.translators.ITranslateAmqpToXmpp;


public class AmqpToXmppUser extends SimpleAmqpConsumer 
{
	public static final String USERS_ROUTING_KEY = "#.user.#";
	
	private XmppSender xmppSender_;
	private ITranslateAmqpToXmpp msgTranslator_;
	
	public AmqpToXmppUser(AmqpReceiver amqpReceiver, XmppSender xmppSender) throws IOException {
		this(amqpReceiver, xmppSender, new AmqpToEnglish());
	}
	
	public AmqpToXmppUser(AmqpReceiver amqpReceiver, XmppSender xmppSender, 
			ITranslateAmqpToXmpp translator) throws IOException {
		
		super(amqpReceiver, USERS_ROUTING_KEY);
		xmppSender_ = xmppSender;
		msgTranslator_ = translator;
	}
	
	public void setMessageTranslator(ITranslateAmqpToXmpp translator) {
		msgTranslator_ = translator;
	}

	@Override
	public void handleAmqpMessage(AmqpMessage amqpMesssage) {
		String translatedMessage = msgTranslator_.translate(amqpMesssage);
		//String messageText = routingKey + "\n\t" + new String(message); 
		try {
			if (amqpMesssage.isToAllUsers()) {
				xmppSender_.sendToAllUsers(translatedMessage);
			}
			if (amqpMesssage.isToSingleUser()) {
				xmppSender_.sendToUser(translatedMessage, amqpMesssage.getUser());
			}
		} catch (XMPPException e) {
			System.err.println("Unable to send xmpp message: \"%s\"" + translatedMessage);
			e.printStackTrace();
		}

	}
	
}
