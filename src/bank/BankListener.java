/**
 * CS 351L Project 5 - Distributed Auction Houses
 * Pun Chhetri, Isha Chauhan, John Cooper, John Tran
 *
 * Listens for socket requests and handles these requests, adding
 * them to the appropriate lists and sending a LOGIN message when
 * the socket has been fully added
 */

package bank;

import common.BankAccount;
import common.MessageEnum;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class BankListener extends Thread{
    // The balance each user starts with
    // Will be used in the User class as well (create an identical BankAccount
    // object with the same initial balance)
    public static final int INITIAL_BALANCE = 1000;
    // The maximum number of houses (way higher ever going to happen)
    private static final int MAX_HOUSES = 100;
    // The open server listening for requests
    private ServerSocket serverSocket;
    // The list of user sockets
    private List<Bank.SocketInfo> userList;
    // List of auction house sockets
    private List<Bank.SocketInfo> houseList;

    // Index of house ids (to make each unique)
    private int houseId;
    // Index of user ids (to make each unique)
    private int userId;

    /**
     * @param serverSocket The server listening for incoming socket requests
     * @param userList The list of user sockets
     * @param houseList The list of house sockets
     */
    public BankListener(ServerSocket serverSocket, List<Bank.SocketInfo> userList, List<Bank.SocketInfo> houseList) {
        this.serverSocket = serverSocket;
        this.userList = userList;
        this.houseList = houseList;
        // House ids start at 0
        houseId = 0;
        // User ids start at MAX_HOUSES so users and houses have distinct ids
        userId = MAX_HOUSES;
    }

    @Override
    public void run() {
        // Always keep accepting incoming socket requests for the duration of the program
        while(true) {
            try{
                Socket socket = serverSocket.accept();

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                // Wait until a message indicating what kind of object is at the other end of the socket
                // For the limited number of sockets in this project, having this (short) busy wait doesn't
                // cause issues. If it did, we would create another thread to do the rest of this run
                while (!reader.ready()) ;

                String line = reader.readLine();
                String[] split = line.split(";");
                // Test if the message indicates a house
                if (MessageEnum.parseCommand(split[0]) == MessageEnum.HOUSE) {
                    // Houses start with a 0 account balance
                    BankAccount account = new BankAccount(0, houseId);
                    synchronized (houseList) {
                        houseList.add(new Bank.SocketInfo(socket, writer, reader, houseId, "", account));
                    }
                    System.out.println("New house has logged in with id " + houseId);
                    houseId += 1;
                    // Message house of successful login
                    writer.println(MessageEnum.LOGIN + ";" + houseId);
                } else {
                    BankAccount account = new BankAccount(INITIAL_BALANCE,
                            userId);
                    synchronized (userList) {
                        userList.add(new Bank.SocketInfo(socket, writer, reader, userId, split[1], account));
                    }
                    System.out.println("New user has logged in with id " + userId);
                    userId += 1;
                    // Message user of successful login
                    writer.println(MessageEnum.LOGIN + ";" + userId);
                }
            }
            catch(Exception ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
