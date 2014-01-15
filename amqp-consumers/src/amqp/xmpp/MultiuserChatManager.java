package amqp.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

/**
 * Used by XmppSender to manage Multiuser chats (chat rooms). Creates chat rooms based on
 * group specified in an AMQP routing key. When a message is received, if there isn't a Multiuser
 * chat with that name, a new Multiuser chat is created. When a message is received it, sends
 * invites to users that in the group, online, and not already in the chat.
 * 
 * @author brosenberg
 *
 */
public class MultiuserChatManager {
	
	private String username_;
	private XMPPConnection connection_;
	private Roster roster_;
	private Map<String, MultiUserChat> chats_ = new HashMap<String, MultiUserChat>();
	private String chatNameSuffix_;

	public MultiuserChatManager(String username, XMPPConnection conn) {
		username_ = username;
		connection_ = conn;
		roster_ = connection_.getRoster();
		chatNameSuffix_ = "@conference." + connection_.getServiceName(); 
	}
	
	
	public void sendToGroup(String message, String groupName) throws XMPPException {
		MultiUserChat chat = getMultiUserChat(groupName);
		addUsersToChat(chat, groupName);
		chat.sendMessage(message);
	}
	
	
	private MultiUserChat getMultiUserChat(String groupName) throws XMPPException {
		String chatRoomName = convertGroupName(groupName);
		if (!chats_.containsKey(chatRoomName)) {
			chats_.put(chatRoomName, createMultiUserChat(chatRoomName));
		}
		return chats_.get(chatRoomName);
	}
	
	
	private void addUsersToChat(MultiUserChat chat, String groupName) throws XMPPException {	
		RosterGroup rosterGroup = roster_.getGroup(groupName);
		if (rosterGroup == null) {
			System.err.println("No group named " + groupName);
		}
		for(RosterEntry entry : rosterGroup.getEntries()) {
			Presence presence = roster_.getPresence(entry.getUser());
			if (!presence.isAvailable()) {
				continue;
			}
			String userJid = entry.getUser();
			
			if (chat.getOccupantPresence(userJid) == null) {
				chat.invite(userJid, "Join this room");
			}
		}
	}
	
	
	private MultiUserChat createMultiUserChat(String groupName) throws XMPPException {
		groupName = convertGroupName(groupName);
		MultiUserChat muc = new MultiUserChat(connection_, groupName);
		try {
			MultiUserChat.getRoomInfo(connection_, groupName);
			muc.join(username_);
		} catch (XMPPException e) {
			if (e.getXMPPError().getCode() != 404) {
				throw e;
			}
			muc.create(username_);
			muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
		}		
		return muc;
	}
	
	private String convertGroupName(String groupName) 
	{
		if (!groupName.endsWith(chatNameSuffix_)) {
			groupName += chatNameSuffix_;
		}
		return groupName;
	}
	
}
