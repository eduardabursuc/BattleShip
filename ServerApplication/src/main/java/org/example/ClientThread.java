package org.example;

import org.example.model.Board;
import org.example.model.Game;
import org.example.model.Player;
import org.example.repository.PlayerRepository;

import java.io.*;
import java.net.*;
import java.sql.Time;
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

    @Override
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
                printBoard(out, game.getPlayer1().getBoard().getBoard());
                if( game.withAI ){
                    game.setPlayer1(player);
                    Player player2 = new Player();
                    player2.setOut(new PrintWriter(System.out));
                    game.setPlayer2(player2);
                }
            } else {
                    printBoard(out, game.getPlayer2().getBoard().getBoard());
            }


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
                    if (game.getPlayer1().getBoard().isSetUp() && game.getPlayer2Board().isSetUp()) {
                        if (game.isPlayer1Turn() && isPlayer1) {
                            if (handleTurn(request, game.getPlayer1(), game.getPlayer2())) {
                                game.toggleTurn();
                            }
                        } else if (!game.isPlayer1Turn() && !isPlayer1) {
                            if(!game.withAI)
                                if (handleTurn(request, game.getPlayer2(), game.getPlayer1())) {
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

    private boolean handleTurn(String request, Player currentPlayer, Player opponent) {


        if (request.startsWith("submit move ")) {
            handleTryCommand(request, currentPlayer, opponent);
        } else {
            out.println("Unidentified command.");
        }

        return true;

    }

    private void handleTryCommand(String request, Player currentPlayer, Player opponent) {

        String subString = request.substring(12); // Adjusted to match "submit move "
        int index = 0;
        while (index < subString.length() && Character.isDigit(subString.charAt(index))) {
            index++;
        }
        String number = subString.substring(0, index);
        String letter = subString.substring(index);


        if( game.withAI )
            opponent.setBoard(game.getPlayer2Board());


        int hit = opponent.tryHit(Integer.parseInt(number), letter.charAt(0));

        switch (hit) {
            case -1:
                out.println("Already attacked ..");
                break;
            case 0:
                out.println("Oops ... missed the target");
                break;
            case 1:
                out.println("The ship sank!");
                if (opponent.getBoard().areAllShipsSunk()) {
                    PlayerRepository pr = new PlayerRepository();
                    pr.updateWins(currentPlayer.getUsername());
                    out.println("Congratulations! You have sunk all opponent's ships. You win!");
                    opponent.out.println("The game is over! You lost.");
                    game.isOver = true;
                    game.setWinner(currentPlayer);
                }
                opponent.out.println("The opponent attacked your ships");
                printBoard(opponent.out, opponent.getBoard().getBoard());
                break;
            case 2:
                out.println("You attacked a ship!");
                opponent.out.println("The opponent attacked your ships");
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
                    : game.getPlayer2Board().setShip(number1, letter1, number2, letter2);

            out.println(successful ? "Ship placed successfully." : "Failed to place ship. Try another location or orientation.");
            if (successful) {
                if (isPlayer1) {
                    if (game.getPlayer1().getBoard().isSetUp()){
                        out.println("Your board is created, now wait for the other player to finish ... ");
                        while(!game.getPlayer2Board().isSetUp()){
                            try{
                                sleep(10);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    printBoard(out, game.getPlayer1().getBoard().getBoard());

                } else {
                    if (game.getPlayer2Board().isSetUp()){
                        out.println("Your board is created, now wait for the other player to finish ... ");
                        while(!game.getPlayer1().getBoard().isSetUp()){
                            try{
                                sleep(10);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    printBoard(out, game.getPlayer2Board().getBoard());

                }
            }

            if (game.getPlayer1().getBoard().isSetUp() && game.getPlayer2Board().isSetUp()) {
                out.println("Game started!");
                game.getPlayer1().setStartTime(System.currentTimeMillis());
                game.getPlayer2().setStartTime(System.currentTimeMillis());
                game.getPlayer1().out.println("Your turn:");
                game.timer.start();
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
