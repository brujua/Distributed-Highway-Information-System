package app;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
    private static final String VELOCITY_LABEL = "velocity";
    private static final double VEL_CHANGE = 0.5;
    private static final String ERROR_VEL_FIELD_MSG = "ERROR: velocity inserted its not a number";
    private final LoggerTextArea loggerTextArea;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Pane pane;
    private Car car;
    private Canvas carVisualContext;
    private CanvasAnimationTimer canvasAnimationTimer;
    private TextField velocityField;

    public CarView() {

        Pane carControls = initCarControls();

        loggerTextArea = new LoggerTextArea();
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
        box.getChildren().add(carControls);
        box.getChildren().add(carVisualContext);
        box.getChildren().add(loggerTextArea);
        pane = box;

    }

    private Pane initCarControls() {
        Label velocityLabel = new Label(VELOCITY_LABEL);
        velocityField = new TextField("1");
        velocityField.setPrefWidth(30);
        velocityField.setMaxWidth(30);

        Button leftButton = new Button("<-");
        leftButton.setOnAction((event -> moveLeft()));

        Button rightButton = new Button("->");
        rightButton.setOnAction((event -> moveRight()));

        GridPane grid = new GridPane();
        grid.add(velocityLabel, 1, 0);
        grid.add(leftButton, 0, 1);
        grid.add(velocityField, 1, 1);
        grid.add(rightButton, 2, 1);
        return grid;
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


    private void moveRight() {
        try {
            double velocity = Double.valueOf(velocityField.getCharacters().toString());
            if (velocity > 0)
                car.move(velocity);
            else
                car.move(-velocity);
        } catch (NumberFormatException e) {
            loggerTextArea.appendText(ERROR_VEL_FIELD_MSG);
        }
    }

    private void moveLeft() {
        try {
            double velocity = Double.valueOf(velocityField.getCharacters().toString());
            if (velocity < 0)
                car.move(velocity);
            else
                car.move(-velocity);
        } catch (NumberFormatException e) {
            loggerTextArea.appendText(ERROR_VEL_FIELD_MSG);
        }
    }

}
