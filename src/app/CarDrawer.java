package app;

import cars.Car;
import cars.CarStNode;
import common.Position;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Can render a car and its surroundings on a Canvas.
 * Periodically query its car to update data needed.
 */
public class CarDrawer implements Drawer {
    public static final Logger logger = LoggerFactory.getLogger(CarDrawer.class);
    private static final String NEIGH_IMG_FILE = "resources/car3.png";
    private static final String CAR_IMG_FILE = "resources/car.png";
    private static final long MIN_TIME_BEFORE_UPDATE = 1000;
    private Image neighImg;
    private Image carImg;

    private Car car;
    private List<CarStNode> neighs;
    private long previousTime = 0;
    private Position carPosition;
    private Position middleCanvasPosition;


    public CarDrawer(Car car) {
        this.car = car;
        initImages();

    }

    public void draw(Canvas canvas, long currentTime) {
        if (updateNeeded(currentTime)) {
            drawBackground(canvas);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            middleCanvasPosition = calculateMiddlePosition(canvas);
            neighs = car.getNeighs();
            carPosition = car.getPulse().getPosition();
            //draw car in the middle
            gc.drawImage(carImg, middleCanvasPosition.getCordx() - (carImg.getWidth() / 2), middleCanvasPosition.getCordy() - (carImg.getHeight() / 2));
            for (CarStNode neigh : neighs) {
                Position neighPos = neigh.getPosition();
                Position relativePos = new Position(
                        neighPos.getCordx() - carPosition.getCordx() + middleCanvasPosition.getCordx(),
                        neighPos.getCordy() - carPosition.getCordy() + middleCanvasPosition.getCordy());
                gc.drawImage(neighImg, relativePos.getCordx() - (neighImg.getWidth() / 2), relativePos.getCordy() - (neighImg.getHeight() / 2));
            }
        }


    }

    private void drawBackground(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private Position calculateMiddlePosition(Canvas canvas) {
        return new Position(canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private boolean updateNeeded(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return true;
        }
        long timeEnlapsed = currentTime - previousTime;
        return timeEnlapsed > MIN_TIME_BEFORE_UPDATE;
    }

    private void initImages() {
        try {
            //the image has a 2:1 ratio
            neighImg = new Image(new File(NEIGH_IMG_FILE).toURI().toString(), 60, 30, true, true);
            carImg = new Image(new File(CAR_IMG_FILE).toURI().toString(), 60, 30, true, true);
            logger.debug("Car Imgs initialized and scaled");
        } catch (IllegalArgumentException e) {
            logger.error("Could not initialize image: " + e.getMessage());
        }
    }
}
