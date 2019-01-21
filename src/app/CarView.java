package app;

import cars.Car;
import common.NoPeersFoundException;
import common.Position;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class CarView implements NodeView {

    private static final String INIT_MSG = "A coordinator will be initialized according to config files...";
    private static final String PEER_DISCOV_ERROR_MSG = "Error: Could not find peers";
    private static final double VBOX_SPACING = 10.0;
    private Pane pane;
    private Car car;


    public CarView() {
        LoggerTextArea loggerTextArea = new LoggerTextArea();
        loggerTextArea.appendText(INIT_MSG);


        car = new Car(new Position(0.0, 75.0), 1);
        try {
            car.listenForMsgs().registerInNetwork().emitPulses();
        } catch (NoPeersFoundException e) {
            loggerTextArea.appendText(PEER_DISCOV_ERROR_MSG);
        }
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
        car.shutdown();
    }
}
