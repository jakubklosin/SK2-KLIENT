package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HostGameView {
    private JFrame frame;
    private String roomCode;
    private int currentQuestionIndex;
    private String currentQuestionText;
    private List<String> questions;
    private Map<String, Integer> playerScores;
    private Map<String, JProgressBar> playerProgressBars;
    private NetworkConnection networkConnection;
    private JLabel currentQuestionLabel;
    private JPanel scorePanel;

    public HostGameView(JFrame frame, String roomCode, NetworkConnection networkConnection, List<String> questions, DataListener dataListener) {
        this.frame = frame;
        this.roomCode = roomCode;
        this.questions = questions;
        this.currentQuestionIndex = 0;
        this.currentQuestionText = "";
        this.playerScores = new HashMap<>();
        this.playerProgressBars = new HashMap<>();
        this.networkConnection = networkConnection;

        // Ustawienie metody callback w DataListener
        dataListener.setOnScoreUpdate(this::updateScores);

        initializeHostGameView();
    }

    private void initializeHostGameView() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.setBackground(new Color(98, 180, 228));

        JLabel roomCodeLabel = new JLabel("Numer pokoju: " + roomCode);
        roomCodeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roomCodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(roomCodeLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        currentQuestionLabel = new JLabel("Użytkownicy odpowiadają na pytania");
        currentQuestionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        currentQuestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(currentQuestionLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scorePanel.setBackground(new Color(98, 180, 228));
        frame.add(scorePanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();

    }


    public void updateScores(Map<String, Integer> newScores) {
        SwingUtilities.invokeLater(() -> {
            newScores.forEach((name, score) -> {
                playerScores.put(name, score);
                if (playerProgressBars.containsKey(name)) {
                    playerProgressBars.get(name).setValue(score);
                } else {
                    addNewPlayer(name, score);
                }
            });
            refreshScoreboard();
        });
    }

    private void addNewPlayer(String playerName, int score) {
        playerScores.put(playerName, score);
        JProgressBar progressBar = new JProgressBar(0, 50); // Zakładając, że maksymalna liczba punktów to 50
        progressBar.setValue(score);
        progressBar.setStringPainted(true);
        playerProgressBars.put(playerName, progressBar);

        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.X_AXIS));
        playerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        playerPanel.setBackground(new Color(98, 180, 228));

        JLabel nameLabel = new JLabel(playerName + ": ");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        playerPanel.add(nameLabel);
        playerPanel.add(progressBar);
        scorePanel.add(playerPanel);
    }

    private void refreshScoreboard() {
        scorePanel.revalidate();
        scorePanel.repaint();
    }

}