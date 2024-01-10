package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Random;
import java.nio.ByteBuffer;


public class QuizClient {
    private String roomCode;
    private List<JSONObject> questionsAndAnswersList;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JFrame frame;
    private NetworkConnection networkConnection;

    public QuizClient(JFrame frame) {
        this.frame = frame;
        this.questionsAndAnswersList = new ArrayList<>();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        this.networkConnection = new NetworkConnection();
        initializeQuiz(frame);
    }

    void initializeQuiz(JFrame frame) {
        for (int i = 0; i < 5; i++) {
            mainPanel.add(createQuestionPanel(i), "QuestionCard" + i);
        }
        this.frame.add(mainPanel);
        cardLayout.show(mainPanel, "QuestionCard0");
    }

    private JPanel createQuestionPanel(int questionIndex) {
        JPanel questionPanel = new JPanel(new GridLayout(0, 1));
        JTextField questionField = new JTextField();
        questionPanel.add(new JLabel("Podaj poprawną odpowiedź jako pierwszą.", SwingConstants.CENTER));
        questionPanel.add(questionField);
        questionPanel.add(new JLabel("Pytanie:", SwingConstants.CENTER));
        questionPanel.add(questionField);

        JTextField[] answerFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            answerFields[i] = new JTextField();
            if(i==0){
                answerFields[i].setBackground(Color.decode("#98FF98"));
            }
            questionPanel.add(new JLabel("Odpowiedź " + (i + 1) + ":", SwingConstants.CENTER));
            questionPanel.add(answerFields[i]);
        }

        String buttonText = questionIndex == 4 ? "Zakończ" : "Następne pytanie";
        JButton actionButton = new JButton(buttonText);
        actionButton.addActionListener(e -> {
            JSONObject questionJson = new JSONObject();
            JSONArray answersJson = new JSONArray();
            for (int i = 0; i < answerFields.length; i++) {
                JSONObject answerJson = new JSONObject();
                answerJson.put("answerText", answerFields[i].getText());
                answerJson.put("answerID", i); // Dodanie identyfikatora odpowiedzi
                answersJson.put(answerJson);
            }
//            for (JTextField answerField : answerFields) {
//                answersJson.put(answerField.getText());
//            }
            questionJson.put("pytanie", questionField.getText());
            questionJson.put("odpowiedzi", answersJson);
            questionsAndAnswersList.add(questionJson);

            if (questionIndex == 4) {
                sendToServer(); // Wysyłanie danych do serwera
            } else {
                cardLayout.show(mainPanel, "QuestionCard" + (questionIndex + 1));
            }
        });
        questionPanel.add(actionButton);

        return questionPanel;
    }


    private void sendToServer() {
        try {
            roomCode = generateRoomCode();
            JSONObject quizJson = new JSONObject();
            quizJson.put("action", "create");
            quizJson.put("pytania", new JSONArray(questionsAndAnswersList));
            quizJson.put("kod pokoju", roomCode);

            String jsonStr = quizJson.toString();

            // Przygotowanie długości wiadomości
            int messageLength = jsonStr.getBytes().length;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(messageLength);
            byte[] lengthBytes = buffer.array();

            // Wysyłanie długości wiadomości
            networkConnection.send(lengthBytes);
            // Wysyłanie samej wiadomości
            networkConnection.send(jsonStr.getBytes());

            showRoomCodeView();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String generateRoomCode() {
        return String.format("%03d", new Random().nextInt(1000));
    }

    private void showRoomCodeView() {
        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout());
        JLabel codeLabel = new JLabel("Kod twojego pokoju: " + roomCode, SwingConstants.CENTER);
        mainPanel.add(codeLabel, BorderLayout.CENTER);
        JButton startGameButton = new JButton("Rozpocznij grę");
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obsłuż rozpoczęcie gry
            }
        });
        mainPanel.add(startGameButton, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
    }
}