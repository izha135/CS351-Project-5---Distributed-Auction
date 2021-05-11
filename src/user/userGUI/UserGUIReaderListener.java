package user.userGUI;

import common.FullMessage;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * SHOULD BE DEPRECATED: has basically the same function as the
 * UserGUIReaderListener, except using its own thread with a very
 * bad idea of a while(true) loop...(constantly checking the sockets that
 * may result in poor performance)
 */
public class UserGUIReaderListener extends Thread {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean run;

    private final List<FullMessage> fullMessagesActionList;

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
            Platform.runLater(() -> {
                try {
                    if(reader.ready()) {
                        String message = reader.readLine();
                        // FIXME: Invoke functions based on input
                        // FIXME: move to other listener...

                        System.out.println();
                        System.out.println("Message received: " + message);

                        FullMessage fullMessage =
                                getFullMessageFromListener(message);

                        synchronized (fullMessagesActionList) {
                            fullMessagesActionList.add(fullMessage);
                        }
                    }
                }
                catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public void stopRunning() {
        run = false;
    }

    private FullMessage getFullMessageFromListener(String message) {
        return new FullMessage(message);
    }
}
