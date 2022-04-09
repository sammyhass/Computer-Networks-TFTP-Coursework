package client;

import exceptions.TFTPException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TFTPClient implements IClient {
    // Socket for the client, that is used to send and receive data
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TFTPClient(Socket socket) throws TFTPException {
        this.socket = socket;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            throw new TFTPException("Error while creating input/output streams for the client");
        }
    }


    @Override
    public boolean sendFile(String filename) {
        return false;
    }

    @Override
    public boolean getFile(String filename) throws TFTPException {
        return false;
    }
}
