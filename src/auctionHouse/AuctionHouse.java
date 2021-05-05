package auctionHouse;

import common.Item;
import common.MessageEnum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AuctionHouse {

    private static int port;
    private static int ahId; // Requested and received from Auction Central
    private static HashMap<Integer, Item> items; //Item ID as key for the item.
    private static HashMap<Integer, TimerThread> itemTimers;
    private static List<SocketInfo> userList;
    private static PrintWriter bankWriter;

    private static boolean run;

    /**
     * Creates an AuctionHouse that has three random items for sale.
     *
     * args[0] is expected to be the hostname
     */
    public static void main(String[] args) {
        items = new HashMap<>();
        itemTimers = new HashMap<>();
        userList = new LinkedList<>();
        String hostName = args[0];
        port = 3030;

        String message;
        String[] split;
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        AuctionHouseListener listener = new AuctionHouseListener(server, userList);
        listener.start();

        try (Socket socket = new Socket(hostName, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            bankWriter = writer;

            writer.println(MessageEnum.HOUSE.toString());
            while(!reader.ready()) System.out.print("");
            //while(!reader.ready()) ;

            // This is dealing with the login message from the Bank
            message = reader.readLine();
            split = message.split(";");
            ahId = Integer.parseInt(split[1]);

            // Get items for the auction house from the bank
            populateItems(writer, reader);

            // Now being listening and parsing messags
            run = true;
            while(run) {
                for(int i = 0; i < userList.size(); i++) {
                    if(userList.get(i).reader.ready()) {
                        SocketInfo user = userList.get(i);
                        message = user.reader.readLine();
                        parseUserMessage(message, user.writer, writer);
                    }
                }
                if(reader.ready()) {
                    message = reader.readLine();
                    parseBankMessage(message, writer);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SocketInfo getUser(int userId) {
        SocketInfo user = null;
        synchronized (userList) {
            for(SocketInfo socket : userList) {
                if(socket.id == userId) {
                    user = socket;
                    break;
                }
            }
        }
        return user;
    }

    // THE writer IS THE WRITER FOR THE USER. USE IT TO SEND MESSAGES
    public static void parseUserMessage(String message, PrintWriter userWriter, PrintWriter bankWriter) {
        String[] split = message.split(";");
        MessageEnum command = MessageEnum.parseCommand(split[0]);

        switch(command) {
            case BID:
                int userId = Integer.parseInt(split[1]);
                double bidAmount = Double.parseDouble(split[2]);
                int itemId = Integer.parseInt(split[3]);
                int houseId = Integer.parseInt(split[4]); // Might wanna check if this agrees with the ahId
                // Check if the item is in the AuctionHouse
                Item relevantItem = null;
                synchronized (items) {
                    for(Item item : items.values()) {
                        if (item.getItemId() == itemId) {
                            relevantItem = item;
                            break;
                        }
                    }
                }
                if(relevantItem == null) {
                    // If not, send a REJECT message (formatted) to the user
                    String userMessage = MessageEnum.REJECT.toString();
                    userMessage += ";" + ahId + ";" + itemId;
                    userWriter.println(userMessage);
                }
                else {
                    // Else, send a VALID_BID message to the bank
                    String bankMessage = MessageEnum.VALID_BID.toString();
                    bankMessage += ";" + houseId + ";" + userId + ";" + bidAmount + ";";
                    bankMessage += itemId + ";" + relevantItem.getBidderId();
                    bankWriter.println(bankMessage);
                }
                break;
            case GET_ITEM:
                itemId = Integer.parseInt(split[1]);
                /*relevantItem = null;
                synchronized (items) {
                    for(Item item : items.values()) {
                        if (item.getItemId() == itemId) {
                            relevantItem = item;
                            break;
                        }
                    }
                }*/
                relevantItem = items.get(itemId);
                if(relevantItem == null) {
                    userWriter.println(MessageEnum.ERROR + ";The Item does not exist");
                }
                else {
                    // Return an ITEM message to the requesting user
                    String userMessage = MessageEnum.ITEM.toString();
                    userMessage += ";" + ahId + ";" + relevantItem.getItemName() + ";";
                    userMessage += relevantItem.getItemId() + ";" + relevantItem.getItemBid();
                    userMessage += ";" + relevantItem.getItemBid() + ";" + relevantItem.getItemDesc();
                    userWriter.println(userMessage);
                }
                break;
            case GET_ITEMS:
                relevantItem = null;
                String userMessage = MessageEnum.ITEMS + ";" + ahId;
                synchronized (items) {
                    userMessage += ";" + items.size();
                    for(Item item : items.values()) {
                        userMessage += ";" + item.getItemName() + ";" + item.getItemId() + ";";
                        userMessage += item.getItemBid() + ";" + item.getBidderId() + ";";
                        userMessage += item.getItemDesc();
                    }
                }
                // Return an ITEMS message to the requesting user
                userWriter.println(userMessage);
                break;
            case EXIT:
                userId = Integer.parseInt(split[1]);
                // Check if the user is the highest bidder on any of the items
                boolean canExit = true;
                synchronized (items) {
                    for(Item item : items.values()) {
                        if (item.getBidderId() == userId) {
                            canExit = false;
                            break;
                        }
                    }
                }
                userMessage = "";
                if(canExit) {
                    // If true, send and ERROR message "Cannot exit"
                    userMessage = MessageEnum.ERROR + ";Cannot Exit";
                }
                else {
                    // Else, return a CAN_EXIT message
                    userMessage = MessageEnum.CAN_EXIT + ";" + ahId;
                }
                userWriter.println(userMessage);
                break;
            default:
                System.out.println("Error: Unhandled user message");
        }
    }

    public static void parseBankMessage(String message, PrintWriter bankWriter) {
        String[] split = message.split(";");
        MessageEnum command = MessageEnum.parseCommand(split[0]);

        switch(command) {
            case ACCEPT_BID:
                int userId = Integer.parseInt(split[1]);
                int itemId = Integer.parseInt(split[2]);
                Item item = items.get(itemId);
                String userMessage = "";
                SocketInfo user = getUser(userId);
                if(item == null) {
                    userMessage = MessageEnum.ERROR + ";Item no longer for bid";
                }
                else {
                    // Add a timer to the item if someone has bid for the first time on it
                    if(item.getBidderId() == -1) {
                        TimerThread thread = new TimerThread(itemId);
                        synchronized (itemTimers){
                            itemTimers.put(itemId, thread);
                        }
                        thread.run();
                    }
                    else {
                        // Otherwise, reset the timer with a valid bid
                        TimerThread thread = itemTimers.get(itemId);
                        if(thread == null) {
                            System.out.println("Something weird happened");
                        }
                        else {
                            thread.resetTimer();
                        }
                    }

                    // Change the highest bidder on the item to the userId in the message
                    item.setBidderId(userId);
                    // Send a ACCEPT message to the user
                    userMessage = MessageEnum.ACCEPT_BID + ";" + userId + ";" + itemId;
                }
                user.writer.println(userMessage);
                break;
            case REJECT_BID:
                userId = Integer.parseInt(split[1]);
                itemId = Integer.parseInt(split[2]);
                item = items.get(itemId);
                userMessage = "";
                user = getUser(userId);
                if(item == null) {
                    userMessage = MessageEnum.ERROR + ";Item no longer for bid";
                }
                else {
                    // Send a REJECT message to the user
                    userMessage = MessageEnum.ACCEPT_BID + ";" + userId + ";" + itemId;
                }
                user.writer.println(userMessage);
                break;
            case CAN_EXIT:
                // The bank says it is okay to exit
                run = false;
                // Do other things to exit gracefully
                break;
            case ERROR:
                String errorMessage = split[1];
                // Print the error message to the screen or something
                System.out.println("Error: " + errorMessage);
                break;
            default:
                System.out.println("Error: Unhandled bank message");
        }
    }

    public static void timerExpired(int itemId) {
        // FIXME: deal with this
        Item item = items.get(itemId);
        if(item == null) {
            System.out.println("Something weird happened");
            return;
        }
        // To the bank, send an AUCTION_ENDED message
        String message = MessageEnum.AUCTION_ENDED + ";" + ahId + ";" + item.getItemName();
        message += ";" + item.getItemId() + ";" + item.getItemBid() + ";" + item.getBidderId();
        message += ";" + item.getItemDesc();
        bankWriter.println(message);
        // Remove the item associated with the itemId from the itemList
        // and remove the item timer from the itemTimer list
        synchronized (items) {
            items.remove(itemId);
        }
        synchronized (itemTimers) {
            itemTimers.remove(itemId);
        }
    }

    /**
     * populateItems()
     * <p>
     * This method fills the auctionHouse with 3 items from bank.
     */
    public static void populateItems(PrintWriter writer, BufferedReader reader) {
        writer.println(MessageEnum.GET_ITEMS_FROM_BANK + ";" + ahId + ";3");

        try {
            while(!reader.ready()) System.out.print("");
            //while(!reader.ready()) ;

            String message = reader.readLine();
            String[] split = message.split(";");
            // Split will have all the elements from a ITEMS list from the bank
            // It can be checked that split[0] is ITEMS or not

            for (int i = 0; i < 3; ++i) {
                String itemName = split[5*i+3];
                int itemId = Integer.parseInt(split[5*i+4]);
                double itemBid = Double.parseDouble(split[5*i+5]);
                //int bidUserId = Integer.parseInt(split[5*i+6]);
                String itemDesc = split[5*i+7];
                Item item = new Item(itemName, itemId, itemBid, itemDesc);
                //item.setBidderId(bidUserId);
                items.put(itemId, item);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getItemsAsString()
     *
     * @return A String of item toString()'s separated by '\n' characters.
     */
    public String getItemsAsString() {
        String output = "";
        ArrayList<Item> itemsAsList = new ArrayList<Item>(items.values());
        for (int i = 0; i < itemsAsList.size(); ++i) {
            output += itemsAsList.get(i).toString() + "\n";
        }
        return output;
    }

    /**
     * placeBid()
     * Called by an Agent to place a bid (or by a Client when a PLACE_BID Message is received)
     *
     * @param biddingID      BIDDING_ID of the Agent who wishes to place a bid
     * @param amount         Amount the bidder wishes to bid.
     * @param itemID         ID of the item the bidder wishes to bid on
     * @param auctionHouseID the ID of this auction house (needed by Client)
     * @return true if Client should move ahead and request a hold to be placed.
     * false if something went wrong (the Agent bid too little,) in which case the Client can send
     * a bidResponse REJECT Message, not this AuctionHouse's ID, the item doesn't exist here)
     * right back to the Agent.
     */
    public boolean placeBid(String biddingID, double amount, int itemID, int auctionHouseID) {
        //Safechecking
        Item item = items.get(itemID);
        if (item == null) {
            //That item isn't for sale here USER OUTPUT
            System.err.println("Bidding ID " + biddingID + " tried to bid on " + itemID + ", which is not an item in "
                    + ". Returning");
            return false;
        }

        return true;
    }

    /**
     * @param itemID ID of the item stored in the timer that is called when the item is sold.
     *               Called when a 'winning' item timer goes off.
     * @return true if AuctionHouse still has items
     * false if AuctionHouse is out of items and needs to close.
     */
    public boolean itemSold(int itemID) {
        Item itemSold = items.remove(itemID);
        return !items.isEmpty();
        //return "Item "+itemSold.getItemName()+" has been sold for $"+itemSold.getCurrentBid()+"!";
    }

    static class SocketInfo {
        final Socket socket;
        final PrintWriter writer;
        final BufferedReader reader;
        final int id;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader, int id) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
            this.id = id;
        }
    }
}