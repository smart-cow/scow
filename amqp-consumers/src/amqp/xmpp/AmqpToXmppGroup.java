package amqp.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpMessage;
import amqp.AmqpReceiver;
import amqp.SimpleAmqpConsumer;
import amqp.translators.AmqpToEnglish;
import amqp.translators.ITranslateAmqpMessage;

/**
 * Takes AMQP messages addressed to groups and sends them to the XMPP group specified by the 
 * received AMQP message's routing key.
 * 
 * @author brosenberg
 *
 */
public class AmqpToXmppGroup extends SimpleAmqpConsumer {

	public static final String GROUP_ROUTING_KEY = "#.group.#";
	
	private XmppSender xmppSender_;
	private ITranslateAmqpMessage msgTranslator_;
	
	public AmqpToXmppGroup(AmqpReceiver amqpReceiver, XmppSender xmppSender) throws IOException {
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
	public AmqpToXmppGroup(AmqpReceiver amqpReceiver, XmppSender xmppSender, 
			ITranslateAmqpMessage translator) throws IOException {
		super(amqpReceiver, GROUP_ROUTING_KEY);
		xmppSender_ = xmppSender;
		msgTranslator_ = translator;
	}
	
	
	public void setMessageTranslator(ITranslateAmqpMessage translator) {
		msgTranslator_ = translator;
	}
	
	
	public void handleAmqpMessage(AmqpMessage amqpMesssage) {
		String translatedMessage = msgTranslator_.translate(amqpMesssage);				
		try {
			if (amqpMesssage.isToSingleGroup()) {
				xmppSender_.sendToGroup(translatedMessage, amqpMesssage.getGroup());
			}
		}
		catch(XMPPException e) {
			System.err.println("Unable to send xmpp message: \"%s\"" + translatedMessage);
			e.printStackTrace();
		}
	}
	
}
