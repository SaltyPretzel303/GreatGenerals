package root.communication.messages;

import java.util.Date;
import java.util.List;

public class ReplayMessage extends Message {

	public List<Message> messages;

	public ReplayMessage(Date timestamp) {
		super(MessageType.ReplayMessage, timestamp);
	}

}
