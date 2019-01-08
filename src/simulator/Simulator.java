package simulator;

import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * Runs and draws in the canvas a simulation with cars and HWNodes.
 */
public class Simulator extends Canvas implements Runnable {

	public static final int WIDTH = 1200, HEIGHT = 600;
	private static final long serialVersionUID = 1L;
    private static final double TICKS_PER_SECOND = 30;
	private Thread thread;
	private boolean running = false;
	private SimMainHandler handler;

	public Simulator() {
        handler = new SimMainHandler(WIDTH, HEIGHT);
        this.addKeyListener(handler);
       /* handler.addObject(new SimCar("prueba", 0.0, 0.5, handler));
        handler.addObject(new SimCar("prueba2", 50, 1, handler));*/
        new Window(WIDTH, HEIGHT, "Highway", this);

    }

	public static void main(String[] args) {
		new Simulator().start();
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

        //render all the objects
		handler.render(graphics);

		graphics.dispose();
		bs.show();
	}

	private void tick() {
		handler.tick();
	}

}
