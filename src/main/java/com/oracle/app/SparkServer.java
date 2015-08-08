package com.oracle.app;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.post;


/**
 * Created by rahul on 29/7/15.
 */
public class SparkServer {

    public static void main(String[] args) throws IOException {

        MessagePersister persister = new MessagePersister("/home/rahul/data-output/hello-kafka/persister.dat");
        persister.start();

        get("/hello", (request, response) -> "Hello World");

        post("/persist", (request, response) -> {
            byte[] bodyAsBytes = request.bodyAsBytes();
            String jsonString = new String(bodyAsBytes);
            Type collectionType = new TypeToken<List<Integer>>(){}.getType();
            persister.addDataToPersist(new Gson().fromJson(jsonString, collectionType));
            return "OK";
        });
    }


    private static class MessagePersister extends Thread {

        private Writer fileWriter;
        private BlockingQueue<Integer> dataQueue;

        public MessagePersister(String filepath) throws IOException {
            fileWriter = new FileWriter(filepath);
            dataQueue = new LinkedBlockingQueue<>();
        }

        public void addDataToPersist(List<Integer> dataList) {
            dataQueue.addAll(dataList);
        }

        @Override
        public void run() {
            long lastFlushTime = System.nanoTime();
            while (true) {
                List<Integer> tempStore = new ArrayList<>();
                dataQueue.drainTo(tempStore);
                if (tempStore.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                        continue;
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                System.out.println("Processing messages: " + tempStore);
                String messagesStr = tempStore.stream().map(i -> i.toString()).collect(Collectors.joining("\n"));
                try {
                    fileWriter.write(messagesStr);
                    if (System.nanoTime() - lastFlushTime > 500 * 1000000) {
                        fileWriter.flush();
                        lastFlushTime = System.nanoTime();
                        System.out.println("Flushed data to disk");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
