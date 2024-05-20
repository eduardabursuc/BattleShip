package org.example.model;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Player {
    private String name;
    private Board board;

    public long startTime;

    public PrintWriter out;

    public Player(String name, PrintWriter out) {
        this.name = name;
        this.board = new Board();
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return board;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long timeLimit) {
        this.startTime = timeLimit;
    }

    public int tryHit(Integer number, Character letter){
        return board.tryHitShip(number, letter);
    }


    // Add methods related to player actions, such as placing ships and making moves.
}