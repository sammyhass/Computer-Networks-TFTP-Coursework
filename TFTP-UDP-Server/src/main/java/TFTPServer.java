import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TFTPServer implements Runnable {
	private DatagramSocket socket;
	private boolean running;
	private DataPacketsBuilder dataPacketsBuilder;

	public TFTPServer(int port) throws Exception {
		socket = new DatagramSocket(port);
		running = true;
		dataPacketsBuilder = new DataPacketsBuilder();
	}

	public void run() {
		while(running) {
			DatagramPacket packet = null;
			try {
				byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error receiving packet");
			}


			TFTPRequestBuilder.OPCODE opcode = null;
			try {
			  opcode = TFTPRequestDecoder.unpackOp(packet.getData());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error unpacking opcode");
			}

			try {
				switch (opcode) {
				case RRQ:
					System.out.println("RRQ received");
					handleRRQorWRQ(packet);
					break;
				case WRQ:
					System.out.println("WRQ received");
					handleRRQorWRQ(packet);
					break;
				case DATA:
					System.out.println("DATA received");
					handleData(packet);
					break;
				case ACK:
					System.out.println("ACK received");
					break;
				case ERROR:
					System.out.println("ERROR received");
					break;
				default:
					System.out.println("Unknown opcode");
				}
			} catch (Exception e) {
				System.err.println("Error handling packet");
				e.printStackTrace();
			}

			try {
			socket.send(packet);
			} catch (IOException e) {
				System.err.println("Error sending packet");
				System.exit(1);
			}
			packet = null;


		}
		socket.close();
	}

	public boolean handleRRQorWRQ(DatagramPacket packet) {
		TFTPRequestDecoder.WrqOrRrqPacket request = null;
		try {
			request = TFTPRequestDecoder.unpackWRQorRRQ(packet.getData(), 0);

			System.out.println("Filename: " + request.filename);
		} catch (Exception e) {
			System.err.println("Error unpacking request");
			return false;
		}

		// If the operation is a write, we should initialize our
		// filename for the data packets builder
		if (request.opcode == TFTPRequestBuilder.OPCODE.WRQ) {
			dataPacketsBuilder.setFilename(request.filename);
		}


		// Send ACK back
		try {
			byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

			TFTPRequestBuilder.packAck(buffer, 0);

			DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());

			socket.send(ackPacket);

		} catch (Exception e) {
			System.err.println("Error sending ACK");
			return false;
		}

		return true;

	}

	public TFTPRequestDecoder.DataPacket handleData(DatagramPacket packet) throws TFTPException {
		TFTPRequestDecoder.DataPacket dataPacket = TFTPRequestDecoder.unpackData(packet.getData(), 0);
		dataPacketsBuilder.addDataPacket(dataPacket);

		if (dataPacket.size < TFTPRequestBuilder.MAX_BYTES - 4) {
				// Check if we have received the last packet by checking if the size is less than the max size minus the opcode and block number
			// If it is the last packet, we should write the file
			System.out.println("Last packet received " + dataPacket.size);
			try {
				dataPacketsBuilder.save();
			} catch (Exception e) {
				System.err.println("Error writing file");
				e.printStackTrace();
			}
		}


		// Send ACK back
		try {
			byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

			TFTPRequestBuilder.packAck(buffer, 0);

			DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());

			socket.send(ackPacket);

		} catch (Exception e) {
			System.err.println("Error sending ACK back to Client");
			return null;
		}

		return dataPacket;
	}




}
