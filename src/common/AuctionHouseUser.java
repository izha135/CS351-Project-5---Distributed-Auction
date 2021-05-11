package common;

import java.util.List;
import java.util.Objects;

public class AuctionHouseUser {
    private int houseID;
    private String houseHostName;
    private int housePort;
    private List<Item> itemList;

    public AuctionHouseUser(int houseID, String houseHostName, int housePort) {
        this.houseID = houseID;
        this.houseHostName = houseHostName;
        this.housePort = housePort;
    }

    public int getHouseID() {
        return houseID;
    }

    public String getHouseHostName() {
        return houseHostName;
    }

    public int getHousePort() {
        return housePort;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuctionHouseUser that = (AuctionHouseUser) o;
        return houseID == that.houseID && housePort == that.housePort
                && Objects.equals(houseHostName, that.houseHostName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseID, houseHostName, housePort);
    }

    @Override
    public String toString() {
        return "House ID: " + houseID + ", house host name: "
                + houseHostName + ", port: " + housePort + "\n";
    }

    public String getFullString() {
        String currentString =
                this.toString();

        for (Item item : itemList) {
            currentString += item;
        }

        return currentString;
    }
}
