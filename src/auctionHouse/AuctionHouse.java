/**
 * CS 351L Project 5 - Distributed Auction Houses
 * Pun Chhetri, Isha Chauhan, John Cooper, John Tran
 *
 * The main construct that acts as an Auction House that
 * communicates to both the Bank and Users. Has the logic
 * that pertains to items being sold.
 */

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

    // The ports that the banks and users connect through
    private static int bankPort, userPort;
    // Requested and received from Auction Central
    private static int ahId;
    // Item ID as key for the item.
    private static HashMap<Integer, Item> items;
    // The map of all of the timers that test when an item has stopped being auctioned
    private static HashMap<Integer, TimerThread> itemTimers;
    // The list of all of the socket infos for all the users
    private static List<SocketInfo> userList;
    // The writer that sends messages to the bank
    private static PrintWriter bankWriter;
    // A variable that dictates if the 'listener' for messages
    // should keep running
    private static boolean run;
    // Listener for incoming requests to join the house
    private static AuctionHouseListener listener;

    /**
     * Creates an AuctionHouse that has three random items for sale.
     *
     * args[0] is expected to be the hostname
     * args[1] is the port for the bank
     * args[2] is the port for the auctionHouse server
     */
    public static void main(String[] args) {
        items = new HashMap<>();
        itemTimers = new HashMap<>();
        userList = new LinkedList<>();
        String hostName = args[0];
        // Set the ports
        bankPort = Integer.parseInt(args[1]);
        userPort = Integer.parseInt(args[2]);

        String message;
        String[] split;
        // Create a server for the users to login to
        ServerSocket server = null;
        try {
            server = new ServerSocket(userPort);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Create a listener listening for users trying to join
        // the auction house
        listener = new AuctionHouseListener(server, userList);
        listener.start();

        // Open a socket to the bank
        try (Socket socket = new Socket(hostName, bankPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            bankWriter = writer;

            // Send a 'HOUSE' message to inform the bank of being a house
            writer.println(MessageEnum.HOUSE.toString() + ";" + userPort);
            while(!reader.ready()) System.out.print("");

            // This is dealing with the login message from the Bank
            message = reader.readLine();
            split = message.split(";");
            ahId = Integer.parseInt(split[1]);

            // Get items for the auction house from the bank
            populateItems(writer, reader);

            // Create command line input stuff
            BufferedReader scan = new BufferedReader(new InputStreamReader(System.in));

            // Now listening and parsing messages
            run = true;
            while(run) {
                for(int i = 0; i < userList.size(); i++) {
                    // Look for user messages
                    if(userList.get(i).reader.ready()) {
                        SocketInfo user = userList.get(i);
                        message = user.reader.readLine();
                        parseUserMessage(message, user.writer, writer);
                    }
                }
                if(reader.ready()) {
                    // Look for bank messages
                    message = reader.readLine();
                    parseBankMessage(message, writer, socket);
                }
                if(scan.ready()) {
                    message = scan.readLine();
                    if(message.equalsIgnoreCase("exit")) {
                        if(userList.size() != 0) {
                            System.out.println("Cannot exit. Users are still connected");
                        }
                        else {
                            writer.println(MessageEnum.EXIT + ";" + ahId);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find a user in the list based off of the userID
     * @param userId The userId for the desired user
     * @return The desired user
     */
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

    /**
     * Take a message from the user and parse what it should do
     * @param message The message that was sent by the user
     * @param userWriter The writer for the user
     * @param bankWriter The writer for the bank
     */
    public static void parseUserMessage(String message, PrintWriter userWriter, PrintWriter bankWriter) {
        System.out.println(message);

        // Parse the parts of the message
        String[] split = message.split(";");
        MessageEnum command = MessageEnum.parseCommand(split[0]);

        switch(command) {
            // Process a bid request from the user
            case BID:
                int userId = Integer.parseInt(split[1]);
                double bidAmount = Double.parseDouble(split[2]);
                int itemId = Integer.parseInt(split[3]);
                int houseId = Integer.parseInt(split[4]);
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
                // Reset the timers
                if(relevantItem.getBidderId() == -1) {
                    TimerThread thread = new TimerThread(itemId);
                    synchronized (itemTimers){
                        itemTimers.put(itemId, thread);
                    }
                    thread.start();
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
                if(relevantItem == null || relevantItem.getItemBid() > bidAmount) {
                    // If not, send a REJECT message (formatted) to the user
                    String userMessage = MessageEnum.REJECT.toString();
                    userMessage += ";" + ahId + ";" + itemId;
                    userWriter.println(userMessage);
                }
                else {
                    // Else, send a VALID_BID message to the bank
                    String bankMessage = MessageEnum.VALID_BID.toString();
                    bankMessage += ";" + houseId + ";" + userId + ";" + bidAmount + ";";
                    bankMessage += itemId + ";" + relevantItem.getBidderId() + ";" + relevantItem.getItemBid();
                    bankWriter.println(bankMessage);
                }
                break;
                // Process a request to get a specific item
            case GET_ITEM:
                itemId = Integer.parseInt(split[1]);
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
                // Process a request to get all the items
            case GET_ITEMS:
                String userMessage = MessageEnum.ITEMS + ";" + ahId;
                synchronized (items) {
                    userMessage += ";" + items.size();
                    for(Item item : items.values()) {
                        userMessage += ";" + item.getItemName() + ";" + item.getItemId() + ";";
                        userMessage += item.getItemBid() + ";";
                        userMessage += item.getItemDesc();
                    }
                }
                // Return an ITEMS message to the requesting user
                userWriter.println(userMessage);
                break;
                // Process a request to exit the house
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
                if(!canExit) {
                    // If true, send and ERROR message "Cannot exit"
                    userMessage = MessageEnum.ERROR + ";Cannot Exit";
                    userWriter.println(userMessage);
                }
                else {
                    // Else, return a CAN_EXIT message
                    userMessage = MessageEnum.CAN_EXIT.toString() + ";" + ahId;
                    userWriter.println(userMessage);

                    try {
                        getUser(userId).socket.close();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    userList.remove(getUser(userId));
                }
                break;
            default:
                System.out.println("Error: Unhandled user message");
        }
    }

    /**
     * Parse a message from the bank
     * @param message The message to parse
     * @param bankWriter The writer to send messages to the bank
     */
    public static void parseBankMessage(String message, PrintWriter bankWriter, Socket socket) {
        System.out.println(message);

        // Part the message
        String[] split = message.split(";");
        MessageEnum command = MessageEnum.parseCommand(split[0]);

        switch(command) {
            // Process a message saying the bid was accepted
            case ACCEPT_BID:
                int userId = Integer.parseInt(split[1]);
                int itemId = Integer.parseInt(split[2]);
                double itemBid = Double.parseDouble(split[3]);
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
                        thread.start();
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
                    item.setItemBid(itemBid);
                    // Send a ACCEPT message to the user
                    userMessage = MessageEnum.ACCEPT + ";" + userId + ";" + itemId;
                }
                user.writer.println(userMessage);
                break;
                // Process a request sent by the bank that the bid was rejected
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
                    userMessage = MessageEnum.REJECT + ";" + userId + ";" + itemId;
                }
                user.writer.println(userMessage);
                break;
                // Process a message saying it is okay for the AH to exit
            case CAN_EXIT:
                // The bank says it is okay to exit
                run = false;
                // Do other things to exit gracefully
                try {
                    socket.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                listener.stopRunning();
                break;
                // Process a message from an error
            case ERROR:
                String errorMessage = split[1];
                writer.println(MessageEnum.ERROR + ";" + errorMessage);
                // Print the error message to the screen or something
                System.out.println("Error: " + errorMessage);
                break;
            default:
                System.out.println("Error: Unhandled bank message");
        }
    }

    /**
     * Invoked by the timers. Run if the timers run out for a specific item
     * @param itemId The item which needs to be sold
     */
    public static void timerExpired(int itemId) {
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
        int count = (int) Math.floor(3 + 2 * Math.random());

        writer.println(MessageEnum.GET_ITEMS_FROM_BANK + ";" + ahId + ";" + count);

        try {
            while(!reader.ready()) System.out.print("");
            //while(!reader.ready()) ;

            String message = reader.readLine();
            String[] split = message.split(";");
            // Split will have all the elements from a ITEMS list from the bank
            // It can be checked that split[0] is ITEMS or not

            for (int i = 0; i < count; i++) {
                String itemName = split[4*i+3];
                int itemId = Integer.parseInt(split[4*i+4]);
                double itemBid = Double.parseDouble(split[4*i+5]);
                String itemDesc = split[4*i+6];
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
    public static String getItemsAsString() {
        String output = "";
        ArrayList<Item> itemsAsList = new ArrayList<Item>(items.values());
        for (int i = 0; i < itemsAsList.size(); ++i) {
            output += itemsAsList.get(i).toString() + "\n";
        }
        return output;
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