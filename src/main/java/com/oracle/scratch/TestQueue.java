package com.oracle.scratch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by rahul on 6/8/15.
 */
public class TestQueue {

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        Runnable adder = new Runnable() {
            @Override
            public void run() {

                List<Integer> batchSizes = Arrays.asList(5,10,15,20);
                Random random = new Random();

                for (int i=0; i < 1000;) {
                    int batchSize = batchSizes.get(random.nextInt(batchSizes.size()));
                    List<Integer> newBatch = new ArrayList<>();
                    for (int j=0; j<batchSize; j++) {
                        newBatch.add(i++);
                    }
                    System.out.println("Adding batch " + newBatch);
                    for (Integer e : newBatch) {
                        try {
                            queue.put(e);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        };

        Runnable remover = new Runnable() {
            @Override
            public void run() {
                while (! Thread.currentThread().isInterrupted()) {

                    try {
                        List<Integer> nextBatch = new ArrayList<>(10);
                        nextBatch.add(queue.take());  // blocking
                        queue.drainTo(nextBatch, 9);  // non-blocking

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        Thread t1 = new Thread(adder);
        Thread t2 = new Thread(remover);

        t1.start();
        t2.start();

        t1.join();
        t2.interrupt();
    }

}
