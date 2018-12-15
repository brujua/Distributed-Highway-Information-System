package test;

import cars.Car;
import cars.CarStNode;
import common.NoPeersFoundException;
import common.Position;
import common.StNode;
import highway.HWCoordinator;
import highway.HWNode;
import highway.Segment;
import network.Messageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class IntegrationTest {

	private static final int NUMBER_OF_SEGMENTS = 10;
	private static final int DEFAULT_SEGMENT_SIDE_SIZE = 5;
	private static HWCoordinator coordinator;
	private static HWNode hwNode;
	private List<Messageable> posibleCoordinators;

	private static final Double coordXOrigin = 0.0;
	private static final Double coordYOrigin = 0.0;
	private List<Segment> segments;
	private static List<StNode> hwNodes;

	@BeforeEach
	void initializeHWNode() {

		// initiaize segments as rectangles in a straight line
		segments = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
			double begin = i * DEFAULT_SEGMENT_SIDE_SIZE;
			double end = begin + DEFAULT_SEGMENT_SIDE_SIZE;
			segments.add(new Segment(begin, end, 0, DEFAULT_SEGMENT_SIDE_SIZE, i));
		}
		coordinator = new HWCoordinator(segments);
		coordinator.listenForMsgs();
		posibleCoordinators = new ArrayList<>();
		posibleCoordinators.add(coordinator);

		hwNode = new HWNode(posibleCoordinators).listenForMsgs();
		hwNodes = new ArrayList<>();
		hwNodes.add(hwNode.getStNode());
		/*HWNode hwNode2 = new HWNode(posibleCoordinators).listenForMsgs();
		hwNodes.add(hwNode2.getStNode());*/
	}
	
	@Test
	void testRegister() {		
		try {
			Car car = new Car(new Position(0.0, 0.0),0,hwNodes);
			car.listenForMsgs().registerInNetwork();
			assert(car.getSelectedHWnode().equals(hwNode.getStNode()));
		} catch (NoPeersFoundException e) {
			fail("NoPeersFoundException");
			e.printStackTrace();
		}		
	}
	
	@Test
	void testNeighbourGivenByHwOnRegister() {
		try {
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),2,hwNodes);
			car1.listenForMsgs().registerInNetwork();
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.listenForMsgs().registerInNetwork();
			List<CarStNode> car2Neighs = car2.getNeighs();
			assert(car2Neighs.contains(car1.getStNode()));
			car1.shutdown();
			car2.shutdown();

		} catch (NoPeersFoundException e) {
			fail("NoPeersFoundException");
			e.printStackTrace();
		}
	}
	
	@Test
	void testPulsesUpdatePosition() {
		try {
			double velocityCar1 = 2;
			Car car1 = new Car(new Position(coordXOrigin+2, coordYOrigin+2),velocityCar1,hwNodes);
			car1.listenForMsgs().registerInNetwork().emitPulses();
			car1.move();
			//Thread.sleep(500);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.listenForMsgs().registerInNetwork().emitPulses();
			Thread.sleep(3500);
			List<CarStNode> car2Neighs = car2.getNeighs();
			assert (!car2Neighs.isEmpty());
			Position car1Pos = car1.getCarStNode().getPosition();
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
			car1.listenForMsgs().registerInNetwork().emitPulses();
			Thread.sleep(1000);
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.listenForMsgs().registerInNetwork().emitPulses();
			Thread.sleep(10001);
			List<CarStNode> car2Neighs = car2.getNeighs();
			assert(!car2Neighs.isEmpty());
			car1.shutdown();
			car2.shutdown();
		} catch (NoPeersFoundException e) {
			fail("NoPeersFoundException");
			e.printStackTrace();
		} catch (InterruptedException e) {
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
			car1.listenForMsgs().registerInNetwork(); //doesn't emit pulses on purpose
			Car car2 = new Car(new Position(coordXOrigin, coordYOrigin),0,hwNodes);
			car2.listenForMsgs().registerInNetwork().emitPulses();
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
