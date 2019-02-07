package network;

import common.StNode;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MT_Broadcast extends Message {
    private static final int DEFAULT_TTL = 5;
    private String broadcastId;
    private int TTL;
    private boolean fromCar;
    private String msg;
    private Instant timestamp;

    public MT_Broadcast(StNode sender, String msg, int TTL, boolean fromCar) {
        super(MsgType.BROADCAST, sender);
        this.TTL = TTL;
        this.fromCar = fromCar;
        this.msg = msg;
        this.broadcastId = UUID.randomUUID().toString();
        timestamp = Instant.now();
    }

    public MT_Broadcast(StNode sender, String msg, boolean fromCar) {
        this(sender, msg, DEFAULT_TTL, fromCar);
    }


    public String getBroadcastId() {
        return broadcastId;
    }

    public int getTTL() {
        return TTL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MT_Broadcast that = (MT_Broadcast) o;
        return broadcastId.equals(that.broadcastId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(broadcastId);
    }

    public boolean isHw() {
        return !fromCar;
    }

    public boolean isFromCar() {
        return fromCar;
    }

    public void setFromCar(boolean fromCar) {
        this.fromCar = fromCar;
    }

    public MT_Broadcast setSender(StNode sender, boolean fromCar) {
        this.sender = sender;
        this.fromCar = fromCar;
        return this;
    }

    public MT_Broadcast setCar() {
        fromCar = true;
        return this;
    }

    public MT_Broadcast setHw() {
        fromCar = false;
        return this;
    }

    public MT_Broadcast decrementTTL() {
        TTL--;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
