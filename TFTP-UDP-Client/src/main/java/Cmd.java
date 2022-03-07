import javafx.util.Pair;

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
			Pair<Command, String> command = parseCommand(scanner.nextLine());
			doCommand(command.getKey(), command.getValue());
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
			System.out.println("Invalid command");
			System.out.println(help());
			break;
		}
	}

	private void doUpload(String filename) {
		client.sendFile(filename);
	}

	private void doDownload(String filename) {
		client.receiveFile(filename);
	}

	public static String help() {
		return "Usage: \n" +
						"\tupload <filename> - uploads the file to the server\n" +
						"\tdownload <filename> - downloads the file from the server\n" +
						"\texit - exits the program\n" +
						"\thelp - prints this help message\n";
	}

	// Parses a command into a Command enum and a filename.
	public Pair<Command, String> parseCommand(String command) {
		String[] parts = command.split(" ");

		// Attempt to parse the command into a Command enum
		Command c;
		try {
			c = Command.valueOf(parts[0].toUpperCase().trim());
		} catch (Exception e) {
			return new Pair<>(Command.HELP, "");
		}

		// If the command is upload or download, we need to parse the filename
		if (c == Command.UPLOAD || c == Command.DOWNLOAD) {
			String filename = parts[1];
			return new Pair<>(c, filename);
		}
		return new Pair<>(c, "");
	}
}
