package simulator;

import common.StNode;
import highway.HWNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimMainHandler {

	List<SimObject> objects = new ArrayList<>();
	//TODO temporary
	List<StNode> hwNodes = new ArrayList<>();

	public SimMainHandler() {
		//TODO temporary
		HWNode hwNode = new HWNode(new ArrayList<>()).listenForMsgs();
		hwNodes = new ArrayList<>();
		hwNodes.add(hwNode.getStNode());
	}

	public void tick() {
		for (SimObject obj : objects) {
			obj.tick();
		}
	}

	public void render(Graphics graphics) {
		for (SimObject obj : objects) {
			obj.render(graphics);
		}
	}

	public void addObject(SimObject obj) {
		objects.add(obj);
	}

	public void removeObject(SimObject obj) {
		objects.remove(obj);
	}

	public List<StNode> getHWNodes() {
		return hwNodes;
	}
}
