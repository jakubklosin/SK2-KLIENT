package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;

public class JoinGame {
    private JFrame frame;
    private JTextField roomNumberField;
    private JButton joinButton;
    private JTextField playerNameField;

    private NetworkConnection networkConnection;
    private String roomCode;
    private String playerName;
    public JoinGame(JFrame frame) {
        this.frame = frame;
        this.networkConnection = new NetworkConnection();
        initializeJoinGamePanel();
    }

    private void initializeJoinGamePanel() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(new Color(98, 180, 228));

        // Etykieta i pole tekstowe dla numeru pokoju
        JLabel roomNumberLabel = new JLabel("Wprowadź numer pokoju", SwingConstants.CENTER);
        roomNumberLabel.setFont(new Font("Arial", Font.BOLD, 20));
        roomNumberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(roomNumberLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        roomNumberField = new JTextField(10);
        roomNumberField.setMaximumSize(roomNumberField.getPreferredSize());
        roomNumberField.setHorizontalAlignment(JTextField.CENTER);
        roomNumberField.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomNumberField.setBackground(new Color(180, 235, 255));
        contentPanel.add(roomNumberField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Etykieta i pole tekstowe dla nazwy gracza
        JLabel playerNameLabel = new JLabel("Nazwa gracza", SwingConstants.CENTER);
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        playerNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(playerNameLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        playerNameField = new JTextField(10);
        playerNameField.setMaximumSize(playerNameField.getPreferredSize());
        playerNameField.setHorizontalAlignment(JTextField.CENTER);
        playerNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerNameField.setBackground(new Color(180, 235, 255));
        contentPanel.add(playerNameField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Przycisk dołączania
        joinButton = new JButton("Dołącz");
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.setMaximumSize(new Dimension(200, 50));
        joinButton.setFont(new Font("Arial", Font.BOLD, 20));
        joinButton.setBackground(new Color(180, 235, 255));
        joinButton.setOpaque(true);
        joinButton.setBorderPainted(false);
        joinButton.setFocusPainted(false);
        joinButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                joinButton.setBackground(new Color(100, 200, 255)); // Ciemniejszy kolor przy najechaniu
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                joinButton.setBackground(new Color(180, 235, 255)); // Powrót do oryginalnego koloru
            }
        });

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                roomCode = roomNumberField.getText(); // Pobranie kodu pokoju
                playerName = playerNameField.getText(); //Pobranie nazwy gracza

                if(playerName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Prosze podać nazwe gracza.");
                    return;
                }
                try {
                    JSONObject joinRequest = new JSONObject();
                    joinRequest.put("action", "join");
                    joinRequest.put("kod pokoju", roomNumberField.getText());
                    String jsonStr = joinRequest.toString();

                    // Wysyłanie żądania dołączenia do serwera
                    sendJoinRequest(jsonStr);

                    // Odbieranie odpowiedzi od serwera w nowym wątku
                    receiveServerResponse();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        contentPanel.add(joinButton);
        contentPanel.add(Box.createVerticalGlue());

        frame.add(contentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void sendJoinRequest(String jsonStr) {
        try {
            JSONObject joinRequest = new JSONObject(jsonStr);
            String playerName = playerNameField.getText().trim();
            if (!playerName.isEmpty()) {
                joinRequest.put("nickname", playerName);
            }

            // Wysyłanie żądania
            String updatedJsonStr = joinRequest.toString();
            int messageLength = updatedJsonStr.getBytes().length;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(messageLength);
            byte[] lengthBytes = buffer.array();

            networkConnection.send(lengthBytes);
            networkConnection.send(updatedJsonStr.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void receiveServerResponse() {
        new Thread(() -> {
            try {
                // Odbieranie długości odpowiedzi
                byte[] lengthBytes = networkConnection.receiveBytes(4);
                ByteBuffer wrapped = ByteBuffer.wrap(lengthBytes);
                int length = wrapped.getInt();

                // Odbieranie i przetwarzanie kompletnego JSON-a
                String responseStr = networkConnection.receiveCompleteJson();
                JSONObject response = new JSONObject(responseStr);
                processResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void processResponse(JSONObject response) {
        SwingUtilities.invokeLater(() -> {
            if (response.has("pytania")) {
                JSONArray questions = response.getJSONArray("pytania");

                // Rozpoczęcie nowej sesji quizu z pytaniami
                DataListener dataListener = new DataListener(networkConnection);
                // Teraz przekazujemy dataListener jako argument do konstruktora GameSession
                GameSession gameSession = new GameSession(frame, roomCode, playerName, networkConnection, dataListener);
                gameSession.setQuestionsList(questions);
            } else {
                JOptionPane.showMessageDialog(frame, "Błąd: " + response.toString());
            }
        });
    }

}