package network;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class MT_Broadcast implements Serializable {
    private String id;
    private int TTL;
    private boolean car;

    public MT_Broadcast(int TTL, boolean car) {
        this(TTL);
        this.id = UUID.randomUUID().toString();
        //this.TTL = TTL;
    }

    public MT_Broadcast(int TTL) {
        this.car = true;
        this.TTL = TTL;
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
        return !car;
    }

    public boolean isCar() {
        return car;
    }

    public MT_Broadcast setCar() {
        car = true;
        return this;
    }

    public MT_Broadcast setHw() {
        car = false;
        return this;
    }
}
