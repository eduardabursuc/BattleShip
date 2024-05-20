package org.example;

import org.example.model.Game;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private int port;
    private static Map<String, LinkedBlockingQueue<Socket>> games = new ConcurrentHashMap<>();

    public GameServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleClient(Socket clientSocket){

        try {

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Try 'create game' or 'join game <id>'");

            String request;
            while ((request = in.readLine()) != null) {
                if (request.equals("stop")) {
                    out.println("Server stopped");
                    break;
                } else if (request.equals("create game")) {
                    String gameId = generateGameId();
                    games.put(gameId, new LinkedBlockingQueue<>(2));
                    games.get(gameId).offer(clientSocket);
                    out.println("Game created with ID: " + gameId);
                    out.println("Waiting for second player to join...");
                    break;
                } else if (request.startsWith("join game ")) {
                    String gameId = request.substring(10).trim();
                    if (games.containsKey(gameId)) {
                        LinkedBlockingQueue<Socket> queue = games.get(gameId);
                        if (queue.size() < 2) {
                            queue.offer(clientSocket);
                            out.println("Joined game with ID: " + gameId);
                            if (queue.size() == 2) {
                                startGame(gameId);

                            }
                            break;
                        } else {
                            out.println("Game is already full.");
                        }
                    } else {
                        out.println("Invalid game ID.");
                    }
                } else {
                    out.println("Invalid command. Try 'create game' or 'join game <id>'.");
                }
            }
        } catch (SocketException e) {
            System.out.println("Client disconnected abruptly: " + clientSocket);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private String generateGameId() {
        Random random = new Random();
        int gameId;
        do {
            gameId = 100000 + random.nextInt(900000);
        } while (games.containsKey(String.valueOf(gameId)));
        return String.valueOf(gameId);
    }

    private static void startGame(String id) {
        try {
            LinkedBlockingQueue<Socket> queue = games.get(id);
            Socket player1Socket = queue.take();
            Socket player2Socket = queue.take();
            Game game = new Game();
            new ClientThread(player1Socket, game, true).start();
            new ClientThread(player2Socket, game, false).start();
            System.out.println("Game started between " + player1Socket.getInetAddress().toString() + " and " + player2Socket.getInetAddress().toString());
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 9999;
        GameServer server = new GameServer(port);
        server.startServer();
    }
}
