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

	// Returns 2 bytes from the packet
	public static int unpackUint16(byte[] packet, int offset) {
		int hi = packet[offset] & 0xFF;
		int lo = packet[offset + 1] & 0xFF;
		return (hi << 8) | lo;
	}
}
