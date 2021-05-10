package auctionHouse;

import bank.Bank;
import common.Item;
import common.MessageEnum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AuctionHouse {

    private static int port;
    private static int ahId; // Requested and received from Auction Central
    private static HashMap<Integer, Item> items; //Item ID as key for the item.
    private static HashMap<Integer, TimerThread> itemTimers;
    private static HashMap<Integer, SocketInfo> users;
    private static HashMap<Integer, Integer> highestBidUser;

    private int itemCounter = 0;
    private static boolean run;

    private Item soldItem;

    /**
     * Creates an AuctionHouse that has three random items for sale.
     *
     * args[0] is expected to be the hostname
     */
    public static void main(String[] args) {
        items = new HashMap<>();
        itemTimers = new HashMap<>();
        users = new HashMap<>();
        highestBidUser = new HashMap<>();
        String hostName = "localhost"; //args[0];
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
        AuctionHouseListener listener = new AuctionHouseListener(server, users);
        listener.start();

        try (Socket socket = new Socket(hostName, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

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
                for(int i = 0; i < users.size(); i++) {
                    if(users.get(i).reader.ready()) {
                        SocketInfo user = users.get(i);
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

    // THE writer IS THE WRITER FOR THE USER. USE IT TO SEND MESSAGES
    public static void parseUserMessage(String message, PrintWriter writer, PrintWriter bankWriter) {
        String[] split = message.split(";");
        MessageEnum command = MessageEnum.parseCommand(split[0]);

        switch(command) {
            case BID:
                int userId = Integer.parseInt(split[1]);
                double bidAmount = Double.parseDouble(split[2]);
                int itemId = Integer.parseInt(split[3]);
                int houseId = Integer.parseInt(split[4]); // Might wanna check if this agrees with the ahId
                // Check if the item is in the AuctionHouse
                // If not, send a REJECT message (formatted) to the user
                // Else, send a VALID_BID message to the bank
                if(items.containsKey(itemId)){
                  int prevHighBidder = highestBidUser.get(itemId);
                  bankWriter.println(MessageEnum.VALID_BID + ";" + houseId + ";" + userId + ";" + bidAmount + ";" + itemId + ";" + prevHighBidder + ";" + ahId + ";3");
                } else{
                  writer.println(MessageEnum.REJECT + ";" + userId + ";" + itemId + ";" + ahId + ";3");
                }
                break;
            case GET_ITEM:
                itemId = Integer.parseInt(split[1]);
                Item it = items.get(itemId);
                writer.println(MessageEnum.ITEM + ";" + it.getItemBid() + ";" + itemId + ";" + it.getHouseId() + ";3");
                // Return an ITEM message to the requesting user
                break;
            case GET_ITEMS:
                ArrayList<Item> itemsAsList = new ArrayList<Item>(items.values());
                writer.println(MessageEnum.ITEMS + ";" +  getItemsAsString());
                // Return an ITEMS message to the requesting user
                break;
            case EXIT:
                userId = Integer.parseInt(split[1]);
                // Get prevHighBidder for all the items
                ArrayList<Integer> allUsers = new ArrayList<Integer>(highestBidUser.values());
                for(Integer h : allUsers){
                    if(userId == h){
                        writer.println(MessageEnum.ERROR + ";" + "Cannot exit;");
                        break;
                    }
                }
                writer.println(MessageEnum.CAN_EXIT);
                // Check if the user is the highest bidder on any of the items
                // If true, send and ERROR message "Cannot exit"
                // Else, return a CAN_EXIT message
                break;
            default:
                System.out.println("Error: Unhandled user message");
        }
    }

    public static void parseBankMessage(String message, PrintWriter writer) {
        String[] split = message.split(";");
        MessageEnum command = MessageEnum.parseCommand(split[0]);

        switch(command) {
            case ACCEPT_BID:
                int userId = Integer.parseInt(split[1]);
                int itemId = Integer.parseInt(split[2]);
                highestBidUser.put(itemId, userId);
                users.get(userId).writer.println(MessageEnum.ACCEPT);
                // Change the highest bidder on the item to the userId in the message
                // Send a ACCEPT message to the user
                break;
            case REJECT_BID:
                userId = Integer.parseInt(split[1]);
                itemId = Integer.parseInt(split[2]);
                message = MessageEnum.REJECT + ";" + userId + ";" + itemId + ";" + ahId + ";3";
                users.get(userId).writer.println(message);
                // Send a REJECT message to the user
                break;
            case CAN_EXIT:
                // The bank says it is okay to exit
                run = false;
                // Do other things to exit gracefully
                break;
            case ERROR:
                String errorMessage = split[1];
                writer.println(MessageEnum.ERROR + ";" + errorMessage);
                // Print the error message to the screen or something
                break;
            default:
                System.out.println("Error: Unhandled bank message");
        }
    }

    public void timerExpired(int itemId) {
        // FIXME: deal with this
        // To the highest bidder on the item with id itemId, send a WINNER message
        // To the bank, send an AUCTION_ENDED message
        // Remove the item associated with the itemId from the itemList
        // and remove the item timer from the itemTimer list
        Item it = items.get(itemId);
        int userId = highestBidUser.get(it.getItemId());
        PrintWriter userWriter = users.get(userId).writer;
        userWriter.println(MessageEnum.WINNER + ";" + ahId + ";3");
        items.remove(itemId);
        itemTimers.remove(itemId);
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
            int j= 1; // first index will be command, hence started at 1;
            for (int i = 0; i < 3; ++i) {
              Item it = new Item(split[i+j], Integer.parseInt(split[i+j+1]), Double.parseDouble(split[i+j+2]), split[i+j+3]);
              j += 4; // next Item starts after 4 `;`.
              items.put(it.getItemId(), it);
              highestBidUser.put(it.getItemId(), -1);
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
    public static String getItemsAsString() {
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

        //TODO: I don't know if this function will be useful - John
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

        /*@Override
        public String toString()
        {
            return "Name: " +  name + " Public ID: " + publicID;
        }*/


    /*private class PendingBidRequest {
        public final String BIDDING_ID;
        public final double AMOUNT;
        public final int ITEM_ID;

        public PendingBidRequest(String bID, double amt, int iID) {
            BIDDING_ID = bID;
            AMOUNT = amt;
            ITEM_ID = iID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PendingBidRequest) {
                PendingBidRequest br = (PendingBidRequest) obj;
                return this.BIDDING_ID == br.BIDDING_ID && this.AMOUNT == br.AMOUNT && this.ITEM_ID == br.ITEM_ID;
            } else return false;
        }

        //TODO: override hash to use in Set.
    }*/

    static class SocketInfo {
        final Socket socket;
        final PrintWriter writer;
        final BufferedReader reader;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
        }
    }
}
