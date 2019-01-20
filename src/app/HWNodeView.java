package app;

import highway.HWNode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class HWNodeView implements NodeView {

    private static final String INIT_MSG = "Initializing HW-Node according to config files";
    private static final double VBOX_SPACING = 25;
    HWNode node;
    private Pane pane;

    public HWNodeView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);

        node = new HWNode();
        node.listenForMsgs();
        node.registerInNetwork();
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

    }
}
