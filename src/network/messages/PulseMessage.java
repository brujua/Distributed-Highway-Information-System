package network.messages;

import cars.CarStNode;
import common.StNode;
import network.Message;

public class PulseMessage extends Message {

    CarStNode carNode;

    public PulseMessage(StNode sender, CarStNode carNode) {
        super(MessageType.PULSE, sender);
        this.carNode = carNode;
    }

    public CarStNode getCarNode() {
        return carNode;
    }
}
