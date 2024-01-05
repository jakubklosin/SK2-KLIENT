package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu {
    private JFrame frame;
    private JButton createGameButton;
    private JButton joinGameButton;

    public MainMenu() {
        createMainMenuPanel();
    }

    private void createMainMenuPanel() {
        frame = new JFrame("Kahoot-Quiz");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 1));

        frame.add(new JLabel("Kahoot-Quiz", SwingConstants.CENTER));

        createGameButton = new JButton("Utwórz grę");
        createGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                frame.setLayout(new BorderLayout());
                QuizClient quizClient = new QuizClient(frame);
                quizClient.initializeQuiz(frame); // Zmodyfikowana metoda initializeQuiz
                frame.revalidate();
                frame.repaint();
            }
        });

        frame.add(createGameButton);

        joinGameButton = new JButton("Dołącz do gry");
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new JoinGame(frame);
            }
        });
        frame.add(joinGameButton);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}