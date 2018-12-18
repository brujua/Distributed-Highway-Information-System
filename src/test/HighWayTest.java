package test;

import highway.HWCoordinator;
import highway.HWNode;
import highway.Segment;
import network.Messageable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class HighWayTest {


    private static final int NUMBER_OF_SEGMENTS = 10;
    private static final int DEFAULT_SEGMENT_SIDE_SIZE = 5;
    private HWCoordinator coordinator;
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

    @AfterEach
    void shutDownCoordinator() {
        coordinator.shutDown();
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    void firstNodeRegistersAndReceivesAllSegments() {
        try {
            System.out.println("creating new hwnode");
            HWNode hwNode = new HWNode();
            hwNode.listenForMsgs().registerInNetwork();

            Thread.sleep(1000);

            Messageable nodeCoordinator = hwNode.getCoordinator();
            assertEquals(coordinator.getId(), nodeCoordinator.getId());
            assertEquals(coordinator.getPort(), nodeCoordinator.getPort());
            List<Segment> hwList = hwNode.getSegments();
            for (Segment seg : segments)
                assert (hwList.contains(seg));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void Coordinator_Segments_EvenDistributionBetweenTwoNodes() {
        try {
            HWNode node1 = new HWNode();
            node1.listenForMsgs().registerInNetwork();
            Thread.sleep(500);
            HWNode node2 = new HWNode();
            node2.listenForMsgs().registerInNetwork();
            Thread.sleep(1000);

            //if even number of segments
            //noinspection ConstantConditions
            if (NUMBER_OF_SEGMENTS % 2 == 0) {
                assertEquals(node1.getSegments().size(), node2.getSegments().size());
            } else {
                //else second one should have less segments
                assertEquals(node1.getSegments().size(), node2.getSegments().size() + 1);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


}