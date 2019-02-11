package network.messages;

import highway.HWStNode;
import network.Message;

public class RegisterMessage extends Message {

    private HWStNode HwNode;

    public RegisterMessage(HWStNode sender) {
        super(MessageType.REGISTER, sender.getHWStNode());
        this.HwNode = sender;
    }

    public HWStNode getHwNode() {
        return HwNode;
    }
}
