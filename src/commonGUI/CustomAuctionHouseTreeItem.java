package commonGUI;

import common.AuctionHouseUser;
import javafx.scene.control.TreeItem;

import java.util.Objects;

public class CustomAuctionHouseTreeItem extends TreeItem<String> {
    private int houseID;
    private AuctionHouseUser auctionHouseUser;

    public CustomAuctionHouseTreeItem(String value,
                                      AuctionHouseUser auctionHouseUser) {
        super(value);
        this.auctionHouseUser = auctionHouseUser;
        this.houseID = auctionHouseUser.getHouseID();
    }

    public boolean checkID(int someID) {
        return someID == houseID;
    }

    public AuctionHouseUser getAuctionHouseUser() {
        return auctionHouseUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomAuctionHouseTreeItem that = (CustomAuctionHouseTreeItem) o;
        return houseID == that.houseID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseID);
    }
}
