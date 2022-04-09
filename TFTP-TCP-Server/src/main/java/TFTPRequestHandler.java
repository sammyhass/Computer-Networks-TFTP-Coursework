import exceptions.TFTPException;
import request.OPCODE;
import request.TFTPRequestDecoder;

import java.io.*;
import java.net.Socket;

// Handles TCP TFTP requests sent by the client
public class TFTPRequestHandler extends Thread {
    private RequestHandlerLogger logger;
    private Socket clientSocket;
    private OutputStream out;
    private InputStream in;

    public TFTPRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        logger = new RequestHandlerLogger(clientSocket.getInetAddress(), clientSocket.getPort());
        try {
            out = clientSocket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.logConnected();
    }
    public void run() {
        try {
            in = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[1024];
        // read the request as bytes
        try {
            int read = in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle(byte[] request) throws TFTPException {
        // Work out the opcode
        OPCODE opcode;

        opcode = TFTPRequestDecoder.unpackOp(request);
        assert opcode != null;

        // Handle the request
        try {
            switch (opcode) {
                case RRQ -> handleRRQ(request);
                case WRQ -> handleWRQ(request);

            }
        } catch (Exception e) {
            if (e instanceof TFTPException) {
                throw (TFTPException) e;
            } else {
                throw new TFTPException(e.getMessage());
            }
        }
    }

    private void handleRRQ(byte[] request) throws Exception {
        out.write(request);
    }

    private void handleWRQ(byte[] request) throws Exception {
        out.write(request);
    }

    private void closeStreams() {
        try {
        in.close();
        out.close();
        clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
