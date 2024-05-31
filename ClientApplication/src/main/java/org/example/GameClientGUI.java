package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameClientGUI extends JFrame {
    private GameClient client;

    public GameClientGUI(GameClient client) {
        this.client = client;
        initUI();
    }

    private void initUI() {
        setTitle("Game Client");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        JButton createGameButton = new JButton("Create Game");
        createGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessage("create game");
            }
        });

        JButton joinGameButton = new JButton("Join Game");
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String gameId = JOptionPane.showInputDialog(GameClientGUI.this, "Enter game code:");
                if (gameId != null && !gameId.trim().isEmpty()) {
                    client.sendMessage("join game " + gameId.trim());
                }
            }
        });

        panel.add(createGameButton);
        panel.add(joinGameButton);

        add(panel);
    }

    public void showGameCreatedPopup(String gameCode) {
        // Close the current window
        dispose();

        // Open a new window with an empty canvas
        JFrame canvasFrame = new JFrame("Game Canvas");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setSize(400, 400);
        canvasFrame.setLocationRelativeTo(null);
        canvasFrame.setVisible(true);
        canvasFrame.add(new JPanel()); // Add an empty canvas
    }
}
