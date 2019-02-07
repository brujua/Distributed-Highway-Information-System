package network.messages;

import cars.CarStNode;
import common.StNode;
import network.Message;

import java.util.List;

public class HelloResponseMessage extends Message {
	
	private Iterable<CarStNode> cars;
	
	/**
	 * @param responseId id of the message you are responding to.
	 * @param sender information to contact the sender.
	 * @param cars Must implement serializable
	 */
	public HelloResponseMessage(String responseId, StNode sender, List<CarStNode> cars) {
		super(MessageType.HELLO_RESPONSE, sender, responseId);
		this.cars = cars;
	}

	public Iterable<CarStNode> getCars() {
		return cars;
	}
	
}
