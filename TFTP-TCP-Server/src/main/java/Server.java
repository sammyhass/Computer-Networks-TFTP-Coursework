import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        System.out.println("Server started on port " + port);
        while (true) {
            Socket clientSocket = serverSocket.accept();

            // Create a new thread for the client
             new TFTPRequestHandler(clientSocket).start();

        }
    }

    private void stop() {
        assert serverSocket != null;
        try {
            serverSocket.close();
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.out.println("Error stopping server");
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
