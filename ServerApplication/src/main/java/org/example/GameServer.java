package org.example;

import org.example.model.Game;
import org.example.model.Player;
import org.example.repository.PlayerRepository;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private int port;
    private static Map<String, LinkedBlockingQueue<Socket>> games = new ConcurrentHashMap<>();
    private PlayerRepository playerRepository;

    public GameServer(int port) {
        this.port = port;
        this.playerRepository = new PlayerRepository();
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

    private void handleClient(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Player player = authenticateClient(in, out);
            if (player == null) {
                out.println("Authentication failed. Disconnecting...");
                clientSocket.close();
                return;
            }

            out.println("Try 'create game', 'join game <id>', or 'play with AI'");

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
                } else if (request.equals("play with AI")) {
                    out.println("Starting game against AI...");
                    startGameWithAI(clientSocket, player);
                    break;
                }  else if (request.equals("rating")) {
                    out.println("rating");
                    break;
                } else {
                    out.println("Invalid command. Try 'create game', 'join game <id>', 'play with AI', or 'rating'.");
                }
            }
        } catch (SocketException e) {
            System.out.println("Client disconnected abruptly: " + clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Player authenticateClient(BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            out.println("Enter 'login' or 'register':");
            String request;

            while ((request = in.readLine()) != null) {
                String[] tokens = request.split(" ");
                if (tokens.length != 3) {
                    out.println("Invalid command format. Use 'login username password' or 'register username password'.");
                    continue;
                }

                String command = tokens[0];
                String username = tokens[1];
                String password = tokens[2];

                if (command.equals("login")) {
                    Player player = playerRepository.loginPlayer(username, password);
                    if (player != null) {
                        out.println("Login successful.");
                        return player;
                    } else {
                        out.println("Login failed. Try again.");
                    }
                } else if (command.equals("register")) {
                    boolean success = playerRepository.registerPlayer(username, password);
                    if (success) {
                        out.println("Registration successful. You can now login.");
                        return playerRepository.loginPlayer(username, password);
                    } else {
                        out.println("Registration failed. Try again with a different username.");
                    }
                } else {
                    out.println("Invalid command. Use 'login username password' or 'register username password'.");
                }
            }
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

    private static void startGameWithAI(Socket playerSocket, Player player) {
        Game game = new Game();
        game.withAI = true;
        new ClientThread(playerSocket, game, true).start();
        new AIThread(game).start();
        System.out.println("Game started between player and AI.");
    }

    public static void main(String[] args) {
        int port = 9999;
        GameServer server = new GameServer(port);
        server.startServer();
    }
}
