package user;

import java.io.*;
import java.net.Socket;

public class User {
    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 3030;

        //try (Socket socket = new Socket(hostName, port);
        //     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){

        PrintWriter writer = new PrintWriter(System.out, true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        //}
        //catch (Exception e) {
        //    e.printStackTrace();
        //}
    }
}
