package request;

/*
request.TFTPRequestBuilder builds a request with all the relevant headers
Here, we are only using octet mode as described in the coursework outline.
 */
public class TFTPRequestBuilder {
	public static int MAX_BYTES = 516;


	// RRQ/WRQ packet
	//
	//  2 bytes     string    1 byte    string    1 byte
	// --------------------------------------------------
	// | Opcode |  Filename  |   0  |    Mode    |   0  |
	// --------------------------------------------------
	// packRRQ returns the length of the packet and fills the buffer with the packet,
	// the packet is the same for both RRQ and WRQ, but WRQ also sends the data
	public static int packRRQ(byte[] buf, String filename) {
		return packRRQorWRQ(buf, OPCODE.RRQ, filename);
	}

	public static int packWRQ(byte[] buf, String filename) {
		return packRRQorWRQ(buf, OPCODE.WRQ, filename);
	}


	// The only difference between RRQ and WRQ is the opcode, so we can use a helper method
	private static int packRRQorWRQ(byte[] buf, OPCODE op, String filename) {
		int length = 0;

		length += packUInt16(buf, length, op.getValue());

		length += packString(buf, length, filename);
		buf[length++] = 0;
		length += packString(buf, length, "octet");
		buf[length++] = 0;

		return length;
	}

	// Error packet
	//
	//  2 bytes     2 bytes     string    1 byte
	// ----------------------------------------
	// | Opcode |  ErrorCode |   ErrMsg   |   0  |
	// ----------------------------------------
	// packError returns the length of the packet and fills the buffer with the packet
	public static int packError(byte[] buf, int errorCode, String errorMessage) {
		int length = 0;

		length += packUInt16(buf, length, OPCODE.ERROR.getValue());

		length += packUInt16(buf, length, errorCode);
		length += packString(buf, length, errorMessage);
		buf[length++] = 0;

		return length;
	}


	//  DATA packet
	//  2 bytes    2 bytes     n bytes
	// ----------------------------------
	// | Opcode |   Block #  |   Data   |
	// ----------------------------------
	// packData returns the length of the packet and fills the buffer with the packet
	// opcode = DATA
	public static int packData(byte[] buf, int block, byte[] data) {
		// ensure that the data is not longer than the maximum packet size
		assert data.length <= MAX_BYTES;
		int length = 0;
		length += packUInt16(buf, length, OPCODE.DATA.getValue());

		// block number is 2 bytes
		// append the block number to the buffer
		length += packUInt16(buf, length, block);

		// Get the data from the file and append it to the buffer

		System.arraycopy(data, 0, buf, length, data.length);
		length += data.length;

		return length;
	}

	// ACK packet
	//  2 bytes    2 bytes
	// ----------------------------------
	// | Opcode |   Block #  |   0  |
	// ----------------------------------
	// packAck returns the length of the packet and fills the buffer with the packet
	public static int packAck(byte[] buf, int block) {
		int length = 0;
		length += packUInt16(buf, length, OPCODE.ACK.getValue());
		length += packUInt16(buf, length, block);
		length += packUInt16(buf, length, 0);
		return length;
	}

	/*
	  packInt16 packs an integer into a 2-byte array and appends it
	  to the buffer at the given offset
	 */
	public static int packUInt16(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value >> 8);
		buf[offset + 1] = (byte) (value);
		return 2;
	}


	/*
	  packString returns the length of the packet and appends the string to the buffer
		at the given offset
	 */
	public static int packString(byte[] buf, int offset, String str) {
		byte[] bytes = str.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			buf[offset + i] = bytes[i];
		}
		return bytes.length;
	}
}
