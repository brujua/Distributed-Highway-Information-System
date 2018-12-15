package simulator;

import cars.Car;
import common.Position;

import java.awt.*;

/**
 * Class responsible to wrap a car in order to simulate it and draw it on the screen
 */
public class CarSim implements SimObject {

	public static final double Y_POS = Simulator.HEIGHT * 0.7;
	private Car car;
	private SimMainHandler handler;

	public CarSim(String name, double xpos, double velocity, SimMainHandler handler) {
		this.handler = handler;
		car = new Car(new Position(xpos, Y_POS), velocity, handler.getHWNodes(), name);
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
		g.setColor(Color.BLUE);
		g.fillRect((int) Math.round(pos.getCordx()), (int) Math.round(pos.getCordy()), 20, 20);
	}
}
