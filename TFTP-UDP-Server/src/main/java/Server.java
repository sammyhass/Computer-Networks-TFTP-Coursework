import java.net.DatagramPacket;

public class Server {
	public static void main(String[] args) throws Exception {
		TFTPServer server = new TFTPServer(8888);
		server.run();
	}

}
