/**
 * CS 351L Project 5 - Distributed Auction Houses
 * Pun Chhetri, Isha Chauhan, John Cooper, John Tran
 *
 * The central Bank construct that facilitates communication between the
 * users and auction houses. Most of the logic happens in this class
 */

package bank;

import common.BankAccount;
import common.Item;
import common.MessageEnum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Bank{
    // The graphic display showing the actions happening within the bank
    private static BankDisplay display;
    // The listener listening for sockets applying
    private static BankListener listener;

    // The list of user sockets and other information about them (accounts, etc.)
    private static List<SocketInfo> userList;
    // The list of auction house sockets and other information about them (accounts, etc.)
    private static List<SocketInfo> houseList;
    // The list of requests by the user that need information from the auction house
    private static List<Request> pendingRequests;
    // The global list of items so no two auction houses have the same item
    private static List<Item> itemList;

    public static void main(String[] args) {
        int port = 3030;

        userList = new ArrayList<>();
        houseList = new ArrayList<>();
        pendingRequests = new LinkedList<>();
        itemList = new LinkedList<>();
        // Fill the item list with all of the items within the initialItems.txt file
        initializeItems();

        // Create a display to show the inner workings of the bank
        display = new BankDisplay();
        //display.createDisplay();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Start the listener for socket requests
            listener = new BankListener(serverSocket, userList, houseList);
            listener.start();

            System.out.println("Server is listening on port " + port);

            String text;

            // Check every user's and house's reader for if they have input to be parsed
            boolean run = true;
            while(run) {
                System.out.print("");
                for(int i = 0; i < userList.size(); i++) {
                    SocketInfo socket = userList.get(i);
                    while(socket.reader.ready()) {
                        text = socket.reader.readLine();
                        handleUserCommand(text);
                    }
                }
                for(int i = 0; i < houseList.size(); i++) {
                    SocketInfo socket = houseList.get(i);
                    while(socket.reader.ready()) {
                        text = socket.reader.readLine();
                        handleHouseCommand(text);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * For a given house id, return the SocketInfo of that house
     * @param id The id of the desired house
     * @return The SocketInfo of the desired house. null if that id was not found
     */
    private static SocketInfo getHouse(int id) {
        for(int i = 0; i < houseList.size(); i++) {
            if (houseList.get(i).id == id) return houseList.get(i);
        }
        return null;
    }

    /**
     * For a given user id, return the SocketInfo of that user
     * @param id The id of the desired user
     * @return The SocketInfo of the desired user. null if that id was not found
     */
    private static SocketInfo getUser(int id) {
        for(int i = 0; i < userList.size(); i++) {
            if (userList.get(i).id == id) return userList.get(i);
        }
        return null;
    }

    /**
     * Parses a string sent by a user
     * @param text The text sent by the user
     */
    private static void handleUserCommand(String text) {
        // Split the string on the delimiter ;
        String[] splitText = text.split(";");
        // Parse the command, which is always the first string
        MessageEnum command = MessageEnum.parseCommand(splitText[0]);
        switch (command) {
            case BID:
                int userId = Integer.parseInt(splitText[1]);
                double bidAmount = Double.parseDouble(splitText[2]);
                int itemId = Integer.parseInt(splitText[3]);
                int houseId = Integer.parseInt(splitText[4]);
                // Try to find the given house id
                if(getHouse(houseId) == null) {
                    PrintWriter userWriter = getUser(userId).writer;
                    userWriter.println(MessageEnum.ERROR + ";Invalid House Id");
                }
                else {
                    // If found, ask the house for the given item
                    PrintWriter houseWriter = getHouse(houseId).writer;
                    houseWriter.println(MessageEnum.GET_ITEM + ";" + itemId);
                    // Save a record of the item bid request
                    Request request = new Request(MessageEnum.GET_ITEM);
                    request.bidAmount = bidAmount;
                    request.itemId = itemId;
                    request.userId = userId;
                    request.houseId = houseId;
                    pendingRequests.add(request);
                }
                break;
            case GET_ITEMS:
                userId = Integer.parseInt(splitText[1]);
                houseId = Integer.parseInt(splitText[2]);

                // Try to found the desired house
                if(getHouse(houseId) == null) {
                    PrintWriter userWriter = getUser(userId).writer;
                    userWriter.println(MessageEnum.ERROR + ";Invalid House Id");
                }
                else {
                    // If found, ask the house for its items
                    PrintWriter houseWriter = getHouse(houseId).writer;
                    houseWriter.println(MessageEnum.GET_ITEMS);
                    // Save a record of the request for the items
                    Request request = new Request(MessageEnum.GET_ITEMS);
                    request.userId = userId;
                    request.houseId = houseId;
                    pendingRequests.add(request);
                }
                break;
            case GET_HOUSES:
                userId = Integer.parseInt(splitText[1]);

                // Create and send a message with all of the house ids separated by ;
                String message = MessageEnum.HOUSE_LIST.toString();
                for(int i = 0; i < houseList.size(); i++) {
                    message += ";" + houseList.get(i).id;
                }
                PrintWriter userWriter = getUser(userId).writer;
                userWriter.println(message);
                break;
            case EXIT:
                userId = Integer.parseInt(splitText[1]);
                boolean canExit = true;

                // Check if the user has any pending requests
                for(Request request : pendingRequests) {
                    if (request.userId == userId) {
                        canExit = false;
                        break;
                    }
                }
                // Also check if the user is currently the highest bidder on some item
                BankAccount account = getUser(userId).account;
                if(account.getBalance() != account.getRemainingBalance()) canExit = false;

                userWriter = getUser(userId).writer;
                if(canExit) {
                    userWriter.println(MessageEnum.CAN_EXIT);
                }
                else {
                    userWriter.println(MessageEnum.ERROR + ";Cannot exit");
                }

                break;
            default:
                System.out.println("Invalid Command for User: " + command);
        }
    }

    private static void handleHouseCommand(String text) {
        // Split the string on the delimiter ;
        String[] splitText = text.split(";");
        // Parse the command, which is always the first string
        MessageEnum command = MessageEnum.parseCommand(splitText[0]);
        switch (command) {
            case ITEM:
                int houseId = Integer.parseInt(splitText[1]);
                //String itemName = splitText[2];
                int itemId = Integer.parseInt(splitText[3]);
                double itemBid = Double.parseDouble(splitText[4]);
                int itemBidUser = Integer.parseInt(splitText[5]);
                //String itemDesc = splitText[6];

                // Find the request associated with this command
                Request request = null;
                for(int i = 0; i < pendingRequests.size(); i++) {
                    if (pendingRequests.get(i).itemId == itemId &&
                        pendingRequests.get(i).houseId == houseId) {
                        request = pendingRequests.get(i);
                        pendingRequests.remove(request);
                        break;
                    }
                }
                PrintWriter userWriter = getUser(request.userId).writer;
                String username = getUser(request.userId).username;
                BankAccount account = getUser(request.userId).account;
                // Error if there wasn't a request
                if(request == null) {
                    System.out.println("Error: Unable to parse ITEM message");
                }
                else if(request.userId == itemBidUser) {
                    // Cannot bid on an item already bid on
                    userWriter.println(MessageEnum.REJECT + ";" + houseId + ";" + itemId);
                }
                else{
                    // If the request is valid, check if the bid is above the current bit
                    if(itemBid < request.bidAmount && account.getRemainingBalance() >= itemBid) {
                        // Success
                        PrintWriter houseWriter = getHouse(houseId).writer;
                        // Remove funds from the bidder
                        account.removeFunds(request.bidAmount);
                        for(int i = 0; i < userList.size(); i++) {
                            // Message all non-bidding users that this item has a new highest bid
                            if(userList.get(i).id != request.userId) {
                                userList.get(i).writer.println(
                                        MessageEnum.OUTBID + ";" + houseId + ";" + itemId + ";" + username + ";" +
                                        request.bidAmount);
                            }
                            // Message the bidding user their bid was successful
                            else {
                                userWriter.println(MessageEnum.ACCEPT + ";" + houseId + ";" + itemId);
                            }

                            // Add the funds back to the user outbid
                            if(userList.get(i).id == itemBidUser) {
                                userList.get(i).account.addFunds(itemBid);
                            }
                        }
                        // Instruct the house to set the highest bid to be a new value
                        houseWriter.println(MessageEnum.SET_HIGH_BID + ";" + request.bidAmount + ";" + request.userId);
                    }
                    else {
                        // Reject
                        userWriter.println(MessageEnum.REJECT + ";" + houseId + ";" + itemId);
                    }
                }
                break;
            case GET_ITEMS_FROM_BANK:
                houseId = Integer.parseInt(splitText[1]);
                int count = Integer.parseInt(splitText[2]);

                // Construct a message with a list of items from the bank
                PrintWriter houseWriter = getHouse(houseId).writer;
                String message = MessageEnum.ITEMS.toString();
                for(int i = 0; i < count; i++) {
                    if(itemList.size() == 0) {
                        break;
                    }
                    Item item = itemList.remove(0);
                    String itemName = item.getItemName();
                    itemId = item.getItemId();
                    itemBid = item.getItemBid();
                    String itemDesc = item.getItemDesc();
                    message += ";" + itemName + ";" + itemId + ";" + itemBid + ";" + itemDesc;
                }
                houseWriter.println(message);

                break;
            case ITEMS:
                int userId = Integer.parseInt(splitText[1]);
                userWriter = getUser(userId).writer;

                // Find the request associated with this command and remove it
                //request = null;
                for(int i = 0; i < pendingRequests.size(); i++) {
                    if (pendingRequests.get(i).userId == userId &&
                            pendingRequests.get(i).command == MessageEnum.GET_ITEMS) {
                        request = pendingRequests.get(i);
                        pendingRequests.remove(request);
                        break;
                    }
                }

                // Create a string with the list of items given by the auction house
                message = MessageEnum.HOUSE_ITEMS.toString();
                int numItems = (splitText.length-2)/5;
                for(int i = 0; i < numItems; i++) {
                    String itemName = splitText[5*i+2];
                    itemId = Integer.parseInt(splitText[5*i+3]);
                    itemBid = Integer.parseInt(splitText[5*i+4]);
                    //itemBidUser = Integer.valueOf(splitText[5*i+5]);
                    String itemDesc = splitText[5*i+6];
                    message += ";" + itemName + ";" + itemId + ";" + itemBid + ";" + itemDesc;
                }
                // Send the user this message
                userWriter.println(message);

                break;
            case AUCTION_ENDED:
                // The item being bid for has now been sold
                houseId = Integer.parseInt(splitText[1]);
                String itemName = splitText[2];
                itemId = Integer.parseInt(splitText[3]);
                itemBid = Double.parseDouble(splitText[4]);
                int winningUser = Integer.parseInt(splitText[5]);
                //String itemDesc = splitText[6];
                houseWriter = getHouse(houseId).writer;

                // Transfer funds appropriately
                account = getUser(winningUser).account;
                account.setBalance(account.getBalance() - itemBid);

                account = getHouse(houseId).account;
                account.setBalance(account.getBalance() + itemBid);

                for(int i = 0; i < userList.size(); i++) {
                    userWriter = userList.get(i).writer;
                    // Message item winner of their winnings
                    if(userList.get(i).id == winningUser) {
                        userWriter.println(MessageEnum.WINNER + ";" + itemName + ";" + houseId + ";" + itemId
                                           + ";" + itemBid);
                    }
                    // Message all other users that this item has been sold (cannot bid on it anymore)
                    else {
                        userWriter.println(MessageEnum.ITEM_WON + ";" + itemName + ";" + houseId + ";" + itemId +
                                           getUser(winningUser).username + ";" + itemBid);
                    }
                }
                houseWriter.println(MessageEnum.REMOVE_ITEM + ";" + itemId);

                break;
            case EXIT:
                // TODO: Make house not exit if it has any items with bids
                houseId = Integer.parseInt(splitText[1]);
                boolean canExit = true;

                // Check if the house is currently having any pending requests
                for(Request request1 : pendingRequests) {
                    if (request1.houseId == houseId) {
                        canExit = false;
                        break;
                    }
                }
                //account = getHouse(houseId).account;
                //if(account.getBalance() != account.getRemainingBalance()) canExit = false;

                houseWriter = getHouse(houseId).writer;
                if(canExit) {
                    houseWriter.println(MessageEnum.CAN_EXIT);
                }
                else {
                    houseWriter.println(MessageEnum.ERROR + ";Cannot exit");
                }

                break;
            default:
                System.out.println("Invalid Command for Auction House: " + command);
        }
    }

    /**
     * Reads in the items from the file initialItems.txt and fills an array with these items
     */
    private static void initializeItems() {
        Scanner fileScan = null;
        try {
            fileScan = new Scanner (
                    new BufferedReader(
            //                new InputStreamReader(Bank.class.getResourceAsStream("/initialItems.txt"))));
                            new FileReader("resources/initialItems.txt")));

            int itemId = 0;
            while(fileScan.hasNext()){
                String text = fileScan.nextLine();
                String[] splitText = text.split(";");
                Item item = new Item(splitText[0], itemId, Integer.parseInt(splitText[1]), splitText[2]);
                itemId += 1;
                itemList.add(item);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Request{
        final MessageEnum command;
        int userId;
        double bidAmount;
        int itemId;
        int houseId;

        public Request(MessageEnum command) {
            this.command = command;
            this.userId = -1;
            this.bidAmount = -1;
            this.itemId = -1;
            this.houseId = -1;
        }
    }

    static class SocketInfo{
        final Socket socket;
        final PrintWriter writer;
        final BufferedReader reader;
        final int id;
        final String username;
        final BankAccount account;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader, int id, String username,
                          BankAccount account) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
            this.id = id;
            this.username = username;
            this.account = account;
        }
    }
}
