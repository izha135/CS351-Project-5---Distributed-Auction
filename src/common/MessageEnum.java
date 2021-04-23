package common;

public enum MessageEnum {
    ITEMS, GET_ITEMS_FROM_BANK, GET_MASTER_LIST, REMOVE_ITEM,
    BID, ACCEPT, REJECT, OUTBID, WINNER, GET_ITEMS, HOUSE_ITEMS, GET_HOUSES, ITEM;

    @Override
    public String toString() {
        switch (this) {
            case BID:
                return "bid";
            case ITEM:
                return "item";
            case ITEMS:
                return "items";
            case ACCEPT:
                return "accept";
            case OUTBID:
                return "outbid";
            case REJECT:
                return "reject";
            case WINNER:
                return "winner";
            case GET_ITEMS:
                return "getItems";
            case GET_HOUSES:
                return "getHouses";
            case HOUSE_ITEMS:
                return "houseItems";
            case REMOVE_ITEM:
                return "removeItem";
            case GET_MASTER_LIST:
                return "getMasterList";
            case GET_ITEMS_FROM_BANK:
                return "getItemsFromBank";
            default:
                return "unknown";
        }
    }
}
