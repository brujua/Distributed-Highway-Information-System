package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class HighwayApp extends Application {

	private static final Logger LOG = LoggerFactory.getLogger(HighwayApp.class);


	@Override
	public void start(Stage primaryStage) {
		Button btn = new Button();
		btn.setText("Log stuff");
		btn.setOnAction(a-> {
			LOG.info("This is some info");
			LOG.error("This is some error");
		});

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

		TextArea textArea = new TextArea();
		OutputStream os = new TextAreaOutputStream(textArea);

		MyStaticOutputStreamAppender.setStaticOutputStream(os);

		GridPane grid = new GridPane();
		grid.add(btnNewCar,0,0);
		grid.add(btnNewHW,1,0);
		grid.add(textArea, 2 ,0);
		grid.add(btn, 2, 1);

		Scene mainScene = new Scene(grid, 800, 300);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	private void createHWNode() {
	}

	private void createCar() {
	}

	private static class TextAreaOutputStream extends OutputStream {

		private TextArea textArea;

		public TextAreaOutputStream(TextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void write(int b) throws IOException {
			textArea.appendText(String.valueOf((char) b));
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
