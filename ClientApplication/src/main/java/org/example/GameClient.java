package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

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

            // Start the GUI
            SwingUtilities.invokeLater(() -> {
                gui = new GameClientGUI(this);
                gui.setVisible(true);
            });

            // Keep the main thread alive until running is set to false
            while (running.get()) {
                Thread.sleep(1000);
            }

            // Clean up resources
            responseThread.join(); // Wait for the response handler to finish
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
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, finalResponse, "Game Created", JOptionPane.INFORMATION_MESSAGE));
                        gui.showGameCreatedPopup(response.split(":")[1].trim());
                    }
                    if (response.startsWith("Joined game with ID:")) {
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, finalResponse, "Game Joined", JOptionPane.INFORMATION_MESSAGE));
                        gui.showGameCreatedPopup(response.split(":")[1].trim());
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
        String serverAddress = "localhost"; // Change this to your server address
        int serverPort = 9999; // Change this to your server port number
        GameClient client = new GameClient(serverAddress, serverPort);
        client.startClient();
    }
}
