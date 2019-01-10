package view;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController implements HWController, ViewController {
    List<DrawableCar> cars = Collections.synchronizedList(new ArrayList<>());
    List<DrawableHWNode> nodes = Collections.synchronizedList(new ArrayList<>());
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    boolean running = false;

    @Override
    public void addCar(DrawableCar car, int delay) {
        Platform.runLater(() -> {
            scheduler.schedule(() -> {
                cars.add(car);
                if (running) {
                    car.start();
                }
            }, delay, TimeUnit.SECONDS);
        });
    }

    @Override
    public void addHWNode(DrawableHWNode hwnode, int delay) {
        Platform.runLater(() -> {
            scheduler.schedule(() -> {
                nodes.add(hwnode);
                if (running)
                    hwnode.start();
            }, delay, TimeUnit.SECONDS);
        });
    }

    @Override
    public void drawMsgArrow(MsgArrow arrow) {

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
}
