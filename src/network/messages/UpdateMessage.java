package network.messages;

import common.StNode;
import highway.HWStNode;
import network.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UpdateMessage extends Message {

	private List<HWStNode> list;
	private Instant timestamp;

	public UpdateMessage(StNode sender, List<HWStNode> list) {
		super(MessageType.UPDATE, sender);
        if (list == null || list.isEmpty())
            throw new IllegalArgumentException();
        this.list = new ArrayList<>(list);
		timestamp = Instant.now();
	}

	public List<HWStNode> getList() {
		return list;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

    @Override
    public String toString() {
	    return "UpdateMessage{" +
                "timestamp=" + timestamp +
                '}';
    }
}
