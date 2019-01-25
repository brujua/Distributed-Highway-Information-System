package app;


import javafx.scene.canvas.Canvas;

public interface Drawer {
    void draw(Canvas canvas, long currentTime);
}
