package app;

import highway.HWCoordinator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CoordinatorView implements NodeView {

    private static final String INIT_MSG = "A coordinator will be initialized according to config files... \n";
    private static final double VBOX_SPACING = 10.0;
    private static final String CONFIG_ERROR_MSG = "Error instantiating coordinator.";
    private Pane pane;
    private HWCoordinator coord;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public CoordinatorView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);


        executorService.submit(() -> {
            coord = Instantiator.getCoordinator();
            if (coord != null)
                coord.listenForMsgs();
            else loggerTextArea.appendText(CONFIG_ERROR_MSG);
        });
        VBox box = new VBox(VBOX_SPACING);
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
        if (coord != null)
            coord.shutDown();
    }
}
