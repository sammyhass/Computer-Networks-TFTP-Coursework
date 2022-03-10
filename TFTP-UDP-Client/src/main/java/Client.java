import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client implements IClient {

	private InetAddress host;
	private int port;

	private DatagramSocket socket;


	public Client(String ip, int port) {
		try {
			this.host = InetAddress.getByName(ip);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error getting host: " + ip);
		}
		this.port = port;

		try {
			socket = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	// sendFile to TFTP Server
	// first sends WRQ req to server
	// waits for ACK
	// split file into 512 byte chunks
	// send each chunk to server
	// wait for ACK
	// repeat until all chunks sent
	public boolean sendFile(String filename) {
		// Read file as byte array
		byte[] file = null;
		try {
			file = Files.readAllBytes(Paths.get(filename));
		} catch (IOException e) {
			System.err.println("Error reading file: " + filename);
//			System.exit(1);
			file = "Error reading file: ".getBytes();
		}

		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

		// Build WRQ packet
		int wrqReqSize = TFTPRequestBuilder.packWRQ(buffer, filename);

		DatagramPacket wrqPacket = new DatagramPacket(buffer, wrqReqSize, host, port);

		// Send WRQ packet
		try {
			socket.send(wrqPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error sending WRQ packet");
			System.exit(1);
		}

		// Wait till we receive ACK
		buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
		DatagramPacket ackPacket = new DatagramPacket(buffer, TFTPRequestBuilder.MAX_BYTES);

		try {
			socket.receive(ackPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error receiving ACK packet");
			System.exit(1);
		}

		// Parse the ACK packet
		try {
			TFTPRequestDecoder.unpackACK(ackPacket.getData());
		} catch (TFTPException e) {
			e.printStackTrace();
			System.err.println("Error parsing ACK packet");
			System.exit(1);
		}


		// Split file into packets
		int numPackets = (int) Math.ceil((double) file.length / TFTPRequestBuilder.MAX_BYTES);

		for (int i = 0; i < numPackets; i++) {
			// Build DATA packet
			int dataReqSize = TFTPRequestBuilder.packData(buffer, i, file);

			DatagramPacket dataPacket = new DatagramPacket(buffer, dataReqSize, host, port);

			// Send DATA packet
			try {
				socket.send(dataPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error sending DATA packet");
				System.exit(1);
			}

			// Wait till we receive ACK
			try {
				socket.receive(ackPacket);

				// Ensure the ACK packet echos the block number we sent
				assert TFTPRequestDecoder.unpackACK(ackPacket.getData()) == i;
			} catch (Exception e) {
				if (e instanceof TFTPException) {
					e.printStackTrace();
				}
				System.err.println("Error receiving ACK packet");
				System.exit(1);
			}

		}


		return true;
	}

	@Override
	public boolean retrieveFile(String filename) {
		return false;
	}
}
