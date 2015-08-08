package com.oracle.app;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by rahul on 30/7/15.
 */
public class MessageConsumerDemo {

    private final ConsumerConnector consumerConnector;
    private final String topic;
    private ExecutorService executor;

    public MessageConsumerDemo(String zookeeper, String groupId, String topic) {
        this.consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig(zookeeper, groupId));
        this.topic = topic;
    }

    public void shutdown() {
        if (consumerConnector != null) consumerConnector.shutdown();
        if (executor != null) executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumerConnector threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
    }

    public void run(int numThreads) {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, new Integer(numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(numThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerThread(stream, threadNumber));
            threadNumber++;
        }
    }

    private static ConsumerConfig createConsumerConfig(String zookeeper, String groupId) {

//        The ‘zookeeper.connect’ string identifies where to find once instance of Zookeeper in your cluster. Kafka uses ZooKeeper to store offsets of messages consumed for a specific topic and partition by this Consumer Group
//        The ‘group.id’ string defines the Consumer Group this process is consuming on behalf of.
//        The ‘zookeeper.session.timeout.ms’ is how many milliseconds Kafka will wait for ZooKeeper to respond to a request (read or write) before giving up and continuing to consume messages.
//        The ‘zookeeper.sync.time.ms’ is the number of milliseconds a ZooKeeper ‘follower’ can be behind the master before an error occurs.
//        The ‘auto.commit.interval.ms’ setting is how often updates to the consumed offsets are written to ZooKeeper. Note that since the commit frequency is time based instead of # of messages consumed, if an error occurs between updates to ZooKeeper on restart you will get replayed messages.

        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }


    private static class ConsumerThread implements Runnable {

        private KafkaStream<byte[], byte[]> stream;

        private int threadNumber;

        private int numMessagesReceived = 0;

        public ConsumerThread(KafkaStream stream, int threadNumber) {
            this.stream = stream;
            this.threadNumber = threadNumber;
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                System.out.println("Received message on thread: " + threadNumber + ": " + new String(it.next().message()));
                numMessagesReceived += 1;
            }
            System.out.println("Shutting down thread: " + threadNumber + " number of messages received: " + numMessagesReceived);
        }

        public int getNumMessagesReceived(){
            return numMessagesReceived;
        }
    }

    public static void main(String[] args) {
        String zooKeeper = args[0];
        String groupId = args[1];
        String topic = args[2];
        int threads = Integer.parseInt(args[3]);

        MessageConsumerDemo example = new MessageConsumerDemo(zooKeeper, groupId, topic);
        example.run(threads);

        try {
            Thread.sleep(60*1000);
        } catch (InterruptedException ie) {

        }
        example.shutdown();
    }


}
