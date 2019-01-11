
package view;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class HighwayApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(HighwayApp.class);
    private static final String TITLE = "HighWay App";
    private static int WIDHT = 1300;
    private static int HEIGHT = 700;
    private static int CANVAS_WIDHT = 1200;
    private static int CANVAS_HEIGHT = 600;
    private Scene mainScene;
    private Stage window;
    private Canvas simCanvas = null;
    private Canvas singleViewCanvas = null;
    private MainController simController;
    private HWController singleViewController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle(TITLE);

        //Text area for showing the logs logs
        TextArea consoleTextArea = new TextArea();
        OutputStream os = new TextAreaOutputStream(consoleTextArea);
        MyStaticOutputStreamAppender.setStaticOutputStream(os);


        BorderPane borderLayout = new BorderPane();
        borderLayout.setPadding(new Insets(10));
        borderLayout.setBottom(consoleTextArea);

        GridPane centerGridLayout = new GridPane();
        centerGridLayout.setVgap(10);

        simController = new MainController();
        SimInitializer simInitializer = new SimInitializer(simController);
        simInitializer.initialize();
        if (simInitializer.isSimModeOn()) {
            simCanvas = new Canvas(CANVAS_WIDHT, CANVAS_HEIGHT);

            centerGridLayout.add(simCanvas, 0, 0);
        }

        borderLayout.setCenter(centerGridLayout);
        window.setScene(new Scene(borderLayout, WIDHT, HEIGHT));
        AnimationTimer simLoop = new SimLoop();
        window.show();
        simLoop.start();
        simController.start();
    }

    private void tick() {
        if (simController != null) {
            simController.tick();
        }
    }

    private void render() {
        if (simCanvas != null && simController != null) {
            GraphicsContext gc = simCanvas.getGraphicsContext2D();
            simController.render(gc);
        }
    }

    // Use to send logger text into the ui textArea
    public static class TextAreaOutputStream extends OutputStream {

        private TextArea textArea;

        public TextAreaOutputStream(TextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            Platform.runLater(() -> {
                textArea.appendText(String.valueOf((char) b));
            });
        }
    }

    private class SimLoop extends AnimationTimer {

        private static final float timeStep = 0.0166f; //update state 60 times per second
        private static final float maximunStep = 5; //max 5 sec to avoid spiral of death. See: http://svanimpe.be/blog/game-loops
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
            tick();
            render();
        }
    }
}
