package commonGUI;

import common.Item;
import javafx.scene.control.TreeItem;

/**
 * Custom tree item for a given item (mostly used as a marker)
 *
 * Holds the corresponding item object associated with the item tree item
 */
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
