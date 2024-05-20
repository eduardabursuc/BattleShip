package org.example.model;

import java.util.Arrays;

import static java.lang.Math.abs;

public class Board {
    private char[][] grid;
    private Ship[] ships = {new Ship(2), new Ship(3), new Ship(3), new Ship(4), new Ship(5)};


    public Board() {
        grid = new char[11][11];
        grid[0][0] = ' ';
        for (int i = 1; i < 11; i++) {
            grid[0][i] = (char) (i + 'A' - 1);
            grid[i][0] = (char) (i + '0');
        }
        for (int i = 1; i < 11; i++) {
            for (int j = 1; j < 11; j++) {
                grid[i][j] = 'O';
            }
        }
    }

    public char[][] getBoard(){
        return grid;
    }

    public boolean isSetUp(){
        for(Ship ship : ships){
            if( !ship.isSet() )
                return false;
        }
        return true;
    }

    public boolean setShip(Integer x1, Character y1, Integer x2, Character y2) {
        // Ensure coordinates are valid and within board limits
        if (x1 == null || y1 == null || x2 == null || y2 == null ||
                x1 < 1 || x1 > 10 || x2 < 1 || x2 > 10 ||
                y1 < 'A' || y1 > 'J' || y2 < 'A' || y2 > 'J') {
            return false;
        }

        // Ensure y1 and y2 are uppercase characters
        y1 = Character.toUpperCase(y1);
        y2 = Character.toUpperCase(y2);

        if (x1.equals(x2)) { // Vertical ship
            int length = Math.abs(y1 - y2) + 1;
            if (length <= 5 && length >= 2) {
                for (Ship ship : ships) {
                    if (ship.getSize() == length && !ship.isSet()) {

                        if (y1 > y2) { // Ensure y1 is less than y2 for easier looping
                            Character aux = y1;
                            y1 = y2;
                            y2 = aux;
                        }

                        // Check for existing ships and boundary conditions
                        for (int i = y1 - 'A' + 1; i <= y2 - 'A' + 1; i++) {
                            if (grid[x1][i] == 'S' ||
                                    (x1 > 1 && grid[x1 - 1][i] == 'S') ||
                                    (x1 < 10 && grid[x1 + 1][i] == 'S') ||
                                    (i == y1 - 'A' + 1 && y1 > 'A' && grid[x1][i - 1] == 'S') ||
                                    (i == y2 - 'A' + 1 && y2 < 'J' && grid[x1][i + 1] == 'S')) {
                                return false;
                            }
                        }
                        ship.setSet(true);
                        ship.setOrientation(1);
                        ship.setStart(y1 - 'A' + 1);
                        ship.setEnd(y2 - 'A' + 1);
                        ship.setLine(x1);
                        // Place the ship on the board
                        for (int i = y1 - 'A' + 1; i <= y2 - 'A' + 1; i++) {
                            grid[x1][i] = 'S';
                        }
                        return true;
                    }
                }
            }
        } else if (y1.equals(y2)) { // Horizontal ship
            int length = Math.abs(x1 - x2) + 1;
            if (length <= 5 && length >= 2) {
                for (Ship ship : ships) {
                    if (ship.getSize() == length && !ship.isSet()) {

                        if (x1 > x2) { // Ensure x1 is less than x2 for easier looping
                            Integer aux = x1;
                            x1 = x2;
                            x2 = aux;
                        }

                        // Check for existing ships and boundary conditions
                        for (int i = x1; i <= x2; i++) {
                            if (grid[i][y1 - 'A' + 1] == 'S' ||
                                    (y1 > 'A' && grid[i][y1 - 'A'] == 'S') ||
                                    (y1 < 'J' && grid[i][y1 - 'A' + 2] == 'S') ||
                                    (i == x1 && x1 > 1 && grid[x1 - 1][y1 - 'A' + 1] == 'S') ||
                                    (i == x2 && x2 < 10 && grid[x2 + 1][y1 - 'A' + 1] == 'S')) {
                                return false;
                            }
                        }
                        ship.setSet(true);
                        ship.setOrientation(2);
                        ship.setStart(x1);
                        ship.setEnd(x2);
                        ship.setLine(y1 - 'A' + 1);
                        // Place the ship on the board
                        for (int i = x1; i <= x2; i++) {
                            grid[i][y1 - 'A' + 1] = 'S';
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public int tryHitShip(Integer x, Character y){
        if( y > 'J' || y < 'A' || x > 10 || x < 1)
            return 0;
        if( grid[x][y - 'A' + 1] == 'S'){
            grid[x][y - 'A' + 1] = 'X';
            for( Ship ship : ships ){
                if(ship.tryHit(x, y - 'A' + 1) && ship.isSank()){
                    return 1;
                }
            }
            return 2;
        } else {
            return 0;
        }
    }

    public boolean areAllShipsSunk(){
        for( Ship ship : ships ){
            if( ship.getSize() > 0 )
                return false;
        }
        return true;
    }

    // Add methods for placing ships, recording hits/misses, etc.
}