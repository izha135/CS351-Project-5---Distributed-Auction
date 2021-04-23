package common;

public enum MessageEnum {
    LOGIN,
    ITEMS, GET_ITEMS_FROM_BANK, GET_MASTER_LIST, REMOVE_ITEM,
    BID, ACCEPT, REJECT, OUTBID, WINNER, GET_ITEMS, HOUSE_ITEMS, GET_HOUSES, ITEM;

    @Override
    public String toString() {
        switch (this) {
            case LOGIN:
                return "login";
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

    public static MessageEnum parseCommand(String command) {
        if(command.equals("bid")) return BID;
        if(command.equals("item")) return ITEM;
        if(command.equals("login")) return LOGIN;
        if(command.equals("items")) return ITEMS;
        if(command.equals("accept")) return ACCEPT;
        if(command.equals("outbid")) return OUTBID;
        if(command.equals("reject")) return REJECT;
        if(command.equals("winner")) return WINNER;
        if(command.equals("getItems")) return GET_ITEMS;
        if(command.equals("getHouses")) return GET_HOUSES;
        if(command.equals("houseItems")) return HOUSE_ITEMS;
        if(command.equals("removeItem")) return REMOVE_ITEM;
        if(command.equals("getMasterList")) return GET_MASTER_LIST;
        if(command.equals("getItemsFromBank")) return GET_ITEMS_FROM_BANK;

        return null;
    }
}
