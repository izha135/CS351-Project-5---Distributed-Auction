package user.userGUI;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserGUIListener extends Thread{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean run;

    public UserGUIListener(Socket socket, BufferedReader reader, PrintWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.run = true;
    }

    @Override
    public void run() {
        while(run) {
            try {
                if(reader.ready()) {
                    String message = reader.readLine();
                    // FIXME: Invoke functions based on input
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
}
