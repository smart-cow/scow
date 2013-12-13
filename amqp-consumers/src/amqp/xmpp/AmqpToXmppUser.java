package amqp.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpMessage;
import amqp.AmqpReceiver;
import amqp.SimpleAmqpConsumer;
import amqp.translators.AmqpToEnglish;
import amqp.translators.ITranslateAmqpMessage;


/**
 * Takes AMQP messages addressed to users and sends them to the XMPP user specified by the 
 * received AMQP message's routing key.
 * 
 * @author brosenberg
 *
 */
public class AmqpToXmppUser extends SimpleAmqpConsumer 
{
	public static final String USERS_ROUTING_KEY = "#.user.#";
	
	private XmppSender xmppSender_;
	private ITranslateAmqpMessage msgTranslator_;
	
	public AmqpToXmppUser(AmqpReceiver amqpReceiver, XmppSender xmppSender) throws IOException {
		this(amqpReceiver, xmppSender, new AmqpToEnglish());
	}
	
	
	
	/**
	 * Configure where to read AMQP messages from and where to send XMPP messages to.
	 * 
	 * @param amqpReceiver
	 * @param xmppSender
	 * @param translator Specifies how to convert an AMQP message to a string to be sent over XMPP
	 * @throws IOException
	 */
	public AmqpToXmppUser(AmqpReceiver amqpReceiver, XmppSender xmppSender, 
			ITranslateAmqpMessage translator) throws IOException {
		
		super(amqpReceiver, USERS_ROUTING_KEY);
		xmppSender_ = xmppSender;
		msgTranslator_ = translator;
	}
	
	
	public void setMessageTranslator(ITranslateAmqpMessage translator) {
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
