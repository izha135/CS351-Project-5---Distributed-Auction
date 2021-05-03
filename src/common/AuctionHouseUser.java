package common;

import java.util.List;

public class AuctionHouseUser {
    private int houseID;
    private String houseHostName;
    private List<Item> itemList;

    public AuctionHouseUser(int houseID, String houseHostName) {
        this.houseID = houseID;
        this.houseHostName = houseHostName;
    }

    public int getHouseID() {
        return houseID;
    }

    public String getHouseHostName() {
        return houseHostName;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    @Override
    public String toString() {
        String currentString = "House ID: " + houseID + "\n";

        for (Item item : itemList) {
            currentString += item;
        }

        return currentString;
    }
}
