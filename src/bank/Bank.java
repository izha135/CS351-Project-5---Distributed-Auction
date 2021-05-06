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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Bank{
    // The graphic display showing the actions happening within the bank
    private static BankDisplay display;
    // The listener listening for sockets applying
    private static BankListener listener;

    // The list of user sockets and other information about them (accounts, etc.)
    private static List<SocketInfo> userList;
    // The list of auction house sockets and other information about them (accounts, etc.)
    private static List<SocketInfo> houseList;
    // The global list of items so no two auction houses have the same item
    private static List<Item> itemList;

    public static void main(String[] args) {
        int port = 3030;

        userList = new ArrayList<>();
        houseList = new ArrayList<>();
        itemList = new LinkedList<>();
        // Fill the item list with all of the items within the initialItems.txt file
        initializeItems();

        // Create a display to show the inner workings of the bank
        BankDisplayThread displayThread = new BankDisplayThread();
        displayThread.start();
        display = displayThread.bankDisplay;

        BufferedReader scan = new BufferedReader(new InputStreamReader(System.in));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Start the listener for socket requests
            listener = new BankListener(serverSocket, userList, houseList, display);
            listener.start();

            System.out.println("Server is listening on port " + port);

            String text;

            // Check every user's and house's reader for if they have input to be parsed
            boolean run = true;
            while(run) {
                System.out.print("");
                for(int i = 0; i < userList.size(); i++) {
                    SocketInfo socket = userList.get(i);
                    if(socket.reader.ready()) {
                        text = socket.reader.readLine();
                        handleUserCommand(text);
                    }
                }
                for(int i = 0; i < houseList.size(); i++) {
                    SocketInfo socket = houseList.get(i);
                    if(socket.reader.ready()) {
                        text = socket.reader.readLine();
                        handleHouseCommand(text);
                    }
                }
                if(scan.ready()) {
                    String inputString = scan.readLine();
                    if(inputString.equals("Stop")) {
                        run = false;
                        listener.stopThread();
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
     * Parses a string sent by a user and executes the correct logic
     * @param text The text sent by the user
     */
    private static void handleUserCommand(String text) {
        // Split the string on the delimiter ;
        String[] splitText = text.split(";");
        System.out.println("User: " + text);
        // Parse the command, which is always the first string
        MessageEnum command = MessageEnum.parseCommand(splitText[0]);
        switch (command) {
            case GET_HOUSES:
                int userId = Integer.parseInt(splitText[1]);
                handleUserGetHouses(userId);
                break;
            case EXIT:
                userId = Integer.parseInt(splitText[1]);
                handleUserExit(userId);
                break;
            default:
                System.out.println("Invalid Command for User: " + command);
        }
    }

    /**
     * Parses a string sent by a auction house and executes the correct logic
     * @param text The text sent by the house
     */
    private static void handleHouseCommand(String text) {
        // Split the string on the delimiter ;
        String[] splitText = text.split(";");
        System.out.println("House: " + text);
        // Parse the command, which is always the first string
        MessageEnum command = MessageEnum.parseCommand(splitText[0]);
        switch (command) {
            case GET_ITEMS_FROM_BANK:
                int houseId = Integer.parseInt(splitText[1]);
                int count = Integer.parseInt(splitText[2]);
                handleHouseGetItemsFromBank(houseId, count);
                break;
            case VALID_BID:
                houseId = Integer.parseInt(splitText[1]);
                int userId = Integer.parseInt(splitText[2]);
                double itemBid = Double.parseDouble(splitText[3]);
                int itemId = Integer.parseInt(splitText[4]);
                int previousHidBidUserId = Integer.parseInt(splitText[5]);
                handleHouseValidBid(houseId, userId, itemBid, itemId, previousHidBidUserId);
                break;
            case AUCTION_ENDED:
                // The item being bid for has now been sold
                houseId = Integer.parseInt(splitText[1]);
                String itemName = splitText[2];
                itemId = Integer.parseInt(splitText[3]);
                itemBid = Double.parseDouble(splitText[4]);
                int winningUser = Integer.parseInt(splitText[5]);
                String itemDesc = splitText[6];
                handleHouseAuctionEnded(houseId, itemName, itemId, itemBid, winningUser, itemDesc);
                break;
            case EXIT:
                houseId = Integer.parseInt(splitText[1]);
                handleHouseExit(houseId);
                break;
            default:
                System.out.println("Invalid Command for Auction House: " + command);
        }
    }

    private static void handleUserGetHouses(int userId) {
        // Create and send a message with all of the house ids separated by ;
        String message = MessageEnum.HOUSE_LIST.toString();
        String hostName;
        for(int i = 0; i < houseList.size(); i++) {
            hostName = houseList.get(i).socket.getInetAddress().getHostAddress();
            message += ";" + houseList.get(i).id +";" + hostName;
        }
        PrintWriter userWriter = getUser(userId).writer;
        userWriter.println(message);
    }

    private static void handleUserExit(int userId) {
        boolean canExit = true;

        // Also check if the user is currently the highest bidder on some item
        BankAccount account = getUser(userId).account;
        if(account.getBalance() != account.getRemainingBalance()) canExit = false;

        PrintWriter userWriter = getUser(userId).writer;
        if(canExit) {
            userWriter.println(MessageEnum.CAN_EXIT);
            SocketInfo socketInfo = getHouse(userId);
            try {
                socketInfo.socket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            userList.remove(socketInfo);
        }
        else {
            userWriter.println(MessageEnum.ERROR + ";Cannot exit");
        }
    }

    private static void handleHouseGetItemsFromBank(int houseId, int count) {
        // Construct a message with a list of items from the bank
        PrintWriter houseWriter = getHouse(houseId).writer;
        String message = MessageEnum.ITEMS.toString() + ";" + houseId + ";" + count;
        for(int i = 0; i < count; i++) {
            if(itemList.size() == 0) {
                break;
            }
            Item item = itemList.remove(0);
            display.addHouseItem(houseId, item);
            String itemName = item.getItemName();
            int itemId = item.getItemId();
            double itemBid = item.getItemBid();
            String itemDesc = item.getItemDesc();
            message += ";" + itemName + ";" + itemId + ";" + itemBid + ";" + itemDesc;
        }
        houseWriter.println(message);
    }

    private static void handleHouseValidBid(int houseId, int userId, double itemBid, int itemId,
                                            int previousHidBidUserId) {
        PrintWriter houseWriter = getHouse(houseId).writer;
        BankAccount account = getUser(userId).account;

        String message;
        if(account.getRemainingBalance() >= itemBid) {
            display.updateHouseItem(houseId, itemId, getUser(userId).username, itemBid);

            // Bid should be accepted
            message = MessageEnum.ACCEPT_BID + ";" + userId + ";" + itemId;

            SocketInfo newUser = getUser(userId);
            SocketInfo oldUser = getUser(previousHidBidUserId);

            newUser.account.removeFunds(itemBid);
            if (oldUser != null) oldUser.account.addFunds(itemBid);

            display.changeUserRemaining(userId, newUser.account.getRemainingBalance());

            // Inform all other users of them being outbid
            for(int i = 0; i < userList.size(); i++) {
                SocketInfo user = userList.get(i);
                if(user.id != userId) {
                    PrintWriter writer = user.writer;
                    writer.println(MessageEnum.OUTBID + ";" + houseId + ";" + itemId + ";" + userId + ";" + itemBid);
                }
            }
        }
        else {
            // Bid should be rejected
            message = MessageEnum.REJECT_BID + ";" + userId + ";" + itemId;
        }
        houseWriter.println(message);
    }

    private static void handleHouseAuctionEnded(int houseId, String itemName, int itemId, double itemBid,
                                                int winningUser, String itemDesc) {
        display.removeHouseItem(houseId, itemId);

        PrintWriter houseWriter = getHouse(houseId).writer;

        // Transfer funds appropriately
        BankAccount account = getUser(winningUser).account;
        account.setBalance(account.getBalance() - itemBid);
        display.changeUserBalance(winningUser, account.getBalance());

        account = getHouse(houseId).account;
        account.setBalance(account.getBalance() + itemBid);

        for(int i = 0; i < userList.size(); i++) {
            PrintWriter userWriter = userList.get(i).writer;
            // Message item winner of their winnings
            if(userList.get(i).id == winningUser) {
                userWriter.println(MessageEnum.WINNER + ";" + houseId + ";" + itemName + ";" + itemId
                        + ";" + itemBid);
            }
            // Message all other users that this item has been sold (cannot bid on it anymore)
            else {
                userWriter.println(MessageEnum.ITEM_WON + ";" + houseId + ";" + itemName + ";" + itemId +
                        getUser(winningUser).username + ";" + itemBid);
            }
        }
        houseWriter.println(MessageEnum.REMOVE_ITEM + ";" + itemId);
    }

    /**
     * This does not check if the house is bound in any bids
     * @param houseId
     */
    private static void handleHouseExit(int houseId) {
        boolean canExit = true;

        PrintWriter houseWriter = getHouse(houseId).writer;
        if(canExit) {
            display.removeHouse(houseId);
            houseWriter.println(MessageEnum.CAN_EXIT);
            SocketInfo socketInfo = getHouse(houseId);
            try {
                socketInfo.socket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            houseList.remove(socketInfo);
        }
        else {
            houseWriter.println(MessageEnum.ERROR + ";Cannot exit");
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
                            new InputStreamReader(Bank.class.getResourceAsStream("/initialItems.txt"))));
            //                new FileReader("../../resources/initialItems.txt")));
            //                new FileReader("resources/initialItems.txt")));
            int itemId = 0;
            while(fileScan.hasNext()){
                String text = fileScan.nextLine();
                String[] splitText = text.split(";");
                Item item = new Item(splitText[0], itemId, Integer.parseInt(splitText[1]), splitText[2]);
                itemId += 1;
                itemList.add(item);
            }
            Collections.shuffle(itemList);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            fileScan.close();
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
