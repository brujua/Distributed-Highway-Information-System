package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class MainHandler implements HWController, ViewController {
    List<DrawableCar> cars = new ArrayList<>();

    @Override
    public void addCar(DrawableCar car, int delay) {
        cars.add(car);
    }

    @Override
    public void addHWNode(DrawableHWNode hwnode, int delay) {

    }

    @Override
    public void drawMsgArrow(MsgArrow arrow) {

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
        synchronized (cars) {
            for (DrawableObject object : cars) {
                object.render(gc);
            }
        }
    }
}
