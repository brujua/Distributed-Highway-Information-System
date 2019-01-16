package app.simulation;

import app.DrawableCar;
import app.DrawableHWNode;
import app.DrawableObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimControllerImpl implements SimController {
    private static final long minTimeBetweenAdds = 400L; //milliseconds
    private List<DrawableCar> cars = Collections.synchronizedList(new ArrayList<>());
    private List<DrawableHWNode> nodes = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean running = false;
    private Object timelock = new Object();
    private long timeLastAdd = 0L;

    @Override
    public void addCar(DrawableCar car, int delay) {
        scheduler.schedule(() -> {
            __addWithMaxFrecuency(car, cars);
            if (running) {
                car.start();
            }
        }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void addHWNode(DrawableHWNode hwnode, int delay) {
        scheduler.schedule(() -> {
            __addWithMaxFrecuency(hwnode, nodes);
            if (running)
                hwnode.start();
        }, delay, TimeUnit.SECONDS);
    }

    public void start() {
        running = true;
        synchronized (nodes) {
            for (DrawableHWNode node : nodes) {
                node.start();
            }
        }

        synchronized (cars) {
            for (DrawableCar car : cars) {
                car.start();
            }
        }
    }

    public void tick() {
        synchronized (cars) {
            for (DrawableObject object : cars) {
                object.tick();
            }
        }
    }

    public void render(GraphicsContext gc) {
        //draw black background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        synchronized (nodes) {
            for (DrawableHWNode node : nodes) {
                node.render(gc);
            }
        }

        synchronized (cars) {
            for (DrawableObject object : cars) {
                object.render(gc);
            }
        }
    }

    private <E> void __addWithMaxFrecuency(E object, List<E> list) {
        Long now = System.currentTimeMillis();
        try {
            synchronized (timelock) {
                Long timeSinceLastAdd = now - timeLastAdd;
                if (timeSinceLastAdd < minTimeBetweenAdds) {
                    Thread.sleep(minTimeBetweenAdds - timeSinceLastAdd);
                }
                synchronized (list) {
                    list.add(object);
                }
                timeLastAdd = System.currentTimeMillis();
            }

        } catch (InterruptedException e) {
            e.printStackTrace(); //log?
        }
    }
}
