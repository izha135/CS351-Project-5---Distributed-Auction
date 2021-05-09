package commonGUI;

import javafx.scene.control.Alert;

public class CustomAlert extends Alert {
    private String titleAlert;
    private String contextTextAlert;

    public CustomAlert(AlertType alertType,
                       String titleAlert, String contextTextAlert) {
        super(alertType);

        // forgot to add this...(that's why the alerts were "blank"...)
        this.setTitle(titleAlert);
        this.setContentText(contextTextAlert);

        this.titleAlert = titleAlert;
        this.contextTextAlert = contextTextAlert;
    }

    public Runnable showAlert() {
        this.show();
        return null;
    }
}
