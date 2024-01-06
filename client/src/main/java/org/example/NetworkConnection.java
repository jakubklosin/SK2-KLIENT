package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkConnection {
    private Socket socket;
    private OutputStream out;

    public NetworkConnection() {
        setupNetwork();
    }

    private void setupNetwork() {
        try {
            socket = new Socket("localhost", 5555);
            out = socket.getOutputStream();
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

}
