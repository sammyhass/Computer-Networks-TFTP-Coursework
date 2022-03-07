import javafx.util.Pair;

import java.util.Arrays;
import java.util.stream.Stream;

// We use this class to ensure that responses from the server are
// correctly formatted. For example, ACK packets must be received prior to sending
// the first data packet.
public class TFTPRequestDecoder {

	// ACK packet
	//
	//  2 bytes    2 bytes
	// -----------------------
	// | Opcode |   Block #  |
	// -----------------------
	// We'll only be unpacking these on the client side to ensure that we can receive and send data. On success, will
	// return the block number.
	public static int unpackACK(byte[] packet) throws TFTPException {
		try {
			int offset = 0;
			int op = unpackUint16(packet, offset);
			assert op == TFTPRequestBuilder.OPCODE.ACK.getValue();

			offset += 2;
			return unpackUint16(packet, offset);
		} catch (Exception e) {
			throw new TFTPException("Invalid ACK packet");
		}
	}

	// Returns opcode from the packet (2 bytes)
	public static TFTPRequestBuilder.OPCODE unpackOp(byte[] packet) throws TFTPException {
		try {
			int op = unpackUint16(packet, 0);
			return TFTPRequestBuilder.OPCODE.values()[op];
		} catch (Exception e) {
			throw new TFTPException("Could not unpack opcode");
		}
	}

	// WRQ/RRQ packet
	//
	//  2 bytes    string    1 byte     string    1 byte
	// ---------------------------------------------------
	// | Opcode |  Filename  |   0  |    Mode    |   0  |
	// ---------------------------------------------------
	// Returns the filename and mode or throws an exception if the packet is invalid.
	public static Pair<TFTPRequestBuilder.OPCODE, String> unpackWRQorRRQ(byte[] packet, int offset) throws TFTPException {
		try {
			// Check opcode
			int op = unpackUint16(packet, offset);
			assert op == TFTPRequestBuilder.OPCODE.WRQ.getValue() || op == TFTPRequestBuilder.OPCODE.RRQ.getValue();

			// Check filename
			offset += 2;
			String filename = unpackString(packet, offset);

			// Check mode
			offset += filename.length() + 1;
			String mode = unpackString(packet, offset);

			return new Pair<>(TFTPRequestBuilder.OPCODE.values()[op], filename);
		} catch (Exception e) {
			throw new TFTPException("Invalid WRQ/RRQ packet");
		}
	}

	// Returns a string from the packet
	private static String unpackString(byte[] packet, int offset) throws TFTPException {
		int i = offset;
		while (packet[i] != 0) {
			i++;
		}
		return new String(packet, offset, i - offset);
	}


	// Returns 2 bytes from the packet
	public static int unpackUint16(byte[] packet, int offset) {
		int hi = packet[offset] & 0xFF;
		int lo = packet[offset + 1] & 0xFF;
		return (hi << 8) | lo;
	}
}
