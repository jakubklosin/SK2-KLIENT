package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DataListener {
    private NetworkConnection networkConnection;
    private Thread listeningThread;
    private boolean isRunning;
    private Consumer<Map<String, Integer>> onScoreUpdate;
    private Consumer<List<String>> onUserJoin;

    public DataListener(NetworkConnection networkConnection) {
        this.networkConnection = networkConnection;
        this.isRunning = true;
        this.listeningThread = new Thread(this::run);
        this.listeningThread.start();
    }

    public void setOnUserJoin(Consumer<List<String>> onUserJoin) {
        this.onUserJoin = onUserJoin;
    }

    private void run() {
        while (isRunning) {
            try {
                byte[] lengthBytes = networkConnection.receiveBytes(4);
                ByteBuffer wrapped = ByteBuffer.wrap(lengthBytes);
                int length = wrapped.getInt();

                String jsonStr = networkConnection.receiveCompleteJson();
                JSONObject jsonObject = new JSONObject(jsonStr);

                if (jsonObject.has("usersJoined")) {
                    JSONArray usersArray = jsonObject.getJSONArray("usersJoined");
                    System.out.println(usersArray);
                    List<String> userNames = new ArrayList<>();
                    for (int i = 0; i < usersArray.length(); i++) {
                        userNames.add(usersArray.getString(i));
                    }
                    if (onUserJoin != null) {
                        onUserJoin.accept(userNames);
                    }
                }

                if (jsonObject.has("users")) {
                    JSONArray usersArray = jsonObject.getJSONArray("users");
                    System.out.println(usersArray);
                    Map<String, Integer> userScores = new HashMap<>();
                    for (int i = 0; i < usersArray.length(); i++) {
                        JSONObject userObject = usersArray.getJSONObject(i);
                        String userName = userObject.getString("user");
                        int score = userObject.getInt("score");
                        userScores.put(userName, score);
                    }
                    if (onScoreUpdate != null) {
                        onScoreUpdate.accept(userScores);
                    }
                }
                // Dodaj obsługę innych przypadków, jeśli jest to potrzebne
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnScoreUpdate(Consumer<Map<String, Integer>> onScoreUpdate) {
        this.onScoreUpdate = onScoreUpdate;
    }

    public void stopListening() {
        isRunning = false;
        listeningThread.interrupt();
    }
}
