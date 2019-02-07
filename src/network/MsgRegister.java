package network;

import highway.HWStNode;

public class MsgRegister extends Message {

    private HWStNode HwNode;

    public MsgRegister(HWStNode sender) {
        super(MsgType.REGISTER, sender.getStNode());
        this.HwNode = sender;
    }

    public HWStNode getHwNode() {
        return HwNode;
    }
}
