package app;

import cars.Car;
import cars.CarStNode;
import common.Position;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Can render a car and its surroundings on a Canvas.
 * Periodically query its car to update data needed.
 */
public class CarDrawer {
    public static final String IMG_FILE = "resources/car3.png";
    public static final Logger logger = LoggerFactory.getLogger(CarDrawer.class);
    private static final long MIN_TIME_BEFORE_UPDATE = 1000;
    private static Image img;

    private Car car;
    private List<CarStNode> neighs;
    private long previousTime = 0;
    private Position carPosition;


    public CarDrawer(Car car) {
        this.car = car;
        initImage();

    }

    public void draw(GraphicsContext gc, long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return;
        }
        long timeEnlapsed = currentTime - previousTime;
        if (timeEnlapsed > MIN_TIME_BEFORE_UPDATE) {
            neighs = car.getNeighs();
            carPosition = car.getPulse().getPosition();
        }
        gc.drawImage(img, carPosition.getCordx(), carPosition.getCordy());


    }

    private void initImage() {
        try {
            //the image has a 2:1 ratio
            img = new Image(new File(IMG_FILE).toURI().toString(), 60, 30, true, true);
            logger.info("Car Img initialized and scaled");
        } catch (IllegalArgumentException e) {
            logger.error("Could not initialize image: " + e.getMessage());
        }
    }
}
