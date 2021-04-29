package common;

import java.util.List;

public class HouseIDItemList {
    private int houseID;
    private List<Item> itemList;

    public HouseIDItemList(int houseID, List<Item> itemList) {
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
