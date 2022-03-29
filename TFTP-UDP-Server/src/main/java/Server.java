import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Server {
	private static final int PORT = 8888;

	private static final int THREAD_POOL_SIZE = 10;

	private static final HashMap<InetAddress, TFTPServerThread> clients = new HashMap<>();


	public static void main(String[] args) throws Exception {
		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			System.out.println("Server is running...");
			while (true) {
				try {
					byte[] buffer = new byte[1024];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);

					InetAddress clientAddress = packet.getAddress();

					Thread t = null;
					TFTPServerThread client = clients.get(clientAddress);
					try {
						if (client == null) {
							System.out.println("New client connected: " + clientAddress);
							client = new TFTPServerThread(socket);
							client.setRequestPacket(packet);
							clients.put(clientAddress, client);
							t = new Thread(client);
							t.start();
						} else {
							client.setRequestPacket(packet);
						}
					} catch (Exception e) {
						e.printStackTrace();
						clients.remove(clientAddress);
						assert t != null;
						t.interrupt();
						client.stop();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
