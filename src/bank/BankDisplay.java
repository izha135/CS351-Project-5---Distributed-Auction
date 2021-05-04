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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class BankDisplay extends Application {
    private final int WIDTH = 800;
    private final int HEIGHT = 700;
    private VBox mainVBox1, mainVBox2;
    private Pane root;
    private Group scene1, scene2;
    private Map<Integer, HouseBox> houses;
    private Map<Integer, UserBox> users;

    public void createDisplay() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        scene1 = new Group();
        scene2 = new Group();
        root = new Pane(scene1);
        scene1.getChildren().add(new Rectangle(10, 10, Color.BLACK));

        initializeElements();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnScroll(event -> {
            root.setTranslateY(root.getTranslateY() - event.getDeltaY() * 0.8);
        });

        scene.setOnMouseClicked(event -> {
            if(scene1 == root.getChildren().get(0)) {
                root.getChildren().clear();
                root.getChildren().add(scene2);
            }
            else {
                root.getChildren().clear();
                root.getChildren().add(scene1);
            }
            root.setTranslateY(0);
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void initializeElements() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPrefWidth(WIDTH);

        scene1.getChildren().add(vbox);
        mainVBox1 = vbox;

        houses = new HashMap<>();
        users = new HashMap<>();
    }

    public void addUser(int userId, String name, double balance) {
        UserBox userBox = new UserBox(name, balance);
        users.put(userId, userBox);
        mainVBox2.getChildren().add(userBox.getUserBox());
    }

    public void removeUser(int userId) {
        mainVBox2.getChildren().remove(userId);
        users.remove(userId);
    }

    public void changeUserBalance(int userId, double balance) {
        users.get(userId).changeBalance(balance);
    }

    public void changeUserRemaining(int userId, double remaining) {
        users.get(userId).changeRemaining(remaining);
    }

    public void addHouse(int houseId) {
        HouseBox houseBox = new HouseBox(houseId);
        houses.put(houseId, houseBox);
        mainVBox1.getChildren().add(houseBox.getHouseBox());
    }

    public void removeHouse(int houseId) {
        mainVBox1.getChildren().remove(houses.get(houseId));
        houses.remove(houseId);
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

    private class UserBox {
        private HBox userBox;
        private Label nameLabel, balanceLabel, remainingLabel;

        public UserBox(String name, double balance) {
            String adjustedBal = Double.toString(Math.round(balance*100)/100.0);

            userBox = new HBox();
            nameLabel = new Label(name);
            balanceLabel = new Label(adjustedBal);
            remainingLabel = new Label(adjustedBal);
        }

        public void changeBalance(double balance) {
            balanceLabel.setText(Double.toString(Math.round(balance*100)/100.0));
        }

        public void changeRemaining(double remaining) {
            balanceLabel.setText(Double.toString(Math.round(remaining*100)/100.0));
        }

        public HBox getUserBox() {
            return userBox;
        }
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
