package app;

import common.Position;
import highway.HWNode;
/*
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.util.ArrayList;

public class HWNodeWindow {
	private static final Position STARTING_POSITION = new Position(0.0,0.0);
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 500;
	private static final String TITLE = "Nodo Autopista";
	Stage window;
	private HWNode node;

	public HWNodeWindow(){
		window = new Stage();
		window.setTitle(TITLE);
		node = new HWNode(new ArrayList<>());
		// layout
		GridPane grid = new GridPane();


		Button btnRegister = new Button();
		btnRegister.setText("Register");
		btnRegister.setOnAction(a-> {
			node.registerInNetwork();
		});

		TextArea textArea = new TextArea();
		OutputStream os = new TextAreaOutputStream(textArea);
		MyStaticOutputStreamAppender.setStaticOutputStream(os);

		grid.add(btnRegister,0,0  );
		grid.add(textArea,1,0);

		window.setScene(new Scene(grid,DEFAULT_WIDTH, DEFAULT_HEIGHT)) ;
		window.show();



	}

	// Use to send logger text into the ui textArea
	private static class TextAreaOutputStream extends OutputStream {

		private TextArea textArea;

		public TextAreaOutputStream(TextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void write(int b) {
			textArea.appendText(String.valueOf((char) b));
		}
	}
}
*/
