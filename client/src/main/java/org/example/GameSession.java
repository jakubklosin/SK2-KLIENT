package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private JFrame frame;
    private List<JSONObject> questionsList;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JButton selectedAnswerButton = null; //Aktualnie wybrana odpowiedz
    private String playerName;
    private int currentQuestionIndex = 0;
    private NetworkConnection networkConnection;
    public GameSession(JFrame frame, String roomCode, String playerName, NetworkConnection networkConnection, DataListener dataListener) {
        this.frame = frame;
        this.playerName = playerName;
        this.networkConnection = networkConnection;
        dataListener.setOnStatusUpdate(this::handleStatusUpdate);
        this.questionsList = new ArrayList<>();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        frame.add(mainPanel);
    }

    public void setQuestionsList(JSONArray questions) {
        questionsList.clear();
        for (int i = 0; i < questions.length(); i++) {
            questionsList.add(questions.getJSONObject(i));
        }

        currentQuestionIndex = 0;
        displayQuestion();
    }

    private void handleStatusUpdate(String status) {
        SwingUtilities.invokeLater(() -> {
            switch (status) {
                case "nextRound":
                    moveToNextQuestion();
                    break;
                case "nextRound5":
                    showTimeWarningAndMoveToNextQuestion();
                    break;
                case "end":
                    displayEndOfQuiz();
                    break;
                case "start":
                    displayQuestion();
                    break;
                default: System.out.println(status);}
        });
    }

    private void displayQuestion() {
        // Usunięcie wszystkich poprzednich komponentów
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout()); // Ustawienie BorderLayout dla ramki

        if (currentQuestionIndex < questionsList.size()) {
            // Pobranie bieżącego pytania na podstawie indeksu
            JSONObject question = questionsList.get(currentQuestionIndex);
            JPanel questionPanel = createQuestionPanel(question);

            // Dodanie panelu pytania do głównego kontenera
            frame.getContentPane().add(questionPanel, BorderLayout.CENTER);

            // Dodanie przycisku "Dalej"
            JButton nextButton = new JButton("Dalej");
            nextButton.addActionListener(e -> {
                if (selectedAnswerButton != null) {
                    // Podświetlenie wybranej odpowiedzi na zielono

                    int answerID = Integer.parseInt(selectedAnswerButton.getActionCommand());
                    sendAnswerToServer(answerID);
                    if(answerID == 0){
                        selectedAnswerButton.setBackground(Color.decode("#98FF98"));
                    }else {
                        selectedAnswerButton.setBackground(Color.decode("#D24545"));
                    }
                    selectedAnswerButton.setOpaque(true);
                    selectedAnswerButton.setBorderPainted(false);


                    // Opóźnienie przejścia do następnego pytania
                    Timer timer = new Timer(500, event -> {
                        // Reset wybranej odpowiedzi
                        selectedAnswerButton.setBackground(UIManager.getColor("Button.background"));
                        selectedAnswerButton.setOpaque(false);
                        selectedAnswerButton.setBorderPainted(true);
                        selectedAnswerButton = null;

                        currentQuestionIndex++; // Zwiększenie indeksu bieżącego pytania
                        if (currentQuestionIndex < questionsList.size()) {
                            displayQuestion(); // Wyświetlenie następnego pytania
                        } else {
                            displayEndOfQuiz(); // Wyświetlenie końca quizu
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    JOptionPane.showMessageDialog(frame, "Proszę wybrać odpowiedź przed przejściem dalej.");
                }
            });

            // Panel na przycisk "Dalej"
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(nextButton);
            frame.getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
        } else {
            displayEndOfQuiz(); // Wyświetlenie końca quizu
        }

        // Odświeżenie i przerysowanie ramki
        frame.revalidate();
        frame.repaint();
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionsList.size()) {
            displayQuestion(); // Wyświetlenie następnego pytania
        } else {
            displayEndOfQuiz(); // Wyświetlenie końca quizu
        }
    }

    // Metoda do wyświetlenia ostrzeżenia o czasie i przejścia do następnego pytania
    private void showTimeWarningAndMoveToNextQuestion() {
        JLabel timeWarningLabel = new JLabel("5 sekund");
        timeWarningLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timeWarningLabel.setHorizontalAlignment(JLabel.CENTER);
        frame.getContentPane().add(timeWarningLabel, BorderLayout.NORTH);

        Timer timer = new Timer(5000, e -> moveToNextQuestion());
        timer.setRepeats(false);
        timer.start();

        frame.revalidate();
        frame.repaint();
    }


    private void displayEndOfQuiz() {
        frame.getContentPane().removeAll();

        JLabel endLabel = new JLabel("Koniec quizu");
        endLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Zwiększenie czcionki
        endLabel.setHorizontalAlignment(JLabel.CENTER); // Wyśrodkowanie etykiety w poziomie
        endLabel.setVerticalAlignment(JLabel.CENTER); // Wyśrodkowanie etykiety w pionie

        JPanel endPanel = new JPanel();
        endPanel.setLayout(new GridBagLayout()); // Używamy GridBagLayout dla wyśrodkowania komponentu
        endPanel.add(endLabel);
        frame.getContentPane().add(endPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }


    private JPanel createQuestionPanel(JSONObject question) {
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Etykieta pytania
        JLabel questionLabel = new JLabel(question.getString("pytanie"));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionPanel.add(questionLabel);

        // Panel na przyciski odpowiedzi
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 10, 10));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSONArray answers = question.getJSONArray("odpowiedzi");

        for (int i = 0; i < answers.length(); i++) {
            JSONObject answerObj = answers.getJSONObject(i);
            String answerText = answerObj.getString("answerText");
            int answerID = answerObj.getInt("answerID");

            JButton answerButton = new JButton(answerText);
            answerButton.setActionCommand(String.valueOf(answerID)); // Przypisanie answerID jako action command
            answerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, answerButton.getMinimumSize().height));
            answerButton.addActionListener(e -> handleAnswer(answerButton, answerText, Integer.parseInt(answerButton.getActionCommand())));
            buttonsPanel.add(answerButton);
        }

        questionPanel.add(buttonsPanel);
        return questionPanel;
    }

    private void handleAnswer(JButton answerButton, String chosenAnswer, int answerID) {
        if (selectedAnswerButton != null) {
            selectedAnswerButton.setBackground(UIManager.getColor("Button.background"));
            selectedAnswerButton.setOpaque(false);
            selectedAnswerButton.setBorderPainted(true);
        }

        selectedAnswerButton = answerButton;
    }

    private void sendAnswerToServer(int answerID) {
        try {
            JSONObject answerJson = new JSONObject();
            answerJson.put("action","answering");
            answerJson.put("nickname", playerName);
            answerJson.put("numer pytania", currentQuestionIndex + 1);
            answerJson.put("answerID", answerID);

            String jsonStr = answerJson.toString();
            int messageLength = jsonStr.getBytes().length;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(messageLength);
            byte[] lengthBytes = buffer.array();

            // Wysyłanie długości wiadomości
            networkConnection.send(lengthBytes);
            // Wysyłanie samej wiadomości
            networkConnection.send(jsonStr.getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}