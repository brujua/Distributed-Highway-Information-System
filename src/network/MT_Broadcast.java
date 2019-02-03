package network;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MT_Broadcast implements Serializable {
    private static final int DEFAULT_TTL = 5;
    private String id;
    private int TTL;
    private boolean fromCar;
    private String msg;
    private Instant timestamp;

    public MT_Broadcast(String msg, int TTL, boolean fromCar) {
        this.TTL = TTL;
        this.fromCar = fromCar;
        this.msg = msg;
        this.id = UUID.randomUUID().toString();
        timestamp = Instant.now();

    }

    public MT_Broadcast(String msg, boolean fromCar) {
        this(msg, DEFAULT_TTL, fromCar);
    }


    public String getId() {
        return id;
    }

    public int getTTL() {
        return TTL;
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

    public boolean isHw() {
        return !fromCar;
    }

    public boolean isFromCar() {
        return fromCar;
    }

    public void setFromCar(boolean fromCar) {
        this.fromCar = fromCar;
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
