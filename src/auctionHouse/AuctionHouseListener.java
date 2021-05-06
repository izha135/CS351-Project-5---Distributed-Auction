package auctionHouse;

import bank.Bank;
import common.BankAccount;
import common.MessageEnum;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class AuctionHouseListener extends Thread{
    private ServerSocket server;
    private List<AuctionHouse.SocketInfo> userList;
    private boolean run;

    public AuctionHouseListener(ServerSocket server, List<AuctionHouse.SocketInfo> userList) {
        this.server = server;
        this.userList = userList;

        run = true;
    }

    @Override
    public void run() {
        while(run) {
            try {
                //System.out.println(server);
                Socket socket = server.accept();
                System.out.println("Accepted user socket");
                System.out.println("Socket Inet Address Host name: " + socket.getInetAddress().getHostName());
                System.out.println("Socket Inet Address Host address: " + socket.getInetAddress().getHostAddress());
                System.out.println(socket.toString());

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                String line = reader.readLine();
                System.out.println(line);
                String[] split = line.split(";");
                // Test if the message indicates a house
                int userId = Integer.parseInt(split[0]);
                synchronized (userList) {
                    userList.add(new AuctionHouse.SocketInfo(socket, writer, reader, userId));
                }
                System.out.println("New user has logged in with id " + userId);
            }
            catch (Exception ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void stopRunning() {
        run = false;
    }
}
