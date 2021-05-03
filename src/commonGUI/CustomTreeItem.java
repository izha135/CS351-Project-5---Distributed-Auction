package commonGUI;

import javafx.scene.control.TreeItem;

public class CustomTreeItem extends TreeItem<String> {
    private int id;

    public CustomTreeItem(String value, int id) {
        super(value);
        this.id = id;
    }
}
