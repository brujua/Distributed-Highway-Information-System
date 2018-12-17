package highway;

import common.StNode;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HWListManager {

	public static final long ALIVE_REFRESH_TIME = 5000;
	private static final Logger logger = LoggerFactory.getLogger(HWListManager.class);
	private static final int MIN_SEGMENTS_PER_NODE = 1;
	private final List<HWStNode> list;
	private final List<Segment> segments;
	private ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

	public HWListManager(List<Segment> segments) {
		if (segments.isEmpty() || segments.size() < MIN_SEGMENTS_PER_NODE)
			throw new InvalidParameterException();
		list = Collections.synchronizedList(new ArrayList<>());
		this.segments = segments;
		Collections.sort(this.segments);
		checkAlives();
	}

	private void checkAlives() {
		timeoutScheduler.scheduleWithFixedDelay(() -> {
			synchronized (list) {
				for (HWStNode node : list) {
					if (!sendAlive(node))
						remove(node);
				}
			}
		}, ALIVE_REFRESH_TIME, ALIVE_REFRESH_TIME, TimeUnit.MILLISECONDS);
	}


	private boolean sendAlive(HWStNode node) {
		Messageable dest = node.getStNode();
		Message msg = new Message(MsgType.ALIVE, null, 0, null);

		return MsgHandler.sendTCPMsg(dest,new Message(MsgType.ALIVE,null,0,null));


		/*try (Socket socket = new Socket(dest.getIP(), dest.getPort())) {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			oos.writeObject(msg);
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			Message response = (Message) ois.readObject();
			if (response.getType() == MsgType.ACK)
				return true;
			logger.error("Response to alive of wrong type");
			return false;

		} catch (IOException e) {m
			logger.error("Node down: " + node);
			return false;
		} catch (ClassNotFoundException e) {
			logger.error("Corrupt message received from" + node);
			return false;
		}*/
	}


	public boolean add(StNode node) {
		if (list.isEmpty()) {
			logger.info("First hwnode added");
			list.add(new HWStNode(node, new ArrayList<>(segments)));
			notifyUpdate();
			return true;
		} else {
			int mostLoadedIndex = findMostLoaded();
			HWStNode mostLoaded = list.get(mostLoadedIndex);
			List<Segment> loadedSegments = mostLoaded.getSegments();
			//divide segments between loaded node and new node
			int sizeSegments = loadedSegments.size();
			if (sizeSegments <= MIN_SEGMENTS_PER_NODE) {
				return false;
			}

			int startIndexSegmentsNewNode = sizeSegments / 2;

			mostLoaded.setSegments(new ArrayList<>(loadedSegments.subList(0, startIndexSegmentsNewNode)));
			//List<Segment> segmentsNewNode = loadedSegments.subList(startIndexSegmentsNewNode, sizeSegments);
			List<Segment> segmentsNewNode = new ArrayList<>(loadedSegments.subList(startIndexSegmentsNewNode, sizeSegments));

			//insert after the most loaded node
			list.add(mostLoadedIndex + 1, new HWStNode(node, segmentsNewNode));
			notifyUpdate();
			return true;
		}

	}

	private int findMostLoaded() {
		int index = 0;
		int max = 0;
		for (int i = 0; i < list.size(); i++) {
			int aux = list.get(i).getSegmentCount();
			if (aux >= max) {
				index = i;
				max = aux;
			}
		}
		return index;
	}

	private void remove(HWStNode node) {
		if (!list.contains(node))
			return;
		int listsize = list.size();
		if (listsize == 1) { //removing only node
			list.remove(node);
			logger.error("FATAL ERROR : NO NODES IN NETWORK");
			return;
		}
		int indexRemoved = list.indexOf(node);
		// on default the segments are added to the node on the right
		// if its the last, they are added to the one on the left
		int indexToAdd = (indexRemoved != listsize - 1) ? indexRemoved + 1 : indexRemoved - 1;
		list.get(indexToAdd).addSegments(node.getSegments());
		list.remove(node);
		notifyUpdate();
	}

	private void notifyUpdate() {
		synchronized (list) {
			MT_Update listUpdate = new MT_Update(list);
			Message updateMsg = new Message(MsgType.UPDATE, null, 0, listUpdate);
			for (HWStNode node : list) {
				if (!MsgHandler.sendTCPMsg(node, updateMsg)) {
					logger.error("Node failed to respond to update on port: " + node.getPort());
					remove(node);
					break; // !important if removed stop sending this version of the list, remove will take care of call notify again.
				}
			}
		}
	}

	public void shutDown() {
		timeoutScheduler.shutdown();
	}
}
