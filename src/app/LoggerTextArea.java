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
        TextAreaOutputStream outputStream = new TextAreaOutputStream(this);
        MyStaticOutputStreamAppender.setStaticOutputStream(outputStream);
    }

    // Use to send logger text into the ui textArea
    public static class TextAreaOutputStream extends OutputStream {

        private static final int MAX_LENGTH = 5000;
        private static final int DELETE_SIZE = 1000;
        private TextArea textArea;
        private StringBuffer buffer;

        public TextAreaOutputStream(TextArea textArea) {
            this.textArea = textArea;
            buffer = new StringBuffer();
        }

        @Override
        public void write(int b) {
            buffer.append((char) b);
            String accumulated = buffer.toString();
            if (accumulated.contains(System.lineSeparator())) {
                sendToUI(accumulated);
                buffer.delete(0, buffer.length()); //clean buffer
            }
        }

        private void sendToUI(String str) {
            Platform.runLater(() -> {
                int caretPosition = textArea.caretPositionProperty().get();
                textArea.appendText(str);
                textArea.positionCaret(caretPosition);
                if (textArea.getLength() > MAX_LENGTH) {
                    textArea.deleteText(0, DELETE_SIZE);
                }
            });

        }
    }
}
