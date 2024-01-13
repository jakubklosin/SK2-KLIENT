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
    private JButton selectedAnswerButton = null;
    private String roomCode;
    private String playerName;
    private int currentQuestionIndex = 0;
    private int userScore = 0;
    private NetworkConnection networkConnection;
    private DataListener dataListener;
    public GameSession(JFrame frame, String roomCode, String playerName, NetworkConnection networkConnection, DataListener dataListener) {
        this.frame = frame;
        this.roomCode = roomCode;
        this.playerName = playerName;
        this.networkConnection = networkConnection;
        this.dataListener = dataListener;

        this.dataListener.setOnHostDisconnect(this::handleHostDisconnect);

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
        currentQuestionIndex = 0; //Reset biezacego indeksu pytania przy ustawianiu nowej listy pytań
        displayQuestion();
    }



    private void displayQuestion() {
        // Usunięcie wszystkich poprzednich komponentów
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

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
                    // Podświetlenie wybranej odpowiedzi na zielono lub czerwono
                    int answerID = Integer.parseInt(selectedAnswerButton.getActionCommand());
                    sendAnswerToServer(answerID);
                    if(answerID == 0){
                        selectedAnswerButton.setBackground(Color.decode("#98FF98"));
                        userScore++;
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



    private void displayEndOfQuiz() {
        frame.getContentPane().removeAll();

        // Etykieta "Koniec quizu"
        JLabel endLabel = new JLabel("Koniec quizu");
        endLabel.setFont(new Font("Arial", Font.BOLD, 30));
        endLabel.setHorizontalAlignment(JLabel.CENTER);
        endLabel.setVerticalAlignment(JLabel.CENTER);

        // Deklaracja etykiety "Zdobyłeś x punktów"
        JLabel scoreLabel;

        if(userScore==0 || userScore==5){
            scoreLabel = new JLabel("Zdobyłeś " + userScore + " punktów");
        } else if (userScore==1) {
            scoreLabel = new JLabel("Zdobyłeś " + userScore + " punkt");
        }
        else {
            scoreLabel = new JLabel("Zdobyłeś " + userScore + " punkty");
        }

        // Ustawienie stylu dla scoreLabel
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setVerticalAlignment(JLabel.CENTER);

        // Panel końcowy
        JPanel endPanel = new JPanel();
        endPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        endPanel.add(endLabel, gbc);
        endPanel.add(scoreLabel, gbc); // Dodanie etykiety z wynikiem
        frame.getContentPane().add(endPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }

    // Metoda do obsługi rozłączenia
    private void handleHostDisconnect(String status) {
        if (status.equals("disconnected")) {
            displayDisconnectScreen();
            dataListener.stopListening();
        }
    }

    private void displayDisconnectScreen() {
        frame.getContentPane().removeAll();

        JLabel disconnectLabel = new JLabel("Koniec quizu, rozłączono z hostem");
        disconnectLabel.setFont(new Font("Arial", Font.BOLD, 30));
        disconnectLabel.setHorizontalAlignment(JLabel.CENTER);
        disconnectLabel.setVerticalAlignment(JLabel.CENTER);

        frame.getContentPane().add(disconnectLabel, BorderLayout.CENTER);

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
            answerButton.setActionCommand(String.valueOf(answerID));
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