package com.oracle.app;


import com.google.gson.Gson;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.DefaultEncoder;
import kafka.serializer.StringEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by rahul on 30/7/15.
 */
public class MessageProducerDemo {

    public static void main(String[] args) {

        long events = Long.parseLong(args[0]);

        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:9092,localhost:9093");
//        props.put("serializer.class", StringEncoder.class.getName());
//        props.put("serializer.class", DefaultEncoder.class.getName());
        props.put("partitioner.class", RandomPartitioner.class.getName());
//        props.put("request.required.acks", "1");
        props.put("producer.type", "async");
//        props.put("batch.num.messages", "10");

        ProducerConfig config = new ProducerConfig(props);
        Producer<byte[], byte[]> producer = new Producer<>(config);

        byte[] key = { (byte)0x0 };
        int batchSize = 12;

        List<byte[]> temp = new ArrayList<>();

        for (int i=0; i < events;) {
            List<Integer> numbers = new ArrayList<>();
            for (int j=0; j<batchSize; j++) {
                numbers.add(i++);
            }
            String json = new Gson().toJson(numbers);
            temp.add(json.getBytes());

        }

        int numRecords = 0;
        long t1 = System.nanoTime();
        System.out.println("Starting sending messages");
        for (byte[] message : temp) {
            KeyedMessage<byte[], byte[]> data = new KeyedMessage<>("numbers-topic", key, message);
            producer.send(data);
            numRecords += 1;
//            System.out.println("data size = " + message.length + "bytes");
        }
        System.out.println("Completed sending messages");
        long t2 = System.nanoTime();
        double seconds = (t2 - t1)/1000000000.0;
        System.out.println("Total time taken = " + seconds + " secs, num sends = " + numRecords);
        System.out.println(numRecords/seconds + "rps");
        producer.close();
    }
}
