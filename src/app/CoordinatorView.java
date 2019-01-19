package app;

import highway.HWCoordinator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class CoordinatorView {

    private static final String INIT_MSG = "A coordinator will be initialized according to config files...";
    private static final double VBOX_SPACING = 10.0;
    private static final int BUTTON_FONT_SIZE = 25;
    private Pane pane;
    private HWCoordinator coord;

    public CoordinatorView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);


        coord = Instantiator.getCoordinator();
        coord.listenForMsgs();
        VBox box = new VBox(VBOX_SPACING);
        box.getChildren().add(loggerTextArea);

        pane = box;
    }

    private void back() {

    }

    public Pane getContent() {
        return pane;
    }
}
