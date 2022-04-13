package client;

import exceptions.TFTPException;
import request.DataPacketsBuilder;
import request.OPCODE;
import request.TFTPRequestBuilder;
import request.TFTPRequestDecoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TFTPClient implements IClient {
    // Socket for the client, that is used to send and receive data
    private final Socket socket;

    private InputStream in;
    private OutputStream out;
    private DataPacketsBuilder dataPacketsBuilder;

    public TFTPClient(Socket socket) throws TFTPException {
        this.socket = socket;
        dataPacketsBuilder = new DataPacketsBuilder();
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            throw new TFTPException("Error while creating input/output streams for the client");
        }
    }



    public void resetStreams(){
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Error while creating input/output streams for the client");
        }
    }

    @Override
    public boolean sendFile(String filename) {
        dataPacketsBuilder.reset();

        try {

            // Read the file into a byte array.
            try {
                dataPacketsBuilder = DataPacketsBuilder.fromFile(filename);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return false;
            }

            // Provided our file exists, we can now send the WRQ packet.
            byte[] wrqPacket = new byte[512];
            TFTPRequestBuilder.packWRQ(wrqPacket, filename);
            out.write(wrqPacket);

            // Send the data packets to the server.
            // Split data packets into max size of 512 bytes.
            int numPackets = dataPacketsBuilder.getNumPackets(TFTPRequestBuilder.MAX_BYTES - TFTPRequestBuilder.HEADER_SIZE);

            byte[] data = dataPacketsBuilder.getData();

            for (int i = 1; i <= numPackets; i++) {
                byte[] buffer = new byte[TFTPRequestBuilder.MAX_BYTES];
                // Get the current packet (leaving room for the opcode and block number - 4 bytes total)
                int start = (i - 1) * (TFTPRequestBuilder.MAX_BYTES - 4);
                int end = Math.min(start + TFTPRequestBuilder.MAX_BYTES - 4, data.length);
                byte[] dataPacket = new byte[end - start];
                System.arraycopy(data, start, dataPacket, 0, end - start);
                // Send the packet to the server.
                TFTPRequestBuilder.packData(buffer, i, dataPacket);
                System.out.println("Sending packet " + i + " of " + numPackets);


                out.write(buffer);

            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean getFile(String filename) {
        dataPacketsBuilder.reset();
        dataPacketsBuilder.setFilename(filename);

        System.out.println("Getting file: " + filename);

        // Send the RRQ packet.
        byte[] rrqPacket = new byte[512];
        TFTPRequestBuilder.packRRQ(rrqPacket, filename);
        try {
            out.write(rrqPacket);
        } catch (IOException e) {
            System.err.println("Error while sending RRQ packet");
        }


        // At this point, if we receive an error packet, we should return false.
        // If we receive a data packet, we should write the data to a file.

        byte[] packet = new byte[TFTPRequestBuilder.MAX_BYTES];
        try {
            int read = in.read(packet);
        } catch (IOException e) {
            System.err.println("Error while reading packet");
        }

        // Check the opcode.
        OPCODE opcode = null;
        try {
            opcode = TFTPRequestDecoder.unpackOp(packet);
        } catch (TFTPException e) {
            System.err.println("Error while unpacking opcode");
            return false;
        }

        if (opcode == OPCODE.ERROR) {
            // Unpack the error code and message.
            TFTPRequestDecoder.ErrorPacket errorPacket = null;
            try {
                errorPacket = TFTPRequestDecoder.unpackError(packet, 0);
            } catch (TFTPException e) {
                System.err.println("Error while unpacking error code and message");
            }
            assert errorPacket != null;
            System.err.printf("%nError (%d): %s%n", errorPacket.errorCode, errorPacket.errorMessage);
            return false;
        }

        // Unpack the data packet.
        while (true) {
            TFTPRequestDecoder.DataPacket dataPacket = null;
            try {
                dataPacket = TFTPRequestDecoder.unpackData(packet, 0);

            } catch (TFTPException e) {
                System.err.println("Error while unpacking data packet");
                return false;
            }
            dataPacketsBuilder.addDataPacket(dataPacket);


            System.out.printf(
                    "Received DATA block %d of size %d bytes\n",
                    dataPacket.blockNumber,
                    dataPacket.size
            );

           if (dataPacket.size < TFTPRequestBuilder.MAX_BYTES - TFTPRequestBuilder.HEADER_SIZE) {
                break;
           }

            try {
                packet = new byte[TFTPRequestBuilder.MAX_BYTES];
                int read = in.read(packet);
            } catch (IOException e) {
                System.err.println("Error while reading packet");
                return false;
            }

        }

        try {
            dataPacketsBuilder.save();
        } catch (IOException e) {
            System.err.println("Error while saving file");
            return false;
        }

        return true;
    }

}
