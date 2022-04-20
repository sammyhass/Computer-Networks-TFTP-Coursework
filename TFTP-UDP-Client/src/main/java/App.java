public class App {

	public static void main(String[] args) {
		TFTPClient client = new TFTPClient("localhost", 8888);
		Cmd cmd = new Cmd(client);
		try {
			cmd.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
