package amqp.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpMessage;
import amqp.AmqpReceiver;
import amqp.SimpleAmqpConsumer;
import amqp.xmpp.translators.AmqpToEnglish;
import amqp.xmpp.translators.ITranslateAmqpToXmpp;

public class AmqpToXmppGroup extends SimpleAmqpConsumer{

	public static final String GROUP_ROUTING_KEY = "#.group.#";
	
	private XmppSender xmppSender_;
	private ITranslateAmqpToXmpp msgTranslator_;
	
	public AmqpToXmppGroup(AmqpReceiver amqpReceiver, XmppSender xmppSender) throws IOException {
		this(amqpReceiver, xmppSender, new AmqpToEnglish());
	}
	
	public AmqpToXmppGroup(AmqpReceiver amqpReceiver, XmppSender xmppSender, 
			ITranslateAmqpToXmpp translator) throws IOException {
		super(amqpReceiver, GROUP_ROUTING_KEY);
		xmppSender_ = xmppSender;
		msgTranslator_ = translator;
	}
	
	public void setMessageTranslator(ITranslateAmqpToXmpp translator) {
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
