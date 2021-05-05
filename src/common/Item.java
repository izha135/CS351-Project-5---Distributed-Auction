package common;

import java.io.Serializable;

public class Item implements Serializable {
    private String itemName;
    private int itemId;
    private int houseId;
    private double itemBid;
    private String itemDesc;
    private int bidderId;

    public Item(String itemName, int itemId, double minBid, String itemDesc) {
        this.itemName = itemName;
        this.itemId = itemId;
        this.itemBid = minBid;
        this.itemDesc = itemDesc;
        this.houseId = -1;
        this.bidderId = -1;
    }

    public void setBidderId(int bidderId) { this.bidderId = bidderId; }

    public int getBidderId() { return bidderId; }

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
        return itemBid;
    }

    public void setItemBid(double itemBid) {
        this.itemBid = itemBid;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    @Override
    public String toString() {
        return "Item name: " + itemName + "\nItem ID: " + itemId + "\nItem " +
                "Bid: " + itemBid + "\nItem Description: " + itemDesc + "\n";
    }

    public String getTreeItemTitle() {
        return itemName + " (item ID: " + itemId + ")";
    }
}
