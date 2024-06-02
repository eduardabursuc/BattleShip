package org.example;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private AtomicBoolean running = new AtomicBoolean(true);
    private PrintWriter writer;
    GameClientGUI gui;

    public GameClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void startClient() {
        try {
            socket = new Socket(serverAddress, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to handle server responses
            Thread responseThread = new Thread(new ServerResponseHandler(socket, running));
            responseThread.start();

            SwingUtilities.invokeLater(() -> {
                gui = new GameClientGUI(this);
                gui.setVisible(true);
            });


            while (running.get()) {
                Thread.sleep(1000);
            }

            responseThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
            if (message.equals("exit")) {
                running.set(false);
            }
        }
    }

    public void sendSetShipMessage(String from, String to) {
        sendMessage("set ship " + from + " " + to);
    }

    public void sendSubmitMoveMessage(String move) {
        sendMessage("submit move " + move);
    }

    private class ServerResponseHandler implements Runnable {
        private Socket socket;
        private AtomicBoolean running;

        public ServerResponseHandler(Socket socket, AtomicBoolean running) {
            this.socket = socket;
            this.running = running;
        }

        @Override
        public void run() {
            try (BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String response;
                while (running.get() && (response = serverReader.readLine()) != null) {
                    System.out.println(response);
                    if (response.equals("Invalid game ID.")) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Invalid game ID.", "Error", JOptionPane.ERROR_MESSAGE));
                    }
                    if (response.startsWith("Game created with ID:")) {
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, finalResponse + "\n Waiting for the other player to join.", "Game Created", JOptionPane.INFORMATION_MESSAGE));
                        gui.showGameCreatedPopup(response.split(":")[1].trim());
                    }
                    if (response.startsWith("Joined game with ID:")) {
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, finalResponse, "Game Joined", JOptionPane.INFORMATION_MESSAGE));
                        gui.showGameCreatedPopup(response.split(":")[1].trim());
                    }
                    if (response.startsWith("Create")) {
                        SwingUtilities.invokeLater(() -> gui.createSouthPanel(gui.southPanel, true));
                    }
                    if (response.startsWith("Your board is created")) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Board is set.", "Board is set.", JOptionPane.INFORMATION_MESSAGE));
                    }
                    if (response.startsWith("Ship placed successfully")) {
                        String from = gui.getStartTextField().getText();
                        String to = gui.getEndTextField().getText();
                        gui.updateMatrixWithShip(from, to);
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, finalResponse, "Ship Placed", JOptionPane.INFORMATION_MESSAGE));
                    }
                    if (response.startsWith("Game started")) {
                        gui.resetBoard();
                        SwingUtilities.invokeLater(() -> gui.createSouthPanel(gui.southPanel, false));
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Game started!", "Game Started", JOptionPane.INFORMATION_MESSAGE));
                    }
                    if(response.startsWith("Your turn")) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "It's your turn!", "Your Turn", JOptionPane.INFORMATION_MESSAGE));
                        gui.getTimerLabel().setText("Time left: 15");
                        gui.startTimer();
                    }
                    if(response.startsWith("The game is over!")) {
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            int result = JOptionPane.showOptionDialog(
                                    frame,
                                    "You lost!",
                                    "LOSE",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"OK"},
                                    "OK"
                            );
                            if (result == JOptionPane.OK_OPTION) {
                                System.exit(0);
                            }
                        });
                    }
                    if(response.startsWith("It's not your turn.")) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "It's not your turn.", "Error", JOptionPane.ERROR_MESSAGE));
                    }
                    if(response.startsWith("You attacked a ship")) {
                        String cell = gui.getAttackTextField().getText();
                        gui.updateMatrixWithAttack(cell, true);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "You attacked a ship!", "HIT", JOptionPane.INFORMATION_MESSAGE));
                    }
                    if(response.startsWith("Oops ... missed")) {
                        String cell = gui.getAttackTextField().getText();
                        gui.updateMatrixWithAttack(cell, false);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Oops ... missed the target.", "MISS", JOptionPane.INFORMATION_MESSAGE));
                    }
                    if(response.startsWith("The ship sank")) {
                        String cell = gui.getAttackTextField().getText();
                        gui.updateMatrixWithAttack(cell, true);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "The ship sank!", "SUNK", JOptionPane.INFORMATION_MESSAGE));
                    }
                    if(response.startsWith("Congratulations")) {
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            int result = JOptionPane.showOptionDialog(
                                    frame,
                                    "You won!",
                                    "Game Over",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"OK"},
                                    "OK"
                            );
                            if (result == JOptionPane.OK_OPTION) {
                                System.exit(0);
                            }
                        });
                    }
                    if(response.contains("You won")) {
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            int result = JOptionPane.showOptionDialog(
                                    frame,
                                    "You won!",
                                    "Game Over",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"OK"},
                                    "OK"
                            );
                            if (result == JOptionPane.OK_OPTION) {
                                System.exit(0);
                            }
                        });
                    }
                    if(response.contains("You lost")) {
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            int result = JOptionPane.showOptionDialog(
                                    frame,
                                    "You lost!",
                                    "LOSE",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"OK"},
                                    "OK"
                            );
                            if (result == JOptionPane.OK_OPTION) {
                                System.exit(0);
                            }
                        });
                    }
                    if(response.startsWith("Time's up")) {
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame();
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            int result = JOptionPane.showOptionDialog(
                                    frame,
                                    "Time limit exceeded! hurry up grandma",
                                    "LOSE",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    new Object[]{"OK"},
                                    "OK"
                            );
                            if (result == JOptionPane.OK_OPTION) {
                                System.exit(0);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                if (running.get()) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 9999;
        GameClient client = new GameClient(serverAddress, serverPort);
        client.startClient();
    }
}
