package network.messages;

import common.StNode;
import network.Message;

public class ErrorMessage extends Message {

    private String errorMsg;

    public ErrorMessage(StNode sender, String errorMsg, String responseId) {
        super(MessageType.ERROR, sender, responseId);
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
