package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient {
    private String serverAddress;
    private int serverPort;
    private AtomicBoolean running = new AtomicBoolean(true);

    public GameClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void startClient() {
        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // Start a thread to handle server responses
            Thread responseThread = new Thread(new ServerResponseHandler(socket, running));
            responseThread.start();

            String command;
            while (running.get() && (command = reader.readLine()) != null) {
                writer.println(command);
                if (command.equals("exit")) {
                    running.set(false);
                    socket.close(); // Close the socket to trigger the end of reading in the other thread
                    break;
                }
            }
            responseThread.join(); // Wait for the response handler to finish
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class ServerResponseHandler implements Runnable {
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
