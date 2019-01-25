package app;

import highway.HWNode;
import highway.Segment;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class HWNodeDrawer implements Drawer {

    public static final Logger logger = LoggerFactory.getLogger(HWNodeDrawer.class);
    public static final String HWNODE_IMG_FILE = "resources/hwnode2.png";
    private static final String NEIGH_IMG_FILE = "resources/car3.png";
    private static final long MIN_TIME_BEFORE_UPDATE = 500;
    private HWNode node;
    private long previousTime;
    private Image carImg;
    private Image nodeImg;


    public HWNodeDrawer(HWNode node) {
        this.node = node;
        initImages();
    }

    private void initImages() {
        try {
            //img has a 1:1 ratio
            nodeImg = new Image(new File(HWNODE_IMG_FILE).toURI().toString(), 50, 50, true, true);
            //img has a 2:1 ratio
            carImg = new Image(new File(NEIGH_IMG_FILE).toURI().toString(), 30, 15, true, true);
            logger.info("Hwnode Imgs initialized and scaled");
        } catch (IllegalArgumentException e) {
            logger.error("Could not initialize image: " + e.getMessage());
        }
    }

    @Override
    public void draw(Canvas canvas, long currentTime) {
        if (updateNeeded(currentTime)) {
            drawBackground(canvas);
            drawNode(canvas);
            drawSegments(canvas);
        }
    }

    /**
     * This function assumes that the segments are equal rectangles starting from (0,0)
     *
     * @param canvas where the segments are going to be drawn
     */
    private void drawSegments(Canvas canvas) {
        List<Segment> segments = node.getSegments();
        if (segments != null && !segments.isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            double maxHeigt = canvas.getHeight() - (nodeImg.getHeight() + 20);
            Segment firstSegment = segments.get(0);
            Segment lastSegment = segments.get(segments.size() - 1);
            double unscaledWidth = lastSegment.getEndX() - firstSegment.getBeginX();
            System.out.println("unscaled width " + unscaledWidth);
            double scaleX = calculateScaleFactor(lastSegment.getEndX() - firstSegment.getBeginX(), canvas.getWidth());
            double scaleY = calculateScaleFactor(lastSegment.getEndY() - lastSegment.getBeginY(), maxHeigt);
            for (Segment seg : node.getSegments()) {
                double segStartX = (seg.getBeginX() - firstSegment.getBeginX()) * scaleX;
                double segEndX = (seg.getEndX() - firstSegment.getBeginX()) * scaleX;
                double segStartY = seg.getBeginY() * scaleY;
                double segEndY = seg.getEndY() * scaleY;
                gc.setStroke(Color.BLUEVIOLET);
                gc.setLineDashes(2d);
                gc.strokeLine(segEndX, segStartY, segEndX, segEndY);
                gc.setFill(Color.GREEN);
                gc.setFont(new Font(10));
                gc.fillText("Segment" + seg.getIndex(), segStartX, segEndY);
            }
        }

    }

    private double calculateScaleFactor(double size1, double size2) {
        return size2 / size1;
    }

    private void drawNode(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        //in middle of the bottom with x and y offsets accounting for the img size
        gc.drawImage(nodeImg, (canvas.getWidth() / 2) - (nodeImg.getWidth() / 2), canvas.getHeight() - (nodeImg.getHeight() + 7));
    }

    private boolean updateNeeded(long currentTime) {
        if (previousTime == 0) {
            previousTime = currentTime;
            return true;
        }
        long timeEnlapsed = currentTime - previousTime;
        return timeEnlapsed > MIN_TIME_BEFORE_UPDATE;
    }

    private void drawBackground(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
    }
}
