import exceptions.TFTPException;
import request.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.file.Files;
import java.nio.file.Paths;


// A Server Thread that handles requests from a single client.
public class TFTPRequestHandler {
	private DatagramPacket requestPacket;
	private boolean running;
	private final DataPacketsBuilder dataPacketsBuilder;
	private  RequestHandlerLogger logger;
	private final DatagramSocket socket;

	public TFTPRequestHandler(DatagramSocket socket)  {
		System.out.println("Server thread started");
		this.socket = socket;
		this.running = true;
		this.dataPacketsBuilder = new DataPacketsBuilder();
	}

	public void handle(DatagramPacket packet) {
		this.logger = new RequestHandlerLogger(packet.getAddress(), packet.getPort());
		this.requestPacket = packet;
		handleRequestPacket();
	}


	private void handleRequestPacket() {
		assert requestPacket.getData() != null;

		OPCODE opcode;

		try {
			opcode = TFTPRequestDecoder.unpackOp(requestPacket.getData());
		} catch (Exception e) {
			System.err.println("Error unpacking opcode");
			return;
		}

		try {
			switch (opcode) {
			case RRQ, WRQ:
				handleRRQorWRQ(requestPacket);
				break;
			case DATA:
				handleData(requestPacket);
				break;
			case ACK:
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
	}

	public void handleRRQorWRQ(DatagramPacket packet) {
		TFTPRequestDecoder.WrqOrRrqPacket request;
		try {
			request = TFTPRequestDecoder.unpackWRQorRRQ(packet.getData(), 0);

			assert request.opcode == OPCODE.RRQ || request.opcode == OPCODE.WRQ;

		} catch (Exception e) {
			System.err.println("Error unpacking request");
			return;
		}

		if (request.opcode == OPCODE.WRQ) {
			// If the operation is a write, we should initialize our
			// filename for the data packets builder

			logger.logWRQ(request.filename);
			dataPacketsBuilder.setFilename(request.filename);

			// Send ACK back
			sendACK(0, packet);
		} else {
			// If the operation is a read, we should send the file
			// to the client
			logger.logRRQ(request.filename);
			try {
				sendFile(request.filename, packet);
			} catch (Exception e) {
				System.err.println("Error sending file");
			}


		}

	}
	// sendFile from TFTP Server
	// first sends WRQ req to server
	// waits for ACK from client
	// split file into 512 byte chunks
	// send each chunk
	// wait for ACK
	// repeat until all chunks sent
	public void sendFile(String filename, DatagramPacket packet) {
		// Read file as byte array
		byte[] file = null;
		try {
			// Read the filename from the resources folder
			String path = new java.io.File(".").getCanonicalPath() +  "/" + filename;
			file = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			// If there was an error reading the file, send an error packet
			sendError(packet);
			return;
		}

		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

		DatagramPacket ackPacket = new DatagramPacket(buffer, TFTPRequestBuilder.MAX_BYTES, packet.getAddress(), packet.getPort());


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
				logger.logDATASent(filename, i, end - start, numPackets);

			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error sending DATA packet");
			}

			// Wait till we receive ACK
			try {
				socket.receive(ackPacket);
				// Ensure the ACK packet echos the block number we sent
				assert TFTPRequestDecoder.unpackACK(ackPacket.getData()) == i;

				logger.logACK(i, true);
			} catch (Exception e) {
				if (e instanceof TFTPException) {
					e.printStackTrace();
				}
				System.err.println("Error receiving ACK packet");
			}


		}

		logger.logDATAEnd(filename, false);
	}

	private void sendError(DatagramPacket packet) {

		String msg = "File Not Found";
		int errorCode = 0b001;

		// Create an error packet
		byte[] buf = new byte[TFTPRequestBuilder.MAX_BYTES];
		int size = TFTPRequestBuilder.packError(buf, errorCode, msg);

		// Send the error packet
		DatagramPacket errorPacket = new DatagramPacket(buf, size, packet.getAddress(), packet.getPort());

		logger.logError(msg);

		try {
			socket.send(errorPacket);
		} catch (IOException e) {
			System.err.println("Error sending error packet");
		}
	}

	// Send an ACK packet to the client with the given block number
	private void sendACK(int block, DatagramPacket packet) {
		try {
			byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

			TFTPRequestBuilder.packAck(buffer, block);

			DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());

			socket.send(ackPacket);
			logger.logACK(block, false);

		} catch (Exception e) {
			System.err.println("Error sending ACK");
		}
	}

	// Handles receiving data packets and sending ACKs back
	public void handleData(DatagramPacket packet) throws TFTPException {
		TFTPRequestDecoder.DataPacket dataPacket = TFTPRequestDecoder.unpackData(packet.getData(), 0);
		dataPacketsBuilder.addDataPacket(dataPacket);
		logger.logDATAReceived(dataPacketsBuilder.getFilename(), dataPacket.blockNumber, dataPacket.size);

		// Send ACK back
		sendACK(dataPacket.blockNumber, packet);

		if (packet.getLength() < TFTPRequestBuilder.MAX_BYTES) {
			// Check if we have received the last packet by checking if the size is less than the max size
			// If it is the last packet, we should write the file
			logger.logDATAEnd(dataPacketsBuilder.getFilename(), true);
			try {
				logger.logFileSave(dataPacketsBuilder.getFilename());
				dataPacketsBuilder.save();
			} catch (Exception e) {
				System.err.println("Error writing file");
				e.printStackTrace();
			}
		}
	}

}
