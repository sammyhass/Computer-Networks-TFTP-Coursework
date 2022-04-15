package request;

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

    private void log(String msg, boolean isError) {
        String logMsg = TAG + " - " +  stringifyClientAddress() + ": " + msg;
        if (isError) {
            System.err.println(logMsg);
        } else {
            System.out.println(logMsg);
        }
    }

    private void log(String msg) {
        log(msg, false);
    }

    private void logReceievedRequest(
            OPCODE opcode, String filename
    ) {
        log(String.format("Received %s - Filename: %s", opcode, filename));
    }

    public void logRRQ(
            String filename
    ) {
        logReceievedRequest(OPCODE.RRQ, filename);
    }

    public void logWRQ(
            String filename
    ) {
        logReceievedRequest(OPCODE.WRQ, filename);
    }


    public void logDATAReceived(
            String filename, int blockNumber, int dataLength
    ) {
        log(String.format("Received DATA for %s - Block no %d of size %d bytes ", filename, blockNumber, dataLength));
    }

    public void logDATASent(
            String filename, int blockNumber, int dataLength, int totalBlocks
    ) {
        log(String.format("Sent DATA block %d/%d for %s - Block size: %d bytes", blockNumber,totalBlocks, filename, dataLength));
    }

    // Logs the end of the data transfer, isReceiving is true if the transfer was a write to the server
    public void logDATAEnd(String filename, boolean isReceiving) {
        log(String.format("%s all data from %s",
                isReceiving ? "Received" : "Sent",
                filename));
    }

    public void logFileSave(String path) {
        log(String.format("Saved file to %s", path));
    }

    public void logError(String errorMessage) {
        log(errorMessage, true);
    }

    public void logACK(int blockNumber, boolean didReceive) {
        log(String.format(
                "%s ACK Block %d", didReceive ? "Received" : "Sent", blockNumber
        ));
    }

}
