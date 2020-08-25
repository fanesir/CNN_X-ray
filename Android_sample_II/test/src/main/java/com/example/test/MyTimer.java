package com.example.test;


public class MyTimer extends Thread {
    static  int a ;
    private final int sec;
    private final Callback callback;

    interface Callback {
        void tick();
    }

    MyTimer(int sec, Callback callback) {
        this.sec = sec;
        this.callback = callback;
    }

    @Override
    public void run() {
        long after = System.currentTimeMillis();
        while(true) {
            long now = System.currentTimeMillis();
            if(now - after > this.sec * 1000) {
                // TODO: alert
                this.callback.tick();
                after = now;
            }
        }
    }
}