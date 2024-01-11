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
    private String roomCode; // Kod pokoju
    private String playerName; // Nazwa gracza
    private int currentQuestionIndex = 0;
    private NetworkConnection networkConnection;
    public GameSession(JFrame frame, String roomCode, String playerName, NetworkConnection networkConnection) {
        this.frame = frame;
        this.roomCode = roomCode;
        this.playerName = playerName;
        this.networkConnection = networkConnection;
        this.questionsList = new ArrayList<>();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        this.mainPanel.setBackground(new Color(98, 180, 228));
        frame.add(mainPanel);
    }

    public void setQuestionsList(JSONArray questions) {
        questionsList.clear();
        for (int i = 0; i < questions.length(); i++) {
            questionsList.add(questions.getJSONObject(i));
        }
        // Wyświetlenie pytań na całym oknie po ich ustawieniu
        //SwingUtilities.invokeLater(this::displayQuestions);
        currentQuestionIndex = 0; //Reset biezacego indeksu pytania przy ustawianiu nowej listy pytań
        displayQuestion();
    }

    private void displayQuestion() {
        // Usunięcie wszystkich poprzednich komponentów
        frame.getContentPane().removeAll();
        frame.getContentPane().setBackground(new Color(98, 180, 228)); // Ustawienie koloru tła dla całego okna
        frame.setLayout(new BorderLayout()); // Ustawienie BorderLayout dla ramki

        if (currentQuestionIndex < questionsList.size()) {
            // Pobranie bieżącego pytania na podstawie indeksu
            JSONObject question = questionsList.get(currentQuestionIndex);
            JPanel questionPanel = createQuestionPanel(question);
            questionPanel.setBackground(new Color(98, 180, 228)); // Ustawienie koloru tła dla panelu pytania

            // Dodanie panelu pytania do głównego kontenera
            frame.getContentPane().add(questionPanel, BorderLayout.CENTER);

            // Dodanie przycisku "Dalej"
            JButton nextButton = new JButton("Dalej");
            // Ustawienie koloru tła dla przycisku, jeśli potrzebujesz
            nextButton.setBackground(new Color(180, 235, 255));
            nextButton.setOpaque(true);
            nextButton.setBorderPainted(false);
            nextButton.addActionListener(e -> {
                if (selectedAnswerButton != null) {
                    // Podświetlenie wybranej odpowiedzi na zielono
                    selectedAnswerButton.setBackground(Color.decode("#98FF98"));
                    selectedAnswerButton.setOpaque(true);
                    selectedAnswerButton.setBorderPainted(false);

                    int answerID = Integer.parseInt(selectedAnswerButton.getActionCommand());
                    sendAnswerToServer(answerID);

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
            buttonPanel.setBackground(new Color(98, 180, 228)); // Ustawienie koloru tła dla panelu z przyciskiem
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
        // Usunięcie wszystkich poprzednich komponentów
        mainPanel.removeAll(); // Zamiast frame.getContentPane().removeAll();
        mainPanel.setLayout(new GridBagLayout()); // Zmiana layoutu dla mainPanel

        JLabel endLabel = new JLabel("Koniec quizu");
        endLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Zwiększenie czcionki
        endLabel.setHorizontalAlignment(JLabel.CENTER); // Wyśrodkowanie etykiety w poziomie
        endLabel.setVerticalAlignment(JLabel.CENTER); // Wyśrodkowanie etykiety w pionie

        mainPanel.add(endLabel); // Dodanie endLabel do mainPanel

        mainPanel.revalidate();
        mainPanel.repaint();
    }



    private JPanel createQuestionPanel(JSONObject question) {
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionPanel.setBackground(new Color(98, 180, 228));

        // Etykieta pytania
        JLabel questionLabel = new JLabel(question.getString("pytanie"));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionLabel.setBackground(new Color(98, 180, 228));
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
            answerButton.setBackground(new Color(180, 235, 255));
            buttonsPanel.add(answerButton);
        }

        questionPanel.add(buttonsPanel);
        return questionPanel;
    }

    private void handleAnswer(JButton answerButton, String chosenAnswer, int answerID) {
        // Ta metoda teraz otrzymuje również answerID
        if (selectedAnswerButton != null) {
            selectedAnswerButton.setBackground(UIManager.getColor("Button.background"));
            selectedAnswerButton.setOpaque(false);
            selectedAnswerButton.setBorderPainted(true);
        }

        selectedAnswerButton = answerButton;
        // Tutaj możesz teraz użyć answerID dla dalszej logiki, np. wysyłania odpowiedzi do serwera
    }

    private void sendAnswerToServer(int answerID) {
        try {
            JSONObject answerJson = new JSONObject();
            answerJson.put("action","answering");
            ///answerJson.put("kod pokoju", roomCode);
            answerJson.put("nickname", playerName);
            answerJson.put("numer pytania", currentQuestionIndex + 1); // Zakładając, że numeracja pytań zaczyna się od 1
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

    // Metody do przełączania pytań, obsługi odpowiedzi itd.
    // ...
}