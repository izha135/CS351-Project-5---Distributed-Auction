package user.userGUI;

import common.FullMessage;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class UserGUIReaderListener extends Thread{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean run;

    private List<FullMessage> fullMessagesActionList;

    public UserGUIReaderListener(Socket socket,
                                 BufferedReader reader,
                                 PrintWriter writer,
                                 List<FullMessage> fullMessagesActionList) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.run = true;

        this.fullMessagesActionList = fullMessagesActionList;
    }

    @Override
    public void run() {
        while(run) {
            try {
                if(reader.ready()) {
                    String message = reader.readLine();
                    // FIXME: Invoke functions based on input

                    System.out.println();
                    System.out.println("Message received: " + message);

                    FullMessage fullMessage =
                            getFullMessageFromListener(message);
                    fullMessagesActionList.add(fullMessage);
                }
            }
            catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopRunning() {
        run = false;
    }

    private FullMessage getFullMessageFromListener(String message) {
        return new FullMessage(message);
    }
}
