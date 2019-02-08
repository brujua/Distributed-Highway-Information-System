package network.messages;

import common.StNode;
import network.Message;

import java.util.Objects;
import java.util.UUID;

public class AlertMessage extends Message {

    private final AlertType alertType;
    //private static final int  = ;
    private String alertId;
    private String msg;


    public AlertMessage(StNode sender, AlertType alertType,String msg ) {
        super(MessageType.ALERT, sender);
        this.msg = msg;
        this.alertId = UUID.randomUUID().toString();
        this.alertType = alertType;
    }


    public String getAlertId() {
        return alertId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertMessage that = (AlertMessage) o;
        return alertId.equals(that.alertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId);
    }
}
