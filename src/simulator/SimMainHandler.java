package simulator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimMainHandler implements SimController, KeyListener {

    private static final int hudStart = 250;
    private final HUD hud;
    private ScheduledExecutorService intantiatorScheduler = Executors.newSingleThreadScheduledExecutor();



    List<SimObject> objects = new ArrayList<>();

    public SimMainHandler(int widht, int height) {
        hud = new HUD(this, widht, height, hudStart);
        new SimInitializer(this).initialize();
    }

	public void tick() {
		for (SimObject obj : objects) {
			obj.tick();
		}
	}

	public void render(Graphics graphics) {
        hud.render(graphics);
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


    @Override
    public void addCar(String name, int startPosition, double velocity, int delay) {
        intantiatorScheduler.schedule(() -> {
            SimCar car = new SimCar(name, startPosition, velocity, this);
            objects.add(car);
        }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void addHWNode(String name, int delay) {
        //TODO
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
