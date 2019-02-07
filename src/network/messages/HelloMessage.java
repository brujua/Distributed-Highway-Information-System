package network.messages;

import cars.CarStNode;
import common.Position;
import common.Pulse;
import network.Message;

public class HelloMessage extends Message {

    private CarStNode carNode;

    public HelloMessage(CarStNode sender) {
        super(MessageType.HELLO, sender.getStNode());
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
