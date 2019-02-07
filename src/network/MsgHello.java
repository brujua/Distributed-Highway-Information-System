package network;

import cars.CarStNode;
import common.Position;
import common.Pulse;

public class MsgHello extends Message {

    private CarStNode carNode;

    public MsgHello(CarStNode sender) {
        super(MsgType.HELLO, sender.getStNode());
        carNode = sender;
    }

    public CarStNode getCarNode() {
        return carNode;
    }

    public Pulse getPulse() {
        return carNode.getPulse();
    }

    public Position getPosition() {
        return carNode.getPosition();
    }
}
