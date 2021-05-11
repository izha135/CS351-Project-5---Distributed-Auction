package user.userGUI;

import common.FullMessage;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Replaced the UserGUIReaderListener: will actively check to see if there
 * are any commands received (from the bank and house) and add them to the list
 * of commands (FullMessage object) to process every 250 milliseconds and
 * execute any commands (corresponding methods given above), if there are
 * any, and also checks if the exit conditions for the user has been met
 * (logic condensed to if the bank allows the user to exit since the bank has
 * information of any bids that are still happening for ALL users)
 */
public class UserGUIReaderTimer {
    private Timer timer;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private final List<FullMessage> fullMessagesActionList;

    public UserGUIReaderTimer(Socket socket, BufferedReader reader,
                              PrintWriter writer,
                              List<FullMessage> fullMessagesActionList) {
        timer = new Timer();

        this.socket = socket;
        this.reader = reader;
        this.writer = writer;

        this.fullMessagesActionList = fullMessagesActionList;

        timer.scheduleAtFixedRate(new UserGUIReaderTimerTask(),
                0, 250);
    }

    private class UserGUIReaderTimerTask extends TimerTask {

        @Override
        public void run() {
            Platform.runLater(() -> {
                try {
                    if (reader != null) {
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
                }
                catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        private FullMessage getFullMessageFromListener(String message) {
            return new FullMessage(message);
        }
    }

    public void stopRunning() {
        timer.cancel();
        timer.purge();
    }
}
