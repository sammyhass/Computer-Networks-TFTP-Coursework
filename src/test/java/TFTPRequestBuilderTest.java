import org.junit.Test;
import static org.junit.Assert.*;


public class TFTPRequestBuilderTest {
	@Test
	public void testPackRRQ() {
		final String filename = "test.txt";
		final int expectedFnameLen = filename.getBytes().length;
		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
		final TFTPRequestBuilder.OPCODE opcode = TFTPRequestBuilder.OPCODE.RRQ;

		int actual = TFTPRequestBuilder.packRRQ(buffer, filename);


		int offset = 0;

		// We expect our opcode to be in the first 2 bytes, since it is not going to take up all that space,
		// we just check the second byte and ensure it is equal to 1
		assertEquals(opcode.getValue(), buffer[++offset]);


		// Check the filename is correct
		for (int i = 0; i < expectedFnameLen; i++) {
			assertEquals(filename.getBytes()[i], buffer[++offset]);
		}

		// Check the 0 byte
		assertEquals(0, buffer[++offset]);

		// Check the mode is correct (octet)
		for (int i = 0; i < "octet".getBytes().length; i++) {
			assertEquals("octet".getBytes()[i], buffer[++offset]);
		}

		// Check the 0 byte
		assertEquals(0, buffer[++offset]);

		offset++; // Increment to the end of the buffer

		// Check the length is correct
		assertEquals(offset, actual);

	}


	@Test
	public void testPackData() {
		final int blockNum = 0x1234;
		final String testStr = "hello";
		byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];

		int actualLen = TFTPRequestBuilder.packData(buffer,  blockNum, testStr.getBytes());

		int offset = 0;

		// Check the opcode is correct
		assertEquals(TFTPRequestBuilder.OPCODE.DATA.getValue(), buffer[++offset]);

		// Check the block number is correct
		assertEquals(0x12, buffer[++offset]);
		assertEquals(0x34, buffer[++offset]);

		// Check the data is correct
		for (int i = 0; i < testStr.getBytes().length; i++) {
			assertEquals(testStr.getBytes()[i], buffer[++offset]);
		}
	}


	@Test
	public void testPackString() {
		final String testStr = "hello";
		final int expectedLen = testStr.getBytes().length;

		byte[] buffer = new byte[expectedLen];
		int actualLen = TFTPRequestBuilder.packString(buffer, 0, testStr);
		assertEquals(expectedLen, actualLen);


		// Test the string is actually packed
		for (int i = 0; i < expectedLen; i++) {
			assertEquals(testStr.getBytes()[i], buffer[i]);
		}
	}

	@Test
	public void testPackUInt16() {
		final int testInt = 0x1234;
		final int expectedLen = 2;

		byte[] buffer = new byte[expectedLen];
		int actualLen = TFTPRequestBuilder.packUInt16(buffer, 0, testInt);
		assertEquals(expectedLen, actualLen);
		// expect to find the correct value in the buffer
		assertEquals(0x12, buffer[0]);
		assertEquals(0x34, buffer[1]);
	}
}