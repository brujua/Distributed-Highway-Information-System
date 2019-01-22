package app;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CarView implements NodeView {

    private static final String INIT_MSG = "A car will be initialized according to config files... \n";
    private static final String PEER_DISCOV_ERROR_MSG = "Error: Could not find peers \n";
    private static final double VBOX_SPACING = 10.0;
    private static final double CANVAS_WIDHT = 1000;
    private static final double CANVAS_HEIGHT = 500;
    private final CanvasAnimationTimer canvasAnimationTimer;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Pane pane;
    private Car car;
    private Canvas carVisualContext;
    private CarDrawer carDrawer = null;

    public CarView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);
        carVisualContext = new Canvas(CANVAS_WIDHT, CANVAS_HEIGHT);
        GraphicsContext gc = carVisualContext.getGraphicsContext2D();
        executorService.submit(() -> {
            car = new Car(new Position((Math.random() * 50) + 1, (Math.random() * 200) + 50), 1, "Car");
            try {
                car.listenForMsgs().registerInNetwork().emitPulses();
                carDrawer = new CarDrawer(car);
            } catch (NoPeersFoundException e) {
                Platform.runLater(() -> {
                    loggerTextArea.appendText(PEER_DISCOV_ERROR_MSG);
                });
            }
        });
        VBox box = new VBox(VBOX_SPACING);
        box.getChildren().add(carVisualContext);
        box.getChildren().add(loggerTextArea);
        pane = box;
        canvasAnimationTimer = new CanvasAnimationTimer();
        canvasAnimationTimer.start();

    }

    @Override
    public Pane getContent() {
        return pane;
    }

    @Override
    public void close() {
        canvasAnimationTimer.stop();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
                executorService.shutdownNow();
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        car.shutdown();
    }

    private class CanvasAnimationTimer extends AnimationTimer {

        private static final float timeStep = 0.0166f; //update state 60 times per second
        private static final float maximunStep = 5f; //max 5 sec to avoid spiral of death. See: http://svanimpe.be/blog/game-loops
        private long previousTime = 0;
        private float accumulatedTime = 0;


        @Override
        public void handle(long currentTime) {
            if (previousTime == 0) {
                previousTime = currentTime;
                return;
            }
            float secondsElapsed = (currentTime - previousTime) / 1e9f; /* nanoseconds to seconds */
            float secondsElapsedCapped = Math.min(secondsElapsed, maximunStep);
            accumulatedTime += secondsElapsedCapped;
            previousTime = currentTime;

            /*while (accumulatedTime >= timeStep) {
                tick();
                accumulatedTime -= timeStep;
            }*/
            if (carDrawer != null)
                carDrawer.draw(carVisualContext, currentTime);
        }
    }

}
