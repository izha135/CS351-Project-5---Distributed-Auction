package bank;

import common.Item;
import common.MessageEnum;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Bank{
    private static BankDisplay display;
    private static BankListener listener;

    private static List<SocketInfo> userList;
    private static List<SocketInfo> houseList;
    private static List<Request> pendingRequests;
    private static List<Item> itemList;

    public static void main(String[] args) {
        int port = 3030;

        userList = new LinkedList<>();
        houseList = new LinkedList<>();
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
                int userId = Integer.valueOf(splitText[1]);
                int bidAmount = Integer.valueOf(splitText[2]);
                int itemId = Integer.valueOf(splitText[3]);
                int houseId = Integer.valueOf(splitText[4]);
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
                userId = Integer.valueOf(splitText[1]);
                houseId = Integer.valueOf(splitText[2]);

                Request request = new Request(MessageEnum.GET_ITEMS);
                request.userId = userId;
                request.houseId = houseId;

                PrintWriter houseWriter = getHouse(houseId).writer;
                houseWriter.println(MessageEnum.GET_ITEMS);
                break;
            case GET_HOUSES:
                userId = Integer.valueOf(splitText[1]);

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
                int houseId = Integer.valueOf(splitText[1]);
                //String itemName = splitText[2];
                int itemId = Integer.valueOf(splitText[3]);
                int itemBid = Integer.valueOf(splitText[4]);
                int itemBidUser = Integer.valueOf(splitText[5]);
                //String itemDesc = splitText[6];

                Request request = null;
                for(int i = 0; i < pendingRequests.size(); i++) {
                    if (pendingRequests.get(i).itemId == itemId &&
                        pendingRequests.get(i).houseId == houseId) {
                        request = pendingRequests.get(i);
                        break;
                    }
                }
                PrintWriter userWriter = getUser(request.userId).writer;
                if(request == null) {
                    userWriter.println(MessageEnum.ERROR + ";Auction house doesn't have that item");
                }
                else{
                    if(itemBid < request.bidAmount) {
                        // Success
                        PrintWriter oldBidderWriter = getUser(itemBidUser).writer;
                        PrintWriter houseWriter = getHouse(houseId).writer;
                        userWriter.println(MessageEnum.ACCEPT + ";" + houseId + ";" + itemId);
                        oldBidderWriter.println(MessageEnum.OUTBID + ";" + houseId + ";" + itemId);
                        houseWriter.println(MessageEnum.SET_HIGH_BID + ";" + request.bidAmount + ";" + request.userId);
                    }
                    else {
                        // Reject
                        userWriter.println(MessageEnum.REJECT);
                    }
                    pendingRequests.remove(request);
                }
                break;
            case GET_ITEMS_FROM_BANK:
                houseId = Integer.valueOf(splitText[1]);
                int count = Integer.valueOf(splitText[2]);

                // TODO: Get Items from the list

                break;
            case ITEMS:
                int userId = Integer.valueOf(splitText[1]);
                userWriter = getUser(userId).writer;

                String message = MessageEnum.HOUSE_ITEMS.toString();
                int numItems = (splitText.length-2)/5;
                for(int i = 0; i < numItems; i++) {
                    String itemName = splitText[5*i+2];
                    itemId = Integer.valueOf(splitText[5*i+3]);
                    itemBid = Integer.valueOf(splitText[5*i+4]);
                    //itemBidUser = Integer.valueOf(splitText[5*i+5]);
                    String itemDesc = splitText[5*i+6];
                    message += ";" + itemName + ";" + itemId + ";" + itemBid + ";" + itemDesc;
                }
                userWriter.println(message);

                break;
            default:
                System.out.println("Invalid Command for Auction House: " + command);
        }
    }

    private static void initializeItems() {
        // TODO: Add stuff to the items list
    }

    static class Request{
        final MessageEnum command;
        int userId;
        int bidAmount;
        int itemId;
        int houseId;

        public Request(MessageEnum command) {
            this.command = command;
        }
    }

    static class SocketInfo{
        Socket socket;
        PrintWriter writer;
        BufferedReader reader;
        int id;
        String username;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader, int id, String username) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
            this.id = id;
            this.username = username;
        }
    }
}
