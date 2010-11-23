package tw.com.cruisy.chat.web.comp;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;
import tw.com.cruisy.chat.ChatRoom;
import tw.com.cruisy.chat.ChatUser;
import tw.com.cruisy.chat.Message;

/**
 * @author Richard Lovell
 * A row of the chat grid. Rows are color-coded: yellow for notifications, green for local
 * messages (from this chat user), and the default alternating blue and white for other
 * chat users.
 */
public class ChatRow extends Row {

	public ChatRow(final Message msg, final ChatRoom chatRoom, ChatUser chatUser) {
		final String nickname = chatUser.getNickname();
		if (!msg.isNotify()) {
			if (msg.getSender().compareTo(nickname) == 0)
				this.setStyle("background:#C5FCB9");// light green
			Hbox msgHb = new Hbox();
			msgHb.setWidth("218px");
			this.appendChild(msgHb);
			Image img = new Image("/images/cruisylogo2.01.png");
			img.setParent(msgHb);
			img.setWidth("40px");
			Vbox vb = new Vbox();
			vb.setParent(msgHb);
			vb.setStyle("padding-top:10px;width:218px;");
			Label contentLbl = new Label(msg.getContent());
			contentLbl.setStyle("font-weight:bold;");
			vb.appendChild(contentLbl);
			Hbox hb = new Hbox();
			hb.setParent(vb);
			Div div = new Div();
			div.setParent(hb);
			div.setStyle("width:218px;text-align:right;");
			//if the sender is IM enabled and the sender isn't this chatUser
			if (chatUser.isIMEnabled() && msg.getSender().compareTo(nickname)!=0) {
				Image pmImg = new Image("/images/pm.gif");
				pmImg.setParent(div);
				pmImg.setHeight("12px");
				pmImg.addEventListener("onClick", new EventListener() {
					public void onEvent(Event e) throws Exception {
						MessageDlg msgDlg = new MessageDlg(msg, nickname,
								chatRoom);
						msgDlg.setSend();
						Window win = (Window) Path.getComponent("/win");
						msgDlg.setParent(win);
					}
				});
				Space space = new Space();
				div.appendChild(space);
			}

			Date now = new java.util.Date();
			Long ts = now.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
			String time = msg.getSender() + " @ " + sdf.format(ts).toString();
			Label sendertimeLbl = new Label(time);
			div.appendChild(sendertimeLbl);
		} else {
			this.setStyle("background:#FAFCB9;text-align:center;color:#DCDCDC;");//light yellow
			this.appendChild(new Label(msg.getContent()));
		}

	}
}
