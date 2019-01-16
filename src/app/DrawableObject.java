package app;

import javafx.scene.canvas.GraphicsContext;


public interface DrawableObject {

	String getID();

	void tick();

	void render(GraphicsContext g);
}
