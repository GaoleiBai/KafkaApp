package com.oracle.app;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

import java.util.Random;

/**
 * Created by rahul on 30/7/15.
 */
public class RandomPartitioner implements Partitioner {

    private Random random = new Random();

    public RandomPartitioner(VerifiableProperties props) {
    }

    @Override
    public int partition(Object o, int numPartitions) {
        return random.nextInt(255) % numPartitions;
    }
}
