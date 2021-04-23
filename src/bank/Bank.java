package bank;

import java.net.ServerSocket;

public class Bank {
    public static void main(String[] args) {
        int port = 3030;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
