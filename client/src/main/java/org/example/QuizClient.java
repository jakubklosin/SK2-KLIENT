package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuizClient {
    private String roomCode;
    private List<JSONObject> questionsAndAnswersList;
    private List<String> questions;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JFrame frame;
    private HostGameView hostGameView;
    private NetworkConnection networkConnection;

    public QuizClient(JFrame frame) {
        this.frame = frame;
        this.questionsAndAnswersList = new ArrayList<>();
        this.questions = new ArrayList<>(); // Inicjalizacja listy pytań
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        this.networkConnection = new NetworkConnection();
        initializeQuiz(frame);
        listenForPlayerScores();
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
        questionPanel.setBackground(new Color(98, 180, 228));
        JTextField questionField = new JTextField();
        JLabel pom = new JLabel("Podaj poprawną odpowiedź jako pierwszą.", SwingConstants.CENTER);
        pom.setFont(new Font("ARIAL", Font.BOLD, 14));
        questionPanel.add(pom);

        questionPanel.add(questionField);
        questionPanel.add(new JLabel("Pytanie:", SwingConstants.CENTER));
        questionField.setBackground(new Color(180, 235, 255));
        questionPanel.add(questionField);

        JTextField[] answerFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            answerFields[i] = new JTextField();
            if(i==0){
                answerFields[i].setBackground(Color.decode("#98FF98"));
                questionPanel.add(new JLabel("Odpowiedź " + (i + 1) + ":", SwingConstants.CENTER));
                questionPanel.add(answerFields[i]);
                continue;
            }
            questionPanel.add(new JLabel("Odpowiedź " + (i + 1) + ":", SwingConstants.CENTER));
            answerFields[i].setBackground(new Color(180, 235, 255));
            questionPanel.add(answerFields[i]);
        }

        String buttonText = questionIndex == 4 ? "Zakończ" : "Następne pytanie";
        JButton actionButton = new JButton(buttonText);
        actionButton.addActionListener(e -> {
            JSONObject questionJson = new JSONObject();
            JSONArray answersJson = new JSONArray();
            questions.add(questionField.getText());
            for (int i = 0; i < answerFields.length; i++) {
                JSONObject answerJson = new JSONObject();
                answerJson.put("answerText", answerFields[i].getText());
                answerJson.put("answerID", i); // Dodanie identyfikatora odpowiedzi
                answersJson.put(answerJson);
            }

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

    private void updateHostGameView(Map<String, Integer> userScores) {
        if (hostGameView != null) {
            SwingUtilities.invokeLater(() -> {
                hostGameView.updateScores(userScores);
            });
        }
    }

    private void listenForPlayerScores() {
        new Thread(() -> {
            while(true) {
                try {
                    // Odbieranie długości odpowiedzi
                    byte[] lengthBytes = networkConnection.receiveBytes(4);
                    ByteBuffer wrapped = ByteBuffer.wrap(lengthBytes);
                    int length = wrapped.getInt();

                    // Odbieranie i przetwarzanie kompletnego JSON-a
                    String jsonStr = networkConnection.receiveCompleteJson();
                    JSONObject jsonObject = new JSONObject(jsonStr);

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

                        // Aktualizacja HostGameView
                        if (hostGameView != null) {
                            hostGameView.updateHostGameView(userScores);
                        }
                    } else if (jsonObject.has("status")) {
                        System.out.println("Pokoj utworzony poprawnie");
                    } else {
                        System.out.println("Nieznany klucz w JSONie");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void showRoomCodeView() {
        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(98, 180, 228));

        JLabel codeLabel = new JLabel("Kod twojego pokoju: " + roomCode, SwingConstants.CENTER);
        codeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(codeLabel, BorderLayout.CENTER);

        JButton startGameButton = new JButton("Rozpocznij grę");
        startGameButton.setFont(new Font("Arial", Font.BOLD, 20));
        startGameButton.setBackground(new Color(180, 235, 255));
        startGameButton.setOpaque(true);
        startGameButton.setBorderPainted(false);

        // Dodanie efektu hover
        startGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startGameButton.setBackground(new Color(100, 200, 255)); // Ciemniejszy kolor przy najechaniu
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startGameButton.setBackground(new Color(180, 235, 255)); // Powrót do oryginalnego koloru
            }
        });

        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONObject json = new JSONObject();
                json.put("status", "beginGame");
                json.put("action", "controls");

                networkConnection.send(json.toString());
                hostGameView = new HostGameView(frame, roomCode, networkConnection, questions);
            }
        });

        mainPanel.add(startGameButton, BorderLayout.SOUTH);

        frame.revalidate();
        frame.repaint();
    }

}