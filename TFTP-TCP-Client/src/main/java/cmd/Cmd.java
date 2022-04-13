package cmd;

import client.TFTPClient;
import exceptions.TFTPException;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/* Implementation of our command line interface that will interact with our client class so the user may upload and retrieve files.
 */
public class Cmd {


    Scanner scanner;
    TFTPClient client;

    String serverAddr;
    int serverPort;

    public Cmd(String serverAddr, int serverPort) {
        this.scanner = new Scanner(System.in);
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
    }

    public void initializeClient() {
        // Initialize the socket
        Socket socket = null;
        try {
            socket = new Socket(serverAddr, serverPort);
        } catch (IOException e) {
            System.err.println("Unknown host: " + serverAddr);
        }
        assert socket != null;
        // Initialize the client
        try {
            client = new TFTPClient(socket);
        } catch (TFTPException e) {
            System.err.println(e.getMessage());
        }
    }

    public void prompt() {
        System.out.print("Enter command: ");
    }

    public void run() throws TFTPException {
        System.out.println("Welcome to the file transfer program!");
        System.out.println(help());
        while (true) {
            // Parse the command into a command and filename or print the help message if the command is invalid
            initializeClient();
            prompt();
            CmdPair cmdPair = parseCommand(scanner.nextLine());
            doCommand(cmdPair.getCmd(), cmdPair.getFilename());
        }
    }

    private void doCommand(Command command, String filename) throws TFTPException {
            switch (command) {
                case UPLOAD -> doUpload(filename);
                case DOWNLOAD -> doDownload(filename);
                case HELP -> System.out.println(help());
                case EXIT -> System.exit(0);
                default -> {
                    System.err.println("Invalid command");
                    System.out.println(help());
                }
            }
    }

    private void doUpload(String filename) {
       client.sendFile(filename);
    }

    private void doDownload(String filename) {
        client.getFile(filename);
    }

    public static String help() {
        return """
                Usage:\s
                \tupload <filename> - uploads the file to the server
                \tdownload <filename> - downloads the file from the server
                \texit - exits the program
                \thelp - prints this help message
                """;
    }

    // Parses a command into a cmd.Command enum and a filename.
    public CmdPair parseCommand(String command) {
        String[] parts = command.split(" ");

        // Attempt to parse the command into a cmd.Command enum
        Command c;
        try {
            c = Command.valueOf(parts[0].toUpperCase().trim());

            // If the command is upload or download, we need to parse the filename
            if (c == Command.UPLOAD || c == Command.DOWNLOAD) {
                assert parts.length == 2;
                String filename = parts[1];
                return new CmdPair(c, filename);
            }
        } catch (Exception e) {
            System.err.println("Invalid command");
            return new CmdPair(Command.HELP);
        }
        return new CmdPair(c);
    }
}
