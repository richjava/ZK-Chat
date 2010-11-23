package tw.com.cruisy.chat.web.comp;

import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Html;
import org.zkoss.zul.Image;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;
import tw.com.cruisy.chat.ChatRoom;
import tw.com.cruisy.chat.Message;

/**
 * @author Richard Lovell
 * A popup dialog used for sending and recieving instant messages.
 */
public class MessageDlg extends Window {
	private Message _msg;
	private Rows rows;
	private String _nickname;
	private ChatRoom _chatRoom;

	public MessageDlg(Message msg, String nickname, ChatRoom chatRoom)
			throws InterruptedException {
		_msg = msg;
		_nickname = nickname;
		_chatRoom = chatRoom;
		this.setClosable(true);
		this.setPosition("top,center");
		this.setMode("overlapped");
		this.setWidth("500px");
		Grid grid = new Grid();
		grid.setWidth("100%");
		grid.setParent(this);
		// define columns
		final Columns columns = new Columns();
		columns.setSizable(false);
		columns.setParent(grid);
		Column clm = new Column();
		clm.setWidth("100%");
		clm.setParent(columns);
		// set rows
		rows = new Rows();
		rows.setParent(grid);
	}

	/**
	 * Task: Configure the dialog to recieve a message.
	 */
	public void setRecieve() {
		this.setTitle("Instant message from " + _msg.getSender());
		Row row = new Row();
		row.setParent(rows);
		Hbox hb = new Hbox();
		hb.setParent(row);
		Image img = new Image("/images/cruisylogo2.01.png");
		img.setParent(hb);
		img.setWidth("80px");
		Div contentDiv = new Div();
		contentDiv.setParent(hb);
		contentDiv.setStyle("margin:15px 10px 10px 0;");
		Html content = new Html();
		contentDiv.appendChild(content);
		content.setContent(_msg.getContent());

		row = new Row();
		row.setParent(rows);
		final Button replyBtn = new Button("reply");
		replyBtn.setParent(row);
		replyBtn.addEventListener("onClick", new EventListener() {
			public void onEvent(Event e) throws Exception {
				Window win = (Window) Path.getComponent("/win");
				MessageDlg.this.detach();
				MessageDlg msgDlg = new MessageDlg(_msg, _nickname, _chatRoom);
				msgDlg.setSend();
				msgDlg.setParent(win);
			}
		});
	}

	/**
	 * Task: Configure the dialog to send a message.
	 */
	public void setSend() {
		this.setTitle("Send an instant message to " + _msg.getSender());
		Row row = new Row();
		final CKeditor ed = new CKeditor();
		ed.setParent(row);
		ed.setCustomConfigurationsPath("/js/ckconfig.js");
		ed.setWidth("477px");
		ed.setHeight("160px");
		row.setParent(rows);
		row = new Row();
		final Button btn = new Button("Send");
		btn.setParent(row);
		row.setParent(rows);
		btn.addEventListener("onClick", new EventListener() {
			public void onEvent(Event e) throws Exception {
				Message im = new Message(ed.getValue(), _nickname, _msg.getSender());
				_chatRoom.broadcast(im);
				MessageDlg.this.detach();
			}
		});

	}
}
