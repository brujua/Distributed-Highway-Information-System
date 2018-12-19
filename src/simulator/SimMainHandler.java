package simulator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimMainHandler {

	List<SimObject> objects = new ArrayList<>();

	public SimMainHandler() {

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

}
