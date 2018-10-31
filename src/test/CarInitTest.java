package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import common.StNode;
import highway.HighWay;

class CarInitTest {

	@Test
	void testRegister() {
		HighWay hwNode = new HighWay(new ArrayList<>(), new Position(Double.valueOf(0.0), Double.valueOf(0.0)));
		List<StNode> hwNodes = new ArrayList<StNode>();
		hwNodes.add(hwNode.getStNode());
		try {
			Car car = new Car(new Position(0.0, 0.0),0,hwNodes);
			assert(car.getSelectedHWnode().equals(hwNode.getStNode()));
		} catch (NoPeersFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
