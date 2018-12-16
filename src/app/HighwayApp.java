/*
package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HighwayApp extends Application {

	private static final Logger LOG = LoggerFactory.getLogger(HighwayApp.class);
	private static final String TITLE = "HighWay App";

	private Stage window;
	private Scene mainScene;
	private List<Stage> nodesWindows = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) {
		window = primaryStage;
		window.setTitle(TITLE);

		*/
/*Button btn = new Button();
		btn.setText("Log stuff");
		btn.setOnAction(a-> {
			LOG.info("This is some info");
			LOG.error("This is some error");
		});
		TextArea textArea = new TextArea();
		OutputStream os = new TextAreaOutputStream(textArea);
		MyStaticOutputStreamAppender.setStaticOutputStream(os);
		*//*


		Button btnNewCar = new Button();
		btnNewCar.setText("Crear Auto");
		btnNewCar.setOnAction(a-> {
			createCar();
		});

		Button btnNewHW = new Button();
		btnNewHW.setText("Crear Nodo HW");
		btnNewHW.setOnAction(a->{
			createHWNode();
		});



		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10));
		grid.setVgap(10);
		grid.add(btnNewCar,0,0);
		grid.add(btnNewHW,1,0);

		mainScene = new Scene(grid, 800, 300);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	private void createHWNode() {

		HWNodeWindow newWindow = new HWNodeWindow();


	}

	private void createCar() {
	}



	public static void main(String[] args) {
		launch(args);
	}

}
*/
