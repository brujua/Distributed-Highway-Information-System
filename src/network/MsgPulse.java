package network;

import cars.CarStNode;
import common.StNode;

public class MsgPulse extends Message {

    CarStNode carNode;

    public MsgPulse(StNode sender, CarStNode carNode) {
        super(MsgType.PULSE, sender);
        this.carNode = carNode;
    }

    public CarStNode getCarNode() {
        return carNode;
    }
}
