public class Server {
	public static void main(String[] args) throws Exception {
		TFTPServerThread server = new TFTPServerThread(8888);
		server.run();
	}

}
