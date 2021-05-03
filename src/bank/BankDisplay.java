package bank;

import common.Item;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class BankDisplay extends Application {
    private final int WIDTH = 800;
    private final int HEIGHT = 700;
    private VBox mainVBox;
    private Pane root;
    private Map<Integer, HouseBox> houses;

    public void createDisplay() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new Pane();

        initializeElements();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnScroll(event -> {
            root.setTranslateY(root.getTranslateY() - event.getDeltaY() * 2);
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void initializeElements() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPrefWidth(WIDTH);

        root.getChildren().add(vbox);
        mainVBox = vbox;

        houses = new HashMap<>();
    }

    public void addHouse(int houseId) {
        HouseBox houseBox = new HouseBox(houseId);
        houses.put(houseId, houseBox);
        root.getChildren().add(houseBox.getHouseBox());
    }

    public void addHouseItem(int houseId, Item item) {
        houses.get(houseId).addItem(item);
    }

    public void removeHouseItem(int houseId, int itemId) {
        houses.get(houseId).removeItem(itemId);
    }

    public void updateHouseItem(int houseId, int itemId, String topBidder, double bid) {
        houses.get(houseId).updateItem(itemId, topBidder, bid);
    }

    private class HouseBox {
        private HBox houseBox;
        // Map from integer id for each item to groups
        private Map<Integer, Group> items;
        private VBox itemsBox;

        public HouseBox(int houseId) {
            houseBox = new HBox();
            houseBox.setSpacing(5);

            Label label = new Label("House " + houseId + ": ");
            houseBox.getChildren().add(label);

            itemsBox = new VBox();
            itemsBox.setBorder(new Border(
                    new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            items = new HashMap<>();
        }

        public void addItem(Item item) {
            Group itemGroup = new Group();

            Label label = new Label(item.getItemName() + ": " + item.getItemDesc());
            itemGroup.getChildren().add(label);

            items.put(item.getItemId(), itemGroup);
            itemsBox.getChildren().add(itemGroup);
        }

        public void updateItem(int itemId, String topBidder, double bid) {
            if(items.get(itemId).getChildren().size() > 1) {
                items.get(itemId).getChildren().remove(1);
            }

            Label label = new Label(topBidder + "  " + bid);
            label.setTranslateX(200);
            items.get(itemId).getChildren().add(label);
        }

        public void removeItem(int itemId) {
            itemsBox.getChildren().remove(items.get(itemId));
            items.remove(itemId);
        }

        public HBox getHouseBox() {
            return houseBox;
        }
    }
}
