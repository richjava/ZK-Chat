package tw.com.cruisy.chat.web;

import java.util.ArrayList;
import java.util.HashMap;
import tw.com.cruisy.chat.ChatRoom;
import tw.com.cruisy.chat.ChatUser;
import tw.com.cruisy.chat.Message;
import tw.com.cruisy.chat.MessageBoard;
import tw.com.cruisy.chat.web.comp.ChatRow;
import tw.com.cruisy.chat.web.comp.MessageDlg;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

/**
 * @author Richard Lovell
 * Main composer for the chat application.
 */
public class ChatComposer extends GenericForwardComposer {
	// auto-wired components
	private Window win;
	private Textbox nameTb;
	private Textbox msgTb;
	private Grid loginGrid;
	private Hbox inputHb;
	private Grid chatGrid;
	private Checkbox IMCb;
	private Div infoDiv;
	private Rows rows;
	private Image newpmImg;
	private Hbox userInfoHb;
	private Label IMLbl;

	// other instance variables
	private ChatRoom _chatRoom;
	private MessageBoard _msgBoard;
	private ChatUser _chatUser;
	private String _nickname;
	private boolean _IMEnabled;
	private MessageDlg _msgDlg;
	private int _noOfUsers;
	private int _noOfIMs;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		init();
	}

	public void init() {
		_noOfIMs = 0;
		_noOfUsers = 0;
		//if MessageBoard is already in the session
		if (Sessions.getCurrent().getAttribute("msgBoard") != null) {
			desktop.enableServerPush(true);
			//get MessageBoard and initialize a new chat user, replacing the old one
			_msgBoard = (MessageBoard) Sessions.getCurrent().getAttribute(
					"msgBoard");
			ArrayList<Message> msgs = _msgBoard.getMessages();
			_chatUser = _msgBoard.getChatUser();
			_IMEnabled = _chatUser.isIMEnabled();
			_chatRoom = _msgBoard.getChatRoom();
			_nickname = _chatUser.getNickname();
			_chatRoom.remove(_nickname);
			_chatUser = new ChatUser(_chatRoom, _nickname, desktop, _IMEnabled);
			_chatUser.start();
			//refresh UI
			displayChatGrid();
			refreshChatGrid(msgs);
		}
	}

	/********************** Handlers for custom events ********************************/

	/**
	 * Handles the event fired when a message is broadcasted from another chat user.
	 * @param event
	 * @throws InterruptedException
	 */
	public void onBroadcast(Event event) throws InterruptedException {
		HashMap<String, Message> hm = (HashMap<String, Message>) event.getData();
		final Message msg = hm.get("msg");
		//if a user is entering or leaving chatroom
		if(msg.isNotify()){
			displayUserInfo(_chatRoom.getChatUsers());
		}
		// if is private message
		if (msg.getRecipient().compareTo(_nickname) == 0) {
			_noOfIMs++;
			String s = "";
			if(_noOfIMs == 1)
			 s= " new instant message";
			else
				s= " new instant messages";
			IMLbl.setValue("You have " +_noOfIMs + s);
			infoDiv.setVisible(true);
			newpmImg.setVisible(true);
			newpmImg.addEventListener("onClick", new EventListener() {
				public void onEvent(Event e) throws Exception {
					_msgDlg = new MessageDlg(msg, _nickname, _chatRoom);
					_msgDlg.setParent(win);
					_msgDlg.setRecieve();
					newpmImg.setVisible(false);
					infoDiv.setVisible(false);
					newpmImg.removeEventListener("onClick", this);
					_noOfIMs = 0;
				}
			});
		} else {
			_msgBoard.setMessage(msg);
			Sessions.getCurrent().setAttribute("msgBoard", _msgBoard);
			appendMessage(msg);
		}
	}

	/**
	 * Handles the event fired when an instant message is sent to this chat user.
	 * @param event
	 * @throws InterruptedException
	 */
	public void onIMSend(Event event) throws InterruptedException {
		IMLbl.setValue("Instant message sent successfully");
		infoDiv.setVisible(true);
		Timer timer = (Timer) Path.getComponent("/win/timer");
		timer.start();
	}

	/********************** Component event handlers ********************************/

	public void onOK$win() {
		if (Sessions.getCurrent().getAttribute("msgBoard") != null) {
			sendMsg();
		} else
			login();
	}

	public void onClick$loginBtn() {
		login();
	}

	public void onClick$sendBtn() {
		sendMsg();
	}

	public void onClick$exitBtn() {
		// clean up
		_chatUser.setDone();
		desktop.enableServerPush(false);
		Sessions.getCurrent().setAttribute("msgBoard", null);
		_chatRoom.broadcast(new Message(_nickname + " has left the chat room",
				_nickname, true));

		// refresh the UI
		loginGrid.setVisible(true);
		chatGrid.setVisible(false);
		inputHb.setVisible(false);
	}

	/********************** Methods ********************************/

	/**
	 * Task: Send the message from this chat user. This involves broadcasting the message to other users,
	 * updating the MessageBoard in the session, and refreshing this chat user's UI.
	 */
	public void sendMsg() {
		// broadcast message to others
		Message msg = new Message(msgTb.getValue(), _nickname);
		_chatRoom.broadcast(msg);
		// set msg to MessageBoard
		_msgBoard.setMessage(msg);
		Sessions.getCurrent().setAttribute("msgBoard", _msgBoard);
		// refresh UI
		appendMessage(msg);
		displayUserInfo(_chatRoom.getChatUsers());
		msgTb.setRawValue("");
		msgTb.setFocus(true);
	}
	/**
	 * Task: Log the chat user in. This involves simple validation, initialization
	 */
	public void login() {
		_nickname = nameTb.getValue();
		_chatRoom = (ChatRoom) desktop.getWebApp().getAttribute("chatroom");
		if (_chatRoom == null) {
			_chatRoom = new ChatRoom();
			desktop.getWebApp().setAttribute("chatroom", _chatRoom);
		}
		//if username is not already being used
		if (_chatRoom.getChatUser(_nickname) == null) {
			//initialize
			desktop.enableServerPush(true);
			//set IM
			if (IMCb.isChecked())
				_IMEnabled = true;
			_chatUser = new ChatUser(_chatRoom, _nickname, desktop, _IMEnabled);
			//broadcast
			_chatRoom.broadcast(new Message(_nickname
					+ " has joined this chatroom", _nickname, true));
						_chatUser.start();
			//set the MessageBoard to the session
			_msgBoard = new MessageBoard(_chatUser, _chatRoom);
			Sessions.getCurrent().setAttribute("msgBoard", _msgBoard);
			//welcome message
			Message msg = new Message("Welcome " + _nickname, _nickname, true);
			_msgBoard.setMessage(msg);
			//refresh UI
			displayChatGrid();
			appendMessage(msg);
		} else {
			alert("This username is already in use. Please choose another.");
		}

	}

	/**
	 * Task: Display the the main grid used for chatting and hide the login grid.
	 */
	private void displayChatGrid() {
		nameTb.setRawValue("");
		msgTb.setFocus(true);
		loginGrid.setVisible(false);
		chatGrid.setVisible(true);
		inputHb.setVisible(true);
		displayUserInfo(_chatRoom.getChatUsers());
	}

	/**
	 * Task: Display the current user information, including number of chat users
	 * and their usernames.
	 * @param chatUsers
	 */
	private void displayUserInfo(ArrayList<ChatUser> chatUsers){
		int noOfUsers = chatUsers.size();
		//if the number of users has been modified
		if (noOfUsers != _noOfUsers) {
			_noOfUsers = noOfUsers;
			//refresh the user info div
			if(win.hasFellow("userInfoDiv"))
				win.getFellow("userInfoDiv").detach();
			Div userInfoDiv = new Div();
			userInfoDiv.setParent(userInfoHb);
			userInfoDiv.setId("userInfoDiv");
			String s = "";
			if(noOfUsers == 1)
				s = " person is chatting: ";
			else
				s = " people are chatting: ";
			Label userInfoLbl = new Label(noOfUsers + s);
			userInfoLbl.setParent(userInfoDiv);
			//append each chat user's nickname to the div
			Label userLbl = null;
			for (int i=0; i < chatUsers.size(); i++){
				ChatUser cu = (ChatUser)chatUsers.get(i);
				userLbl = new Label(cu.getNickname()+" ");
				userLbl.setStyle("color: blue;");
				userLbl.setParent(userInfoDiv);
			}
		}
	}

	/**
	 * Task: Populate the chat grid with messages.
	 * @param msgs
	 */
	private void refreshChatGrid(ArrayList<Message> msgs) {
		for (int i = 0; i < msgs.size(); i++)
			appendMessage(msgs.get(i));
	}

	/**
	 * Append a ChatRow to the chat grid.
	 * @param msg
	 */
	private void appendMessage(Message msg) {
		ChatRow row = new ChatRow(msg, _chatRoom, _chatUser);
		rows.appendChild(row);
		Clients.scrollIntoView(row);
	}

}
