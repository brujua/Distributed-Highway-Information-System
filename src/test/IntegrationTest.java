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

class IntegrationTest {
	private static final Double coordXOrigin = 0.0;
	private static final Double coordYOrigin = 0.0;
	
	private static HighWay hwNode;
	private static List<StNode> hwNodes;

	@BeforeEach
	void initializeHWNode() {
		hwNode = new HighWay(new ArrayList<>(), new Position(coordXOrigin, coordYOrigin));
		hwNodes = new ArrayList<>();
		hwNodes.add(hwNode.getStNode());
	}
	
	@Test
	void testRegister() {		
		try {
			Car car = new Car(new Position(0.0, 0.0),0,hwNodes);
			car.registerInNetwork();
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
			car1.registerInNetwork();
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.registerInNetwork();
			List<StNode> car2Neighs = car2.getNeighs();
			assert(car2Neighs.contains(car1.getStNode()));
			car1.shutdown();
			car2.shutdown();

		} catch (NoPeersFoundException e) {
			// TODO Auto-generated catch block
			fail("NoPeersFoundException");
			e.printStackTrace();
		} /*catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Test
	void testPulsesUpdatePosition() {
		try {
			double velocityCar1 = 2;
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),velocityCar1,hwNodes);
			car1.registerInNetwork().emitPulses();
			car1.move();
			//Thread.sleep(500);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.registerInNetwork().emitPulses();
			Thread.sleep(3500);
			List<StNode> car2Neighs = car2.getNeighs();
			assert (!car2Neighs.isEmpty());
			Position car1Pos = car1.getStNode().getPosition();
			Position car12Pos = car2Neighs.get(0).getPosition();
			assertEquals(car1Pos, car12Pos);
			car1.shutdown();
			car2.shutdown();
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
			car1.registerInNetwork().emitPulses();
			Thread.sleep(1000);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.registerInNetwork().emitPulses();
			Thread.sleep(10001);
			List<StNode> car2Neighs = car2.getNeighs();
			assert(!car2Neighs.isEmpty());
			car1.shutdown();
			car2.shutdown();
		} catch (NoPeersFoundException e) {
			// TODO Auto-generated catch block
			fail("NoPeersFoundException");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Checks if a car that does not emit pulses gets removed from the list of neighbours of the other car
	 */
	@Test
	void testCarTimeOutsInactivesNeighsbours(){
		try {
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),2,hwNodes);
			car1.registerInNetwork();
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.registerInNetwork().emitPulses();
			assert (car2.getNeighs().contains(car1.getStNode()));
			Thread.sleep(10001);
			assert(car2.getNeighs().isEmpty());
			car1.shutdown();
			car2.shutdown();
		} catch (NoPeersFoundException e) {
			fail("NoPeersFoundException");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
