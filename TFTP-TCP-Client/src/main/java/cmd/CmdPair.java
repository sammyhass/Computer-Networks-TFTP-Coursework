package cmd;

// Pair consisting of a command and its arguments
public class CmdPair {
	private final Command cmd;
	private final String filename;

	public CmdPair(Command cmd, String filename) {
		this.cmd = cmd;
		this.filename = filename;
	}


	public CmdPair(Command cmd) {
		this.cmd = cmd;
		this.filename = null;
	}

	public Command getCmd() {
		return cmd;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public String toString() {
		return cmd.toString() + " " + filename;
	}
}
