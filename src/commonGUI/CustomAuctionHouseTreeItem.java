package commonGUI;

import common.AuctionHouseUser;
import javafx.scene.control.TreeItem;

import java.util.Objects;

/**
 * Custom tree item for a given auction house (mostly used as a marker and
 * with custom equals() to fetch the same GUI element every time the display
 * is refreshed...)
 *
 * Holds the corresponding house ID and the actual Auction House object
 * (house ID could have been removed since it's linked with the house object...)
 */
public class CustomAuctionHouseTreeItem extends TreeItem<String> {
    private int houseID;
    private AuctionHouseUser auctionHouseUser;

    public CustomAuctionHouseTreeItem(String value,
                                      AuctionHouseUser auctionHouseUser) {
        super(value);
        this.auctionHouseUser = auctionHouseUser;

        if (auctionHouseUser != null) {
            this.houseID = auctionHouseUser.getHouseID();
        }
    }

    public CustomAuctionHouseTreeItem(String value) {
        super(value);
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
