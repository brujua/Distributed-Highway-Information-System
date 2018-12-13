package test;

import highway.HWCoordinator;
import highway.HWNode;
import highway.Segment;
import network.Messageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

class HighWayTest {


	private static final int NUMBER_OF_SEGMENTS = 10;
	private static final int DEFAULT_SEGMENT_SIDE_SIZE = 5;
	private static HWCoordinator coordinator;
	private List<Segment> segments;

	@BeforeEach
	void initializeCoordinator() {
		// initiaize segments as rectangles in a straight line
		segments = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
			double begin = i * DEFAULT_SEGMENT_SIDE_SIZE;
			double end = begin + DEFAULT_SEGMENT_SIDE_SIZE;
			segments.add(new Segment(begin, end, 0, DEFAULT_SEGMENT_SIDE_SIZE, i));
		}
		coordinator = new HWCoordinator(segments);
		coordinator.listenForMsgs();
	}

	@Test
	public void firstNodeRegistersAndReceivesAllSegments() {
		try {

			ArrayList<Messageable> posibleCoordinators = new ArrayList<>();
			posibleCoordinators.add(coordinator);
			HWNode hwNode = new HWNode(posibleCoordinators);
			hwNode.registerInNetwork();
			List<Segment> hwList = hwNode.getSegments();

			for (Segment seg : segments)
				assert (hwList.contains(seg));

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void Coordinator_Segments_EvenDistributionBetweenTwoNodes() {

	}


}