package org.example;

import org.example.model.Board;
import org.example.model.Game;
import org.example.model.Ship;

import java.io.PrintWriter;
import java.util.Random;

class Coordinate {
    int x;
    char y;

    Coordinate(int x, char y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + (int) y;
        return result;
    }
}

public class AIThread extends Thread {

    public Board AIboard = new Board();
    private int[][] opponentBoard = new int[11][11];
    private Coordinate firstAttacked;
    private Coordinate lastAttacked;
    private int position = 0; // 0 - not set, 1 - vertical, 2 - horizontal
    private boolean inAttack = false; // true = there is a ship attacked, otherwise try randomly
    private Game game;
    private static final int BOARD_SIZE = 10;
    private Random random = new Random();

    public AIThread(Game game) {
        this.game = game;
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++)
                opponentBoard[i][j] = 0;
        }
    }

    public void setGame(Game game){
        this.game = game;
    }

    @Override
    public void run() {

        // Board initialization
        for (Ship ship : AIboard.getShips()) {
            placeShipRandomly(ship);
        }

        while (!game.isOver) {

            if (!game.isPlayer1Turn()) {
                System.out.println("AI's turn to attack.");
                attack();
            } else {
                try{
                    sleep(20);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    private void placeShipRandomly(Ship ship) {
        boolean placed = false;
        int size = ship.getSize();

        while (!placed) {
            int x1 = random.nextInt(BOARD_SIZE) + 1;
            int y1 = random.nextInt(BOARD_SIZE) + 'A';
            boolean horizontal = random.nextBoolean();

            int x2 = x1;
            int y2 = y1;

            if (horizontal) {
                x2 = x1 + size - 1;
            } else {
                y2 = y1 + size - 1;
            }

            if (isValidCoordinate(x2, y2) && AIboard.setShip(x1, (char) y1, x2, (char) y2)) {
                placed = true;
            }
        }
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 1 && x <= BOARD_SIZE && y >= 'A' && y <= 'J';
    }

    private void attack() {
        if (!inAttack) {
            System.out.println(1);
            int x = random.nextInt(BOARD_SIZE) + 1;
            int y = random.nextInt(BOARD_SIZE) + 'A';

            while (!isValidCoordinate(x, y) || opponentBoard[x][y - 'A'] != 0) {
                x = random.nextInt(BOARD_SIZE) + 1;
                y = random.nextInt(BOARD_SIZE) + 'A';
            }

            System.out.println(new Coordinate(x, (char)(y)));

            if (game.getPlayer1().getBoard().tryHitShip(x, (char) y) == 2) {
                inAttack = true;
                lastAttacked= new Coordinate(x, (char)(y));
                firstAttacked = lastAttacked;
                opponentBoard[x][y - 'A'] = 1;
            } else {
                opponentBoard[x][y - 'A'] = 3;
            }

        } else {
            System.out.println(2);
            if (position == 0) {
                Coordinate[] check = {
                        new Coordinate(lastAttacked.x, (char) (lastAttacked.y + 1)),
                        new Coordinate(lastAttacked.x, (char) (lastAttacked.y - 1)),
                        new Coordinate(lastAttacked.x + 1, lastAttacked.y),
                        new Coordinate(lastAttacked.x - 1, lastAttacked.y)
                };

                for (Coordinate coordinate : check) {
                    if (isValidCoordinate(coordinate.x, coordinate.y) && opponentBoard[coordinate.x][coordinate.y - 'A'] != 1 && opponentBoard[coordinate.x][coordinate.y - 'A'] != 3  && checkAround(coordinate)) {
                        handleAttack(coordinate);
                        System.out.println(coordinate);
                        break;
                    }
                }
            } else {
                System.out.println(3);
                if (position == 2) {
                    Coordinate[] coordinates = {
                            new Coordinate(lastAttacked.x, (char) (lastAttacked.y + 1)),
                            new Coordinate(lastAttacked.x, (char) (lastAttacked.y - 1)),
                            new Coordinate(lastAttacked.x, (char) (firstAttacked.y + 1)),
                            new Coordinate(lastAttacked.x, (char) (firstAttacked.y - 1))
                    };

                    for (Coordinate c : coordinates) {
                        if (isValidCoordinate(c.x, c.y) && opponentBoard[c.x][c.y - 'A'] != 1 && opponentBoard[c.x][c.y - 'A'] != 3 && checkAround(c)) {
                            handleAttack(c);
                            System.out.println(c);
                            break;
                        }
                    }

                } else {
                    Coordinate[] coordinates = {
                            new Coordinate(lastAttacked.x + 1, lastAttacked.y),
                            new Coordinate(lastAttacked.x - 1, lastAttacked.y),
                            new Coordinate(firstAttacked.x + 1, lastAttacked.y),
                            new Coordinate(firstAttacked.x - 1, lastAttacked.y)
                    };

                    for (Coordinate c : coordinates) {
                        if (isValidCoordinate(c.x, c.y) && opponentBoard[c.x][c.y - 'A'] != 1 && opponentBoard[c.x][c.y - 'A'] != 3 && checkAround(c)) {
                            handleAttack(c);
                            System.out.println(c);
                            break;
                        }
                    }
                }
            }
        }
        game.toggleTurn();
    }

    boolean checkAround(Coordinate coordinate){
        Coordinate[] coordinates = {
                new Coordinate(coordinate.x, (char) (coordinate.y + 1)),
                new Coordinate(coordinate.x, (char) (coordinate.y - 1)),
                new Coordinate(coordinate.x + 1, coordinate.y),
                new Coordinate(coordinate.x - 1, coordinate.y)
        };

        for( Coordinate c : coordinates ){
            if( c.x != lastAttacked.x && c.y != lastAttacked.y && isValidCoordinate(c.x, c.y) && opponentBoard[c.x][c.y - 'A'] == 1)
                return false;
        }
        return true;
    }

    void placeAround(Coordinate c) {
        Coordinate[] coordinates = {
                new Coordinate(c.x + 1, c.y),
                new Coordinate(c.x - 1, c.y),
                new Coordinate(c.x, (char) (c.y + 1)),
                new Coordinate(c.x, (char) (c.y - 1)),
                new Coordinate(c.x + 1, (char) (c.y + 1)),
                new Coordinate(c.x - 1, (char) (c.y + 1)),
                new Coordinate(c.x - 1, (char) (c.y + 1)),
                new Coordinate(c.x + 1, (char) (c.y - 1)),
        };

        for (Coordinate coordinate : coordinates) {
            if (isValidCoordinate(coordinate.x, coordinate.y) && opponentBoard[coordinate.x][coordinate.y - 'A'] == 0)
                opponentBoard[coordinate.x][coordinate.y - 'A'] = 2;
        }
    }

    void handleAttack(Coordinate coordinate) {
        opponentBoard[coordinate.x][coordinate.y - 'A'] = 1;
        switch (game.getPlayer1().getBoard().tryHitShip(coordinate.x, coordinate.y)) {
            case 2:
                if (lastAttacked.x == coordinate.x) {
                    position = 2;
                } else {
                    position = 1;
                }
                lastAttacked = coordinate;
                placeAround(coordinate);
                game.getPlayer1().out.println("The opponent attacked your ships");
                printBoard(game.getPlayer1().out, game.getPlayer1().getBoard().getBoard());
                break;
            case 1:
                if( game.getPlayer1().getBoard().areAllShipsSunk() ){
                    game.isOver = true;
                    game.getPlayer1().out.println("The game is over! You lost.");
                } else {
                    game.getPlayer1().out.println("The opponent attacked your ships");
                    printBoard(game.getPlayer1().out, game.getPlayer1().getBoard().getBoard());
                }
                inAttack = false;
                position = 0;
                placeAround(coordinate);
                break;
            case 0:
                opponentBoard[coordinate.x][coordinate.y - 'A'] = 3;
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