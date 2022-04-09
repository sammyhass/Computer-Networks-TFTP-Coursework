import request.OPCODE;

import java.net.InetAddress;

// Logger for TFTP Server
public class RequestHandlerLogger {
    private static final String TAG = "TFTP_REQUEST_HANDLER";

    private final InetAddress clientAddress;
    private final int clientPort;

    // Constructor
    public RequestHandlerLogger(InetAddress clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort  = clientPort;
    }

    private String stringifyClientAddress() {
        return clientAddress.getHostAddress() + ":" + clientPort;
    }

    private void log(String msg) {
        System.out.println(TAG + " - " +  stringifyClientAddress() + ": " + msg );
    }

    public void logConnected() {
        log("Connected");
    }

    private void logReceievedRequest(
            OPCODE opcode, String filename, String mode
    ) {
        log(String.format("Received request: %s %s %s", opcode, filename, mode));
    }

    public void logRRQ(
            String filename, String mode
    ) {
        logReceievedRequest(OPCODE.RRQ, filename, mode);
    }

    public void logWRQ(
            String filename, String mode
    ) {
        logReceievedRequest(OPCODE.WRQ, filename, mode);
    }

    public void logDATA(
            int blockNumber, byte[] data
    ) {
        log(String.format("Received DATA: %d %s", blockNumber, data));
    }

}
