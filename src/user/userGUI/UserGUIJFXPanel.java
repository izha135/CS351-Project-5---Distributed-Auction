package user.userGUI;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UserGUIJFXPanel extends JFXPanel {
    private void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader();
            BorderPane root =
                    (BorderPane) loader.load(
                            getClass().getResource(
                                    "UserGUI.fxml").openStream());
            setScene(new Scene(root));

            // DISABLED the close button on the window
            //primaryStage.setOnCloseRequest(this::preventCloseWindow);

            root.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

//    public void createDisplay() {
//        //launch();
//        Platform.runLater(() -> {
//            Stage stage = new Stage();
//            start(stage);
//        });
//    }
}
