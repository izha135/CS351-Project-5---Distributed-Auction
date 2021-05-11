/**
 * CS 351L Project 5 - Distributed Auction Houses
 * Pun Chhetri, Isha Chauhan, John Cooper, John Tran
 *
 * The thread that listens for incoming requests for users
 * to connect to the auction house
 */

package auctionHouse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class AuctionHouseListener extends Thread{
    // The server listening for requests to join
    private ServerSocket server;
    // The list of the socket-info structures for every joined user
    private List<AuctionHouse.SocketInfo> userList;
    // true if the thread should run
    private boolean run;

    /**
     * @param server The server to listen for requests through
     * @param userList The list of user socketInfo's
     */
    public AuctionHouseListener(ServerSocket server, List<AuctionHouse.SocketInfo> userList) {
        this.server = server;
        this.users = userList;

        run = true;
    }

    @Override
    public void run() {
        while(run) {
            try {
                // Waits for incoming requests
                Socket socket = server.accept();
                // Prints debug information
                System.out.println("Accepted user socket");
                System.out.println("Socket Inet Address Host name: " + socket.getInetAddress().getHostName());
                System.out.println("Socket Inet Address Host address: " + socket.getInetAddress().getHostAddress());
                System.out.println(socket.toString());

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                // Read in the userId of the user sending the message
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

    /**
     * Stops the thread running
     */
    public synchronized void stopRunning() {
        run = false;
        System.exit(0);
    }
}
