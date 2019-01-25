package app;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CarView implements NodeView {

    private static final String INIT_MSG = "A car will be initialized according to config files... \n";
    private static final String PEER_DISCOV_ERROR_MSG = "Error: Could not find peers \n";
    private static final double VBOX_SPACING = 10.0;
    private static final double CANVAS_WIDHT = 600;
    private static final double CANVAS_HEIGHT = 300;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Pane pane;
    private Car car;
    private Canvas carVisualContext;
    private CanvasAnimationTimer canvasAnimationTimer;
    private CarDrawer carDrawer = null;

    public CarView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);
        carVisualContext = new Canvas(CANVAS_WIDHT, CANVAS_HEIGHT);
        executorService.submit(() -> {
            car = new Car(new Position((Math.random() * 50) + 1, (Math.random() * 200) + 50), 1, "Car");
            try {
                car.listenForMsgs().registerInNetwork().emitPulses();
                canvasAnimationTimer = new CanvasAnimationTimer(carVisualContext, new CarDrawer(car));
                Platform.runLater(() -> canvasAnimationTimer.start()); //need to be in JavaFX Application thread
            } catch (NoPeersFoundException e) {
                Platform.runLater(() -> {
                    loggerTextArea.appendText(PEER_DISCOV_ERROR_MSG);//need to be in JavaFX Application thread
                });
            }
        });
        VBox box = new VBox(VBOX_SPACING);
        box.getChildren().add(carVisualContext);
        box.getChildren().add(loggerTextArea);
        pane = box;


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



}
