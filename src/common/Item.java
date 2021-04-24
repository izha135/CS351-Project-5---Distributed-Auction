package common;

public class Item {
    private String itemName;
    private int itemId;
    private int houseId;
    private int minBid;
    private String itemDesc;

    public Item(String itemName, int itemId, int minBid, String itemDesc) {
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

    public int getMinBid() {
        return minBid;
    }

    public void setMinBid(int minBid) {
        this.minBid = minBid;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }
}
