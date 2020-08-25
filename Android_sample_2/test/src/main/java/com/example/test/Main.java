package com.example.test;
//https://ithelp.ithome.com.tw/articles/10231346
//                System.out.printf("%1dx%1d=%2d ", i, j, j*i);

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

class  Callback implements MyTimer.Callback {

    int i = 0;
    @Override
    public void tick() {

        System.out.println("ONE SEC " + i);
        i = i + 1;
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        float[][][][] input = new float[1][224][224][3];
        float f = 2.34f;
        System.out.print("ans"+321/f);

        //MyTimer t = new MyTimer(1, new Callback());
        //t.start();
        //t.join();


    }
}
