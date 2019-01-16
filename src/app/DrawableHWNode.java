package app;


import highway.HWNode;
import highway.Segment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DrawableHWNode implements DrawableObject {
    private static final double DASHED_LINE_Y_END = 300;
    private HWNode node;

    public DrawableHWNode(String name) {
        node = new HWNode(name);

    }

    @Override
    public String getID() {
        return node.getId();
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(GraphicsContext gc) {
        if (node.getSegments() != null) {
            for (Segment seg : node.getSegments()) {
                double segStart = seg.getBeginX();
                double segEnd = seg.getEndX();
                double middle = (segEnd + segStart) / 2;
                gc.setStroke(Color.BLUEVIOLET);
                gc.setLineDashes(2d);
                gc.strokeLine(segEnd, 0d, segEnd, DASHED_LINE_Y_END);
                gc.setFill(Color.GREEN);
                gc.fillText("Segment" + seg.getIndex(), middle, DASHED_LINE_Y_END);
            }
        }
    }

    public void start() {
        node.listenForMsgs().registerInNetwork();
    }
}
