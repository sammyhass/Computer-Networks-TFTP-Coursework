
import java.util.Scanner;

/* Implementation of our command line interface that will interact with our client class so the user may upload and retrieve files.
 */
public class Cmd {


	Scanner scanner;
	Client client;

	public Cmd(Client client) {
		this.client = client;
		this.scanner = new Scanner(System.in);
	}

	public void run() {
		System.out.println("Welcome to the file transfer program!");
		System.out.println(help());
		while (true) {
			// Parse the command into a command and filename or print the help message if the command is invalid
			System.out.print("Enter command: ");
			CmdPair cmdPair = parseCommand(scanner.nextLine());
			doCommand(cmdPair.getCmd(), cmdPair.getFilename());
		}
	}

	private void doCommand(Command command, String filename) {
		switch (command) {
		case UPLOAD:
			doUpload(filename);
			break;
		case DOWNLOAD:
			doDownload(filename);
			break;
		case HELP:
			System.out.println(help());
			break;
		case EXIT:
			System.exit(0);
			break;
		default:
			System.err.println("Invalid command");
			System.out.println(help());
			break;
		}
	}

	private void doUpload(String filename) {
		client.sendFile(filename);
	}

	private void doDownload(String filename) {
		client.getFile(filename);
	}

	public static String help() {
		return "Usage: \n" +
						"\tupload <filename> - uploads the file to the server\n" +
						"\tdownload <filename> - downloads the file from the server\n" +
						"\texit - exits the program\n" +
						"\thelp - prints this help message\n";
	}

	// Parses a command into a Command enum and a filename.
	public CmdPair parseCommand(String command) {
		String[] parts = command.split(" ");

		// Attempt to parse the command into a Command enum
		Command c;
		try {
			c = Command.valueOf(parts[0].toUpperCase().trim());
		} catch (Exception e) {
			return new CmdPair(Command.HELP);
		}

		// If the command is upload or download, we need to parse the filename
		if (c == Command.UPLOAD || c == Command.DOWNLOAD) {
			String filename = parts[1];
			return new CmdPair(c, filename);
		}
		return new CmdPair(c);
	}
}
