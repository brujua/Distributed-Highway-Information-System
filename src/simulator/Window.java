package simulator;

import javax.swing.*;
import java.awt.*;

public class Window extends Canvas {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private int width;
	private int height;
	private JFrame frame;

	public Window(int width, int height, String title, Component component) {
		super();
		this.width = width;
		this.height = height;
		frame = new JFrame(title);
		frame.setPreferredSize(new Dimension(width, height));
		frame.setMaximumSize(new Dimension(width, height));
		frame.setMinimumSize(new Dimension(width, height));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(component);
		frame.setVisible(true);

	}
}
