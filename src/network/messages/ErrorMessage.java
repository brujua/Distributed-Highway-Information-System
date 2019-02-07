package network.messages;

import common.StNode;
import network.Message;

public class ErrorMessage extends Message {

    String errorMsg;

    public ErrorMessage(StNode sender, String errorMsg) {
        super(MessageType.ERROR, sender);
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
