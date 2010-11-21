/* ChatUser.java

 {{IS_NOTE
 Purpose:

 Description:

 History:
 Aug 17, 2007 12:58:55 PM , Created by robbiecheng
 Nov 03, 2010 , Revised by Richard Lovell
 }}IS_NOTE

 Copyright (C) 2007 Potix Corporation. All Rights Reserved.

 {{IS_RIGHT
 This program is distributed under GPL Version 2.0 in the hope that
 it will be useful, but WITHOUT ANY WARRANTY.
 }}IS_RIGHT
 */

package tw.com.cruisy.chat;

import java.util.HashMap;
import org.zkoss.lang.Threads;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

/**
 *  @author robbiecheng revised by Richard Lovell
 *  A thread representing a chatroom user.
 */
public class ChatUser extends Thread {
	private static final Log log = Log.lookup(ChatUser.class);
	private boolean _ceased;
	private ChatRoom _chatRoom;
	private final Desktop _desktop;
	private String _nickname;
	private Message _msg;
	private Boolean _IMEnabled;

	public ChatUser(ChatRoom chatRoom, String nickname, Desktop desktop, Boolean IMEnabled) {
		_chatRoom = chatRoom;
		_nickname = nickname;
		_desktop = desktop;
		_msg = null;
		_IMEnabled = IMEnabled;
		_chatRoom.add(this);
	}

	/**
	 * Send new messages to UI if necessary.
	 */
	public void run() {
		if (!_desktop.isServerPushEnabled())
			_desktop.enableServerPush(true);
		log.info("Active chatUser thread: " + getName());
		try {
			while (!_ceased) {
				try {
					if (_msg == null) {
						Threads.sleep(500);// Update each 0.5 seconds
					} else {
						Executions.activate(_desktop);
						try {
							process();
						} finally {
							Executions.deactivate(_desktop);
						}
					}
				} catch (DesktopUnavailableException ex) {
					log.info("Browser exited.");
					cleanUp();
				} catch (Throwable ex) {
					log.error(ex);
					throw UiException.Aide.wrap(ex);
				}
			}
		} finally {
			cleanUp();
		}
		log.info("chatUser thread ceased: " + getName() );
	}

	/**
	 * Task: If there is a new message for the chat user, post a new "onBroadcast" event with
	 * the message passed in.
	 * @throws Exception
	 */
	private void process() throws Exception {
		if (_msg != null) {
			log.info("processing message: "+_msg.getContent());
			HashMap<String, Message> msgs = new HashMap<String, Message>();
			msgs.put("msg",_msg);
			Events.postEvent(new Event("onBroadcast", null, msgs));
			_msg = null;//reset message
		}
	}


	/**
	 * Task: Clean up before stopping thread.
	 */
	public void cleanUp(){
		log.info(getNickname() + " has logged out of the chatroom!");
		_chatRoom.remove(this);
		if (_desktop.isServerPushEnabled())
			Executions.getCurrent().getDesktop().enableServerPush(false);
		setDone();
	}

	/**
	 * Task: Stop this thread.
	 */
	public void setDone() {
		_ceased = true;
	}

	/**
	 * Task: Set message for this chatUser.
	 * @param message
	 */
	public void addMessage(Message message) {
		_msg = message;
	}

	/**
	 * return sender's name
	 *
	 * @return
	 */
	public String getNickname() {
		return _nickname;
	}

	public Boolean isIMEnabled() {
		return _IMEnabled;
	}

}
