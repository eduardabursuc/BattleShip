package org.example;

import org.example.model.Board;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.Ship;

import java.util.Random;

class Coordinate {
    int x;
    char y;

    Coordinate(int x, char y){
        this.x = x;
        this.y = y;
    }
}

public class AIThread extends Thread {

    public Board AIboard = new Board();
    private int[][] opponentBoard;
    private Coordinate lastAttacked = new Coordinate(0, 'Z');
    private int position = 0; // 0 - not set, 1 - vertical, 2 - horizontal
    private boolean inAttack = false; // true = there is a ship attacked, otherwise try randomly
    private Game game;
    private boolean attacked = false;
    private static final int BOARD_SIZE = 10;
    private Random random = new Random();

    public AIThread() {
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++)
                opponentBoard[i][j] = 0;
        }
    }

    public AIThread(Game game) {
        this.game = game;
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++)
                opponentBoard[i][j] = 0;
        }
    }

    @Override
    public void run() {

        // Board initialization
        for (Ship ship : AIboard.getShips()) {
            placeShipRandomly(ship);
        }

        // Game logic
        while (!game.isOver) {
            attack();
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
            int x = random.nextInt(BOARD_SIZE) + 1;
            int y = random.nextInt(BOARD_SIZE) + 'A';

            while (!isValidCoordinate(x, y) || opponentBoard[x][y - 'A'] != 0) {
                x = random.nextInt(BOARD_SIZE) + 1;
                y = random.nextInt(BOARD_SIZE) + 'A';
            }

            if (game.getPlayer1().getBoard().tryHitShip(x, (char) y) == 2) {
                inAttack = true;
                lastAttacked.x = x;
                lastAttacked.y = (char) y;
                opponentBoard[x][y - 'A'] = 1;
            }

        } else {
            if (position == 0) {
                Coordinate[] check = {
                        new Coordinate(lastAttacked.x, (char) (lastAttacked.y + 1)),
                        new Coordinate(lastAttacked.x, (char) (lastAttacked.y - 1)),
                        new Coordinate(lastAttacked.x + 1, lastAttacked.y),
                        new Coordinate(lastAttacked.x - 1, lastAttacked.y)
                };

                for (Coordinate coordinate : check) {
                    if (isValidCoordinate(coordinate.x, coordinate.y) && opponentBoard[coordinate.x][coordinate.y - 'A'] != 1 && checkAround(coordinate)) {
                        handleAttack(coordinate);
                        break;
                    }
                }
            } else {
                if (position == 1) {
                    Coordinate[] coordinates = {
                            new Coordinate(lastAttacked.x, (char) (lastAttacked.y + 1)),
                            new Coordinate(lastAttacked.x, (char) (lastAttacked.y - 1))
                    };

                    for (Coordinate c : coordinates) {
                        if (isValidCoordinate(c.x, c.y) && opponentBoard[c.x][c.y - 'A'] != 1 && checkAround(c)) {
                            handleAttack(c);
                            break;
                        }
                    }
                } else {
                    Coordinate[] coordinates = {
                            new Coordinate(lastAttacked.x + 1, lastAttacked.y),
                            new Coordinate(lastAttacked.x - 1, lastAttacked.y)
                    };

                    for (Coordinate c : coordinates) {
                        if (isValidCoordinate(c.x, c.y) && opponentBoard[c.x][c.y - 'A'] != 1 && checkAround(c)) {
                            handleAttack(c);
                            break;
                        }
                    }
                }
            }
        }
    }

    boolean checkAround(Coordinate coordinate){
        Coordinate[] coordinates = {
                new Coordinate(coordinate.x, (char) (coordinate.y + 1)),
                new Coordinate(coordinate.x, (char) (coordinate.y - 1)),
                new Coordinate(coordinate.x + 1, coordinate.y),
                new Coordinate(coordinate.x - 1, coordinate.y)
        };

        for( Coordinate c : coordinates ){
            if( c != coordinate && isValidCoordinate(c.x, c.y) && opponentBoard[c.x][c.y - 'A'] == 1)
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
                break;
            case 1:
                inAttack = false;
                position = 0;
                placeAround(coordinate);
                break;
            case 0:
                opponentBoard[coordinate.x][coordinate.y - 'A'] = 0;
        }
    }

}

