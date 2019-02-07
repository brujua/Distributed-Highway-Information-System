package test;

import cars.Car;
import cars.CarStNode;
import common.NoPeersFoundException;
import common.Position;
import highway.HWCoordinator;
import highway.HWNode;
import highway.Segment;
import network.messages.BroadcastMessage;
import org.junit.jupiter.api.AfterEach;
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
    private static HWNode hwNode2;

	private static final Double coordXOrigin = 0.0;
	private static final Double coordYOrigin = 0.0;

	@BeforeEach
	void initializeHWNode() {

		// initiaize segments as rectangles in a straight line
        List<Segment> segments = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
			double begin = i * DEFAULT_SEGMENT_SIDE_SIZE;
			double end = begin + DEFAULT_SEGMENT_SIDE_SIZE;
			segments.add(new Segment(begin, end, 0, DEFAULT_SEGMENT_SIDE_SIZE, i));
		}
		coordinator = new HWCoordinator(segments);
		coordinator.listenForMsgs();

		try {
            hwNode = new HWNode().listenForMsgs().registerInNetwork().sendAliveToCarsPeriodically();
            //sleep to give time for registration of the hwnode
            Thread.sleep(500);
            hwNode2 = new HWNode().listenForMsgs().registerInNetwork().sendAliveToCarsPeriodically();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void shutdownNodes() {
        coordinator.shutDown();
        hwNode.shutdown();
        hwNode2.shutdown();
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
	
	@Test
	void testRegister() {		
		try {
            Car car = new Car(new Position(0.0, 0.0), 0);
			car.listenForMsgs().registerInNetwork();
			assert(car.getSelectedHWnode().equals(hwNode.getStNode()));
            car.shutdown();
		} catch (NoPeersFoundException e) {
			fail("NoPeersFoundException");
			e.printStackTrace();
		}		
	}
	
	@Test
	void testNeighbourGivenByHwOnRegister() {
		try {
            Car car1 = new Car(new Position(coordXOrigin + 2, coordYOrigin + 2), 2);
			car1.listenForMsgs().registerInNetwork();
            Car car2 = new Car(new Position(coordXOrigin, coordYOrigin), 0);
			car2.listenForMsgs().registerInNetwork();
			List<CarStNode> car2Neighs = car2.getNeighs();
			assert(car2Neighs.contains(car1.getCarStNode()));
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
            Car car1 = new Car(new Position(coordXOrigin + 2, coordYOrigin + 2), velocityCar1);
			car1.listenForMsgs().registerInNetwork().emitPulses();
			car1.move();
            Car car2 = new Car(new Position(coordXOrigin, coordYOrigin), 0);
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
            Car car1 = new Car(new Position(coordXOrigin + 2, coordYOrigin + 2), 2);
			car1.listenForMsgs().registerInNetwork().emitPulses();
			Thread.sleep(1000);
            Car car2 = new Car(new Position(coordXOrigin, coordYOrigin), 0);
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
    void testCarTimeOutsInactiveNeighbours() {
		try {
            Car car1 = new Car(new Position(coordXOrigin + 2, coordYOrigin + 2), 2);
			car1.listenForMsgs().registerInNetwork(); //doesn't emit pulses on purpose
            Car car2 = new Car(new Position(coordXOrigin, coordYOrigin), 0);
			car2.listenForMsgs().registerInNetwork().emitPulses();
			assert (car2.getNeighs().contains(car1.getCarStNode()));
			Thread.sleep(10001);
            assert (!car2.getNeighs().contains(car1.getCarStNode()));
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
	void Car_HWNode_RedirectWhenChangeSegment() {
		try {

            Car car1 = new Car(new Position(coordXOrigin + 2, coordYOrigin + 2), 2);
            car1.listenForMsgs().registerInNetwork().emitPulses();
            Car car2 = new Car(new Position(coordXOrigin, coordYOrigin), 0);
            car2.listenForMsgs().registerInNetwork().emitPulses();
            double possX = ((NUMBER_OF_SEGMENTS*DEFAULT_SEGMENT_SIDE_SIZE) /2)+DEFAULT_SEGMENT_SIDE_SIZE;
            double possY = DEFAULT_SEGMENT_SIDE_SIZE;
            Thread.sleep(500);
            car1.setPosition(new Position(possX,possY));
            Thread.sleep(5000);
            assert(!car2.getSelectedHWnode().equals(car1.getSelectedHWnode()));
            car1.shutdown();
            car2.shutdown();

		} catch (NoPeersFoundException | InterruptedException e) {
            e.printStackTrace();
		}
    }

    @Test
    void Car_sendBroadcasMsg() {
	    try {

            HWNode hwNode3 = new HWNode().listenForMsgs().registerInNetwork().sendAliveToCarsPeriodically();


		    double possX = ((NUMBER_OF_SEGMENTS * DEFAULT_SEGMENT_SIDE_SIZE) / 2) + DEFAULT_SEGMENT_SIDE_SIZE;
		    double possY = DEFAULT_SEGMENT_SIDE_SIZE;

		    Thread.sleep(500);
            Car car1 = new Car(new Position(5.0, coordYOrigin), 0, "Car1");
		    car1.listenForMsgs().registerInNetwork().emitPulses();

            Car car2 = new Car(new Position(49.0, coordYOrigin), 0, "Car2");
		    car2.listenForMsgs().registerInNetwork().emitPulses();


            Car car3 = new Car(new Position(25.0, coordYOrigin), 0, "Car3");
		    car3.listenForMsgs().registerInNetwork().emitPulses();
		    Thread.sleep(500);

/*			Car car3 = new Car(new Position(coordXOrigin, coordYOrigin), 0);
			car3.listenForMsgs().registerInNetwork().emitPulses();*/

		    //	Thread.sleep(500);

		    //assert(!car1.getSelectedHWnode().equals(car3.getSelectedHWnode()));
		    /*			car1.setPosition(new Position(possX,possY));*/
		    Thread.sleep(5000);

		    assert (!car2.getSelectedHWnode().equals(car1.getSelectedHWnode()));

		    assert (!car2.getSelectedHWnode().equals(car3.getSelectedHWnode()));

		    assert (!car3.getSelectedHWnode().equals(car1.getSelectedHWnode()));

		    String msgBroadcast = "Accidente en kilometro " + car1.getPulse().getPosition().getCordx();
		    BroadcastMessage broadcast = car1.sendBroadcast(msgBroadcast);
		    Thread.sleep(500);
		    assert (car1.containBroadcast(broadcast));
		    assert (car3.containBroadcast(broadcast));
		    assert (car2.containBroadcast(broadcast));


		    car1.shutdown();
		    car2.shutdown();
		    car3.shutdown();
		    hwNode3.shutdown();

        } catch (NoPeersFoundException | InterruptedException e) {
		    e.printStackTrace();
	    }

    }

}
