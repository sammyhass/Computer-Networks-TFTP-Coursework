/*
Interface representing our TFTP client.
Our client should be able to send and receive files from a server.
Since we are only using octet mode, we will use bytes.
*/
public interface IClient {
	// Sends a file to the server.
	// @param filename - relative or absolute path to the file to send
	 boolean sendFile(String filename);

	// Receives a file from the server.
	// @return true if the file was received successfully, false otherwise
	 boolean getFile(String filename) throws TFTPException;
}
