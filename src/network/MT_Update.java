package network;

import highway.HWStNode;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MT_Update implements Serializable {

	private List<HWStNode> list;
	private Instant timestamp;

	public MT_Update(List<HWStNode> list) {
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
