package app;

import highway.HWNode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HWNodeView implements NodeView {

    private static final String INIT_MSG = "Initializing HW-Node according to config files...\n";
    private static final double VBOX_SPACING = 25;
    private HWNode node;
    private Pane pane;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public HWNodeView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);

        executorService.submit(() -> {
            node = new HWNode();
            node.listenForMsgs();
            System.out.println("registering in network");
            node.registerInNetwork();
            node.sendAliveToCarsPeriodically();
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
        node.shutdown();
    }
}
