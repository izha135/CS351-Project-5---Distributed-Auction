package commonGUI;

import common.Item;
import javafx.scene.control.TreeItem;

public class CustomItemTreeItem extends TreeItem<String> {
    private Item item;

    public CustomItemTreeItem(String value, Item item) {
        super(value);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
