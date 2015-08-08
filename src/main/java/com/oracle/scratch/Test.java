package com.oracle.scratch;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 29/7/15.
 */
public class Test {

//    public static void main(String[] args) {
//
//        List<Integer> t = Arrays.asList(1, 2, 3, 4);
//        String json = new Gson().toJson(t);
//        System.out.println(json);
//
//        Type collectionType = new TypeToken<List<Integer>>(){}.getType();
//        List<Integer> tt = new Gson().fromJson(json, collectionType);
//        System.out.println(tt);
//    }

    public static void main(String[] args) throws IOException {
        for (int i=0; i < 5000;) {
            List<Integer> numbers = new ArrayList<>();
            for (int j=0; j<5; j++) {
                numbers.add(i++);
            }
            String json = new Gson().toJson(numbers);
            System.out.println("Sending message: " + json);
            sendPost(json);
        }
    }

    private static void sendPost(String body) throws IOException{
        String postUrl = "http://localhost:4567/persist";
        URL obj = new URL(postUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("POST request not worked");
        }
    }
}
