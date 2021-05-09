package user.userGUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.swing.*;

public class UserGUIApp extends Application {
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

//    public void start(Stage primaryStage) {
//        try {
//            Platform.runLater(() -> {
//                FXMLLoader loader = new FXMLLoader();
//                BorderPane root =
//                        (BorderPane) loader.load(
//                                getClass().getResource(
//                                        "UserGUI.fxml").openStream());
//                primaryStage.setScene(new Scene(root));
//                primaryStage.show();
//                primaryStage.setTitle("User Window");
//
//                // DISABLED the close button on the window
//                primaryStage.setOnCloseRequest(this::preventCloseWindow);
//
//                root.requestFocus();
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }

    private void preventCloseWindow(Event event) {
        event.consume();
    }

    public static void main(String[] args) {
        bankPort = Integer.parseInt(args[0]);
        housePort = Integer.parseInt(args[1]);
        username = args[2];
        launch(args);
//        UserGUIApp userGUIApp = new UserGUIApp();
//        userGUIApp.createDisplay();

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
    }

    public void createDisplay() {
        //launch();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            start(stage);
        });
    }

//    private void newSwingStart() {
//        JFrame jFrame = new JFrame("User Program");
//        JFXPanel jfxPanel = new JFXPanel();
//
//        jFrame.add(jfxPanel);
//
//        Platform.runLater(() -> {
//
//        });
//    }
}
