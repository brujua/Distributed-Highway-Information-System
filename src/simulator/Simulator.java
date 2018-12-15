package simulator;

import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * Runs and draws in the canvas a simulation with cars and HWNodes.
 */
public class Simulator extends Canvas implements Runnable {

	public static final int WIDTH = 640, HEIGHT = WIDTH / 12 * 9;
	private static final long serialVersionUID = 1L;
	private static final double TICKS_PER_SECOND = 1;
	private Thread thread;
	private boolean running = false;
	private SimMainHandler handler;

	public Simulator() {
		handler = new SimMainHandler();
		new Window(WIDTH, HEIGHT, "Highway", this);
		handler.addObject(new CarSim("prueba", 0.0, 1.5, handler));
		handler.addObject(new CarSim("prueba2", 50, 10, handler));
	}

	public static void main(String[] args) {
		new Simulator();
	}

	public synchronized void start() {
		thread = new Thread(this);
		running = true;
		thread.start();
	}

	private synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		requestFocus();
		long lastTime = System.nanoTime();
		double ns = 1000000000 / TICKS_PER_SECOND;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick();
				delta--;
			}
			if (running)
				render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println("FPS: " + frames);
				frames = 0;
			}
		}
		stop();
	}

	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}

		Graphics graphics = bs.getDrawGraphics();

		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
		handler.render(graphics);

		graphics.dispose();
		bs.show();
	}

	private void tick() {
		handler.tick();
	}

}
