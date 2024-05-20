package org.example.model;

public class Ship {
    private int size;
    private int orientation; // 1- horizontal, 2- vertical

    int line;

    int start;
    int end;
    private boolean isSet = false;

    public Ship(int size) {
        this.size = size;
    }

    public void setSet(boolean set) {
        isSet = set;
    }

    public boolean isSet() {
        return isSet;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setLine(int line){
        this.line = line;
    }

    public boolean isSank(){
        return size==0;
    }

    public boolean tryHit(int x, int y){
        if( orientation == 1 )
            if( line == x )
                if( y >= start && y <= end ){
                    size--;
                    return true;
                }else return false;
             else return false;
         else
            if( line == y )
                if( x >= start && x<= end) {
                    size--;
                    return true;
                }
             else return false;

        return false;

    }

}
