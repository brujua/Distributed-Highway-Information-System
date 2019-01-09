
package app;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulator.Simulator;

import javax.swing.*;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class HighwayApp extends Application {

	private static final Logger LOG = LoggerFactory.getLogger(HighwayApp.class);
	private static final String TITLE = "HighWay App";
    private static int WIDHT = 1300;
    private static int HEIGHT = 700;
    Simulator simulator = new Simulator();
	private Scene mainScene;
	private List<Stage> nodesWindows = new ArrayList<>();
    private Stage window;

	@Override
	public void start(Stage primaryStage) {

        final AwtInitializerTask awtInitializerTask = new AwtInitializerTask(() -> {
            JPanel jPanel = new JPanel();

            jPanel.add(simulator);

            return jPanel;
        });

        SwingUtilities.invokeLater(awtInitializerTask);

        SwingNode swingNode = new SwingNode();
        try {
            swingNode.setContent(awtInitializerTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        TextArea textArea = new TextArea();
        OutputStream os = new HWNodeWindow.TextAreaOutputStream(textArea);
        MyStaticOutputStreamAppender.setStaticOutputStream(os);
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.add(textArea, 0, 0);
        grid.add(swingNode, 0, 1);

        primaryStage.setScene(new Scene(grid, WIDHT, HEIGHT));
        primaryStage.setResizable(false);
        primaryStage.show();
        simulator.start();
/*
		window = primaryStage;
		window.setTitle(TITLE);*/


    /*Button btn = new Button();
		btn.setText("Log stuff");
		btn.setOnAction(a-> {
			LOG.info("This is some info");
			LOG.error("This is some error");
		});*/




		/*Button btnNewCar = new Button();
		btnNewCar.setText("Crear Auto");
		btnNewCar.setOnAction(a-> {
			createCar();
		});*/

		/*Button btnNewHW = new Button();
		btnNewHW.setText("Crear Nodo HW");
		btnNewHW.setOnAction(a->{
			createHWNode();
		});*/



		/*GridPane grid = new GridPane();
		grid.setPadding(new Insets(10));
		grid.setVgap(10);
		*//*grid.add(btnNewCar,0,0);
		grid.add(btnNewHW,1,0);*//*
		grid.add(textArea,0,0);
		Simulator sim = new Simulator();
        SwingNode snode = new SwingNode();
        panel.add(sim);
		grid.add(panel,0,1);
		mainScene = new Scene(grid, 800, 300);
		primaryStage.setScene(mainScene);
		primaryStage.show();*/
    }

    private class AwtInitializerTask extends FutureTask<JPanel> {
        public AwtInitializerTask(Callable<JPanel> callable) {
            super(callable);
        }
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
