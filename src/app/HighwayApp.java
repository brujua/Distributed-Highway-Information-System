
package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighwayApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(HighwayApp.class);
    private static final String TITLE = "HighWay App";
    private static final double BACK_BTN_FONT_SIZE = 25;
    private static final String CAR_TITLE = "Car";
    private static final String HWNODE_TITLE = "HW Node";
    private static final String COORD_TITLE = "Coordinator";
    private Scene mainScene;
    private Stage window;
    private BorderPane mainPane;
    private Pane defaultCenter;
    private Pane navigation;
    private NodeView currentView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle(TITLE);
        navigation = initNavigation();
        defaultCenter = initDefaultCenter();

        mainPane = new BorderPane();
        mainPane.setCenter(defaultCenter);
        mainScene = new Scene(mainPane);

        window.setScene(mainScene);
        window.centerOnScreen();
        window.show();
    }

    private Pane initDefaultCenter() {
        Button coordButton = new Button();
        coordButton.setText("Create Coordinator");
        coordButton.setOnAction(event -> {
            createCoordinator();
        });
        Button hwnodeButton = new Button();
        hwnodeButton.setText("Create HW-Node");
        hwnodeButton.setOnAction(event -> {
            createHWNode();
        });
        Button carButton = new Button();
        carButton.setText("Create a Car");
        carButton.setOnAction(event -> {
            createCar();
        });

        GridPane grid = new GridPane();
        grid.add(coordButton, 0, 0);
        grid.add(hwnodeButton, 1, 0);
        grid.add(carButton, 2, 0);
        grid.setPadding(new Insets(25));
        GridPane.setMargin(coordButton, new Insets(5));
        GridPane.setMargin(hwnodeButton, new Insets(5));
        GridPane.setMargin(carButton, new Insets(5));
        return grid;
    }

    private Pane initNavigation() {
        Button btnBack = new Button("<-");
        btnBack.setFont(Font.font(BACK_BTN_FONT_SIZE));
        btnBack.setOnAction((event) -> {
            back();
        });
        return new Pane(btnBack);
    }

    private void back() {
        try {
            currentView.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cant go back right now, awating taks...");
            alert.show();
            return;
        }
        mainPane.setCenter(defaultCenter);
        mainPane.setTop(null);
        window.sizeToScene();
    }

    private void createCar() {
        showNodeView(new CarView());
        window.setTitle(CAR_TITLE);
    }

    private void createHWNode() {
        window.setTitle(HWNODE_TITLE);
        showNodeView(new HWNodeView());
    }

    private void createCoordinator() {
        window.setTitle(COORD_TITLE);
        showNodeView(new CoordinatorView());
    }

    private void showNodeView(NodeView view) {
        currentView = view;
        mainPane.setCenter(view.getContent());
        mainPane.setTop(navigation);
        window.sizeToScene();
    }

    /*private void tick() {
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



    private class SimLoop extends AnimationTimer {

        private static final float timeStep = 0.0166f; //update state 60 times per second
        private static final float maximunStep = 5f; //max 5 sec to avoid spiral of death. See: http://svanimpe.be/blog/game-loops
        private long previousTime = 0;
        private float accumulatedTime = 0;


        @Override
        public void handle(long currentTime) {
            if (previousTime == 0) {
                previousTime = currentTime;
                return;
            }
            float secondsElapsed = (currentTime - previousTime) / 1e9f; *//* nanoseconds to seconds *//*
            float secondsElapsedCapped = Math.min(secondsElapsed, maximunStep);
            accumulatedTime += secondsElapsedCapped;
            previousTime = currentTime;

            while (accumulatedTime >= timeStep) {
                tick();
                accumulatedTime -= timeStep;
            }
            //tick();
            render();
        }
    }*/
}
