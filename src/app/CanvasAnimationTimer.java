package app;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;

public class CanvasAnimationTimer extends AnimationTimer {

    private Canvas canvas;
    private Drawer drawer;

    public CanvasAnimationTimer(Canvas canvas, Drawer drawer) {
        super();
        if (canvas == null) {
            throw new IllegalArgumentException();
        }
        this.canvas = canvas;
        this.drawer = drawer;
    }

    @Override
    public void handle(long currentTime) {
        if (drawer != null)
            drawer.draw(canvas, currentTime);
    }
}
