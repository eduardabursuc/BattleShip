package org.example;

import org.example.model.Game;

import java.awt.desktop.SystemSleepEvent;
import java.util.concurrent.*;

public class TimerThread extends Thread{

    private long stopTime;
    Game game;

    public TimerThread(Game game) {
        this.game = game;
    }

    @Override
    public void run(){
        stopTime = System.currentTimeMillis() + 15000;
        while( System.currentTimeMillis() < stopTime ){
            try{
                sleep(1000);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        game.isOver = true;
        game.getPlayer1().out.println("Time is up!");
        game.getPlayer2().out.println("Time is up!");
    }


    public void resetTimer(){
        this.stopTime = System.currentTimeMillis() + 15000;
    }

}
