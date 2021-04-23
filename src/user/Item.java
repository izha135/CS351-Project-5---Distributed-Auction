package user;

public class Item {
    public static final String DELIMITER = ";";

    private String itemName;
    private int itemID;
    private String itemDescription;

    public Item(String itemName, int itemID, String itemDescription) {
        this.itemName = itemName;
        this.itemID = itemID;
        this.itemDescription = itemDescription;
    }
}
