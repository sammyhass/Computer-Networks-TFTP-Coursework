public class App {

	public static void main(String[] args) {
		Client client = new Client("localhost", 8888);
		Cmd cmd = new Cmd(client);
		try {
			cmd.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
