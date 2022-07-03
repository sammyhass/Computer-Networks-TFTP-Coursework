import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Server {
	private static final int PORT = 8888;

	private static final HashMap<String, TFTPRequestHandler> clients = new HashMap<>();

	public static void main(String[] args) throws Exception {
		DatagramSocket socket = new DatagramSocket(PORT);
		while (true) {
			byte[] buffer = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);

			String clientAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();

			if (!clients.containsKey(clientAddress)) {
				System.out.println("New client: " + clientAddress);
				clients.put(clientAddress, new TFTPRequestHandler(socket));
			}
			TFTPRequestHandler handler = clients.get(clientAddress);
			handler.handle(packet);
		}

	}
}
