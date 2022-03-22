
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Build up data packets for a given file
// and save them to a file
public class DataPacketsBuilder {
	private static final int MAX_BYTES_PER_FILE = 33554432; // 32 MB limit on file size in TFTP

	private byte[] data;
	private int size = 0;
	private String filename;

	public DataPacketsBuilder() {
		data = new byte[MAX_BYTES_PER_FILE];
	}

	public void setFilename(String filename) {
		this.filename = filename;
		System.out.println("Filename: " + filename);
	}


	public void addDataPacket(TFTPRequestDecoder.DataPacket dataPacket) {
		System.arraycopy(dataPacket.data, 0, data, size, dataPacket.size);
		size += dataPacket.size;
	}

	public byte[] getData() {
		return data;
	}

	// Save the data packets to a file
	public boolean save() throws IOException {
		// Print out the content of the data packets
		System.out.println("saving");


		File file = new File(filename);

		FileOutputStream fos = new FileOutputStream(file);

		fos.write(data, 0, size);

		fos.flush();
		reset();

		return true;
	}

	private void reset() {
		size = 0;
		filename = null;
		data = new byte[MAX_BYTES_PER_FILE];
	}


}
