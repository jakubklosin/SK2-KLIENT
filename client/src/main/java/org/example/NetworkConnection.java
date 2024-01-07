package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkConnection {
    private Socket socket;
    private OutputStream out;

    private InputStream in;

    public NetworkConnection() {
        setupNetwork();
    }

    private void setupNetwork() {
        try {
            socket = new Socket("localhost", 5555);
            out = socket.getOutputStream();
            in = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String data) {
        try {
            send(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] data) {
        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive(int length) throws IOException {
        byte[] data = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int result = in.read(data, bytesRead, length - bytesRead);
            if (result == -1) {
                throw new IOException("End of stream reached");
            }
            bytesRead += result;
        }
        String receivedData = new String(data);
        System.out.println("Odebrano dane: " + receivedData);
        return data;
    }
}
