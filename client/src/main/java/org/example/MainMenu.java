package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        frame.getContentPane().setBackground(new Color(240, 240, 240)); // delikatny kolor tła

        JLabel titleLabel = new JLabel("Kahoot-Quiz", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        frame.add(titleLabel);

        createGameButton = new JButton("Utwórz grę");
        styleButton(createGameButton);
        createGameButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.setLayout(new BorderLayout());
            QuizClient quizClient = new QuizClient(frame);
            quizClient.initializeQuiz(frame);
            frame.revalidate();
            frame.repaint();
        });
        frame.add(createGameButton);

        joinGameButton = new JButton("Dołącz do gry");
        styleButton(joinGameButton);
        joinGameButton.addActionListener(e -> new JoinGame(frame));
        frame.add(joinGameButton);

        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null); // Centrowanie okna
        frame.setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(new Color(98, 180, 228));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(98, 180, 228));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}
