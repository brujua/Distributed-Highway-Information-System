package app;

import cars.Car;
import cars.CarStNode;
import common.Position;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
    private static final double SCALE_FACTOR_X = 4;
    private static final double SCALE_FACTOR_Y = 1;
    private static final double TRIANGLE_WIDTH = 10;
    private static final double TRIANGLE_HEIGHT = 7;
    private static final double NEIGH_FONT_SIZE = 11;
    private static final double ETIQUETTE_MAX_WIDHT = 100;
    private Image neighImg;
    private Image carImg;

    private Car car;
    private long previousTime = 0;


    public CarDrawer(Car car) {
        this.car = car;
        initImages();

    }

    private void initImages() {
        try {
            //the image has a 2:1 ratio
            neighImg = new Image(new File(NEIGH_IMG_FILE).toURI().toString(), 40, 20, true, true);
            carImg = new Image(new File(CAR_IMG_FILE).toURI().toString(), 50, 25, true, true);
            logger.debug("Car Imgs initialized and scaled");
        } catch (IllegalArgumentException e) {
            logger.error("Could not initialize image: " + e.getMessage());
        }
    }

    @Override
    public void draw(Canvas canvas, long currentTime) {
        if (updateNeeded(currentTime)) {
            drawBackground(canvas);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            Position middleCanvasPosition = calculateMiddlePosition(canvas);
            Position carPosition = car.getPulse().getPosition();
            List<CarStNode> neighs = car.getNeighs();
            //draw car in the middle
            gc.drawImage(carImg, middleCanvasPosition.getCordx() - (carImg.getWidth() / 2), middleCanvasPosition.getCordy() - (carImg.getHeight() / 2));
            for (CarStNode neigh : neighs) {
                drawNeighbourCar(carPosition, neigh, middleCanvasPosition, canvas);
            }
        }
    }

    private boolean updateNeeded(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return true;
        }
        long timeEnlapsed = currentTime - previousTime;
        return timeEnlapsed > MIN_TIME_BEFORE_UPDATE;
    }

    private void drawBackground(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private Position calculateMiddlePosition(Canvas canvas) {
        return new Position(canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private void drawNeighbourCar(Position carPos, CarStNode neigh, Position canvasMiddlePosition, Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Position neighPos = neigh.getPosition();
        double scaledCoordX = ((neighPos.getCordx() - carPos.getCordx()) * SCALE_FACTOR_X) + canvasMiddlePosition.getCordx();
        double scaledCoordY = ((neighPos.getCordy() - carPos.getCordy()) * SCALE_FACTOR_Y) + canvasMiddlePosition.getCordy();

        //Warning! Assuming that scaledCoordY never exceeds canvas bounds. if it does, wont be drawn
        String etiquette = getNeighEtiquette(neigh);
        double etiquetteYAdjusted = scaledCoordY + (NEIGH_FONT_SIZE / 3.0); //arbitrary adjustment

        gc.setFill(Color.GREEN);
        gc.setFont(new Font(NEIGH_FONT_SIZE));

        if (scaledCoordX > canvas.getWidth()) {// out of canvas by the right
            gc.fillPolygon(new double[]{canvas.getWidth() - TRIANGLE_WIDTH, canvas.getWidth() - 1, canvas.getWidth() - TRIANGLE_WIDTH},
                    new double[]{scaledCoordY + TRIANGLE_HEIGHT, scaledCoordY, scaledCoordY - TRIANGLE_HEIGHT},
                    3);
            gc.fillText(etiquette, canvas.getWidth() - TRIANGLE_WIDTH - ETIQUETTE_MAX_WIDHT, etiquetteYAdjusted, ETIQUETTE_MAX_WIDHT);
        } else if (scaledCoordX < 0) { //out of canvas by the left
            gc.fillPolygon(new double[]{TRIANGLE_WIDTH, 1, TRIANGLE_WIDTH},
                    new double[]{scaledCoordY + TRIANGLE_HEIGHT, scaledCoordY, scaledCoordY - TRIANGLE_HEIGHT},
                    3);
            gc.fillText(etiquette, TRIANGLE_WIDTH + 2, etiquetteYAdjusted, ETIQUETTE_MAX_WIDHT); //arbitrary adjustment
        } else { //within bounds of the canvas
            gc.drawImage(neighImg, scaledCoordX - (neighImg.getWidth() / 2), scaledCoordY - (neighImg.getHeight() / 2));
            gc.fillText(etiquette, scaledCoordX - (neighImg.getWidth()), scaledCoordY - (neighImg.getHeight() / 2));
        }

    }

    private String getNeighEtiquette(CarStNode neigh) {
        return "#" + neigh.getId().substring(0, 5) + " vel:" + neigh.getPulse().getVelocity() + "Km/h";
    }
}
