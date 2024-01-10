package org.example;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HostGameView {
    private JFrame frame;
    private String roomCode;
    private int currentQuestionIndex;
    private String currentQuestionText;
    private Map<String, Integer> playerScores;
    private Map<String, JProgressBar> playerProgressBars;
    private NetworkConnection networkConnection;

    public HostGameView(JFrame frame, String roomCode, NetworkConnection networkConnection) {
        this.frame = frame;
        this.roomCode = roomCode;
        this.currentQuestionIndex = 1; // Przykładowy indeks pytania
        this.currentQuestionText = "Jaka jest stolica Francji?"; // Przykładowa treść pytania
        this.playerScores = new HashMap<>(); // Przykładowa lista graczy i ich punktów
        this.playerProgressBars = new HashMap<>();
        this.networkConnection = networkConnection;
        listenForScoreUpdates();

        // Przykładowe dane
        playerScores.put("Gracz1", 10);
        playerScores.put("Gracz2", 20);
        playerScores.put("Gracz3", 10);
        playerScores.put("Gracz4", 45);

        initializeHostGameView();
    }

    private void initializeHostGameView() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Panel górny dla numeru pokoju i pytania
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roomCodeLabel = new JLabel("Numer pokoju: " + roomCode);
        roomCodeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roomCodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(roomCodeLabel);

        topPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Zwiększony odstęp

        JLabel currentQuestionLabel = new JLabel("<html><div style='text-align: center;'>Pytanie " + currentQuestionIndex + ": " + currentQuestionText + "</div></html>");
        currentQuestionLabel.setFont(new Font("Arial", Font.BOLD, 30));
        currentQuestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(currentQuestionLabel);

        frame.add(topPanel, BorderLayout.NORTH);

        topPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Zwiększony odstęp

        // Panel dla scoreboardu
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Wyrównanie do lewej

        // Tworzenie pasków postępu dla graczy
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.X_AXIS));
            playerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nameLabel = new JLabel(entry.getKey() + ": ");
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JProgressBar scoreBar = new JProgressBar(0, 50); // zakładając, że maksymalna liczba punktów to 50
            scoreBar.setValue(entry.getValue());
            scoreBar.setStringPainted(true);
            scoreBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            scoreBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

            JLabel scoreLabel = new JLabel(entry.getValue() + " pkt");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
            scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            playerPanel.add(nameLabel);
            playerPanel.add(scoreBar);
            playerPanel.add(Box.createRigidArea(new Dimension(6, 0))); // Odstęp między paskiem a liczbą punktów
            playerPanel.add(scoreLabel);

            playerProgressBars.put(entry.getKey(), scoreBar);

            scorePanel.add(playerPanel);
            scorePanel.add(Box.createRigidArea(new Dimension(0, 10))); // Dodanie przestrzeni pomiędzy elementami
        }

        frame.add(scorePanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }

    public void updatePlayerScore(String playerName, int score) {
        // Ta metoda powinna być wywoływana, kiedy otrzymasz aktualizację punktów z serwera
        if (playerProgressBars.containsKey(playerName)) {
            JProgressBar progressBar = playerProgressBars.get(playerName);
            progressBar.setValue(score);
            // Zaktualizuj także etykietę z punktami
            Component[] components = progressBar.getParent().getComponents();
            for (Component component : components) {
                if (component instanceof JLabel && ((JLabel)component).getText().contains(playerName)) {
                    ((JLabel)component).setText(playerName + ": " + score + " pkt");
                }
            }
        }
    }

    // Metoda do nasłuchiwania na aktualizacje punktacji
    //Przepraszam za aktywne czekanie :((( chociaz z thread.sleep() :)
    private void listenForScoreUpdates() {
        new Thread(() -> {
            while (true) { // Używamy nieskończonej pętli do ciągłego nasłuchiwania
                try {
                    String scoreUpdateJsonStr = networkConnection.receiveCompleteJson();
                    JSONObject scoreUpdate = new JSONObject(scoreUpdateJsonStr);
                    updateScores(scoreUpdate);
                } catch (IOException e) {
                    e.printStackTrace();
                    break; // W przypadku błędu zakończ wątek
                }
            }
        }).start();
    }

    // Metoda do aktualizacji punktacji w interfejsie użytkownika
    private void updateScores(JSONObject scoreUpdate) {
        SwingUtilities.invokeLater(() -> {
            // Tutaj aktualizujemy interfejs użytkownika na podstawie otrzymanego JSON
            for (String key : scoreUpdate.keySet()) {
                playerScores.put(key, scoreUpdate.getInt(key));
            }
            refreshScoreboard();
        });
    }

    // Metoda do odświeżania panelu z punktami
    private void refreshScoreboard() {
        // Tu zaimplementuj logikę do odświeżania panelu z punktami
    }


}
