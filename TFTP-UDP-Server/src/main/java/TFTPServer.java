import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TFTPServer {
	private DatagramSocket socket;
	private boolean running;
	private byte[] buffer = new byte[1024];

	public TFTPServer(int port) throws Exception {
		socket = new DatagramSocket(port);
		running = true;
	}

	public void run() throws IOException {
		while(running) {
			DatagramPacket packet = null;
			try {
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error receiving packet");
				System.exit(1);
			}

			TFTPRequestBuilder.OPCODE opcode = null;
			try {
			 opcode = TFTPRequestDecoder.unpackOp(packet.getData());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error unpacking opcode");
				System.exit(1);
			}

			switch(opcode) {
			case RRQ:
				System.out.println("RRQ received");
				break;
			case WRQ:
				System.out.println("WRQ received");
				break;
			case DATA:
				System.out.println("DATA received");
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
			socket.send(packet);
		}
		socket.close();
	}




}
