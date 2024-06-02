package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;

@Entity
@Table(name = "players")
public class Player implements Serializable {

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "wins")
    private Integer wins;

    private transient Board board;

    public transient long startTime;

    public transient PrintWriter out;

    public Player(){

    }

    public Player(String username, PrintWriter out) {
        this.username = username;
        this.board = new Board();
        this.out = out;
    }

    public Player(String username, String password, Integer wins){
        this.username = username;
        this.password = password;
        this.wins = wins;
    }

    public String getUsername() {
        return username;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + "'; " +
                "password='" + password + "'; " +
                "wins=" + wins  +
                '}';
    }

    // Add methods related to player actions, such as placing ships and making moves.
}