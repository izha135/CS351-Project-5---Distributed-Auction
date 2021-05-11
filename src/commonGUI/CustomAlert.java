package commonGUI;

import javafx.scene.control.Alert;

/**
 * A class made to make initializing and showing some alert a lot easier --
 * some defined arguments to be displayed with the alert (alert type, title
 * of the alert, and the context text of the alert)
 */
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

    // only used for testing purposes...
    public Runnable showAlert() {
        this.show();
        return null;
    }
}
