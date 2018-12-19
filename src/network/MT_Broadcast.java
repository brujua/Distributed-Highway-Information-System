package network;

import java.util.Objects;
import java.util.UUID;

public class MT_Broadcast {
    private String id;
    private int TTL;

    public MT_Broadcast(int TTL) {
        super();
        this.id = UUID.randomUUID().toString();
        this.TTL = TTL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MT_Broadcast that = (MT_Broadcast) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getId() {
        return id;
    }

    public int getTTL() {
        return TTL;
    }
}
