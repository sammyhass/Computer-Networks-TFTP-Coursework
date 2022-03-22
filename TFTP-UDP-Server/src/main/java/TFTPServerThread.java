import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TFTPServerThread implements Runnable {
	private DatagramSocket socket;
	private boolean running;
	private DataPacketsBuilder dataPacketsBuilder;
	// Client address
	private String clientAddress;
	// Client port
	private int clientPort;

	public TFTPServerThread(int port) throws Exception {
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
				clientAddress = packet.getAddress().getHostAddress();
				clientPort = packet.getPort();
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

			assert request.opcode == TFTPRequestBuilder.OPCODE.RRQ || request.opcode == TFTPRequestBuilder.OPCODE.WRQ;

			System.out.println("Filename: " + request.filename);
		} catch (Exception e) {
			System.err.println("Error unpacking request");
			return false;
		}

		if (request.opcode == TFTPRequestBuilder.OPCODE.WRQ) {
			// If the operation is a write, we should initialize our
			// filename for the data packets builder
			dataPacketsBuilder.setFilename(request.filename);

			// Send ACK back
			sendACK(0, packet);
		} else {
			// If the operation is a read, we should send the file
			// to the client
			try {
				sendFile(request.filename, packet);
			} catch (Exception e) {
				System.err.println("Error sending file");
			}


		}

		return true;

	}
	// sendFile to TFTP Server
	// first sends WRQ req to server
	// waits for ACK
	// split file into 512 byte chunks
	// send each chunk
	// wait for ACK
	// repeat until all chunks sent
	public void sendFile(String filename, DatagramPacket packet) {
		// Read file as byte array
		byte[] file = null;
		try {
			// Read the filename from the resources folder
			String path = new java.io.File(".").getCanonicalPath() + "/src/main/resources/" + filename;
			System.out.println(path);
			file = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			System.err.println("Error reading file: " + filename);
//			System.exit(1);
			file = "Helllooooo".getBytes();
		}

		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

		// Build WRQ packet
		int wrqReqSize = TFTPRequestBuilder.packWRQ(buffer, filename);

		DatagramPacket wrqPacket = new DatagramPacket(buffer, wrqReqSize, packet.getAddress(), packet.getPort());

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

		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e) {
			System.err.println("Error setting socket timeout");
		}

		DatagramPacket ackPacket = new DatagramPacket(buffer, TFTPRequestBuilder.MAX_BYTES, packet.getAddress(), packet.getPort());

		while (!hasReceivedACK) {
			try {
				socket.receive(ackPacket);
			} catch (IOException e) {
				System.err.println("Error receiving ACK packet. Retrying...");
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

			byte[] packetToSend = new byte[end - start];



			System.arraycopy(file, start, packetToSend, 0, end - start);



			// Build data packet by splitting file into 512 byte chunks
			int dataReqSize = TFTPRequestBuilder.packData(buffer, i, packetToSend);


			DatagramPacket dataPacket = new DatagramPacket(buffer, dataReqSize, packet.getAddress(), packet.getPort());

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

			System.out.println("Sent packet " + i);

		}
	}



	private void sendData(int blockNumber, DatagramPacket packet) {
		byte[] buf = new byte[TFTPRequestBuilder.MAX_BYTES];
		TFTPRequestBuilder.packData(buf, blockNumber, dataPacketsBuilder.getData());
		System.out.println("Sending data packet " + blockNumber);
	}

	// Send an ACK packet to the client with the given block number
	private boolean sendACK(int block, DatagramPacket packet) {
		try {
			byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

			TFTPRequestBuilder.packAck(buffer, block);

			DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());

			System.out.println("Sending ACK for block number " + block);
			socket.send(ackPacket);

		} catch (Exception e) {
			System.err.println("Error sending ACK");
			return true;
		}
		return false;
	}

	// Handles receiving data packets and sending ACKs back
	public TFTPRequestDecoder.DataPacket handleData(DatagramPacket packet) throws TFTPException {
		TFTPRequestDecoder.DataPacket dataPacket = TFTPRequestDecoder.unpackData(packet.getData(), 0);
		dataPacketsBuilder.addDataPacket(dataPacket);

		if (dataPacket.size < TFTPRequestBuilder.MAX_BYTES - 4) {
				// Check if we have received the last packet by checking if the size is less than the max size minus the opcode and block number
			// If it is the last packet, we should write the file
			System.out.println("Last packet received");
			try {
				dataPacketsBuilder.save();
			} catch (Exception e) {
				System.err.println("Error writing file");
				e.printStackTrace();
			}
		}
		// Send ACK back
		sendACK(dataPacket.blockNumber, packet);
		return dataPacket;
	}




}
