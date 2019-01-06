package simulator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class SimMainHandler implements SimController, KeyListener {

    private static final int hudStart = 250;
    private final HUD hud;
    List<SimObject> objects = new ArrayList<>();

    public SimMainHandler(int widht, int height) {
        hud = new HUD(this, widht, height, hudStart);
    }

	public void tick() {
		for (SimObject obj : objects) {
			obj.tick();
		}
	}

	public void render(Graphics graphics) {
		for (SimObject obj : objects) {
            hud.render(graphics);
			obj.render(graphics);
		}

	}

	public void addObject(SimObject obj) {
		objects.add(obj);
	}

	public void removeObject(SimObject obj) {
		objects.remove(obj);
	}

    @Override
    public void addCar(int startPosition) {

    }

    @Override
    public void addHWNode() {

    }

    @Override
    public void setSimModeOn(boolean on) {
        for (SimObject obj : objects) {
            obj.setSimModeOn(on);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        hud.keyTyped(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        hud.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        hud.keyReleased(e);
    }
}
