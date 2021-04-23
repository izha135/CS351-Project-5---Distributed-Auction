package bank;

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

    public static void main(String[] args) {
        int port = 3030;

        userList = new LinkedList<>();
        houseList = new LinkedList<>();

        display = new BankDisplay();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            listener = new BankListener(serverSocket, userList, houseList);
            listener.start();

            String text;
            String[] splitText;
            MessageEnum command;

            boolean run = true;
            while(run) {
                System.out.println("");
                for(int i = 0; i < userList.size(); i++) {
                    SocketInfo socket = userList.get(i);
                    while(socket.reader.ready()) {
                        text = socket.reader.readLine();
                        splitText = text.split(";");
                        command = MessageEnum.parseCommand(splitText[0]);
                        handleUserCommand(command);
                    }
                }
                for(int i = 0; i < houseList.size(); i++) {
                    SocketInfo socket = houseList.get(i);
                    while(socket.reader.ready()) {
                        text = socket.reader.readLine();
                        splitText = text.split(";");
                        command = MessageEnum.parseCommand(splitText[0]);
                        handleHouseCommand(command);
                    }
                }
            }

            System.out.println("Server is listening on port " + port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleUserCommand(MessageEnum command) {
        switch (command) {
            case BID:

                break;
            case GET_ITEMS:

                break;
            case GET_HOUSES:

                break;
            default:
                System.out.println("Invalid Command for User: " + command);
        }
    }

    private static void handleHouseCommand(MessageEnum command) {
        switch (command) {
            case GET_ITEMS_FROM_BANK:

                break;
            case GET_MASTER_LIST:

                break;
            case ITEMS:

                break;
            default:
                System.out.println("Invalid Command for House: " + command);
        }
    }

    static class SocketInfo{
        Socket socket;
        PrintWriter writer;
        BufferedReader reader;
        int id;

        public SocketInfo(Socket socket, PrintWriter writer, BufferedReader reader, int id) {
            this.socket = socket;
            this.writer = writer;
            this.reader = reader;
            this.id = id;
        }
    }
}
