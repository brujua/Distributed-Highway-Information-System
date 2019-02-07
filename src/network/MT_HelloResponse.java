package network;

import cars.CarStNode;
import common.StNode;

import java.util.List;

public class MT_HelloResponse extends Message {
	
	private Iterable<CarStNode> cars;
	
	/**
	 * @param responseId id of the message you are responding to.
	 * @param sender information to contact the sender.
	 * @param cars Must implement serializable
	 */
	public MT_HelloResponse(String responseId, StNode sender, List<CarStNode> cars) {
		super(MsgType.HELLO_RESPONSE, sender, responseId);
		this.cars = cars;
	}

	public Iterable<CarStNode> getCars() {
		return cars;
	}
	
}
