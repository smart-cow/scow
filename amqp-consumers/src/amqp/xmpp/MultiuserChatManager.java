package amqp.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class MultiuserChatManager {
	
	private String username_;
	private Connection connection_;
	private Roster roster_;
	private Map<String, MultiUserChat> chats_ = new HashMap<String, MultiUserChat>();
	private String chatNameSuffix_;

	public MultiuserChatManager(String username, Connection conn) {
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
		muc.create(username_);
		muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
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
