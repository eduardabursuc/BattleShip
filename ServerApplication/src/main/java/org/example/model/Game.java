package org.example.model;

public class Game {
    private Player player1;
    private Player player2;

    private Player winner;
    private boolean isPlayer1Turn;

    public boolean isOver = false;

    public synchronized void setPlayer1(Player player) {
        this.player1 = player;
    }

    public synchronized void setPlayer2(Player player) {
        this.player2 = player;
        this.isPlayer1Turn = true;
    }

    public synchronized void notifyPlayers() {
        notifyAll();
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public void toggleTurn() {
        isPlayer1Turn = !isPlayer1Turn;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }


    // Add methods for game logic, such as placing ships, making moves, checking win conditions, etc.
}
