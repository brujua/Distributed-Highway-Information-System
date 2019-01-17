package app;

import highway.HWCoordinator;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class CoordinatorView {

    private static final String INIT_MSG = "A coordinator will be initialized according to config files...";
    private Scene scene;
    private HWCoordinator coord;

    public CoordinatorView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);
        coord = Instantiator.getCoordinator();
        coord.listenForMsgs();
        VBox box = new VBox();
        box.getChildren().add(loggerTextArea);
        scene = new Scene(box);
    }

    public Scene getScene() {
        return scene;
    }
}
