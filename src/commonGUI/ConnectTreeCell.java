package commonGUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;

public class ConnectTreeCell extends TreeCell<String> {
    private ContextMenu connectContextMenu = new ContextMenu();

    public ConnectTreeCell() {
        MenuItem connectMenuItem = new MenuItem("Connect to this Auction " +
                "House");
        connectContextMenu.getItems().add(connectMenuItem);
        connectMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });
    }
}
