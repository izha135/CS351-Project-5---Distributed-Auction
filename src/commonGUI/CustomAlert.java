package commonGUI;

import javafx.scene.control.Alert;

public class CustomAlert extends Alert {
    private String titleAlert;
    private String contextTextAlert;

    public CustomAlert(AlertType alertType,
                       String titleAlert, String contextTextAlert) {
        super(alertType);

        this.titleAlert = titleAlert;
        this.contextTextAlert = contextTextAlert;
    }
}
