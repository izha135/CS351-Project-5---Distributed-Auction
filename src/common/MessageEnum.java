package common;

public enum MessageEnum {
    LOGIN, HOUSE, USER, GET_ITEM, ERROR, SET_HIGH_BID, HOUSE_LIST,
    ITEMS, GET_ITEMS_FROM_BANK, REMOVE_ITEM, ITEM_WON, AUCTION_ENDED,
    BID, ACCEPT, REJECT, OUTBID, WINNER, GET_ITEMS, HOUSE_ITEMS, GET_HOUSES, ITEM;

    @Override
    public String toString() {
        switch (this) {
            case AUCTION_ENDED:
                return "auctionEnded";
            case ITEM_WON:
                return "itemWon";
            case HOUSE_LIST:
                return "houseList";
            case SET_HIGH_BID:
                return "setHighBid";
            case ERROR:
                return "error";
            case GET_ITEM:
                return "getItem";
            case USER:
                return "user";
            case HOUSE:
                return "house";
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
            case GET_ITEMS_FROM_BANK:
                return "getItemsFromBank";
            default:
                return "unknown";
        }
    }

    public static MessageEnum parseCommand(String command) {
        if(command.equals("bid")) return BID;
        if(command.equals("user")) return USER;
        if(command.equals("item")) return ITEM;
        if(command.equals("error")) return ERROR;
        if(command.equals("house")) return HOUSE;
        if(command.equals("login")) return LOGIN;
        if(command.equals("items")) return ITEMS;
        if(command.equals("accept")) return ACCEPT;
        if(command.equals("outbid")) return OUTBID;
        if(command.equals("reject")) return REJECT;
        if(command.equals("winner")) return WINNER;
        if(command.equals("itemWon")) return ITEM_WON;
        if(command.equals("getItem")) return GET_ITEM;
        if(command.equals("getItems")) return GET_ITEMS;
        if(command.equals("houseList")) return HOUSE_LIST;
        if(command.equals("getHouses")) return GET_HOUSES;
        if(command.equals("houseItems")) return HOUSE_ITEMS;
        if(command.equals("removeItem")) return REMOVE_ITEM;
        if(command.equals("setHighBid")) return SET_HIGH_BID;
        if(command.equals("auctionEnded")) return AUCTION_ENDED;
        if(command.equals("getItemsFromBank")) return GET_ITEMS_FROM_BANK;

        return null;
    }
}
