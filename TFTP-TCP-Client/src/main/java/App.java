import client.TFTPClient;
import cmd.Cmd;
import exceptions.TFTPException;

import java.io.IOException;
import java.net.Socket;

public class App {
    public static void main(String[] args) throws TFTPException, IOException {
        Cmd cmd = new Cmd("localhost", 8080);
        try {
            cmd.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
