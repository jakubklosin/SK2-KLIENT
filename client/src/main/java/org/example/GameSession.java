package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
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
                    selectedAnswerButton.setBackground(Color.decode("#98FF98"));
                    selectedAnswerButton.setOpaque(true);
                    selectedAnswerButton.setBorderPainted(false);
                    sendAnswerToServer(selectedAnswerButton.getText());

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
        questionPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie panelu w osi X

        // Etykieta pytania
        JLabel questionLabel = new JLabel(question.getString("pytanie"));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie etykiety w osi X
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Zwiększenie czcionki etykiety

        // Panel na przyciski odpowiedzi
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(4, 1, 10, 10)); // GridLayout z marginesami
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie panelu przycisków w osi X

        JSONArray answers = question.getJSONArray("odpowiedzi");

        for (int i = 0; i < answers.length(); i++) {
            String answerText = answers.getString(i);
            JButton answerButton = new JButton(answerText);
            answerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, answerButton.getMinimumSize().height)); // Maksymalna szerokość, minimalna wysokość
            answerButton.addActionListener(e -> handleAnswer(answerButton, answerText));
            buttonsPanel.add(answerButton); // Dodanie do panelu przycisków
        }

        // Dodanie komponentów do panelu pytania
        questionPanel.add(Box.createVerticalGlue()); // Dodanie pustego miejsca na górze
        questionPanel.add(questionLabel);
        questionPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Dodanie przestrzeni między pytaniem a przyciskami
        questionPanel.add(buttonsPanel);
        questionPanel.add(Box.createVerticalGlue()); // Dodanie pustego miejsca na dole

        return questionPanel;
    }

    private void handleAnswer(JButton answerButton, String chosenAnswer) {
        if (selectedAnswerButton != null) {
            // Opcjonalnie resetuj kolor poprzednio wybranego przycisku, jeśli chcesz zezwolić na zmianę wyboru przed kliknięciem "Dalej"
            selectedAnswerButton.setBackground(UIManager.getColor("Button.background"));
            selectedAnswerButton.setOpaque(false);
            selectedAnswerButton.setBorderPainted(true);
        }

        selectedAnswerButton = answerButton; // Zapisz referencję do wybranego przycisku
    }

    private void sendAnswerToServer(String chosenAnswer) {
        try {
            JSONObject answerJson = new JSONObject();
            answerJson.put("kod pokoju", roomCode);
            answerJson.put("action", "answering");
            answerJson.put("nickname", playerName);
            answerJson.put("numer pytania", currentQuestionIndex + 1); // Zakładając, że numeracja pytań zaczyna się od 1
            answerJson.put("odpowiedź", chosenAnswer);
            networkConnection.send(answerJson.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Metody do przełączania pytań, obsługi odpowiedzi itd.
    // ...
}
