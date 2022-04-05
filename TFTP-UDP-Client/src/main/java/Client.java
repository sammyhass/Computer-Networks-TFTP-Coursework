import java.io.IOException;
import java.net.*;
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
	// send each chunk
	// wait for ACK
	// repeat until all chunks sent
	public boolean sendFile(String filename) {
		// Read file as byte array
		byte[] file = null;
		try {
			// Read the filename from the resources folder
			String path = new java.io.File(".").getCanonicalPath() + '/' + filename;
			System.out.println(path);
			file = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			System.err.println("Error reading file: " + filename);
			e.printStackTrace();
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
		// clear buffer
		buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

		boolean hasReceivedACK = false;
		int numRetries = 0;

		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e) {
			System.err.println("Error setting socket timeout");
			return false;
		}

		DatagramPacket ackPacket = new DatagramPacket(buffer, TFTPRequestBuilder.MAX_BYTES, host, port);
		while (!hasReceivedACK && numRetries < 3) {
			try {
				socket.receive(ackPacket);
			} catch (IOException e) {
				System.err.println("Error receiving ACK packet. Retrying...");
				numRetries++;
				continue;
			}

			try {
				int n = TFTPRequestDecoder.unpackACK(ackPacket.getData());
				hasReceivedACK = true;
			} catch (TFTPException e) {
				continue;
			}
			System.out.println("Received ACK, sending data...");
		}


		// Split file into packets
		int numPackets = (int) Math.ceil((double) file.length / (TFTPRequestBuilder.MAX_BYTES - 4));


		for (int i = 1; i <= numPackets; i++) {
			// clear buffer
			buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

			// Get the current packet (leaving room for the opcode and block number - 4 bytes total)
			int start = (i - 1) * (TFTPRequestBuilder.MAX_BYTES - 4);
			int end = Math.min(start + TFTPRequestBuilder.MAX_BYTES - 4, file.length);

			byte[] packet = new byte[end - start];



			System.arraycopy(file, start, packet, 0, end - start);



			// Build data packet by splitting file into 512 byte chunks
			int dataReqSize = TFTPRequestBuilder.packData(buffer, i, packet);


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
				System.err.println("Timed out waiting for ACK.\n");
				return false;
			}

			System.out.println("Sent packet " + i);

		}

		System.out.println("File sent successfully");


		return true;
	}

	@Override
	// Get file from TFTP Server
	// first sends RRQ req to server
	// waits for DATA
	// send ACK
	// repeat until all chunks received
	public boolean getFile(String filename) {



		DataPacketsBuilder dataPacketsBuilder = new DataPacketsBuilder();
		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
		int size = TFTPRequestBuilder.packRRQ(buffer, filename);
		DatagramPacket rrqPacketDatagram = new DatagramPacket(buffer, size, host, port);

		try {
			socket.send(rrqPacketDatagram);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error sending RRQ packet");
		}

		// Wait till we receive DATA
		// clear buffer
		buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
		int numRetries = 0;

		try {
		socket.setSoTimeout(2000);
		} catch (SocketException e) {
			System.err.println("Error setting socket timeout");
		}
		while (numRetries < 3) {
			DatagramPacket dataPacket = new DatagramPacket(buffer, TFTPRequestBuilder.MAX_BYTES, host, port);
			try {
				socket.receive(dataPacket);
			} catch (IOException e) {
				System.err.println("Error receiving DATA packet. Retrying...");
			}

			TFTPRequestDecoder.DataPacket packet;
			try {
				packet = TFTPRequestDecoder.unpackData(dataPacket.getData(), 0);
			} catch (TFTPException e) {
				numRetries++;
				continue;
			}

			dataPacketsBuilder.addDataPacket(packet);


			// Build ACK packet
			buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
			int ackReqSize = TFTPRequestBuilder.packAck(buffer, packet.blockNumber);
			DatagramPacket ackPacket = new DatagramPacket(buffer, ackReqSize, host, port);

			// Send ACK packet
			try {
				socket.send(ackPacket);
			} catch (IOException e) {
				e.printStackTrace();

			}

			if (packet.size < TFTPRequestBuilder.MAX_BYTES - 4) {
				System.out.println("Received last packet. Saving file...");
				dataPacketsBuilder.setFilename(filename);
				try {
					dataPacketsBuilder.save();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}


		}
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (numRetries == 3) {
			System.err.println("Error receiving DATA packet. Cancelling transfer.");
			return false;
		}


		return false;
	}
}
