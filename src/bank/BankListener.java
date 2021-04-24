package bank;

import common.MessageEnum;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class BankListener extends Thread{
    private ServerSocket serverSocket;
    private List<Bank.SocketInfo> userList;
    private List<Bank.SocketInfo> houseList;

    private int houseId;
    private int userId;

    public BankListener(ServerSocket serverSocket, List<Bank.SocketInfo> userList, List<Bank.SocketInfo> houseList) {
        this.serverSocket = serverSocket;
        this.userList = userList;
        this.houseList = houseList;
        houseId = 0;
        userId = 0;
    }

    @Override
    public void run() {
        while(true) {
            try{
                Socket socket = serverSocket.accept();
                synchronized (houseList) {
                    synchronized (userList) {
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        while (!reader.ready()) ;

                        String line = reader.readLine();
                        String[] split = line.split(";");
                        if (MessageEnum.parseCommand(split[0]) == MessageEnum.HOUSE) {
                            writer.println(MessageEnum.LOGIN + ";" + houseId);
                            houseList.add(new Bank.SocketInfo(socket, writer, reader, houseId, ""));
                            houseId += 1;
                        } else {
                            writer.println(MessageEnum.LOGIN + ";" + userId);
                            userList.add(new Bank.SocketInfo(socket, writer, reader, userId, split[1]));
                            userId += 1;
                        }
                    }
                }
            }
            catch(Exception ex) {

            }
        }
    }
}
