package request;

// Opcodes for the request
public enum OPCODE {
	NOOP(0), RRQ(1), WRQ(2), DATA(3), ACK(4), ERROR(5);
	private final int value;

	OPCODE(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
