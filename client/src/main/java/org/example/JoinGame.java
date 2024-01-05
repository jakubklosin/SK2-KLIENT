package org.example;

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

    private NetworkConnection networkConnection;

    public JoinGame(JFrame frame) {
        this.frame = frame;
        this.networkConnection = new NetworkConnection();
        initializeJoinGamePanel();
    }

    private void initializeJoinGamePanel() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout()); // Use BorderLayout for the frame

        // Create a panel to hold the components and use BoxLayout for vertical arrangement
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding

        // Create a label and center it horizontally
        JLabel label = new JLabel("Wprowadź numer pokoju", SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create the text field for room number input
        roomNumberField = new JTextField(10); // Set the width to 10 columns
        roomNumberField.setMaximumSize(roomNumberField.getPreferredSize());
        roomNumberField.setHorizontalAlignment(JTextField.CENTER);
        roomNumberField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create the join button and center it horizontally
        joinButton = new JButton("Dołącz");
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JSONObject joinRequest = new JSONObject();
                    joinRequest.put("action", "join");
                    joinRequest.put("RoomId", roomNumberField.getText());
                    String jsonStr = joinRequest.toString();

                    // Przygotowanie długości wiadomości
                    int messageLength = jsonStr.getBytes().length;
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    buffer.putInt(messageLength);
                    byte[] lengthBytes = buffer.array();

                    // Wysyłanie długości wiadomości
                    networkConnection.send(lengthBytes);
                    // Wysyłanie samej wiadomości
                    networkConnection.send(jsonStr.getBytes());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add components to the content panel with glue between them to push them apart
        contentPanel.add(Box.createVerticalGlue()); // Glue at the top
        contentPanel.add(label);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Fixed space between label and field
        contentPanel.add(roomNumberField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Fixed space between field and button
        contentPanel.add(joinButton);
        contentPanel.add(Box.createVerticalGlue()); // Glue at the bottom

        // Add the content panel to the center of the frame's BorderLayout
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }


}
