package tw.com.cruisy.chat;

/**
 * @author Richard Lovell
 * A POJO representing a chat message.
 */
public class Message {
	private String _content;
	private String _sender;
	private Boolean _notify;
	private String _recipient;


	public Message(String content, String sender){
		_content = content;
		_sender = sender;
		_notify = false;
		_recipient = "";
	}

	public Message(String content, String sender, String recipient){
		_content = content;
		_sender = sender;
		_notify = false;
		_recipient = recipient;
	}

	public Message(String content, String sender, Boolean notify){
		_content = content;
		_sender = sender;
		_notify = notify;
		_recipient = "";
	}

	public Boolean isNotify() {
		return _notify;
	}

	public String getContent() {
		return _content;
	}


	public String getSender() {
		return _sender;
	}

	public String getRecipient() {
		return _recipient;
	}

}
