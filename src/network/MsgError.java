package network;

import common.StNode;

public class MsgError extends Message {

    String errorMsg;

    public MsgError(StNode sender, String errorMsg) {
        super(MsgType.ERROR, sender);
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
