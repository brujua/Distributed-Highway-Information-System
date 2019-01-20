package app;

import javafx.scene.layout.Pane;

public interface NodeView {
    Pane getContent();
    void close();
}
