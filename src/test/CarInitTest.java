package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import common.StNode;
import highway.HighWay;

class CarInitTest {
	static final Double coordXOrigin = 0.0;
	static final Double coordYOrigin = 0.0;
	
	static HighWay hwNode;
	static List<StNode> hwNodes;

	@BeforeAll
	static void initializeHWNode() {
		hwNode = new HighWay(new ArrayList<>(), new Position(coordXOrigin, coordYOrigin));
		hwNodes = new ArrayList<StNode>();
		hwNodes.add(hwNode.getStNode());
	}
	
	@Test
	void testRegister() {		
		try {
			Car car = new Car(new Position(0.0, 0.0),0,hwNodes);
			assert(car.getSelectedHWnode().equals(hwNode.getStNode()));
		} catch (NoPeersFoundException e) {
			// TODO Auto-generated catch block
			fail("NoPeersFoundException");
			e.printStackTrace();
		}		
	}
	
	@Test
	void testNeighbourGivenByHwOnRegister() {
		try {
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),2,hwNodes);
			Thread.sleep(1000);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			List<StNode> car2Neighs = car2.getNeighs();
			assert(car2Neighs.contains(car1.getStNode()));		
			
		} catch (NoPeersFoundException e) {
			// TODO Auto-generated catch block
			fail("NoPeersFoundException");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
