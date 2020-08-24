package com.example.test;


class MyTimer extends Thread {
    private final int sec;

    MyTimer(int sec) {
        this.sec = sec;
    }

    @Override
    public void run() {
        long after = System.currentTimeMillis();
        while(true) {
            long now = System.currentTimeMillis();
            if(now - after > this.sec) {
                // TODO: alert
                after = now;

            }
        }
    }
}