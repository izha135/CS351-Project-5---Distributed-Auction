package commonGUI;

import javafx.scene.control.Label;

/**
 * Concatenates the two strings and sets it as the text for the label
 * Custom Label class that takes in an initial message and an output message
 * that can be changed and the text of the label can be updated all at once
 */
public class CustomLabel extends Label {
    private String initialMessage;
    private String outputMessage;

    /**
     * Constructor for CustomLabel
     * @param initialMessage
     * @param outputMessage
     */
    public CustomLabel(String initialMessage, String outputMessage) {
        super(initialMessage + " " + outputMessage);

        this.initialMessage = initialMessage;
        this.outputMessage = outputMessage;
    }

    public CustomLabel(Label label) {
        super(label.getText());

        this.initialMessage = label.getText();
        outputMessage = "";
    }

    public void updateLabel(String outputMessage) {
        this.outputMessage = outputMessage;

        this.setText(initialMessage + " " + this.outputMessage);
    }

    public String getOutputMessage() {
        return outputMessage;
    }

    public int getIntOutputMessage() {
        if (outputMessage.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(outputMessage);
    }
}
