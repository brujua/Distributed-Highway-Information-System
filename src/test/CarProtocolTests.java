package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import common.StNode;
import highway.HighWay;

class CarProtocolTests {
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
	
	@Test
	void testHelloCarCar() {
		try {
			double velocityCar1 = 2;
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),velocityCar1,hwNodes);
			car1.move();
			Thread.sleep(500);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			Thread.sleep(1500);
			List<StNode> car2Neighs = car2.getNeighs();
			assert (!car2Neighs.isEmpty());
			Position car1Pos = car1.getStNode().getPosition();
			Position car12Pos = car2Neighs.get(0).getPosition();
			assertEquals(car1Pos, car12Pos);
		} catch (NoPeersFoundException e) {
			fail("NoPeersFoundException");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testCarMonitorKeepCars() {
		try {
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),2,hwNodes);
			Thread.sleep(1000);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);

			Thread.sleep(10001);
			List<StNode> car2Neighs = car2.getNeighs();
			assert(car2Neighs.isEmpty());
			
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
