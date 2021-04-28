package common;

import java.util.List;

public class HouseIDList {
    private int houseID;
    private List<Item> itemList;

    public HouseIDList(int houseID, List<Item> itemList) {
        this.houseID = houseID;
        this.itemList = itemList;
    }

    public int getHouseID() {
        return houseID;
    }

    public List<Item> getItemList() {
        return itemList;
    }
}
