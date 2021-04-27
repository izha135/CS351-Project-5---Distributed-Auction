package user;

import java.io.*;
import java.net.Socket;

public class User {
    private String userName;

    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 3030;

        //try (Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        // FIXME: use socket above
        try (PrintWriter writer = new PrintWriter(System.out, true);
             BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(System.in))) {
            // maybe incorporate GUI later...

            // get the list of auction houses first
            // FIXME: sort out the id
            writer.write("getHouses;");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


        //}
        //catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    private void promptInitialMessage() {
        System.out.println();
        System.out.println("Welcome to the Auction House simulation!");
        System.out.println("Please ");
    }
}
