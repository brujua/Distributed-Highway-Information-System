package app.simulation;

import app.DrawableCar;
import app.DrawableHWNode;

public interface SimController {

    void addCar(DrawableCar car, int delay);

    void addHWNode(DrawableHWNode hwnode, int delay);

}
