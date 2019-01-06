package simulator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class HUD implements SimObject, KeyListener {

    private static final String addCarStr = "Add car at position: ";
    private static final String addNodeStr = "Add node";
    private static final String simModeStr = "Simulation Mode: \n";
    private static final String keyControlsDescription = "Controls> Car Pos: Arrow up/down \n Add Car: \'C\' \n Add Node: \'N\' \n Change Sim Mode: \'S\' ";
    private static final int POS_INCREMENT = 3;
    private static final int padding = 25;
    private final int width;
    private final int height;
    private final int heightstart;
    private int carStartPosition = 0;
    private SimController simController;
    private boolean simModeOn;

    public HUD(SimController simController, int width, int height, int heightstart) {
        this.simController = simController;
        this.width = width;
        this.height = height;
        this.heightstart = heightstart;
        simModeOn = true;
    }

    @Override
    public String getID() {
        return "HUD";
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Graphics graphics) {
        drawRoad(graphics);
        drawControls(graphics);
    }

    @Override
    public void setSimModeOn(boolean on) {
        simModeOn = on;
    }

    private void drawControls(Graphics graphics) {
        if (graphics instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) graphics;
            graphics.setColor(Color.green);
            Stroke oldStroke = g2d.getStroke();
            Stroke dashedStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            //divisory line for controls
            g2d.setStroke(dashedStroke);
            g2d.drawLine(0, heightstart, width, heightstart);
            g2d.setStroke(oldStroke);
            //controls
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawString(addCarStr + carStartPosition, padding, heightstart + padding);
            g2d.drawString(keyControlsDescription, padding, heightstart + padding + 40);
            g2d.drawString(simModeStr, padding, heightstart + 80 + padding);
            if (simModeOn) {
                g2d.drawString("ON", padding, heightstart + padding + 80 + padding);
            } else {
                g2d.setColor(Color.red);
                g2d.drawString("OFF", padding, heightstart + padding + 80 + padding);
            }
        }


    }

    private void drawRoad(Graphics graphics) {
        graphics.setColor(Color.green);
        graphics.drawLine(0, 15, width, 15);
        graphics.drawLine(0, 200, width, 200);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN: {
                carStartPosition -= POS_INCREMENT;
                break;
            }
            case KeyEvent.VK_UP: {
                carStartPosition += POS_INCREMENT;
                break;
            }
            case KeyEvent.VK_C: {
                simController.addCar(carStartPosition);
                break;
            }
            case KeyEvent.VK_N: {
                simController.addHWNode();
                break;
            }
            case KeyEvent.VK_S: {
                simModeOn = !simModeOn;
                simController.setSimModeOn(simModeOn);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
