package app;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CarView implements NodeView {

    private static final String INIT_MSG = "A car will be initialized according to config files... \n";
    private static final String PEER_DISCOV_ERROR_MSG = "Error: Could not find peers \n";
    private static final double VBOX_SPACING = 10.0;
    private static final double CANVAS_WIDHT = 1000;
    private static final double CANVAS_HEIGHT = 500;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Pane pane;
    private Car car;
    private Canvas carVisualContext;

    public CarView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);
        carVisualContext = new Canvas(CANVAS_WIDHT, CANVAS_HEIGHT);
        GraphicsContext gc = carVisualContext.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDHT, CANVAS_HEIGHT);
        executorService.submit(() -> {
            car = new Car(new Position(0.0, 75.0), 1, "Car");
            try {
                car.listenForMsgs().registerInNetwork().emitPulses();
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

    }

    @Override
    public Pane getContent() {
        return pane;
    }

    @Override
    public void close() {
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
