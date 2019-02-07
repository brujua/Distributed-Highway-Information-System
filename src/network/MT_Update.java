package network;

import common.StNode;
import highway.HWStNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MT_Update extends Message {

	private List<HWStNode> list;
	private Instant timestamp;

	public MT_Update(StNode sender, List<HWStNode> list) {
		super(MsgType.UPDATE, sender);
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
        return "MT_Update{" +
                "timestamp=" + timestamp +
                '}';
    }
}
