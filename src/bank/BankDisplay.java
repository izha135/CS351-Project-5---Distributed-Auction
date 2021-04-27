package bank;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BankDisplay extends Application {
    private static int WIDTH = 800;
    private static int HEIGHT = 700;

    public void createDisplay() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new Pane();

        initializeElements(root);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        AnimationTimer at = new AnimationTimer() {
            @Override
            public void handle(long now) {

            }
        };

        at.start();
        // TODO: Make a graphic display for the Bank (For visualization purposes)
    }

    public void initializeElements(Pane root) {

    }
}
