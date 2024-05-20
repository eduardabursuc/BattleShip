package org.example;

import org.example.model.Game;
import org.example.model.Player;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread {
    private Socket socket;
    private Game game;
    private boolean isPlayer1;
    private PrintWriter out;
    private BufferedReader in;

    public ClientThread(Socket socket, Game game, boolean isPlayer1) {
        this.socket = socket;
        this.game = game;
        this.isPlayer1 = isPlayer1;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Notify both players when the game starts
            game.notifyPlayers();

            // Create a new player instance for the connected client
            Player player = new Player(socket.getInetAddress().toString(), out);

            out.println("Create your board: ");
            if (isPlayer1) {
                game.setPlayer1(player);
                printBoard(out, game.getPlayer1().getBoard().getBoard());
            } else {
                game.setPlayer2(player);
                printBoard(out, game.getPlayer2().getBoard().getBoard());
            }


            long timeLimit = 15000; // 50 seconds time limit for each turn

            String request;
            while ((request = in.readLine()) != null) {
                if (request.equals("stop")) {
                    out.println("Server stopped");
                    break;
                } else {
                    if( game.isOver ){
                        game.getPlayer1().out.println("The game is over. " + (game.getWinner() == game.getPlayer1() ? "You won!" : "You lost."));
                        game.getPlayer2().out.println("The game is over. " + (game.getWinner() == game.getPlayer2() ? "You won!" : "You lost."));
                        break;
                    }
                    if (game.getPlayer1().getBoard().isSetUp() && game.getPlayer2().getBoard().isSetUp()) {
                        if (game.isPlayer1Turn() && isPlayer1) {
                            if (handleTurn(request, game.getPlayer1(), game.getPlayer2(), game.getPlayer1().startTime, timeLimit)) {
                                game.getPlayer2().startTime = System.currentTimeMillis();
                                game.toggleTurn();
                            }
                        } else if (!game.isPlayer1Turn() && !isPlayer1) {
                            if (handleTurn(request, game.getPlayer2(), game.getPlayer1(), game.getPlayer2().startTime, timeLimit)) {
                                game.getPlayer1().startTime = System.currentTimeMillis();
                                game.toggleTurn();
                            }
                        } else {
                            out.println("It's not your turn.");
                        }
                    } else {
                        if (request.startsWith("set ship ")) {
                            handleSetCommand(request);
                        } else {
                            out.println("Unidentified command.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("IOException in ClientThread: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean handleTurn(String request, Player currentPlayer, Player opponent, long startTime, long timeLimit) {
        if (game.isOver) {
            game.getPlayer1().out.println("The game is over. " + (game.getWinner() == game.getPlayer1() ? "You won!" : "You lost."));
            game.getPlayer2().out.println("The game is over. " + (game.getWinner() == game.getPlayer2() ? "You won!" : "You lost."));
            return false;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > timeLimit) {
            out.println("Time's up!");
            game.isOver = true;
            game.setWinner(opponent);
            return false;
        }

        if (request.startsWith("submit move ")) {
            handleTryCommand(request, currentPlayer, opponent);
            if(!game.isOver)
                if(currentPlayer == game.getPlayer2())
                    game.getPlayer1().out.println("Your turn: ");
                else game.getPlayer2().out.println("Your turn: ");
            return true;
        } else {
            out.println("Unidentified command.");
            return false;
        }
    }

    private void handleTryCommand(String request, Player currentPlayer, Player opponent) {
        String subString = request.substring(12); // Adjusted to match "submit move "
        int index = 0;
        while (index < subString.length() && Character.isDigit(subString.charAt(index))) {
            index++;
        }
        String number = subString.substring(0, index);
        String letter = subString.substring(index);

        int hit = opponent.tryHit(Integer.parseInt(number), letter.charAt(0));

        switch (hit) {
            case 0:
                out.println("Oops ... missed the target");
                opponent.out.println("The opponent missed your ships!");
                break;
            case 1:
                out.println("The ship sank!");
                if (opponent.getBoard().areAllShipsSunk()) {
                    out.println("Congratulations! You have sunk all opponent's ships. You win!");
                    game.isOver = true;
                    game.setWinner(currentPlayer);
                }
                opponent.out.println("The opponent drawn one of your ships: ");
                printBoard(opponent.out, opponent.getBoard().getBoard());
                break;
            case 2:
                out.println("You attacked a ship!");
                opponent.out.println("The opponent attacked your ships: ");
                printBoard(opponent.out, opponent.getBoard().getBoard());
                break;
        }
    }

    private void handleSetCommand(String request) {
        Pattern pattern = Pattern.compile("set ship (\\d+)(\\w) (\\d+)(\\w)");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            int number1 = Integer.parseInt(matcher.group(1));
            char letter1 = matcher.group(2).charAt(0);
            int number2 = Integer.parseInt(matcher.group(3));
            char letter2 = matcher.group(4).charAt(0);

            boolean successful = isPlayer1 ? game.getPlayer1().getBoard().setShip(number1, letter1, number2, letter2)
                    : game.getPlayer2().getBoard().setShip(number1, letter1, number2, letter2);

            out.println(successful ? "Ship placed successfully." : "Failed to place ship. Try another location or orientation.");
            if (successful) {
                if (isPlayer1) {
                    if (game.getPlayer1().getBoard().isSetUp()){
                        out.println("Your board is created, now wait for the other player to finish ... ");
                        while(!game.getPlayer2().getBoard().isSetUp()){
                            try{
                                sleep(10);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    printBoard(out, game.getPlayer1().getBoard().getBoard());

                } else {
                    if (game.getPlayer2().getBoard().isSetUp()){
                        out.println("Your board is created, now wait for the other player to finish ... ");
                        while(!game.getPlayer1().getBoard().isSetUp()){
                            try{
                                sleep(10);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    printBoard(out, game.getPlayer2().getBoard().getBoard());

                }
            }

            if (game.getPlayer1().getBoard().isSetUp() && game.getPlayer2().getBoard().isSetUp()) {
                out.println("Game started!");
                game.getPlayer1().setStartTime(System.currentTimeMillis());
                game.getPlayer2().setStartTime(System.currentTimeMillis());
                game.getPlayer1().out.println("Your turn:");
            }
        } else {
            out.println("Incorrect syntax for setting a ship.");
        }
    }

    private void printBoard(PrintWriter out, char[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                out.print(board[i][j] + " ");
            }
            out.println();
        }
    }
}
