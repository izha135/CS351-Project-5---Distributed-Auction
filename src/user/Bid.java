package user;

import common.Item;
import common.MessageEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * REMOVE SERIALIZABLE
 *
 * The class that encompasses everything related to making a bid for the user
 *
 * Contains the bid amount, house ID that contains the item to bid, the
 * actual item object that also encompasses the item to be bid on, and the
 * MessageEnum object (holds the command for reference)
 */
public class Bid implements Serializable {
    private double bidAmount;
    private int houseID;
    private Item item;
    private MessageEnum messageEnum;

    public Bid(double bidAmount, int houseID, Item item,
               MessageEnum messageEnum) {
        this.bidAmount = bidAmount;
        this.houseID = houseID;
        this.item = item;
        this.messageEnum = messageEnum;
    }

    public Bid(double bidAmount, int houseID, Item item) {
        this.bidAmount = bidAmount;
        this.houseID = houseID;
        this.item = item;
        messageEnum = null;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public int getHouseID() {
        return houseID;
    }

    public Item getItem() {
        return item;
    }

    public MessageEnum getMessageEnum() {
        return messageEnum;
    }

    public void updateBidMessageEnum(MessageEnum messageEnum) {
        this.messageEnum = messageEnum;
    }

    public static Bid getBidFromItemID(List<Bid> bidList, int itemIDToSearch) {
        for (Bid bid : bidList) {
            Item currentItem = bid.getItem();
            int currentItemID = currentItem.getItemId();

            if (currentItemID == itemIDToSearch) {
                return bid;
            }
        }

        return null; // shouldn't happen, hopefully...
    }

    // REALLY IMPORTANT WHEN USER PLACES MULTIPLE BIDS AT ONCE
    // WILL BE USED TO PREVENT USER FROM MAKING MULTIPLE BIDS ON THE SAME ITEM
    // Via contains() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        //return Objects.equals(item, bid.item);
        return item.equals(bid.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }

    @Override
    public String toString() {
        String messageEnumName;
        if (messageEnum == null) {
            messageEnumName = "[Empty]";
        } else {
            messageEnumName = messageEnum.name();
        }

        return messageEnumName + ": (House ID: " + houseID + ") "
                + item.getTreeItemTitle() + " at bid" +
                " $" + bidAmount;
    }

    // The methods below are method overloading of getAlternativeBidString()
    // to print to the display for the user GUI (Edit: just realized that a
    // bid object could been created -- new constructor with the item ID
    // instead of a item object -- for some of these...)

    public static String getAlternateBidString(MessageEnum messageEnum,
                                               int houseID, int itemID,
                                               double bidAmount) {
        return messageEnum.name() + ": (House ID: " + houseID
                + ") Item ID - " + itemID + " at bid" +
                " $" + bidAmount;
    }

    public static String getAlternateBidString(MessageEnum messageEnum,
                                               int houseID, int itemID,
                                               double bidAmount,
                                               int lastBidderID) {
        return messageEnum.name() + ": (House ID: " + houseID
                + ") Item ID - " + itemID + " at bid" +
                " $" + bidAmount + " from User ID: " + lastBidderID;
    }

    public static String getAlternateBidString(MessageEnum messageEnum,
                                               int houseID, String itemName,
                                               int itemID,
                                               double bidAmount) {
        return messageEnum.name() + ": (House ID: " + houseID + ") "
                + itemName + "(item ID - "
                + itemID + ") at bid" +
                " $" + bidAmount;
    }


    public static String getAlternateBidString(MessageEnum messageEnum,
                                               int houseID,
                                               String itemName, int itemID,
                                               String winnerUsername,
                                               double bidAmount) {
        return messageEnum.name() + ": (House ID: " + houseID + ") "
                + itemName + "(item ID - "
                + itemID + ") at bid" +
                " $" + bidAmount + " from winner username: " + winnerUsername;
    }
}
