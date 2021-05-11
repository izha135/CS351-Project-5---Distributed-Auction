package common;

import java.io.Serializable;
import java.util.Objects;

/**
 * REMOVE SERIALIZABLE
 *
 * The class that encompasses everything related to storing an item for the user
 *
 * Contains the item name, item ID, house ID from where item is gotten from,
 * the minimum bid amount for the item, and some item description (we decided
 * the item description were going to be brief for our list, but I think the
 * bank will take in some item list and the items can vary from that given file)
 */
public class Item implements Serializable {
    private String itemName;
    private int itemId;
    private int houseId;
    private double minBid;
    private String itemDesc;

    public Item(String itemName, int itemId, double minBid, String itemDesc) {
        this.itemName = itemName;
        this.itemId = itemId;
        this.minBid = minBid;
        this.itemDesc = itemDesc;
        this.houseId = -1;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public double getItemBid() {
        return minBid;
    }

    public void setItemBid(double minBid) {
        this.minBid = minBid;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return itemId == item.itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return "Item name: " + itemName + "\nItem ID: " + itemId + "\nItem " +
                "Bid: " + minBid + "\nItem Description: " + itemDesc + "\n";
    }

    public String getTreeItemTitle() {
        return itemName + " (item ID: " + itemId + ")";
    }
}
