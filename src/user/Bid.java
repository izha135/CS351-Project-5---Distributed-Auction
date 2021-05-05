package user;

import common.Item;
import common.MessageEnum;

import java.io.Serializable;

public class Bid implements Serializable {
    private double bidAmount;
    private Item item;
    private MessageEnum messageEnum;

    public Bid(double bidAmount, Item item, MessageEnum messageEnum) {
        this.bidAmount = bidAmount;
        this.item = item;
        this.messageEnum = messageEnum;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    @Override
    public String toString() {
        return messageEnum.name() + ": " + item.getTreeItemTitle();
    }
}
