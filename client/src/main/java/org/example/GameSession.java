package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private JFrame frame;
    private NetworkConnection networkConnection;
    private List<JSONObject> questionsList;
    private int currentQuestionIndex = 0;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public GameSession(JFrame frame, NetworkConnection networkConnection) {
        this.frame = frame;
        this.networkConnection = networkConnection;
        this.questionsList = new ArrayList<>();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        frame.add(mainPanel);
    }

    public void startSession() {
        // Oczekiwanie na pytania od serwera
        new Thread(() -> {
            try {
                byte[] lengthBytes = networkConnection.receive(4);
                ByteBuffer wrapped = ByteBuffer.wrap(lengthBytes);
                int length = wrapped.getInt();

                byte[] messageBytes = networkConnection.receive(length);
                String messageStr = new String(messageBytes);
                JSONObject response = new JSONObject(messageStr);

                // Sprawdzenie, czy odpowiedź zawiera pytania
                if (response.has("questions")) {
                    JSONArray questions = response.getJSONArray("questions");
                    for (int i = 0; i < questions.length(); i++) {
                        questionsList.add(questions.getJSONObject(i));
                    }

                    // Wyświetlanie pytań
                    SwingUtilities.invokeLater(this::displayQuestions);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayQuestions() {
        if (questionsList.isEmpty()) {
            return; // Brak pytań do wyświetlenia
        }

        // Tworzenie widoków dla każdego pytania
        for (int i = 0; i < questionsList.size(); i++) {
            mainPanel.add(createQuestionPanel(questionsList.get(i), i), "QuestionCard" + i);
        }
        cardLayout.show(mainPanel, "QuestionCard0");
        frame.revalidate();
    }

    private JPanel createQuestionPanel(JSONObject question, int questionIndex) {
        // Tutaj logika tworzenia panelu dla każdego pytania
        // Przykładowa implementacja:
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));

        // Dodaj elementy do questionPanel na podstawie obiektu question

        return questionPanel;
    }

    // Metody do przełączania pytań, obsługi odpowiedzi itd.
    // ...
}
