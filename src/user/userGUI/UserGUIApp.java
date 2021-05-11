package user.userGUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The application part containing the main for the user GUI
 *
 * Standard boiler plate to load the FXML file and set up the stage
 *
 * IMPORTANT: the window CANNOT be close normally and has to verify with the
 * network before it can be closed (as part of the program without force
 * closing it...)
 */
public class UserGUIApp extends Application {
    public static String bankHostNameInput;
    public static int bankPort;
    public static int housePort;
    public static String username;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            BorderPane root =
                    (BorderPane) loader.load(
                            getClass().getResource(
                                    "UserGUI.fxml").openStream());
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            primaryStage.setTitle("User Window");

            // DISABLED the close button on the window
            primaryStage.setOnCloseRequest(this::preventCloseWindow);

            root.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void preventCloseWindow(Event event) {
        event.consume();
    }

    public static void main(String[] args) {
//        bankPort = Integer.parseInt(args[0]);
//        housePort = Integer.parseInt(args[1]);
//        username = args[2];

        // FIXME: move ports to readers...
        bankHostNameInput = args[0];
        bankPort = Integer.parseInt(args[1]);
        username = args[2];

        launch(args);
    }

    // just trying out stuff...
    public void createDisplay() {
        //launch();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            start(stage);
        });
    }
}
