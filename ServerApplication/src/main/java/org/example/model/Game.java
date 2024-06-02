package org.example.model;

import org.example.AIThread;
import org.example.TimerThread;

import java.io.Serializable;

public class Game implements Serializable {
    private Player player1;
    private Player player2;
    private AIThread AI = new AIThread(this);
    public TimerThread timer = new TimerThread(this);

    private Player winner;
    private boolean isPlayer1Turn = true;

    public boolean isOver = false;
    public boolean withAI = false;

    public synchronized void setPlayer1(Player player) {
        this.player1 = player;
    }

    public synchronized void setPlayer2(Player player) {
        this.player2 = player;
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
        timer.resetTimer();
        if(!withAI)
            if (isPlayer1Turn) {
                player1.out.println("Your turn");
            } else {
                player2.out.println("Your turn");
            }

    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Board getPlayer2Board(){
        if(withAI){
            return AI.AIboard;
        } else {
            return player2.getBoard();
        }
    }

    public void setWithAI(boolean ai){
        withAI = ai;
        AI.start();
    }



}
