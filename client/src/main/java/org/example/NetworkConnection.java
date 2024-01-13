package org.example;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class NetworkConnection {
    private Socket socket;
    private OutputStream out;

    private InputStream in;
    private StringBuilder partialData = new StringBuilder();

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
            byte[] dataBytes = data.getBytes();
            byte[] length = ByteBuffer.allocate(4).putInt(dataBytes.length).array();

            out.write(length); // Najpierw wysyła długość wiadomości
            out.write(dataBytes); // Następnie wysyła samą wiadomośćą
            out.flush();
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

    public String receiveCompleteJson() throws IOException {
        while (true) {
            if (in.available() > 0) {
                byte[] buffer = new byte[1024]; // Bufor do odbierania danych
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    throw new IOException("End of stream reached");
                }
                partialData.append(new String(buffer, 0, bytesRead));

                if (isCompleteJson(partialData.toString())) {
                    String completeJson = partialData.toString();
                    partialData.setLength(0); // Reset bufora
                    return completeJson;
                }
            } else {
                try {
                    Thread.sleep(100); // Opóźnienie o 100 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Thread interrupted", e);
                }
            }
        }
    }


    private boolean isCompleteJson(String jsonStr) {
        try {
            new JSONObject(jsonStr); // Spróbuj przekształcić ciąg znaków na JSONObject
            return true; // Jeśli nie ma wyjątku, JSON jest kompletny
        } catch (Exception e) {
            return false; // Jeśli wystąpi wyjątek, JSON jest niekompletny
        }
    }

    public byte[] receiveBytes(int length) throws IOException {
        byte[] data = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int result = in.read(data, bytesRead, length - bytesRead);
            if (result == -1) {
                throw new IOException("End of stream reached");
            }
            bytesRead += result;
        }
        return data;
    }
}