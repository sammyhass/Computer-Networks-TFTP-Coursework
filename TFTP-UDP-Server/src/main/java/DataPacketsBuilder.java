
import java.io.File;
import java.io.FileInputStream;
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

	public void setData(byte[] data) {
		this.data = data;
		this.size = data.length;
	}

	public void addDataPacket(TFTPRequestDecoder.DataPacket dataPacket) {
		System.arraycopy(dataPacket.data, 0, data, size, dataPacket.size);
		size += dataPacket.size;
	}

	public byte[] getData() {
		return data;
	}

	public byte getData(int index) {
		return data[index];
	}

	public int getSize() {
		return size;
	}

	public String getFilename() {
		return filename;
	}

	// Save the data packets to a file
	public boolean save() throws IOException {

		// Save the file into the resources folder of the project
		// Get the resources folder
		String path = new java.io.File(".").getCanonicalPath() + "/src/main/resources/" + filename;

		// Create the file
		File file = new File(path);

		System.out.println("saving");


		FileOutputStream fos = new FileOutputStream(file);

		fos.write(data, 0, size);

		fos.flush();
		reset();

		return true;
	}

	public void reset() {
		size = 0;
		filename = null;
		data = new byte[MAX_BYTES_PER_FILE];
	}


	// Calculate the number of data packets needed to send the file given
	// a packet size in bytes
	public int getNumPackets(int packetSize) {
		int ret =  (int) Math.ceil((double) size / packetSize);
		return ret;

	}
}
