import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DataPacketsBuilder {
	private static final int MAX_NUM_OF_PACKETS = 1024;

	private byte[] data;
	private int size = 0;
	private String filename;

	public DataPacketsBuilder() {
		data = new byte[MAX_NUM_OF_PACKETS];
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	public void addDataPacket(TFTPRequestDecoder.DataPacket dataPacket) {
		for (int i = 0; i < dataPacket.size; i++) {
			data[size++] = dataPacket.data[i];
		}
	}

	public byte[] getData() {
		return data;
	}

	// Save the data packets to a file
	public boolean save() throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(filename));
		for (int i = 0; i < size; i++) {
			if (data[i] == 0) {
				break;
			}
			fos.write(data[i]);
		}

		fos.close();
		reset();

		return true;
	}

	private void reset() {
		size = 0;
		filename = null;
		data = new byte[MAX_NUM_OF_PACKETS];
	}


}
