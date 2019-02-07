package app;

import highway.HWNode;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HWNodeView implements NodeView {

    private static final String INIT_MSG = "Initializing HW-Node according to config files...\n";
    private static final double VBOX_SPACING = 25;
    private static final double VISUALS_WIDTH = 600;
    private static final double VISUALS_HEIGHT = 300;
    private HWNode node;
    private Pane pane;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Canvas canvas = null;
    private CanvasAnimationTimer canvasAnimationTimer;

    public HWNodeView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);
        canvas = new Canvas(VISUALS_WIDTH, VISUALS_HEIGHT);

        //long lasting task in other thread so it doesn't freezes the view
        executorService.submit(() -> {
            node = new HWNode();
            node.listenForMsgs().registerInNetwork().sendAliveToCarsPeriodically();
            canvasAnimationTimer = new CanvasAnimationTimer(canvas, new HWNodeDrawer(node));
            Platform.runLater(() -> canvasAnimationTimer.start());
        });


        VBox box = new VBox(VBOX_SPACING);
        box.getChildren().add(canvas);
        box.getChildren().add(loggerTextArea);

        pane = box;
    }

    @Override
    public Pane getContent() {
        return pane;
    }

    @Override
    public void close() {
        executorService.shutdownNow();
        if (canvasAnimationTimer != null)
            canvasAnimationTimer.stop();
        if (node != null)
            node.shutdown();
    }
}
