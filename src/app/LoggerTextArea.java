package app;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.OutputStream;

public class LoggerTextArea extends TextArea {

    private static final double PREF_WIDHT = 600;

    public LoggerTextArea() {
        super();
        this.setEditable(false);
        this.setPrefWidth(PREF_WIDHT);
        MyStaticOutputStreamAppender.setStaticOutputStream(new TextAreaOutputStream(this));
    }

    // Use to send logger text into the ui textArea
    public static class TextAreaOutputStream extends OutputStream {

        private TextArea textArea;

        public TextAreaOutputStream(TextArea textArea) {
            this.textArea = textArea;
        }


        @Override
        public void write(int b) {
            Platform.runLater(() -> {
                int caretPosition = textArea.caretPositionProperty().get();
                textArea.appendText(String.valueOf((char) b));
                textArea.positionCaret(caretPosition);
            });
        }
    }
}
