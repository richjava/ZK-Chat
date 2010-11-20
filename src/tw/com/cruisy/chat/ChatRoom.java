package tw.com.cruisy.chat;

import java.util.ArrayList;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import tw.com.cruisy.chat.ChatUser;

/**
 * @author robbiecheng revised by Richard Lovell
 * A list of chat user threads.
 */
public class ChatRoom {
	private ArrayList<ChatUser> _chatUsers;

	public ChatRoom() {
		_chatUsers = new ArrayList<ChatUser>();
	}

	/**
	 * Task: Send messages to all chatUsers except sender.
	 * @param message
	 */
	public void broadcast(Message msg) {
		// if message is not private
		if (msg.getRecipient() == "") {
			synchronized (_chatUsers) {
				for (ChatUser chatUser : _chatUsers)
					if (chatUser.getNickname().compareTo(msg.getSender()) != 0) {
						chatUser.addMessage(msg);
					}
			}
		} else {
			ChatUser chatUser = getChatUser(msg.getRecipient());
			if (chatUser != null) {
				chatUser.addMessage(msg);
				Events.postEvent(new Event("onIMSend", null, null));
			}
		}
	}

	/**
	 * Add a chat user.
	 * @param chatUser
	 */
	public void add(ChatUser chatUser) {
		synchronized (_chatUsers) {
			_chatUsers.add(chatUser);
		}
	}

	/**
	 * Task: Remove a chat user.
	 * @param chatUser
	 */
	public void remove(ChatUser chatUser) {
		synchronized (_chatUsers) {
			_chatUsers.remove(chatUser);
		}
	}

	/**
	 * Task: Remove a chatUser with a given nickname.
	 * @param nickname
	 */
	public void remove(String nickname) {
		synchronized (_chatUsers) {
			_chatUsers.remove(getChatUser(nickname));
		}
	}

	/**
	 * Task: Get a chat user with a given nickname.
	 * @param nickname
	 * @return
	 */
	public ChatUser getChatUser(String nickname) {
		ChatUser cu = null;
		synchronized (_chatUsers) {
			for (ChatUser chatUser : _chatUsers)
				if (chatUser.getNickname().compareTo(nickname) == 0) {
					cu = chatUser;
					break;
				}
		}
		return cu;
	}

	/**
	 * Task: Get a list of chat users.
	 * @return _chatUsers
	 */
	public ArrayList<ChatUser> getChatUsers() {
		return _chatUsers;
	}
}
