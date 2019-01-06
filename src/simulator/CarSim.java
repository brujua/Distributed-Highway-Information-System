package simulator;

import cars.Car;
import common.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class responsible to wrap a car in order to simulate it and draw it on the screen
 */
public class CarSim implements SimObject {

    public static final Logger logger = LoggerFactory.getLogger(CarSim.class);
    public static final double Y_POS = 92;
    public static final String IMG_FILE = "resources/car2.png";
    private static Image img;
	private Car car;
	private SimMainHandler handler;

	public CarSim(String name, double xpos, double velocity, SimMainHandler handler) {
        if (img == null)
            initImage();
		this.handler = handler;
        car = new Car(new Position(xpos, Y_POS), velocity, name);

    }

    private void initImage() {
        try {
            BufferedImage imgAux = ImageIO.read(new File(IMG_FILE));
            //the image has a 2:1 ratio
            img = imgAux.getScaledInstance(60, 30, Image.SCALE_SMOOTH);
            logger.info("Img initialized and scalated");
        } catch (IOException e) {
            logger.error("Coudnt initialize image: " + e.getMessage());
            //create a default img
            BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, 50, 50); // give the whole image a white background
        }
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
	public void render(Graphics g) {
		Position pos = car.getPulse().getPosition();
        g.drawImage(img, pos.getCordx().intValue(), pos.getCordy().intValue(), null);
		/*g.setColor(Color.green);
		g.fillRect((int) Math.round(pos.getCordx()), (int) Math.round(pos.getCordy()), 20, 20);
        */

	}

    @Override
    public void setSimModeOn(boolean on) {

    }
}
