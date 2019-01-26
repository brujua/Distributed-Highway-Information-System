
package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;

public class HighwayApp extends Application {

    private static final String TITLE = "HighWay App";
    private static final double BACK_BTN_FONT_SIZE = 16;
    private static final String CAR_TITLE = "Car";
    private static final String HWNODE_TITLE = "HW Node";
    private static final String COORD_TITLE = "Coordinator";
    private static final String STYLE_FILE = "/css/dark.css";
    private static final String ICON_FILE = "resources/hwnode2.png";
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
        window.getIcons().add(new Image(new File(ICON_FILE).toURI().toString()));
        navigation = initNavigation();
        defaultCenter = initDefaultCenter();

        mainPane = new BorderPane();
        mainPane.setCenter(defaultCenter);
        mainScene = new Scene(mainPane);
        mainScene.getStylesheets().add(STYLE_FILE);

        window.setScene(mainScene);
        window.setOnCloseRequest((event) -> {
            if (mainPane.getCenter() != defaultCenter) {
                currentView.close();
            }
            Platform.exit();
            System.exit(0);
        });
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
        Button btnBack = new Button("Shutdown");
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



    */
}
