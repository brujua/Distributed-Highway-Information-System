package view;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

/**
 * Class responsible to wrap a car in order to draw it on the screen
 */
public class DrawableCar implements DrawableObject {

    public static final String IMG_FILE = "resources/car3.png";
    public static final Logger logger = LoggerFactory.getLogger(DrawableCar.class);
    public static double[] y_posiciones = {30, 92, 135};
    private double y_pos;
    private static Image img;
	private Car car;
    private ViewController controller = null;

    public DrawableCar(String name, double xpos, double velocity) {
        if (img == null)
            initImage();
        int yPosIndex = new Random().nextInt(y_posiciones.length);
        y_pos = y_posiciones[yPosIndex];
        car = new Car(new Position(xpos, y_pos), velocity, name); 


    }

    public void setController(ViewController controller) {
        this.controller = controller;
    }

    @Override
	public String getID() {
		return car.getID();
	}

	@Override
	public void tick() {
		car.move();
	}

	@Override
    public void render(GraphicsContext gc) {
		Position pos = car.getPulse().getPosition();
        gc.drawImage(img, pos.getCordx(), pos.getCordy());

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

    public void start() {
        try {
            car.listenForMsgs().registerInNetwork().emitPulses();
        } catch (NoPeersFoundException e) {
            logger.error(car.getName() + "Could not register in the network");
        }
    }
}
