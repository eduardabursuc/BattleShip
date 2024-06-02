package org.example;

import org.example.model.Game;

import java.util.concurrent.*;

public class TimerThread {
    private ScheduledExecutorService scheduler;
    private long interval = 15000;
    private long stopTime;
    Game game;

    public TimerThread(Game game) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.game = game;
    }

    public synchronized void startTimer() {
        stopTime = System.currentTimeMillis() + interval;
        scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() >= stopTime) {
                if(game.isPlayer1Turn()){
                    game.setWinner(game.getPlayer2());
                } else {
                    game.setWinner(game.getPlayer1());
                }

                game.getPlayer1().out.println("The game is over. " + (game.getWinner() == game.getPlayer1() ? "You won!" : "You lost."));
                game.getPlayer2().out.println("The game is over. " + (game.getWinner() == game.getPlayer2() ? "You won!" : "You lost."));
                notifyPlayers();
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopTimer() {
        scheduler.shutdown();
    }

    public synchronized void restartTimer() {
        stopTimer();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startTimer();
    }

    public synchronized void notifyPlayers() {
        // Notify players here
        System.out.println("Timer expired without any action taken.");
    }
}
