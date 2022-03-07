import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TFTPRequestDecoderTest {
	@Test
	public void testUnpackUInt16() {
		int expected = 0x1234;
		byte[] bytes = new byte[] { (byte) 0x12, (byte) 0x34 };
		int actual = TFTPRequestDecoder.unpackUint16(bytes, 0);
		assertEquals(expected, actual);
	}

	@Test
	public void testUnpackACK() throws TFTPException {
		int expected = 0x1234;
		byte[] bytes = new byte[] { (byte) 0x00, (byte) 0x04, (byte) 0x12, (byte) 0x34 };
		int actual = TFTPRequestDecoder.unpackACK(bytes);
		assertEquals(expected, actual);
	}

	@Test
	public void testUnpackACKFailure() {
		byte[] bytes = new byte[] { (byte) 0x00, (byte) 0x04 };
		// assert that an exception is thrown
		try {
			TFTPRequestDecoder.unpackACK(bytes);
			TestCase.fail("Expected exception not thrown");
		} catch (TFTPException e) {
			// expected
		}
	}


}