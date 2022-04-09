import client.TFTPClient;
import cmd.Cmd;
import exceptions.TFTPException;

import java.io.IOException;
import java.net.Socket;

public class App {
    Socket socket;
    public static void main(String[] args) throws TFTPException, IOException {
        Socket socket = new Socket("localhost", 8080);
        TFTPClient client = new TFTPClient(socket);
        Cmd cmd = new Cmd(client);
        try {
            cmd.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
