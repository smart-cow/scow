package amqp.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class XmppSender 
{
	private XMPPConnection connection_;
	private ChatManager chatManager_;
	private String usernameSuffix_;
    private Map<String, Chat> chats_ = new HashMap<String, Chat>();
	private MultiuserChatManager mucManger;
	
	
	public XmppSender(String host, String notfierUsername, String password) throws XMPPException 
	{
		connection_ = new XMPPConnection(host);
		connection_.connect();
		connection_.login(notfierUsername, password);
		chatManager_ = connection_.getChatManager();

		usernameSuffix_ = '@' + connection_.getServiceName();
		
		mucManger = new MultiuserChatManager(notfierUsername, connection_);
	}
	

	public void sendToUser(String message, String username) throws XMPPException 
	{
		username = convertUserName(username);
		if (!chats_.containsKey(username)) {
			Chat chat = chatManager_.createChat(username, new DefaultMessageListener());
			chats_.put(username, chat);
		}
		chats_.get(username).sendMessage(message);
	}
	
	
	public void sendToAllUsers(String message) throws XMPPException 
	{
		for(String username : chats_.keySet()) {
			sendToUser(message, username);
		}
	}

	
	private String convertUserName(String username) 
	{
		if (!username.endsWith(usernameSuffix_)) {
			username += usernameSuffix_;
		}
		return username;
	}
	
	
	public void sendToGroup(String message, String group) throws XMPPException 
	{
		mucManger.sendToGroup(message, group);
	}
	
	
	
	
	
    private static class DefaultMessageListener implements MessageListener
    {
        public void processMessage(Chat chat, Message message)
        {
        	System.err.printf("XmppSender received message \"%s\"\n", message.getBody());
        }
    }
}
