package bank;

import common.Item;
import common.MessageEnum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Bank{
    private static BankDisplay display;
    private static BankListener listener;

    private static List<SocketInfo> userList;
    private static List<SocketInfo> houseList;
    private static List<Request> pendingRequests;
    private static List<Item> itemList;

    public static void main(String[] args) {
        int port = 3030;

        userList = new ArrayList<>();
        houseList = new ArrayList<>();
        pendingRequests = new LinkedList<>();
        itemList = new LinkedList<>();
        initializeItems();

        display = new BankDisplay();
        display.createDisplay();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            listener = new BankListener(serverSocket, userList, houseList);
            listener.start();

            String text;

            boolean run = true;
            while(run) {
                System.out.println("");
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

            System.out.println("Server is listening on port " + port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SocketInfo getHouse(int id) {
        for(int i = 0; i < houseList.size(); i++) {
            if (houseList.get(i).id == id) return houseList.get(i);
        }
        return null;
    }

    private static SocketInfo getUser(int id) {
        for(int i = 0; i < userList.size(); i++) {
            if (userList.get(i).id == id) return userList.get(i);
        }
        return null;
    }

    private static void handleUserCommand(String text) {
        String[] splitText = text.split(";");
        MessageEnum command = MessageEnum.parseCommand(splitText[0]);
        switch (command) {
            case BID:
                int userId = Integer.parseInt(splitText[1]);
                double bidAmount = Double.parseDouble(splitText[2]);
                int itemId = Integer.parseInt(splitText[3]);
                int houseId = Integer.parseInt(splitText[4]);
                if(getHouse(houseId) == null) {
                    PrintWriter userWriter = getUser(userId).writer;
                    userWriter.println(MessageEnum.ERROR + ";Invalid House Id");
                }
                else {
                    PrintWriter houseWriter = getHouse(houseId).writer;
                    houseWriter.println(MessageEnum.GET_ITEM + ";" + itemId);
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

                if(getHouse(houseId) == null) {
                    PrintWriter userWriter = getUser(userId).writer;
                    userWriter.println(MessageEnum.ERROR + ";Invalid House Id");
                }
                else {
                    PrintWriter houseWriter = getHouse(houseId).writer;
                    houseWriter.println(MessageEnum.GET_ITEMS);
                    Request request = new Request(MessageEnum.GET_ITEMS);
                    request.userId = userId;
                    request.houseId = houseId;
                    pendingRequests.add(request);
                }
                break;
            case GET_HOUSES:
                userId = Integer.parseInt(splitText[1]);

                String message = MessageEnum.HOUSE_LIST.toString();
                for(int i = 0; i < houseList.size(); i++) {
                    message += ";" + houseList.get(i).id;
                }
                PrintWriter userWriter = getUser(userId).writer;
                userWriter.println(message);
                break;
            default:
                System.out.println("Invalid Command for User: " + command);
        }
    }

    private static void handleHouseCommand(String text) {
        String[] splitText = text.split(";");
        MessageEnum command = MessageEnum.parseCommand(splitText[0]);
        switch (command) {
            case ITEM:
                int houseId = Integer.parseInt(splitText[1]);
                //String itemName = splitText[2];
                int itemId = Integer.parseInt(splitText[3]);
                double itemBid = Double.parseDouble(splitText[4]);
                int itemBidUser = Integer.parseInt(splitText[5]);
                //String itemDesc = splitText[6];

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
                bank.BankAccount account = getUser(request.userId).account;
                if(request == null) {
                    System.out.println("Error: Unable to parse ITEM message");
                }
                else if(request.userId == itemBidUser) {
                    // Cannot bid on an item already bid on
                    userWriter.println(MessageEnum.REJECT + ";" + houseId + ";" + itemId);
                }
                else{
                    if(itemBid < request.bidAmount && account.getRemainingBalance() >= itemBid) {
                        // Success
                        PrintWriter houseWriter = getHouse(houseId).writer;
                        account.removeFunds(request.bidAmount);
                        for(int i = 0; i < userList.size(); i++) {
                            if(userList.get(i).id != request.userId) {
                                userList.get(i).writer.println(
                                        MessageEnum.OUTBID + ";" + houseId + ";" + itemId + ";" + username + ";" +
                                        request.bidAmount);
                            }
                            else {
                                userWriter.println(MessageEnum.ACCEPT + ";" + houseId + ";" + itemId);
                            }

                            if(userList.get(i).id == itemBidUser) {
                                userList.get(i).account.addFunds(itemBid);
                            }
                        }
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
                userWriter.println(message);

                break;
            case AUCTION_ENDED:
                houseId = Integer.parseInt(splitText[1]);
                String itemName = splitText[2];
                itemId = Integer.parseInt(splitText[3]);
                itemBid = Double.parseDouble(splitText[4]);
                int winningUser = Integer.parseInt(splitText[5]);
                //String itemDesc = splitText[6];
                houseWriter = getHouse(houseId).writer;

                account = getUser(winningUser).account;
                account.setBalance(account.getBalance() - itemBid);

                account = getHouse(houseId).account;
                account.setBalance(account.getBalance() + itemBid);

                for(int i = 0; i < userList.size(); i++) {
                    userWriter = userList.get(i).writer;
                    if(userList.get(i).id == winningUser) {
                        userWriter.println(MessageEnum.WINNER + ";" + itemName + ";" + houseId + ";" + itemId
                                           + ";" + itemBid);
                    }
                    else {
                        userWriter.println(MessageEnum.ITEM_WON + ";" + itemName + ";" + houseId + ";" + itemId +
                                           getUser(winningUser).username + ";" + itemBid);
                    }
                }
                houseWriter.println(MessageEnum.REMOVE_ITEM + ";" + itemId);

                break;
            default:
                System.out.println("Invalid Command for Auction House: " + command);
        }
    }

    private static void initializeItems() {
        Scanner fileScan = null;
        try {
            fileScan = new Scanner (
                    new BufferedReader(
                            new InputStreamReader(Bank.class.getResourceAsStream("/initialItems.txt"))));
            //                new FileReader("resources/initialItems.txt")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int itemId = 0;
        while(fileScan.hasNext()){
            String text = fileScan.nextLine();
            String[] splitText = text.split(";");
            Item item = new Item(splitText[0], itemId, Integer.parseInt(splitText[1]), splitText[2]);
            itemId += 1;
            itemList.add(item);
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
        }
    }

    static class SocketInfo{
        final Socket socket;
        final PrintWriter writer;
        final BufferedReader reader;
        final int id;
        final String username;
        final bank.BankAccount account;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader, int id, String username,
                          bank.BankAccount account) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
            this.id = id;
            this.username = username;
            this.account = account;
        }
    }
}
